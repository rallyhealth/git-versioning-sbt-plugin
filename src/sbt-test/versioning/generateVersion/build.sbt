import java.io.FileInputStream

import sbt.Keys.version
import sbt.complete.DefaultParsers.spaceDelimited

import scala.collection.JavaConverters._

organization := "com.rallyhealth.sbt.scripted"

scalaVersion := "2.11.12"

scalacOptions ++= Seq("-Xfatal-warnings", "-Xlint")

publish := {}

logLevel := sbt.Level.Info

lazy val assertGenerateVersion = inputKey[Unit]("Checks the file generated by 'generateVersion' task")

/**
  * Asserts that the file created by 'generateVersion' exists and has the expected content.
  *
  * #param versionRegex The version must match this regex
  */
assertGenerateVersion := {
  
  val generatedFile: File = com.rallyhealth.sbt.versioning.GitVersioningPlugin.autoImport.generateVersion.value
  
  assert(generatedFile.exists(), s"generatedFile=$generatedFile does not exist")
  assert(generatedFile.getName == "rally-version.properties", s"generatedFile=$generatedFile name is different")
  
  val properties = new java.util.Properties()
  properties.load(new FileInputStream(generatedFile))
  
  assert(
    properties.size() == 3,
    s"generatedFile=$generatedFile does not contains 3 values: " + properties.keys().asScala.map(_.toString).mkString(", "))
  
  val generatedVersion = properties.getProperty("Version")
  
  assert(generatedVersion == version.value, s"generatedFile.Version=$generatedVersion != sbt.version=${version.value}")
  
  val versionRegex = spaceDelimited("<version regex>").parsed.head
  assert(
    generatedVersion.matches(versionRegex),
    s"generatedFile.Version=$generatedVersion does not match regex=$versionRegex")
  
  assert(
    properties.getProperty("Name") == name.value,
    s"generatedFile.Name=${properties.getProperty("Name")} != sbt.name=${name.value}")
  assert(
    properties.getProperty("Created").toLong + 10000 >= System.currentTimeMillis(),
    s"generatedFile.Created=${properties.getProperty("Created")} is too old")
}
