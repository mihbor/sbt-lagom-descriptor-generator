
import sbt.Keys.{ crossScalaVersions, homepage, libraryDependencies }


// ---------------   SETTINGS   ---------------

lazy val commonSettings =
  Seq(
    organization := "com.lightbend.lagom",
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
    homepage := Some(url("https://github.com/lagom/sbt-lagom-descriptor-generator")),
    sonatypeProfileName := "com.lightbend",
    // Scala settings
    scalaVersion := Version.scala,
    crossScalaVersions := List(scalaVersion.value, "2.10.6"),

    pomExtra := {
      <scm>
        <url>https://github.com/lagom/sbt-lagom-descriptor-generator</url>
        <connection>scm:git:git@github.com:lagom/sbt-lagom-descriptor-generator.git</connection>
      </scm>
        <developers>
          <developer>
            <id>lagom</id>
            <name>Lagom Contributors</name>
            <url>https://github.com/lagom</url>
          </developer>
        </developers>
    },
    pomIncludeRepository := { _ => false }

  )

// --------------- ROOT ------------------

lazy val root = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin && Sonatype && BintrayPlugin)
  .settings(name := "sbt-lagom-descriptor-generator")
  .aggregate(
    `lagom-descriptor-generator-api`,
    `openapi-parser`,
    `lagom-descriptor-renderer-javadsl`,
    `lagom-descriptor-renderer-scaladsl`,
    `lagom-descriptor-generator`,
    `lagom-descriptor-generator-sbt-plugin`
  )
  .settings(librarySettings: _*)
  .settings(
    publishLocal := {},
    publishArtifact in Compile := false,
    publish := {}
  ).
  settings(
    // this is required during release because for some reason the version value is not available.
    version := version.value,
    bintrayPackage := bintrayPackage.value
  )

// ---------------   PROJECTS   ---------------

def RootPlugins        = AutomateHeaderPlugin && Sonatype                          && BintrayPlugin
def RuntimeLibPlugins  = AutomateHeaderPlugin && Sonatype                          && PluginsAccessor.exclude(BintrayPlugin)
def SbtPluginPlugins   = AutomateHeaderPlugin && PluginsAccessor.exclude(Sonatype) && BintrayPlugin

lazy val librarySettings = commonSettings ++
  Settings.headerLicenseSettings ++
  Settings.commonScalariformSettings ++
  Settings.bintraySettings ++
  Settings.releaseSettings

lazy val sbtPluginSettings = librarySettings ++
  Settings.publishMavenStyleSettings ++
  Settings.scriptedTestsSettings

lazy val `lagom-descriptor-generator-sbt-plugin` = project
  .in(file("lagom-descriptor-generator-sbt-plugin"))
  .enablePlugins(SbtPluginPlugins)
  .settings(sbtPluginSettings)
  .settings(
    name := "lagom-descriptor-generator-sbt-plugin",
    sbtPlugin := true,
    publishTo := {
      if (isSnapshot.value) {
        // Bintray doesn't support publishing snapshots, publish to Sonatype snapshots instead
        Some(Opts.resolver.sonatypeSnapshots)
      } else publishTo.value
    },
    publishMavenStyle := isSnapshot.value
  ).dependsOn(`lagom-descriptor-generator`)

lazy val `lagom-descriptor-generator` =
  (project in file("lagom-descriptor-generator"))
    .enablePlugins(RuntimeLibPlugins)
    .settings(librarySettings)
    .configs(IntegrationTest)
    .dependsOn(`openapi-parser`, `lagom-descriptor-renderer-javadsl`, `lagom-descriptor-renderer-scaladsl`)
    .settings(
      Defaults.itSettings,
      libraryDependencies += Library.scalaTest % "test,it"
    )

lazy val `lagom-descriptor-renderer-javadsl` =
  (project in file("lagom-renderers") / "javadsl")
    .enablePlugins(RuntimeLibPlugins)
    .settings(librarySettings)
    .dependsOn(`lagom-descriptor-generator-api`)
    .settings(libraryDependencies += Library.scalaTest % "test")

lazy val `lagom-descriptor-renderer-scaladsl` =
  (project in file("lagom-renderers") / "scaladsl")
    .enablePlugins(RuntimeLibPlugins)
    .settings(librarySettings)
    .dependsOn(`lagom-descriptor-generator-api`)
    .settings(libraryDependencies += Library.scalaTest % "test")

lazy val `openapi-parser` =

  (project in file("spec-parsers") / "openapi")
    .enablePlugins(RuntimeLibPlugins)
    .settings(librarySettings)
    .dependsOn(`lagom-descriptor-generator-api`)
    .settings(
      libraryDependencies ++= List(Library.swaggerParser, Library.scalaTest % "test")
    )

lazy val `lagom-descriptor-generator-api` =
  (project in file("lagom-descriptor-generator-api"))
    .enablePlugins(RuntimeLibPlugins)
    .settings(librarySettings)
    .settings(libraryDependencies += Library.scalaTest % "test")

