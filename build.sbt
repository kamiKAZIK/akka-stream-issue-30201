name := "akka-stream-issue-30201"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % versions.value.akka,
  "com.typesafe.akka" %% "akka-stream" % versions.value.akka,
  "com.typesafe.akka" %% "akka-http" % versions.value.akkaHttp,

  "ch.qos.logback" % "logback-classic" % versions.value.logback % Test,
  "org.scalactic" %% "scalactic" % versions.value.scalactic % Test,
  "org.scalatest" %% "scalatest" % versions.value.scalatest % Test,
  "org.scalamock" %% "scalamock" % versions.value.scalamock % Test
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-testkit-typed",
  "com.typesafe.akka" %% "akka-stream-testkit"
).map(_ % versions.value.akka % Test)
