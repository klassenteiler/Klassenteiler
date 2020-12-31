package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import models.SchoolClassModel
import models.SchoolClassCC
import models.SchoolClassDB
import models.ClassTeacherCC

import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.ExecutionContext
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Future

/** This controller handles request that pertain to the creation, deletion and modification of schoolclasses
  */
@Singleton
class SchoolClassController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    val cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile] {

  // this is a deserializer needed to convert Json to SchoolClass case class and ClassTeacherCC

  implicit val schoolClassReads = Json.reads[SchoolClassCC]
  implicit val schoolClassWrites = Json.writes[SchoolClassCC]

  implicit val classTeacherReads = Json.reads[ClassTeacherCC]
  implicit val classTeacherWrites = Json.writes[ClassTeacherCC]

  private val model = new SchoolClassModel(db)

  /** Create an Action to create a new SchoolClass
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `POST` request with
    * a path of `/createClass`.
    */
  def createSchoolClass(): play.api.mvc.Action[play.api.mvc.AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    request.body.asJson match {
      // since this returns an option we need to cover the case that
      // the json contains something and the case that it is empty
      case Some(content) =>
        // extract json values from JsObject
        val schoolClassJs: JsValue = content("schoolClass")
        val classTeacherJs: JsValue = content("classTeacher")

        // parse the Json Values
        val schoolClassOption: JsResult[SchoolClassCC] =
          Json.fromJson[SchoolClassCC](schoolClassJs)
        val classTeacherOption: JsResult[ClassTeacherCC] =
          Json.fromJson[ClassTeacherCC](classTeacherJs)

        // we need to check whether the parsing was successful and, if not, return a 415
        if (schoolClassOption.isSuccess && classTeacherOption.isSuccess) {
          val sc: SchoolClassCC = schoolClassOption.get
          val ct: ClassTeacherCC = classTeacherOption.get

          val schoolClass: SchoolClassDB = SchoolClassDB(
            null, // id
            sc.className,
            sc.schoolName,
            sc.classSecret,
            sc.publicKey,
            ct.teacherSecret,
            ct.encryptedPrivateKey,
            null // surveystatus
          )
          model
            .createSchoolClass(schoolClass) // returns SchoolClassCC
            .map(insertedClass => Ok(Json.toJson(insertedClass))) //return
        } else
          Future.successful(
            UnsupportedMediaType(Json.toJson("Wrong JSON format")) //return
          )

      case None => Future.successful(BadRequest("Empty Body")) //return
    }
  }

  // GET /getClass/:id/:classSecret
  // returns schoolclass as json
  def getSchoolClass(id: Int, classSecret: String): play.api.mvc.Action[play.api.mvc.AnyContent] = Action.async { implicit request: Request[AnyContent] =>
      val accepted: Future[Boolean] = model.validateAccess(id, classSecret)
      accepted.flatMap(a => {
        if(a) {
          model.getSchoolClass(id).map(returnedClass => Ok(Json.toJson(returnedClass))) //return
        }else{
          Future.successful(NotFound("Schoolclass with that id not found or wrong classSecret")) //return
        }
      })
      
  }
  // GET /teacherAuth/:id/:classSecret
  // returns ClassTeacherT, eine abgespeckte version der schoolclass
  def authenticateTeacher(id: Int, classSecret: String): play.api.mvc.Action[play.api.mvc.AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val accepted: Future[Boolean] = model.validateAccess(id, classSecret)
    accepted.flatMap(a => {
        if(a) {
          request.headers.get("teacherSecret") match {
            case Some(teacherSecret) => {
                model.getTeacher(id, teacherSecret).map(result => result match {
                  case Some(teacher) => Ok(Json.toJson(teacher)) //return
                  case None => Forbidden("Wrong teacherSecret") //return
                })
            }
            case None => Future.successful(BadRequest("No teacherSecret provided")) //return
          }
        }else{
          Future.successful(NotFound("Schoolclass with that id not found or wrong classSecret")) //return
        }
     })
    

  }

}
