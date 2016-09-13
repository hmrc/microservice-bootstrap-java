import de.heikoseeberger.sbtheader.license.Apache2_0
import uk.gov.hmrc.HeaderSettings
import play.core.PlayVersion

name := "microservice-bootstrap-java"
autoScalaLibrary := false

sources in (Compile, doc) <<= sources in (Compile, doc) map { _.filterNot(_.getName endsWith ".scala") }

testFrameworks := Seq(TestFrameworks.JUnit)

// [START] Temporary solution until release of new version of sbt-auto-build with junit fix
headers += { "java" -> Apache2_0(HeaderSettings.copyrightYear, HeaderSettings.copyrightOwner) }

testOptions in Test := Seq()
testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "sequential", "true", "junitxml", "console")
testOptions in Test += Tests.Argument(TestFrameworks.JUnit, "--ignore-runners=org.specs2.runner.JUnitRunner", "-q", "-v", "-a")
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-o", "-u", "target/test-reports", "-h", "target/test-reports/html-report")
// [END] Temporary solution until release of new version of sbt-auto-build with junit fix

val plugins = SbtTwirl && SbtAutoBuildPlugin && SbtGitVersioning

val compileDependencies = Seq(
  filters,
  javaCore,
  "com.typesafe.play" %% "play" % PlayVersion.current,
  "uk.gov.hmrc" %% "crypto" % "3.1.0",
  "uk.gov.hmrc" %% "play-filters-java" % "0.5.0",
  "uk.gov.hmrc" %% "play-whitelist-filter" % "1.1.0",
  "uk.gov.hmrc" %% "frontend-bootstrap" % "6.5.0",
  "uk.gov.hmrc" %% "play-authorisation" % "3.3.0",
  "uk.gov.hmrc" %% "play-authorised-frontend" % "5.7.0",
  "uk.gov.hmrc" %% "microservice-bootstrap" % "4.4.0",
  "uk.gov.hmrc" %% "play-graphite" % "2.0.0",
  "uk.gov.hmrc" %% "play-config" % "2.0.1",
  "com.kenshoo" %% "metrics-play" % "2.3.0_0.1.8"
)

val testDependencies = Seq(
  javaWs,
  "uk.gov.hmrc" %% "hmrctest" % "1.7.0",
  "org.pegdown" % "pegdown" % "1.6.0",
  "com.novocode" % "junit-interface" % "0.10",
  "com.typesafe.play" %% "play-test" % PlayVersion.current
).map(d => d % Test)

libraryDependencies ++= compileDependencies
libraryDependencies ++= testDependencies

lazy val `microservice-bootstrap-java` = project in file(".") enablePlugins plugins
