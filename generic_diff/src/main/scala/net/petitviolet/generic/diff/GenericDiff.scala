package net.petitviolet.generic.diff

import shapeless._
import shapeless.labelled.FieldType

object GenericDiff {
  sealed abstract class Field(name: Symbol)
  case class FieldSame(name: Symbol) extends Field(name)
  case class FieldDiff[A](name: Symbol, before: A, after: A) extends Field(name)

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
                                    gen: Lazy[GenericDiff[HL]]): Seq[Field] = {
      GenericDiff.diff(left, right)
    }
  }

  def diff[A, HL <: HList](left: A, right: A)(implicit G: LabelledGeneric.Aux[A, HL],
                                              gen: Lazy[GenericDiff[HL]]): Seq[Field] = {
    gen.value.apply(G.to(left), G.to(right))
  }

}
