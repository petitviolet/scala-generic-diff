package net.petitviolet.generic.diff

import shapeless._
import shapeless.labelled.FieldType

import scala.language.dynamics
import scala.language.experimental.macros

object GenericDiff {

  sealed trait Field extends Any {
    def name: Symbol
  }
  case class FieldSame(name: Symbol) extends AnyVal with Field
  case class FieldDiff[A](name: Symbol, before: A, after: A) extends Field

  case class DiffResult[T](fields: Seq[Field]) extends Dynamic {
    val (sames: Seq[Field], diffs: Seq[Field]) = fields.partition {
      case _: FieldSame    => true
      case _: FieldDiff[_] => false
    }

    def selectDynamic(name: String): Field = macro GenericDiffMacro.selectDynamic[T]

    private[diff] def field(name: String): Field =
      fields.find {
        _.name.name == name
      } getOrElse {
        throw new NoSuchElementException(s"field #$name not found.")
      }
  }

  trait GenericDiff[HL <: HList] {
    def apply(left: HL, right: HL): Seq[Field]
  }

  implicit lazy val hnilDiff: GenericDiff[HNil] = new GenericDiff[HNil] {
    override def apply(left: HNil, right: HNil): Seq[Field] = Nil
  }

  implicit def hlistDiff[S <: Symbol, H, T <: HList](
    implicit wit: Witness.Aux[S],
    gen: Lazy[GenericDiff[T]]
  ): GenericDiff[FieldType[S, H] :: T] = new GenericDiff[FieldType[S, H] :: T] {
    override def apply(left: FieldType[S, H] :: T, right: FieldType[S, H] :: T): Seq[Field] = {
      if (left.head == right.head) FieldSame(wit.value) +: gen.value.apply(left.tail, right.tail)
      else {
        FieldDiff(wit.value, left.head, right.head) +: gen.value.apply(left.tail, right.tail)
      }
    }
  }

//  implicit def nestDiff[S <: Symbol, H, T <: HList, U <: HList](
//    implicit wit: Witness.Aux[S],
//    G: LabelledGeneric.Aux[H, T],
//    genT: Lazy[GenericDiff[T]],
//    genU: Lazy[GenericDiff[U]]
//  ): GenericDiff[H :: U] = {
//    (left: H :: U, right: H :: U) => { // SAM-pattern
//      val diffT: Seq[Field] = genT.value.apply(G.to(left.head), G.to(right.head))
//      diffT ++ genU.value.apply(left.tail, right.tail)
//    }
//  }

  implicit class Differable[A](val left: A) extends AnyVal {
    def diff[HL <: HList](right: A)(implicit G: LabelledGeneric.Aux[A, HL],
                                    gen: Lazy[GenericDiff[HL]]): DiffResult[A] = {
      GenericDiff.diff[A, HL](left, right)
    }
  }

  def diff[A, HL <: HList](left: A, right: A)(implicit G: LabelledGeneric.Aux[A, HL],
                                              gen: Lazy[GenericDiff[HL]]): DiffResult[A] = {
    DiffResult[A](gen.value.apply(G.to(left), G.to(right)))
  }

}
