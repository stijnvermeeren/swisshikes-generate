name := "swisshikes-generate"

version := "1.0"

scalaVersion := "3.3.4"

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.1.0"
libraryDependencies += "com.typesafe" % "config" % "1.4.1"
libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "6.5.0.202303070854-r"
libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.15.0"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.0"
libraryDependencies += "commons-io" % "commons-io" % "2.11.0"
libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.12.470"
libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.2.2"
libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.3.1"

assembly / assemblyJarName := "swisshikes-generate.jar"

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
