name := "users"
version := "1.0.0"

scalaVersion := "2.12.3"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-Ypartial-unification"
)

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= {
  val akkaV = "2.4.18"
  val akkaHttpV = "10.0.11"
  val specs2V = "3.8.6"

  Seq(
    "com.github.melrief"         %% "pureconfig" % "0.5.1",
    "com.softwaremill.quicklens" %% "quicklens" % "1.4.11",
    "com.typesafe"               %  "config" % "1.3.1",
    "com.typesafe.akka"          %% "akka-actor" % akkaV,
    "com.typesafe.akka"          %% "akka-http" % akkaHttpV,
    "com.typesafe.akka"          %% "akka-http-testkit" % akkaHttpV,
    "org.specs2"                 %% "specs2-core" % specs2V,
    "org.typelevel"              %% "cats-core" % "1.0.0-MF",
    compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
  )
}
