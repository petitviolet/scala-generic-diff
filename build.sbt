lazy val commonDependencies = Seq(
  "com.chuusai" %% "shapeless" % "2.3.2",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
)

def commonSettings(projectName: String) = Seq(
  name := projectName,
  organization := "net.petitviolet",
  version := "0.1.0",
  scalaVersion := "2.12.8",
  crossScalaVersions := Seq("2.11.11", "2.12.8"),
  libraryDependencies ++= commonDependencies,
  scalafmtOnCompile := true,
  scalafmtSbtCheck := true,
)

lazy val genericDiffRoot = (project in file("."))
  .settings(commonSettings("GenericDiffRoot"))
  .aggregate(genericDiff, example)

lazy val genericDiff = (project in file("generic_diff"))
  .settings(commonSettings("GenericDiff"))
  .settings(testOptions in Test += Tests.Argument(
    TestFrameworks.ScalaTest, "-u", {
      val dir = System.getenv("CI_REPORTS")
      if(dir == null) "target/reports" else dir
    })
  )


lazy val example = (project in file("example/"))
  .settings(commonSettings("example"))
  .dependsOn(genericDiff)

