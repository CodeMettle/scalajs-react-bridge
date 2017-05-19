enablePlugins(ScalaJSPlugin)

organization := "com.payalabs"
name := "scalajs-react-bridge"

version := "0.3.1-CODEMETTLE"

crossScalaVersions := Seq("2.12.1", "2.11.7")

scalaVersion := crossScalaVersions.value.head

jsDependencies += RuntimeDOM

jsEnv := PhantomJSEnv().value

libraryDependencies ++= {
  val scalaJsDomV = "0.9.1"
  val scalaJsReactV = "0.11.3"
  val scalatestV = "3.0.1"
  Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
    "org.scala-js" %%% "scalajs-dom" % scalaJsDomV % Provided,
    "com.github.japgolly.scalajs-react" %%% "core" % scalaJsReactV % Provided,
    "org.scalatest" %%% "scalatest" % scalatestV % Test,
    "com.github.japgolly.scalajs-react" %%% "test" % scalaJsReactV % Test
  )
}

jsDependencies += "org.webjars.npm" % "react" % "0.14.0" % Test / "react-with-addons.js" minified "react-with-addons.min.js" commonJSName "React"
jsDependencies += "org.webjars.npm" % "react-dom" % "0.14.0" % Test / "react-dom.js" minified "react-dom.min.js" commonJSName "ReactDOM"
jsDependencies in Test += ProvidedJS / "test-component.js" dependsOn "react-with-addons.js"

sourceGenerators in Compile <+= sourceManaged in Compile map { dir =>
  val file = dir / "com" / "payalabs" / "scalajs" / "react" / "bridge" / "GeneratedImplicits.scala"

  val f0_22 = (0 to 22).map { arity =>
    val argsParams = (1 to arity).map(i => s"T$i").mkString(",")
    val params = if (argsParams.isEmpty) "R" else s"$argsParams,R"
    val callbackParams = if (argsParams.isEmpty) "CallbackTo[R]" else s"$argsParams,CallbackTo[R]"
    val valueParams = (1 to arity).map(i => s"_:T$i").mkString(",")
    s"""
      |implicit def function${arity}Writer[$params]: JsWriter[Function$arity[$params]] = {
      |  new JsWriter[Function$arity[$params]] {
      |    override def toJs(value: Function$arity[$params]): js.Any = fromFunction$arity(value)
      |  }
      |}
      |
      |implicit def function${arity}CallbackWriter[$params]: JsWriter[Function$arity[$callbackParams]] = {
      |  new JsWriter[Function$arity[$callbackParams]] {
      |    override def toJs(value: Function$arity[$callbackParams]): js.Any = fromFunction$arity(value($valueParams).runNow)
      |  }
      |}""".stripMargin
  }.mkString("\n")


  IO.write(file, s"""
    |package com.payalabs.scalajs.react.bridge
    |
    |import scala.scalajs.js
    |import scala.scalajs.js.Any._
    |import japgolly.scalajs.react.CallbackTo
    |
    |trait GeneratedImplicits {
    |  $f0_22
    |}
    """.stripMargin
  )

  Seq(file)
}

pomExtra in Global := {
  <url>https://github.com/payalabs/scalajs-react-bridge</url>
  <licenses>
    <license>
      <name>The MIT License (MIT)</name>
      <url>https://opensource.org/licenses/MIT</url>
    </license>
  </licenses>
  <scm>
    <connection>scm:git:github.com/payalabs/scalajs-react-bridge</connection>
    <developerConnection>scm:git:git@github.com:payalabs/scalajs-react-bridge</developerConnection>
    <url>github.com/payalabs/scalajs-react-bridge</url>
  </scm>
  <developers>
    <developer>
      <id>ramnivas</id>
      <name>Ramnivas Laddad</name>
    </developer>
  </developers>
}
    
	publishMavenStyle := true
	    publishTo := Some("CodeMettle Maven" at ("http://maven.codemettle.com/repository/" + (if (isSnapshot.value) "snapshots" else "internal")))
		    credentials += Credentials(Path.userHome / ".m2" / "credentials-snapshots")
			    credentials += Credentials(Path.userHome / ".m2" / "credentials-internal")

