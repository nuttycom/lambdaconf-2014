name := "fperfect"

resolvers ++= Seq(
  "typesafe"           at "http://repo.typesafe.com/typesafe/releases",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

libraryDependencies ++= Seq(
  "org.scalaz"      %% "scalaz-core"        % "7.1.0-M6",
  "org.scalaz"      %% "scalaz-effect"      % "7.1.0-M6",
  "org.spire-math"  %% "spire"              % "0.7.3"
)
