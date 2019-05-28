package net.petitviolet.generic

import shapeless.labelled.FieldType
import shapeless.{ ::, HList, HNil, LabelledGeneric, Lazy, Witness }

package object diff { here =>

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
      here.diff[A, HL](left, right)
    }
  }

  def diff[A, HL <: HList](left: A, right: A)(implicit G: LabelledGeneric.Aux[A, HL],
                                              gen: Lazy[GenericDiff[HL]]): DiffResult[A] = {
    DiffResult[A](gen.value.apply(G.to(left), G.to(right)))
  }

  implicit lazy val hnilDiff: GenericDiff[HNil] = new GenericDiff[HNil] {
    override def apply(left: HNil, right: HNil): Seq[Field] = Nil
  }

  implicit def hlistDiff[S <: Symbol, H, T <: HList](
    implicit wit: Witness.Aux[S],
    gen: Lazy[GenericDiff[T]]
  ): GenericDiff[FieldType[S, H] :: T] = new GenericDiff[FieldType[S, H] :: T] {
    override def apply(left: FieldType[S, H] :: T, right: FieldType[S, H] :: T): Seq[Field] = {
      if (left.head == right.head)
        FieldSame(wit.value.name, left.head) +: gen.value.apply(left.tail, right.tail)
      else {
        FieldDiff(wit.value.name, left.head, right.head) +: gen.value.apply(left.tail, right.tail)
      }
    }
  }

}
