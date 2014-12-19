organization  := "ru.nkdhny"

version       := "0.1-SNAPSHOT"

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
