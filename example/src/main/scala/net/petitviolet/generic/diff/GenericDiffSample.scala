package net.petitviolet.generic.diff

object GenericDiffSample {
  private case class User(id: Long, name: String, age: Int)

  private case class GroupName(value: String)
  private case class Group(id: Long, name: GroupName)

  import GenericDiff._

  def main(args: Array[String]): Unit = {
    val userDiff = User(1L, "alice", 20) diff User(2L, "alice", 35)
    assert(userDiff == List(FieldDiff('id, 1, 2), FieldSame('name), FieldDiff('age, 20, 35)))

    val groupDiff = Group(1, GroupName("tech")) diff Group(2L, GroupName("hoge"))
    assert(
      groupDiff == List(
        FieldDiff('id, 1, 2),
        FieldDiff('name, GroupName("tech"), GroupName("hoge"))
      )
    )
  }

}
