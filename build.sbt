val libraryVersion = "0.6.0-RC2"
val libraryName = "generic-diff"

lazy val commonDependencies = Seq(
  "com.chuusai" %% "shapeless" % "2.3.3",
  "org.scalatest" %% "scalatest" % "3.1.0-SNAP11" % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
)

def commonSettings(projectName: String) = Seq(
  name := projectName,
  organization := "net.petitviolet",
  version := libraryVersion,
  scalaVersion := "2.13.0-RC2",
  crossScalaVersions := Seq("2.11.11", "2.12.8", scalaVersion.value),
  libraryDependencies ++= commonDependencies,
  scalafmtOnCompile := true,
  scalafmtSbtCheck := true,
)

lazy val genericDiffRoot = (project in file("."))
  .settings(commonSettings("GenericDiffRoot"))
  .aggregate(genericDiffMacro, genericDiff, example)

lazy val genericDiff = (project in file("generic_diff"))
  .settings(commonSettings(libraryName))
  .settings(testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-u", {
    val dir = System.getenv("CI_REPORTS")
    if (dir == null) "target/reports" else dir
  }))
  .dependsOn(genericDiffMacro)

lazy val genericDiffMacro = (project in file("generic_diff_macro"))
  .settings(
    commonSettings("generic-diff-macro"),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "compile",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "optional"
    )
  )

lazy val example = (project in file("example"))
  .settings(commonSettings("example"))
  .dependsOn(genericDiff)
//  .settings(
//    libraryDependencies += organization.value %% libraryName % libraryVersion
//  )
