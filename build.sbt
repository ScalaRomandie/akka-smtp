name := "akka-email"

version := "1.0"

scalaVersion:="2.11.2"

libraryDependencies ++= Seq (
  "com.typesafe.akka" %% "akka-actor" % "2.3.6"   ,
  "org.subethamail" % "subethasmtp-wiser" % "1.2"
)

