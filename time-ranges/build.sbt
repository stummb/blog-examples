
organization := "de.boris-stumm.blog"
version      := "0.1.0-SNAPSHOT"
scalaVersion := "2.12.2"
javacOptions ++= Seq(
  "-source", "1.8",
  "-target", "1.8"
)
scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-Xmax-classfile-name", "120",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings"
)

libraryDependencies += "com.google.guava" % "guava" % "22.0"

