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
class SurveyController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    val cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile] {
  

    // POST /submitStudentSurvey/:id/:classSecret
    // fügt student zu student table hinzu
    // fügt friends und student zu relations table hinzu
    def submitSurvey(id: Int, classSecret: String) = Action { implicit request: Request[AnyContent] =>
        Ok("todo")
    }

    // PUT /closeSurvey/:id/:classSecret
    // setzt survey status von schoolclass mit der relevanten id und 
    def closeSurvey(id: Int, classSecret: String) = Action { implicit request: Request[AnyContent] =>
        Ok("todo")
    }

    // PUT
    // setzt survey status auf 2 ('calculating') 
    // ruft alle relationships auf
    // ruft internen algorithmus auf
    // ändert alle groupbelonging einträge in der students datenbank
    // setzt survey status auf 3 (done)
    def startCalculating(id: Int, classSecret: String) = Action { implicit request: Request[AnyContent] =>
        Ok("todo")
    }

    // GET /getResult/:id/:classSecret
    // checkt ob der status von der relevanten schoolclass korrekt ist und queried die studenttable mit classid == id
    // returns Array[StudentCC] 
    def getResults(id: Int, classSecret: String) = Action { implicit request: Request[AnyContent] =>
        Ok("todo")
    } 

}
