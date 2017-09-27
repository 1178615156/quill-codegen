package gen

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

import slick.jdbc.JdbcProfile
import slick.model.Model


trait DataBaseModel {
  val profileInstance: JdbcProfile
  val jdbcDriver     : String
  val url            : String
  val user           : Option[String]
  val password       : Option[String]

  val ignoreInvalidDefaults: Boolean = true

  lazy          val dbFactory = profileInstance.api.Database
  lazy          val db        = dbFactory.forURL(url, driver = jdbcDriver, user = user.orNull, password = password.orNull, keepAliveConnection = true)
  implicit lazy val m: Model  = Await.result(db.run(profileInstance.createModel(None, ignoreInvalidDefaults)(ExecutionContext.global).withPinnedSession), Duration.Inf)
}
