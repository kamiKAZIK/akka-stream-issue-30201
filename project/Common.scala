import sbt.Keys._
import sbt._

object Common {

  final lazy val settings = Seq(
    organization := "com.sd",
    scalaVersion := VersionRegistry.scala,
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    resolvers ++= Seq[sbt.Resolver](
      Resolver.jcenterRepo
    )
  )
}
