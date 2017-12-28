import CommonBuild._
import Versions._
import com.typesafe.sbt.packager.docker.Cmd
import uk.gov.hmrc.gitstamp.GitStampPlugin._

organization in ThisBuild := "it.gov.daf"
name := "daf-iot-ingestion-manager"

Seq(gitStampSettings: _*)

version in ThisBuild := "1.0.0-bug178"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-Xfuture"
)

// wartremoverErrors ++= Warts.allBut(Wart.Nothing, Wart.PublicInference, Wart.Any, Wart.Equals, Wart.Option2Iterable)
// wartremoverExcluded ++= getRecursiveListOfFiles(baseDirectory.value / "target" / "scala-2.11" / "routes").toSeq
// wartremoverExcluded ++= routes.in(Compile).value

lazy val client = (project in file("client")).
  settings(Seq(
    name := "daf-iot-ingestion-manager-client",
    swaggerGenerateClient := true,
    swaggerClientCodeGenClass := new it.gov.daf.swaggergenerators.DafClientGenerator,
    swaggerCodeGenPackage := "it.gov.daf.iotingestionmanager",
    swaggerModelFilesSplitting := "oneFilePerModel",
    swaggerSourcesDir := file(s"${baseDirectory.value}/../conf"),
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % playVersion,
      "com.typesafe.play" %% "play-ws" % playVersion
    )
  )).
  enablePlugins(SwaggerCodegenPlugin)

lazy val common = (project in file("common")).
  settings(Seq(
    name := "daf-iot-ingestion-manager-common",
    libraryDependencies ++= Seq(
      "org.apache.avro" % "avro" % avroVersion,
      "com.twitter" %% "bijection-avro" % twitterBijectionVersion,
      "org.specs2" %% "specs2-core" % specs2Version % "test",
      "org.specs2" %% "specs2-matcher" % specs2Version % "test",
      "org.json4s" %% "json4s-native" % json4sVersion % "test"
    )
  ) ++ sbtavrohugger.SbtAvrohugger.specificAvroSettings ++ Seq(
    (scalaSource in avroConfig) := new java.io.File(s"${baseDirectory.value}/src/generated/scala")
  ))

lazy val event_generator = (project in file("event-generator")).
  settings(Seq(
    name := "daf-iot-ingestion-manager-event-generator",
    libraryDependencies ++= Seq(
      "org.apache.avro" % "avro" % avroVersion % "compile",
      "com.twitter" %% "bijection-avro" % twitterBijectionVersion % "compile",
      "org.apache.kafka" % "kafka-clients" % kafkaVersion % "compile",
      "org.rogach" %% "scallop" % scallopVersion % "compile"
    )
  )).
  enablePlugins(AssemblyPlugin).
  dependsOn(common)

lazy val root = (project in file(".")).
  enablePlugins(PlayScala, ApiFirstCore, ApiFirstPlayScalaCodeGenerator, ApiFirstSwaggerParser, /*AutomateHeaderPlugin,*/ DockerPlugin).
  dependsOn(client, common).aggregate(client, common)

scalaVersion in ThisBuild := "2.11.11"

val sparkExcludes =
  (moduleId: ModuleID) => moduleId.
    exclude("org.apache.hadoop", "hadoop-client").
    exclude("org.apache.hadoop", "hadoop-yarn-client").
    exclude("org.apache.hadoop", "hadoop-yarn-api").
    exclude("org.apache.hadoop", "hadoop-yarn-common").
    exclude("org.apache.hadoop", "hadoop-yarn-server-common").
    exclude("org.apache.hadoop", "hadoop-yarn-server-web-proxy").
    exclude("org.apache.zookeeper", "zookeeper").
    exclude("commons-collections", "commons-collections").
    exclude("commons-beanutils", "commons-beanutils").
    exclude("org.slf4j", "slf4j-log4j12")

val hbaseExcludes =
  (moduleID: ModuleID) => moduleID.
    exclude("org.apache.thrift", "thrift").
    exclude("org.jruby", "jruby-complete").
    exclude("org.slf4j", "slf4j-log4j12").
    exclude("org.mortbay.jetty", "jsp-2.1").
    exclude("org.mortbay.jetty", "jsp-api-2.1").
    exclude("org.mortbay.jetty", "servlet-api-2.5").
    exclude("com.sun.jersey", "jersey-core").
    exclude("com.sun.jersey", "jersey-json").
    exclude("com.sun.jersey", "jersey-server").
    exclude("org.mortbay.jetty", "jetty").
    exclude("org.mortbay.jetty", "jetty-util").
    exclude("tomcat", "jasper-runtime").
    exclude("tomcat", "jasper-compiler").
    exclude("org.jboss.netty", "netty").
    exclude("io.netty", "netty").
    exclude("commons-logging", "commons-logging").
    exclude("org.apache.xmlgraphics", "batik-ext").
    exclude("commons-collections", "commons-collections").
    exclude("xom", "xom").
    exclude("commons-beanutils", "commons-beanutils")

val hadoopExcludes =
  (moduleId: ModuleID) => moduleId.
    exclude("org.slf4j", "slf4j-api").
    exclude("org.slf4j", "slf4j-log4j12").
    exclude("org.mortbay.jetty", "jetty").
    exclude("org.mortbay.jetty", "jetty-util").
    exclude("org.mortbay.jetty", "jetty-sslengine").
    exclude("javax.servlet", "servlet-api")

val hadoopHBaseExcludes =
  (moduleId: ModuleID) => moduleId.
    exclude("org.slf4j", "slf4j-log4j12").
    exclude("javax.servlet", "servlet-api").
    excludeAll(ExclusionRule(organization = "javax.servlet"))

val kafkaExcludes =
  (moduleId: ModuleID) => moduleId.
    exclude("org.slf4j", "slf4j-log4j12")

val applicationLibraries = Seq(
  "org.apache.spark.opentsdb" %% "spark-opentsdb" % sparkOpenTSDBVersion % "compile" exclude("org.slf4j", "slf4j-log4j12"),
  "org.apache.kudu" % "kudu-client" % kuduVersion % "compile",
  "org.apache.kudu" %% "kudu-spark2" % kuduVersion % "compile",
  sparkExcludes("org.apache.spark" %% "spark-core" % sparkVersion % "compile"),
  sparkExcludes("org.apache.spark" %% "spark-sql" % sparkVersion % "compile"),
  sparkExcludes("org.apache.spark" %% "spark-yarn" % sparkVersion % "compile"),
  sparkExcludes("org.apache.spark" %% "spark-mllib" % sparkVersion % "compile"),
  sparkExcludes("org.apache.spark" %% "spark-streaming" % sparkVersion % "compile"),
  sparkExcludes("org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion % "compile"),
  kafkaExcludes("org.apache.kafka" %% "kafka" % kafkaVersion % "compile"),
  kafkaExcludes("org.apache.kafka" % "kafka-clients" % kafkaVersion % "compile"),
  hbaseExcludes("org.apache.hbase" % "hbase-client" % hbaseVersion % "compile"),
  hbaseExcludes("org.apache.hbase" % "hbase-protocol" % hbaseVersion % "compile"),
  hbaseExcludes("org.apache.hbase" % "hbase-hadoop-compat" % hbaseVersion % "compile"),
  hbaseExcludes("org.apache.hbase" % "hbase-server" % hbaseVersion % "compile"),
  hbaseExcludes("org.apache.hbase" % "hbase-common" % hbaseVersion % "compile"),
  hadoopExcludes("org.apache.hadoop" % "hadoop-yarn-server-web-proxy" % hadoopVersion % "compile"),
  hadoopExcludes("org.apache.hadoop" % "hadoop-client" % hadoopVersion % "compile"),
  "org.typelevel" %% "cats" % catsVersion % "compile",
  hadoopHBaseExcludes("org.apache.hbase" % "hbase-server" % hbaseVersion % "test" classifier "tests"),
  hadoopHBaseExcludes("org.apache.hbase" % "hbase-common" % hbaseVersion % "test" classifier "tests"),
  hadoopHBaseExcludes("org.apache.hbase" % "hbase-testing-util" % hbaseVersion % "test" classifier "tests"
    exclude("org.apache.hadoop<", "hadoop-hdfs")
    exclude("org.apache.hadoop", "hadoop-minicluster")
    exclude("org.apache.hadoo", "hadoop-client")),
  hadoopHBaseExcludes("org.apache.hbase" % "hbase-hadoop-compat" % hbaseVersion % "test" classifier "tests"),
  hadoopHBaseExcludes("org.apache.hbase" % "hbase-hadoop-compat" % hbaseVersion % "test" classifier "tests" extra "type" -> "test-jar"),
  hadoopHBaseExcludes("org.apache.hbase" % "hbase-hadoop2-compat" % hbaseVersion % "test" classifier "tests" extra "type" -> "test-jar"),
  hadoopHBaseExcludes("org.apache.hadoop" % "hadoop-client" % hadoopVersion % "test" classifier "tests"),
  hadoopHBaseExcludes("org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % "test" classifier "tests"),
  hadoopHBaseExcludes("org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % "test" classifier "tests" extra "type" -> "test-jar"),
  hadoopHBaseExcludes("org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % "test" extra "type" -> "test-jar"),
  hadoopHBaseExcludes("org.apache.hadoop" % "hadoop-client" % hadoopVersion % "test" classifier "tests"),
  hadoopHBaseExcludes("org.apache.hadoop" % "hadoop-minicluster" % hadoopVersion % "test"),
  hadoopHBaseExcludes("org.apache.hadoop" % "hadoop-common" % hadoopVersion % "test" classifier "tests" extra "type" -> "test-jar"),
  hadoopHBaseExcludes("org.apache.hadoop" % "hadoop-mapreduce-client-jobclient" % hadoopVersion % "test" classifier "tests"),
  kafkaExcludes("org.apache.kafka" %% "kafka" % kafkaVersion % "test" classifier "test"),
  kafkaExcludes("org.apache.kafka" % "kafka-clients" % kafkaVersion % "test" classifier "test"),
  "org.json4s" %% "json4s-native" % json4sVersion % "test",
  "com.github.pathikrit" %% "better-files" % betterFilesVersion % Test,
  "org.apache.kudu" % "kudu-client" % kuduVersion % "test" classifier "tests"
)

dependencyOverrides += "com.google.guava" % "guava" % "12.0.1" % "compile"

libraryDependencies ++= Seq(
  cache,
  ws,
  "io.swagger" % "swagger-core" % "1.5.8",
  "org.webjars" % "swagger-ui" % swaggerUiVersion,
  "it.gov.daf" %% "common" % dafCommonVersion,
  specs2 % Test
) ++ applicationLibraries

resolvers ++= Seq(
  "zalando-bintray" at "https://dl.bintray.com/zalando/maven",
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  "jeffmay" at "https://dl.bintray.com/jeffmay/maven",
  Resolver.url("sbt-plugins", url("http://dl.bintray.com/zalando/sbt-plugins"))(Resolver.ivyStylePatterns),
  "cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
  "daf repo" at "http://nexus.default.svc.cluster.local:8081/repository/maven-public/"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

apiFirstParsers := Seq(ApiFirstSwaggerParser.swaggerSpec2Ast.value).flatten

playScalaAutogenerateTests := false

playScalaCustomTemplateLocation := Some(baseDirectory.value / "templates")

licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
headerLicense := Some(HeaderLicense.ALv2("2017", "TEAM PER LA TRASFORMAZIONE DIGITALE"))
headerMappings := headerMappings.value + (HeaderFileType.conf -> HeaderCommentStyle.HashLineComment)

dockerBaseImage := "anapsix/alpine-java:8_jdk_unlimited"
dockerCommands := dockerCommands.value.flatMap {
  case cmd@Cmd("FROM", _) => List(cmd,
    Cmd("RUN", "apk update && apk add bash krb5-libs krb5"),
    Cmd("RUN", "ln -sf /etc/krb5.conf /opt/jdk/jre/lib/security/krb5.conf")
  )
  case other => List(other)
}
dockerEntrypoint := Seq(s"bin/${name.value}", "-Dconfig.file=conf/production.conf")
dockerCmd := Seq("-jvm-debug", "5005")
dockerExposedPorts := Seq(9900)
dockerRepository := Option("10.98.74.120:5000")

publishTo in ThisBuild := {
  val nexus = "http://nexus.default.svc.cluster.local:8081/repository/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "maven-snapshots/")
  else
    Some("releases" at nexus + "maven-releases/")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
