import sbt._

object DependencyVersionProvider extends AutoPlugin {
  override final def trigger: PluginTrigger = allRequirements

  final object autoImport {
    lazy val versions = settingKey[VersionRegistry]("Bundles dependency versions")
  }

  import autoImport._

  override final def projectSettings: Seq[Def.Setting[_]] = Seq(
    versions := VersionRegistry
  )
}
