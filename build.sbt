name := "gasket"

version := s"${sys.props.getOrElse("build.majorMinor", "2.0")}.${sys.props.getOrElse("build.version", "SNAPSHOT")}"

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.11.7", "2.10.5")

organization := "com.themillhousegroup"

libraryDependencies ++= Seq(
    "com.google.gdata"      	%   "core"                    			% "1.47.1",
    "com.google.api-client"		%   "google-api-client"       			% "1.20.0",
    "com.google.http-client"	%   "google-http-client-jackson"    % "1.19.0",
    "ch.qos.logback"        	%   "logback-classic"         			% "1.1.3",
    "org.mockito"           	%   "mockito-all"             			% "1.10.19",
    "org.specs2"            	%%  "specs2"                  			% "2.3.13"  	% "test"
)

resolvers ++= Seq(  "oss-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
				    "oss-releases"  at "https://oss.sonatype.org/content/repositories/releases",
				    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/")

jacoco.settings

parallelExecution in Test := false

seq(bintraySettings:_*)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

scalariformSettings

net.virtualvoid.sbt.graph.Plugin.graphSettings

scalariformSettings



