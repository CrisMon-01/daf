package it.gov.daf.catalogmanager.utilities

/**
  * Created by ale on 11/05/17.g
  */

import javax.inject.Inject

import play.api.{Configuration, Environment}

/**
  * Created by ale on 16/04/17.
  */
private class AppConfig @Inject()(playConfig: Configuration) {
  val userIdHeader: Option[String] = playConfig.getString("app.userid.header")
  val ckanHost: Option[String] = playConfig.getString("app.ckan.url")
  val dbHost: Option[String] = playConfig.getString("mongo.host")
  val dbPort: Option[Int] = playConfig.getInt("mongo.port")
  val userName :Option[String] = playConfig.getString("mongo.username")
  val password :Option[String] = playConfig.getString("mongo.password")
  val database :Option[String] = playConfig.getString("mongo.database")
  val localUrl :Option[String] = playConfig.getString("app.local.url")
  val securityManHost :Option[String] = playConfig.getString("security.manager.host")
  val cookieExpiration :Option[Long] = playConfig.getLong("cookie.expiration")
  val ingestionUser :Option[String] = playConfig.getString("ingestion.user")
  val ingestionPass :Option[String] = playConfig.getString("ingestion.pass")
}



object ConfigReader {
  private val config = new AppConfig(Configuration.load(Environment.simple()))
  def userIdHeader: String = config.userIdHeader.getOrElse("userid")
  def getCkanHost: String = config.ckanHost.getOrElse("localhost")
  def getDbHost: String = config.dbHost.getOrElse("localhost")
  def getDbPort: Int = config.dbPort.getOrElse(27017)
  def database :String = config.database.getOrElse("catalog_manager")
  def password :String = config.password.getOrElse("")
  def userName :String = config.userName.getOrElse("")
  def localUrl :String = config.localUrl.getOrElse("http://localhost:9001")
  def securityManHost :String = config.securityManHost.getOrElse("http://localhost:9002/security-manager")
  def cookieExpiration:Long = config.cookieExpiration.getOrElse(30L)// 30 min by default
  def ingestionUser :String = config.ingestionUser.getOrElse("daf_ingestion")
  def ingestionPass :String = config.ingestionPass.getOrElse("XXXXXXXXXXXXX")


}

