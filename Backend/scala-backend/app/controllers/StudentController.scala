package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import models._

import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.ExecutionContext
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Future



@Singleton
class StudentController @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, val cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile] {
  


  // GET /nSignups/:id/:classSecret
  // queries Student Table nach allen einträgen mit classId = id und self-reported == true
  def getSignups(id: Int, classSecret: String) = Action { implicit request: Request[AnyContent] =>
    Ok("todo")
  }

}