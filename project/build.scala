import sbt._, Keys._
import play._
import PlayImport._
import PlayKeys._

object ApplicationBuild extends Build {
  val dependencies = Seq(
    "com.stripe" % "stripe-java" % "1.29.0",
    "com.google.oauth-client" % "google-oauth-client" % "1.20.0",
    "com.google.oauth-client" % "google-oauth-client-java6" % "1.20.0",
    "org.apache.oltu.oauth2" % "org.apache.oltu.oauth2.client" % "1.0.0",
    "com.typesafe" % "config" % "1.3.0",
    jdbc,
    anorm,
    cache,
    ws)

  lazy val buildSettings: Seq[Setting[_]] = Seq(
    organization        := "com.cooper.stripeplay",
    scalaVersion        := "2.11.5",
    scalacOptions       ++= Seq("-deprecation", "-feature", "-unchecked"),
    playPlugin          := true,
    version             := "0.0.1",
    libraryDependencies ++= dependencies,
    javaOptions in run  += "-Xmx8g",
    initialCommands in console := "import com.stripe._, com.stripe.exception._, com.stripe.model._, com.stripe.net._, scala.collection.JavaConversions._, scala.collection.JavaConverters._, java.util.{HashMap, Map}")

  lazy val root = Project(
    id = "stripe-play",
    base = file(".")
  ).settings(buildSettings: _*).enablePlugins(PlayScala)
}
