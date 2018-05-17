name := "json_validation_service"
 
version := "1.0"

lazy val `json_validation_service` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += "emueller-bintray" at "http://dl.bintray.com/emueller/maven"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )

libraryDependencies += "com.eclipsesource"  %% "play-json-schema-validator" % "0.9.4"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test


unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )