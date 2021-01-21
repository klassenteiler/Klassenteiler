package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import utils.MockDatabase
import scala.concurrent.Future
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._
import models._

/** Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class SchoolClassControllerSpec
    extends PlaySpec
    with MockDatabase
    with Injecting {

  // dbConfigProvider comes from MockDatabase, stubControllerComponents from Helpers
  val authenticationController =
    new AuthenticationController(dbConfigProvider, stubControllerComponents())
  val controller = new SchoolClassController(
    dbConfigProvider,
    stubControllerComponents(),
    authenticationController
  )

  implicit val schoolClassReads = Json.reads[SchoolClassCC]

  val json: JsValue = Json.obj(
    "schoolClass" ->
      Json.obj(
        "schoolName" -> "testSchool",
        "className" -> "testClassName",
        "classSecret" -> "afasdf",
        "publicKey" -> "uiuiuiuiui"
      ),
    "classTeacher" ->
      Json.obj(
        "encryptedPrivateKey" -> "asdfadsf",
        "teacherSecret" -> "äölklökök"
      )
  )

  val wrongJson: JsValue = Json.obj(
    "schoolClass" ->
      Json.obj(
        "schoolName" -> "testSchool",
        "className" -> "testClassName",
        // class secret is missing
        "publicKey" -> "uiuiuiuiui"
      ),
    "classTeacher" ->
      Json.obj(
        "encryptedPrivateKey" -> "asdfadsf",
        "teacherSecret" -> "äölklökök"
      )
  )

  // clear Database and insert one schoolClass
  this.clearDatabase();
  val classModel: SchoolClassModel = new SchoolClassModel(db)
  val schoolClass: SchoolClassDB = SchoolClassDB(
    None,
    "test",
    Some("AMG"),
    "clsSecret",
    "teachsecret",
    "puKey",
    "encPrivateKey",
    Some(0)
  )
  val createdSchoolClass: SchoolClassCC =
    awaitInf(classModel.createSchoolClass(schoolClass))
  val classId = createdSchoolClass.id.get

  "SchoolClassController" should {
    "return status 400 if no body is provided" in {
      val result: Future[Result] =
        controller.createSchoolClass().apply(FakeRequest())
      status(result) mustBe BadRequest.header.status //i.e. 400
    }
    "return status 415 if format of body is wrong" in {
      val body: JsObject = Json.obj("value" -> "wrong")
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(body)
      val result: Future[Result] = controller.createSchoolClass().apply(request)
      status(result) mustBe UnsupportedMediaType.header.status //i.e. 415

      val request2: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(
          wrongJson
        ) // schoolClass and classTeacher are provided, but with a missing value
      val result2: Future[Result] =
        controller.createSchoolClass().apply(request)
      status(result2) mustBe UnsupportedMediaType.header.status //i.e. 415
    }
    "return status 200 and the created schoolClass if a correct request is sent" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(json)
      val result: Future[Result] = controller.createSchoolClass().apply(request)
      status(result) mustBe Ok.header.status
      val resultBody: String =
        contentAsString(result) // automatically waits for future
      val jsBody: JsValue = Json.parse(resultBody)
      val schoolClass: SchoolClassCC = Json.fromJson[SchoolClassCC](jsBody).get
      schoolClass.className mustBe "testClassName"
      schoolClass.id.get mustBe 2 // because we initially entered one class manually

      val createdClass: SchoolClassCC = awaitInf(classModel.getSchoolClass(schoolClass.id.get))
      createdClass.className mustBe "testClassName"
    }
    "should return schoolClass by id" in {
      val result: Future[Result] =
        controller
          .getSchoolClass(classId, schoolClass.classSecret)
          .apply(
            FakeRequest().withHeaders(
              Headers("teacherSecret" -> schoolClass.teacherSecret)
            )
          )
      status(result) mustBe Ok.header.status
      val resultBody: String =
        contentAsString(result) // automatically waits for future
      val jsBody: JsValue = Json.parse(resultBody)
      val returnedSchoolClass: SchoolClassCC =
        Json.fromJson[SchoolClassCC](jsBody).get
      returnedSchoolClass.className mustBe schoolClass.className
    }
    "return status 404 if classSecret or id is wrong" in {
      val result1: Future[Result] =
        controller
          .getSchoolClass(99, schoolClass.classSecret) // wrong id
          .apply(FakeRequest())
      status(result1) mustBe NotFound.header.status

      val result2: Future[Result] =
        controller
          .getSchoolClass(classId, "wrong Secret")
          .apply(FakeRequest())
      status(result2) mustBe NotFound.header.status
    }
  }
}
