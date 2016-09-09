import com.lihaoyi.workbench.Plugin._

enablePlugins(ScalaJSPlugin)

name := "scalajs-react-template"

version := "1.0"

scalaVersion := "2.11.7"


// create launcher file ( its search for object extends JSApp , make sure there is only one file)
persistLauncher := true

persistLauncher in Test := false

val scalaJSReactVersion = "0.11.1"

val scalaCssVersion = "0.4.1"

val reactJSVersion = "15.2.1"

val jQuery = "1.11.1"
val bootstrap = "3.3.6"

libraryDependencies ++= Seq("com.github.japgolly.scalajs-react" %%% "core" % scalaJSReactVersion,
  "com.github.japgolly.scalajs-react" %%% "extra" % "0.11.1",
    "com.github.japgolly.scalacss" %%% "core" % "0.5.0",
   "com.github.japgolly.scalacss" %%% "ext-react" % "0.5.0",
   "com.lihaoyi" %%% "upickle" % "0.4.1")


jsDependencies ++= Seq(

  "org.webjars.bower" % "react" % reactJSVersion
    /        "react-with-addons.js"
    minified "react-with-addons.min.js"
    commonJSName "React",

  "org.webjars.bower" % "react" % reactJSVersion
    /         "react-dom.js"
    minified  "react-dom.min.js"
    dependsOn "react-with-addons.js"
    commonJSName "ReactDOM",

  "org.webjars.bower" % "react" % reactJSVersion
    /         "react-dom-server.js"
    minified  "react-dom-server.min.js"
    dependsOn "react-dom.js"
    commonJSName "ReactDOMServer",
  "org.webjars" % "jquery" %  jQuery
    /         "jquery.js"
    minified  "jquery.min.js",
  "org.webjars" % "bootstrap" %  bootstrap
    /         "bootstrap.js"
    minified  "bootstrap.min.js"
    dependsOn "jquery.js"
)

// creates single js resource file for easy integration in html page
skip in packageJSDependencies := false



// copy  javascript files to js folder,that are generated using fastOptJS/fullOptJS

crossTarget in (Compile, fullOptJS) := file("js")

crossTarget in (Compile, fastOptJS) := file("js")

crossTarget in (Compile, packageJSDependencies) := file("js")

crossTarget in (Compile, packageScalaJSLauncher) := file("js")

crossTarget in (Compile, packageMinifiedJSDependencies) := file("js")

artifactPath in (Compile, fastOptJS) := ((crossTarget in (Compile, fastOptJS)).value /
  ((moduleName in fastOptJS).value + "-opt.js"))



scalacOptions += "-feature"

scalaJSUseRhino in Global := false

workbenchSettings

bootSnippet := "ReactApp().main();"

refreshBrowsers <<= refreshBrowsers.triggeredBy(fastOptJS in Compile)
//updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)


