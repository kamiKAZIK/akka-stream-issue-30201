sealed trait VersionRegistry {
  final lazy val scala = "2.13.6"
  final lazy val logback = "1.2.3"
  final lazy val akka = "2.6.16"
  final lazy val akkaHttp = "10.2.6"
  final lazy val scalactic = "3.2.9"
  final lazy val scalatest = "3.2.9"
  final lazy val scalamock = "5.1.0"
}

object VersionRegistry extends VersionRegistry
