lazy val akkaHttpVersion = "10.1.9"
lazy val akkaVersion    = "2.6.0-M5"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.stocks",
      scalaVersion := "2.12.6",
      name := "stock-backend"
    )),
    name := "TestProject",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "com.typesafe.akka" %% "akka-http-jackson"    % akkaHttpVersion,

      "com.typesafe.akka" %% "akka-testkit"      % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "junit"              % "junit"             % "4.12"          % Test,
      "com.novocode"       % "junit-interface"   % "0.10"          % Test
    ),

    testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
  )
