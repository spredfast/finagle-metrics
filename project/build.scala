import sbt._
import sbt.Keys._
import Dependencies._
import sbtrelease._
import sbtrelease.ReleaseStep
import sbtrelease.ReleasePlugin._
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleaseStateTransformations._

object AppBuilder extends Build {

  val appName = "finagle-metrics"

  val appSettings = Seq(
    name          := appName,
    organization  := "com.github.rlazoti",
    scalaVersion  := "2.11.8",
    unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)(Seq(_)),
    unmanagedSourceDirectories in Test    <<= (scalaSource in Test)(Seq(_))
  )

  val sfPublishSettings = Seq(
    publishTo := Some("Spredfast Artifactory" at "https://buildrepo.sf-ops.net/artifactory/sf-release-local")
  )

  val appReleaseSettings = releaseSettings ++ Seq(
    tagName        <<= (version in ThisBuild) map (v => "v" + v),
    tagComment     <<= (version in ThisBuild) map (v => "[BUILD] Release %s" format v),
    commitMessage  <<= (version in ThisBuild) map (v => "[BUILD] Set version to %s" format v),
    releaseProcess <<= thisProjectRef apply { ref =>
      Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runTest,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        ReleaseStep(action = Command.process("publishSigned", _)),
        setNextVersion,
        commitNextVersion,
        ReleaseStep(action = Command.process("sonatypeReleaseAll", _))
      )
    }
  )

  lazy val app = Project(appName, file("."))
    .settings(appSettings: _*)
    .settings(appReleaseSettings: _*)
    .settings(sfPublishSettings: _*)
    .settings(resolvers ++= appDependencyResolvers)
    .settings(libraryDependencies ++= appDependencies)

}
