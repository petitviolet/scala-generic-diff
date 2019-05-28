package net.petitviolet.generic.diff

import org.scalatest.Matchers
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import shapeless.LabelledGeneric

class GenericDiffTest extends AnyFeatureSpec with GeneratorDrivenPropertyChecks with Matchers {

  Feature("GenericDiff#diff for simple case class") {
    case class Clazz(i: Int, s: String)
    Scenario("same clazz") {
      diff(Clazz(1, "hello"), Clazz(1, "hello")).fields shouldBe List(
        FieldSame("i"),
        FieldSame("s")
      )
    }
    Scenario("not same clazz of i") {
      diff(Clazz(1, "hello"), Clazz(2, "hello")).fields shouldBe List(
        FieldDiff("i", 1, 2),
        FieldSame("s")
      )
    }
    Scenario("not same clazz of s") {
      diff(Clazz(1, "hello"), Clazz(1, "world")).fields shouldBe List(
        FieldSame("i"),
        FieldDiff("s", "hello", "world")
      )
    }
    Scenario("not same clazz") {
      diff(Clazz(1, "hello"), Clazz(2, "world")).fields shouldBe List(
        FieldDiff("i", 1, 2),
        FieldDiff("s", "hello", "world")
      )
    }
  }
  Feature("DiffResult#hasDiff") {
    case class Clazz(i: Int, s: String)

    Scenario("same clazz") {
      diff(Clazz(1, "hello"), Clazz(1, "hello")).hasDiff shouldBe false
    }
    Scenario("not same clazz of i") {
      diff(Clazz(1, "hello"), Clazz(2, "hello")).hasDiff shouldBe true
    }
  }

  Feature("GenericDiff#diff for simple case class has a collection") {
    case class Clazz[A](list: Seq[A])
    Scenario("same clazz") {
      diff(Clazz(List(1)), Clazz(List(1))).fields shouldBe List(FieldSame("list"))
    }
    Scenario("not same clazz of list elements") {
      diff(Clazz(List(1)), Clazz(List(1, 2))).fields shouldBe List(
        FieldDiff("list", List(1), List(1, 2))
      )
    }
    Scenario("not same clazz of list type") {
      diff(Clazz(List(1)), Clazz(Vector(1))).fields shouldBe List(FieldSame("list"))
    }
  }

  Feature("GenericDiff#diff for case class include class") {
    class Value[A](val a: A)
    case class Clazz[A](i: Int, value: Value[A])

    Scenario("has same instance") {
      val value = new Value[Int](100)
      diff(
        Clazz(1, value),
        Clazz(1, value)
      ).fields shouldBe List(FieldSame("i"), FieldSame("value"))
    }
    Scenario("has not same instance") {
      val value1 = new Value[Int](100)
      val value2 = new Value[Int](100)
      diff(
        Clazz(1, value1),
        Clazz(1, value2)
      ).fields shouldBe List(FieldSame("i"), FieldDiff("value", value1, value2))
    }
  }

  Feature("GenericDiff#diff for case class include class with custom equals") {
    class Value[A](val a: A) {
      override def equals(obj: scala.Any): Boolean = obj match {
        case v: Value[A] => a == v.a
        case _           => false
      }
    }
    case class Clazz[A](i: Int, value: Value[A])

    Scenario("has same instance") {
      val value = new Value[Int](100)
      diff(
        Clazz(1, value),
        Clazz(1, value)
      ).fields shouldBe List(FieldSame("i"), FieldSame("value"))
    }
    Scenario("has not same instance") {
      val value1 = new Value[Int](100)
      val value2 = new Value[Int](100)
      diff(
        Clazz(1, value1),
        Clazz(1, value2)
      ).fields shouldBe List(FieldSame("i"), FieldSame("value"))
    }
  }

  Feature("GenericDiff#diff for class") {
    import shapeless.{ ::, HNil, Lazy }
    class Clazz[A](val elem: A)
    implicit val G: Lazy[LabelledGeneric.Aux[Clazz[Int], Int :: HNil]] = Lazy.apply(
      new LabelledGeneric[Clazz[Int]] {
        type Repr = Int :: HNil
        override def to(t: Clazz[Int]): Repr = t.elem :: HNil
        override def from(r: Repr): Clazz[Int] = new Clazz(r.head)
      }
    )

    Scenario("same instance") {
      val instance = new Clazz[Int](100)
      diff(instance, instance).fields shouldBe List(FieldSame("elem"))
    }
    Scenario("not same instance") {
      diff(
        new Clazz[Int](100),
        new Clazz[Int](100),
      ).fields shouldBe List(FieldSame("elem"))
    }
    Scenario("different instance") {
      diff(
        new Clazz[Int](100),
        new Clazz[Int](200),
      ).fields shouldBe List(FieldDiff("elem", 100, 200))
    }
  }

  Feature("GenericDiff#diff for nested case class") {
    case class Id(value: Int)
    case class Name(value: String)
    case class User(id: Id, name: Name)

    Scenario("same User") {
      diff(
        User(Id(1), Name("alice")),
        User(Id(1), Name("alice"))
      ).fields shouldBe List(FieldSame("id"), FieldSame("name"))
    }
    Scenario("not same User of id") {
      diff(
        User(Id(1), Name("alice")),
        User(Id(2), Name("alice"))
      ).fields shouldBe List(FieldDiff("id", Id(1), Id(2)), FieldSame("name"))
    }
    Scenario("not same User of id and name") {
      diff(
        User(Id(1), Name("alice")),
        User(Id(2), Name("bob"))
      ).fields shouldBe List(
        FieldDiff("id", Id(1), Id(2)),
        FieldDiff("name", Name("alice"), Name("bob"))
      )
    }
  }

  Feature("DiffResults") {
    case class Clazz(i: Int, s: String)

    Scenario("diffs with no diff") {
      diff(Clazz(1, "hello"), Clazz(1, "hello")).diffs shouldBe empty
    }
    Scenario("diffs with diff") {
      diff(Clazz(1, "hello"), Clazz(1, "world")).diffs shouldBe List(
        FieldDiff("s", "hello", "world")
      )
    }

    Scenario("sames with no diff") {
      diff(Clazz(1, "hello"), Clazz(1, "hello")).sames shouldBe List(FieldSame("i"), FieldSame("s"))
    }

    Scenario("sames with diff") {
      diff(Clazz(1, "hello"), Clazz(1, "world")).sames shouldBe List(FieldSame("i"))
    }

    Scenario("access with field name") {
      val diffResults = diff(Clazz(1, "hello"), Clazz(1, "world"))
      diffResults.i shouldBe FieldSame("i")
      diffResults.s shouldBe FieldDiff("s", "hello", "world")
    }
  }

  Feature("implicit diff and diff") {
    case class Clazz(i: Int, s: String)
    Scenario("all are same results") {
      forAll { (i1: Int, s1: String, i2: Int, s2: String) =>
        val (clazz1, clazz2) = (Clazz(i1, s1), Clazz(i2, s2))
        (clazz1 diff clazz2) shouldBe diff(clazz1, clazz2)
      }
    }
  }
}
