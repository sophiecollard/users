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

  Seq(
    "com.softwaremill.quicklens" %% "quicklens" % "1.4.11",
    "com.typesafe.akka"          %% "akka-actor" % akkaV,
    "com.typesafe.akka"          %% "akka-http" % akkaHttpV,
    "org.typelevel"              %% "cats-core" % "1.0.0-MF",
    compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
  )
}
