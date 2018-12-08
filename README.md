# Scala generic diff

[![MavenCentral](https://maven-badges.herokuapp.com/maven-central/net.petitviolet/genericdiff_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.petitviolet/genericdiff_2.12)
 [![CircleCI](https://circleci.com/gh/petitviolet/scala-generic-diff.svg?style=svg)](https://circleci.com/gh/petitviolet/scala-generic-diff)
 [![Coverage Status](https://coveralls.io/repos/github/petitviolet/scala-generic-diff/badge.svg?branch=master)](https://coveralls.io/github/petitviolet/scala-generic-diff?branch=master)

## Getting Started

```scala
libraryDependencies += "net.petitviolet" %% "generic-diff" % "<version>"
```

## Example

```scala
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
```

## LICENSE

[LICENSE](https://github.com/petitviolet/scala-generic-diff/blob/master/LICENSE)
