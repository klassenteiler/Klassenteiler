package controllers

import scala.concurrent.Future
import play.api._
import play.api.mvc._
import play.api.libs.json._
import models._

import javax.inject._
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.ExecutionContext
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._


// the AuthenticationController provides authentication wrapper methods for the other controllers to use
// it can be used via dependency injection
@Singleton
class AuthenticationController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    val cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile] {
  
  implicit val classTeacherWrites = Json.writes[ClassTeacherCC]

  private val scModel: SchoolClassModel = new SchoolClassModel(db)

  //this method can be used whenever an endpoint requires the teacher secret in addition to a correct class secret
    // wrapper that takes in a function and returns a Future[mvc.Result]
                                                      // needs the request and other parameters implicitly
  def withTeacherAuthentication(f: ClassTeacherCC => Future[mvc.Result])(implicit request: Request[AnyContent], id: Int, classSecret: String) = { 
    val accepted: Future[Boolean] = scModel.validateAccess(id, classSecret)
    accepted.flatMap(a => {
        if (a) {
            request.headers.get("teacherSecret") match {
            case Some(teacherSecret) => {
                scModel.getTeacher(id, teacherSecret).flatMap(result =>
                result match {
                    case Some(teacher) =>
                    f(teacher) //return whatever the passed function returns
                    case None => Future.successful(Forbidden("Wrong teacherSecret")) //return
                }
                )
            }
            case None =>
                Future.successful(
                BadRequest("No teacherSecret provided")
                ) //return
            }
        } else {
            Future.successful(
            NotFound("Schoolclass with that id not found or wrong classSecret")
            ) //return
        }
    })
  }

  // GET /teacherAuth/:id/:classSecret
  // returns ClassTeacherT, eine abgespeckte version der schoolclass
  def authenticateTeacher(implicit id: Int, classSecret: String): play.api.mvc.Action[play.api.mvc.AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val accepted: Future[Boolean] = scModel.validateAccess(id, classSecret)

    val body = {teacher: ClassTeacherCC => Future.successful(Ok(Json.toJson(teacher)))}

    withTeacherAuthentication(body)
    

  }

}
