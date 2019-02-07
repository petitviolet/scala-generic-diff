package net.petitviolet.generic.diff

import shapeless.HList

import scala.language.dynamics
import scala.language.experimental.macros

sealed trait Field extends Any {
  def name: String
}
case class FieldSame(name: String) extends AnyVal with Field
case class FieldDiff[A](name: String, before: A, after: A) extends Field

case class DiffResult[T](fields: Seq[Field]) extends Dynamic {

  val (sames: Seq[Field], diffs: Seq[Field]) = fields.partition {
    case _: FieldSame    => true
    case _: FieldDiff[_] => false
  }

  def selectDynamic(name: String): Field = macro GenericDiffMacro.selectDynamic[T]

  private[diff] def field(name: String): Field =
    fields.find {
      _.name == name
    } getOrElse {
      throw new NoSuchElementException(s"field #$name not found.")
    }
}

trait GenericDiff[HL <: HList] {
  def apply(left: HL, right: HL): Seq[Field]
}
