package net.petitviolet.generic.diff
import org.scalatest.{ FeatureSpec, Matchers }
import shapeless.LabelledGeneric

class GenericDiffTest extends FeatureSpec with Matchers {
  import GenericDiff._

  feature("GenericDiff#diff for simple case class") {
    case class Clazz(i: Int, s: String)
    scenario("same clazz") {
      diff(Clazz(1, "hello"), Clazz(1, "hello")) shouldBe List(FieldSame('i), FieldSame('s))
    }
    scenario("not same clazz of i") {
      diff(Clazz(1, "hello"), Clazz(2, "hello")) shouldBe List(FieldDiff('i, 1, 2), FieldSame('s))
    }
    scenario("not same clazz of s") {
      diff(Clazz(1, "hello"), Clazz(1, "world")) shouldBe List(FieldSame('i), FieldDiff('s, "hello", "world"))
    }
    scenario("not same clazz") {
      diff(Clazz(1, "hello"), Clazz(2, "world")) shouldBe List(FieldDiff('i, 1, 2), FieldDiff('s, "hello", "world"))
    }
  }

  feature("GenericDiff#diff for simple case class has a collection") {
    case class Clazz[A](list: Seq[A])
    scenario("same clazz") {
      diff(Clazz(List(1)), Clazz(List(1))) shouldBe List(FieldSame('list))
    }
    scenario("not same clazz of list elements") {
      diff(Clazz(List(1)), Clazz(List(1, 2))) shouldBe List(FieldDiff('list, List(1), List(1, 2)))
    }
    scenario("not same clazz of list type") {
      diff(Clazz(List(1)), Clazz(Vector(1))) shouldBe List(FieldSame('list))
    }
  }

  feature("GenericDiff#diff for case class include class") {
    class Value[A](val a: A)
    case class Clazz[A](i:Int, value: Value[A])

    scenario("has same instance") {
      val value = new Value[Int](100)
      diff(
        Clazz(1, value),
        Clazz(1, value)
      ) shouldBe List(FieldSame('i), FieldSame('value))
    }
    scenario("has not same instance") {
      val value1 = new Value[Int](100)
      val value2 = new Value[Int](100)
      diff(
        Clazz(1, value1),
        Clazz(1, value2)
      ) shouldBe List(FieldSame('i), FieldDiff('value, value1, value2))
    }
  }

  feature("GenericDiff#diff for case class include class with custom equals") {
    class Value[A](val a: A) {
      override def equals(obj: scala.Any): Boolean = obj match {
        case v: Value[A] => a == v.a
        case _ => false
      }
    }
    case class Clazz[A](i:Int, value: Value[A])

    scenario("has same instance") {
      val value = new Value[Int](100)
      diff(
        Clazz(1, value),
        Clazz(1, value)
      ) shouldBe List(FieldSame('i), FieldSame('value))
    }
    scenario("has not same instance") {
      val value1 = new Value[Int](100)
      val value2 = new Value[Int](100)
      diff(
        Clazz(1, value1),
        Clazz(1, value2)
      ) shouldBe List(FieldSame('i), FieldSame('value))
    }
  }

  feature("GenericDiff#diff for class") {
    import shapeless.{ :: , HNil, Lazy }
    class Clazz[A](val elem: A)
    implicit val G: Lazy[LabelledGeneric.Aux[Clazz[Int], Int :: HNil]] = Lazy.apply(
      new LabelledGeneric[Clazz[Int]] {
        private type HL = Int :: HNil
        type Repr = HL
        override def to(t: Clazz[Int]): HL = t.elem :: HNil
        override def from(r: HL): Clazz[Int] = new Clazz(r.head)
      }
    )

    scenario("same instance") {
      val instance = new Clazz[Int](100)
      diff( instance, instance ) shouldBe List(FieldSame('elem))
    }
    scenario("not same instance") {
      diff(
        new Clazz[Int](100),
        new Clazz[Int](100),
      ) shouldBe List(FieldSame('elem))
    }
    scenario("differrent instance") {
      diff(
        new Clazz[Int](100),
        new Clazz[Int](200),
      ) shouldBe List(FieldDiff('elem, 100, 200))
    }
  }

  feature("GenericDiff#diff for nested case class") {
    case class Id(value: Int)
    case class Name(value: String)
    case class User(id: Id, name: Name)

    scenario("same User") {
      diff(
        User(Id(1), Name("alice")),
        User(Id(1), Name("alice"))
      ) shouldBe List(FieldSame('id), FieldSame('name))
    }
    scenario("not same User of id") {
      diff(
        User(Id(1), Name("alice")),
        User(Id(2), Name("alice"))
      ) shouldBe List(FieldDiff('id, Id(1), Id(2)), FieldSame('name))
    }
    scenario("not same User of id and name") {
      diff(
        User(Id(1), Name("alice")),
        User(Id(2), Name("bob"))
      ) shouldBe List(FieldDiff('id, Id(1), Id(2)), FieldDiff('name, Name("alice"), Name("bob")))
    }
  }
}