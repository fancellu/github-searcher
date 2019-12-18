name := "github-searcher"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies += "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.1.2"
libraryDependencies += "com.typesafe.play" %% "play-ws-standalone-json" % "2.1.2"

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.29"

fork in run := true