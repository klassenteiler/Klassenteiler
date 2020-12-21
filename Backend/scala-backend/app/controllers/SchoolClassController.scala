package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import models.SchoolClassModel
import models.SchoolClassCC

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

  // this is a deserializer needed to convert Json to SchoolClass case class
  implicit val schoolClassReads = Json.reads[SchoolClassCC]

  private val model = new SchoolClassModel(db)

  /** Create an Action to create a new SchoolClass
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `POST` request with
    * a path of `/createClass`.
    */
  def createSchoolClass() = Action.async { implicit request =>
    request.body.asJson match {
      // since this returns an option we need to cover the case that 
      // the json contains something and the case that it is empty
      case Some(x) =>
        // this parses the json into a Schoolclass case class
        Json.fromJson[SchoolClassCC](x) match {
          case JsSuccess(schoolclass, path) =>
            // calls the SchoolClass model and returns either true or false
            model.createSchoolClass(schoolclass).map{ creationSuccess =>
              if (creationSuccess){
                  Ok(Json.toJson(true))
              }else{
                  Conflict(Json.toJson("Creation failed"))
              }
            }
          case e  @ JsError(_) => Future.successful(UnsupportedMediaType(Json.toJson("Wrong format")))
        }
        
      case None => Future.successful(BadRequest("Empty Body"))
    }
  }
}
