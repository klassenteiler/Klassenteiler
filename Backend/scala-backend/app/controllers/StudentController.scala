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
class StudentController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    val cc: ControllerComponents,
    val auth: AuthenticationController
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile] {

  private val model: StudentModel = new StudentModel(db)
  
  def getSignups(implicit id: Int, classSecret: String): play.api.mvc.Action[play.api.mvc.AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
   
    // this is the body of this method put into a function which is then passed to the authentication wrapper method
    // the function must take a classTeacher as input but we do not use it
    val body = {_:ClassTeacherCC => model.getNumberOfStudents(id).map(number => Ok(Json.toJson(number)))} //return

    // this calls the teacher authentication wrapper method, because the teacher needs to submit the correct teachersecret in order to get access to the student count
    auth.withTeacherAuthentication(body)
  }
}
