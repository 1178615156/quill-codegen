name := "quill-codegen"

version := "1.0"

scalaVersion := "2.12.2"


libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick-codegen" % "3.2.1",
  "mysql" % "mysql-connector-java" % "6.0.6"
)
