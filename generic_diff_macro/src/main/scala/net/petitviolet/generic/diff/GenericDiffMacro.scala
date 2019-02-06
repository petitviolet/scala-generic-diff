package net.petitviolet.generic.diff

import scala.reflect.macros.blackbox

object GenericDiffMacro {

  def selectDynamic[A](c: blackbox.Context)(name: c.Tree): c.Tree = {
    import c.universe._

    // primary constructor args of type A
    val expectedNames: Seq[String] = c
      .weakTypeOf[A]
      .decls
      .collectFirst {
        case m: MethodSymbol if m.isPrimaryConstructor => m
      }
      .fold(List.empty[String]) { constructor: c.universe.MethodSymbol =>
        constructor.paramLists.flatMap { symbols: List[Symbol] =>
          symbols map { _.name.encodedName.toString.trim }
        }
      }

    name match {
      case Literal(Constant(value: String))
          if expectedNames.nonEmpty && !expectedNames.contains(value) =>
        c.error(
          c.enclosingPosition,
          s"${c.weakTypeOf[A]}#$value not found. Expected fields are ${expectedNames.mkString("#", ", #", "")}."
        )
      case _ => ()
    }

    q"""${c.prefix.tree}.field($name)"""
  }
}
