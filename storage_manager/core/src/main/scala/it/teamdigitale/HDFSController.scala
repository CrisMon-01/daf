package it.teamdigitale

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Try}

class HDFSController(sparkSession: SparkSession) {

  val alogger: Logger = LoggerFactory.getLogger(this.getClass)

  def readData(path: String, format: String): Try[DataFrame] =  {
    format match {
      case "csv" => Try {
        val pathFixAle = path + "/" + path.split("/").last + ".csv"
        println(s"questo e' il path ${pathFixAle}")
        sparkSession.read.csv(pathFixAle)
      }
      case "parquet" => Try(sparkSession.read.parquet(path))
      case "avro" =>
        import com.databricks.spark.avro._
        Try(sparkSession.read.avro(path))
     case x => Failure(new IllegalArgumentException(s"Format $x is not implemented"))
    }
  }
}
