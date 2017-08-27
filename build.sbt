name := """highLoad"""
organization := "kotobotov.ru"

version := "1.0-SNAPSHOT"

lazy val root = project.in(file(".")).enablePlugins(PlayScala).disablePlugins(PlayFilters)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test


//https://github.com/schmitch/performance - posmotret pro perfomanse i nastroyki
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "kotobotov.ru.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "kotobotov.ru.binders._"
