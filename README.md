# Scala generic diff

[![MavenCentral](https://maven-badges.herokuapp.com/maven-central/net.petitviolet/generic-diff_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.petitviolet/generic-diff_2.12)
 [![CircleCI](https://circleci.com/gh/petitviolet/scala-generic-diff.svg?style=svg)](https://circleci.com/gh/petitviolet/scala-generic-diff)
 [![Coverage Status](https://coveralls.io/repos/github/petitviolet/scala-generic-diff/badge.svg?branch=master)](https://coveralls.io/github/petitviolet/scala-generic-diff?branch=master)

## Getting Started

```scala
libraryDependencies += "net.petitviolet" %% "generic-diff" % "<version>"
```

## Example

```scala
import net.petitviolet.generic.diff._

case class User(id: Long, name: String, age: Int)

case class GroupName(value: String)
case class Group(id: Long, name: GroupName)

def main(args: Array[String]): Unit = {
  // extract difference between 2 objects
  val userDiff = User(1L, "alice", 20) diff User(2L, "alice", 35)

  // result contains sequence of FieldDiff and FieldSame
  assert(userDiff.fields == List(FieldDiff("id", 1, 2), FieldSame("name", "alice"), FieldDiff("age", 20, 35)))

  // dynamic field access
  assert(userDiff.name == FieldSame("name"))
  assert(userDiff.age == FieldDiff("age", 20, 35))
  // userDiff.foo // compile error!

  // extract diff from nested objects
  val groupDiff = Group(1, GroupName("tech")) diff Group(2L, GroupName("hoge"))
  assert(
    groupDiff.fields == List(
      FieldDiff("id", 1, 2),
      FieldDiff("name", GroupName("tech"), GroupName("hoge"))
    )
  )
}
```

## Publish

```console
sbt 'project genericDiffMacro' +publishSigned sonatypeReleaseAll \
    'project genericDiff' +publishSigned sonatypeReleaseAll
```

## LICENSE

[Apache-2.0](https://github.com/petitviolet/scala-generic-diff/blob/master/LICENSE)
