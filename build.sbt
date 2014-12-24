organization  := "com.github.nkdhny"

version       := "0.2-SNAPSHOT"

scalaVersion  := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  Seq(
    "io.spray"            %%  "spray-json"    % "1.2.6",
    "org.mongodb"         %%  "casbah" 	      % "2.7.2",
    "org.specs2"          %%  "specs2"        % "2.2.3" % "test",    
    "org.mockito"         %   "mockito-all"   % "1.9.5" % "test"
  )
}


publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }


pomExtra := (
  <url>https://github.com/nkdhny/domain-mdb</url>
  <licenses>
    <license>
      <name>MIT</name>
      <url>http://opensource.org/licenses/MIT</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>https://github.com/nkdhny/domain-mdb.git</url>
    <connection>scm:git:https://github.com/nkdhny/domain-mdb.git</connection>
  </scm>
  <developers>
    <developer>
      <id>nkdhny</id>
      <name>Alexey Golomedov</name>
      <url>http://github.com/nkdhny</url>
    </developer>
  </developers>)
