logLevel := Level.Warn

resolvers ++= Seq(
  Resolver.bintrayIvyRepo("scalameta", "maven"),
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.4")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.4")

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")