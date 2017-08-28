

name := """highLoad"""
organization := "kotobotov.ru"

version := "1.0-SNAPSHOT"

lazy val root = project.in(file(".")).enablePlugins(PlayScala).disablePlugins(PlayFilters)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test
//val unpac = if (new File("C:\\inetpub\\play\\Obuchenie\\highload\\resource\\data.zip" ).exists()) sbt.IO.unzip(new File("C:\\inetpub\\play\\Obuchenie\\highload\\resource\\data.zip" ), new File("C:\\inetpub\\play\\Obuchenie\\highload\\resource\\" )).mkString
val unpac = if (new File("/tmp/data/data.zip" ).exists()) sbt.IO.unzip(new File("/tmp/data/data.zip" ), new File("/root/resource/" )).mkString

//https://github.com/schmitch/performance - posmotret pro perfomanse i nastroyki
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "kotobotov.ru.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "kotobotov.ru.binders._"
