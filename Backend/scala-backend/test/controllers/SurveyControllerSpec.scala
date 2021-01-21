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
import org.scalatest.BeforeAndAfterEach
import Array._
import scala.collection.mutable.ArrayBuffer

/** Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class SurveyControllerSpec
    extends PlaySpec
    with MockDatabase
    with Injecting
    with BeforeAndAfterEach {

  // dbConfigProvider comes from MockDatabase, stubControllerComponents from Helpers
  val authenticationController =
    new AuthenticationController(dbConfigProvider, stubControllerComponents())
  val controller = new SurveyController(
    dbConfigProvider,
    stubControllerComponents(),
    authenticationController
  )

  val classModel = new SchoolClassModel(db)
  val studentModel = new StudentModel(db)
  val relModel = new RelationshipModel(db)

  var classId: Option[Int] = None
  var classSecret: Option[String] = None

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

  override def beforeEach(): Unit = {
    // clear Database and insert one schoolClass
    this.clearDatabase();

    val createdSchoolClass: SchoolClassCC =
      awaitInf(classModel.createSchoolClass(schoolClass))
    classId = createdSchoolClass.id
    classSecret = Some(createdSchoolClass.classSecret)
  }

  val json: JsValue = Json.obj(
    "me" ->
      Json.obj(
        "encryptedName" -> "asdf",
        "hashedName" -> "ego",
        "selfReported" -> true
      ),
    "friends" -> Json.arr(
      Json.obj(
        "encryptedName" -> "asdfadsf",
        "hashedName" -> "alter1",
        "selfReported" -> false
      ),
      Json.obj(
        "encryptedName" -> "asdfadsffdfas",
        "hashedName" -> "alter2",
        "selfReported" -> false
      )
    )
  )

  val friendLimit: Int =
    sys.env.getOrElse("FRIEND_LIMIT", 5).toString.toInt
  val tooManyFriendsArray: ArrayBuffer[JsObject] = ArrayBuffer[JsObject]()
  range(0, friendLimit + 1).foreach(index => {
    tooManyFriendsArray += Json.obj(
      "encryptedName" -> "asdfadsf",
      "hashedName" -> index.toString,
      "selfReported" -> false
    )
  })
  val overLimitJson: JsValue = Json.obj(
    "me" ->
      Json.obj(
        "encryptedName" -> "asdf",
        "hashedName" -> "ego",
        "selfReported" -> true
      ),
    "friends" -> JsArray(tooManyFriendsArray)
  )

  "SurveyController /submitStudentSurvey" should {
    "return status 201, contain message and create students and relationships if request is correct" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(json)
      val result: Future[Result] =
        controller.submitSurvey(classId.get, classSecret.get).apply(request)
      status(result) mustBe Created.header.status
      contentAsString(result) mustBe Json
        .obj(
          "message" -> "success - created"
        )
        .toString
      val allStudents: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId.get))
      allStudents.length mustBe 3

      val allRelations: Seq[(Int, Int)] =
        awaitInf(relModel.getAllRelationIdsOfClass(classId.get))
      allRelations.length mustBe 2

    }
    "return status 403 if student with that name already exists" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(json)
      val result1: Future[Result] =
        controller.submitSurvey(classId.get, classSecret.get).apply(request)
      status(result1) mustBe Created.header.status

      // sending the same request again
      val result2: Future[Result] =
        controller.submitSurvey(classId.get, classSecret.get).apply(request)
      status(result2) mustBe Forbidden.header.status

      val allStudents: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId.get))
      allStudents.length mustBe 3

      val allRelations: Seq[(Int, Int)] =
        awaitInf(relModel.getAllRelationIdsOfClass(classId.get))
      allRelations.length mustBe 2
    }
    "return status 400 if too many friends are tried to be added" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(overLimitJson)
      val result1: Future[Result] =
        controller.submitSurvey(classId.get, classSecret.get).apply(request)
      status(result1) mustBe BadRequest.header.status
    }
    "return status 415 if json body is in wrong format" in {
      val body: JsObject = Json.obj("value" -> "wrong")
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(body)
      val result1: Future[Result] =
        controller.submitSurvey(classId.get, classSecret.get).apply(request)
      status(result1) mustBe UnsupportedMediaType.header.status
    }
    "return status 400 if no body is provided" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
      val result: Future[Result] =
        controller.submitSurvey(classId.get, classSecret.get).apply(request)
      status(result) mustBe BadRequest.header.status
    }
    "return status 410 if the survey of the class is in wrong status" in {
      classModel.updateStatus(classId.get, SurveyStatus.Closed)
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(json)
      val result1: Future[Result] =
        controller.submitSurvey(classId.get, classSecret.get).apply(request)
      status(result1) mustBe Gone.header.status

      classModel.updateStatus(classId.get, SurveyStatus.Calculating)
      val result2: Future[Result] =
        controller.submitSurvey(classId.get, classSecret.get).apply(request)
      status(result2) mustBe Gone.header.status

      classModel.updateStatus(classId.get, SurveyStatus.Done)
      val result3: Future[Result] =
        controller.submitSurvey(classId.get, classSecret.get).apply(request)
      status(result3) mustBe Gone.header.status
    }
    "return status 404 if classSecret or id is wrong" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(json)
      // wrong id
      val result: Future[Result] =
        controller.submitSurvey(99, classSecret.get).apply(request)
      status(result) mustBe NotFound.header.status
      // wrong secret
      val result2: Future[Result] =
        controller.submitSurvey(classId.get, "wrong secret").apply(request)
      status(result2) mustBe NotFound.header.status
    }
  }

  "SurveyController /closeSurvey" should {
    "return status 200, contain message and update status if request is correct" in {
      awaitInf(classModel.getStatus(classId.get)) mustBe SurveyStatus.Open
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )
      val result: Future[Result] =
        controller.closeSurvey(classId.get, classSecret.get).apply(request)
      status(result) mustBe Ok.header.status
      contentAsString(result) mustBe Json
        .obj(
          "message" -> "success - survey closed"
        )
        .toString
      awaitInf(classModel.getStatus(classId.get)) mustBe SurveyStatus.Closed
    }
    "return status 410 if the survey of the class is in wrong status" in {

      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )

      awaitInf(classModel.updateStatus(classId.get, SurveyStatus.Closed))
      val result1: Future[Result] =
        controller.closeSurvey(classId.get, classSecret.get).apply(request)
      status(result1) mustBe Gone.header.status

      awaitInf(classModel.updateStatus(classId.get, SurveyStatus.Calculating))
      val result2: Future[Result] =
        controller.closeSurvey(classId.get, classSecret.get).apply(request)
      status(result2) mustBe Gone.header.status

      awaitInf(classModel.updateStatus(classId.get, SurveyStatus.Done))
      val result3: Future[Result] =
        controller.closeSurvey(classId.get, classSecret.get).apply(request)
      status(result3) mustBe Gone.header.status
    }
    "return status 404 if classSecret or id is wrong" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )

      // wrong id
      val result: Future[Result] =
        controller.closeSurvey(99, classSecret.get).apply(request)
      status(result) mustBe NotFound.header.status
      // wrong secret
      val result2: Future[Result] =
        controller.closeSurvey(classId.get, "wrong secret").apply(request)
      status(result2) mustBe NotFound.header.status
    }
    "return status 401 if teacherSecret is wrong" in {
      val result: Future[Result] =
        controller
          .closeSurvey(classId.get, schoolClass.classSecret)
          .apply(
            FakeRequest().withHeaders(
              Headers("teacherSecret" -> "wrong secret")
            )
          )
      status(result) mustBe Unauthorized.header.status
    }
    "return status 400 if no teacherSecret is provided" in {
      val result: Future[Result] =
        controller
          .closeSurvey(classId.get, schoolClass.classSecret)
          .apply(FakeRequest())
      status(result) mustBe BadRequest.header.status
    }
  }

  "SurveyController /startCalculating" should {
    "return status 200, contain message and update statuses if request is correct" in {
      awaitInf(classModel.updateStatus(classId.get, SurveyStatus.Closed))
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )
      val result: Future[Result] =
        controller.startCalculating(classId.get, classSecret.get).apply(request)
      status(result) mustBe Ok.header.status
      val surveyStatus: Int = awaitInf(classModel.getStatus(classId.get))
      surveyStatus must (equal(SurveyStatus.Calculating) or equal(SurveyStatus.Done))
      contentAsString(result) mustBe Json
        .obj(
          "message" -> "success - started calculating"
        )
        .toString

    }
    "return status 410 if the survey of the class is in wrong status (Open,Calculating,Done)" in {
      // status 0
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )
      val result1: Future[Result] =
        controller.startCalculating(classId.get, classSecret.get).apply(request)
      status(result1) mustBe Gone.header.status

      awaitInf(classModel.updateStatus(classId.get, SurveyStatus.Calculating))
      val result2: Future[Result] =
        controller.startCalculating(classId.get, classSecret.get).apply(request)
      status(result2) mustBe Gone.header.status

      awaitInf(classModel.updateStatus(classId.get, SurveyStatus.Done))
      val result3: Future[Result] =
        controller.startCalculating(classId.get, classSecret.get).apply(request)
      status(result3) mustBe Gone.header.status

    }
    "return status 404 if classSecret or id is wrong" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )

      // wrong id
      val result: Future[Result] =
        controller.startCalculating(99, classSecret.get).apply(request)
      status(result) mustBe NotFound.header.status
      // wrong secret
      val result2: Future[Result] =
        controller.startCalculating(classId.get, "wrong secret").apply(request)
      status(result2) mustBe NotFound.header.status
    }
    "return status 401 if teacherSecret is wrong" in {
      val result: Future[Result] =
        controller
          .startCalculating(classId.get, schoolClass.classSecret)
          .apply(
            FakeRequest().withHeaders(
              Headers("teacherSecret" -> "wrong secret")
            )
          )
      status(result) mustBe Unauthorized.header.status
    }
    "return status 400 if no teacherSecret is provided" in {
      val result: Future[Result] =
        controller
          .startCalculating(classId.get, schoolClass.classSecret)
          .apply(FakeRequest())
      status(result) mustBe BadRequest.header.status
    }
  }

  "SurveyController /getResults" should {
    "return status 200 and array of students if request is correct" in {
      awaitInf(classModel.updateStatus(classId.get, SurveyStatus.Done))
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )
      val result: Future[Result] =
        controller.getResults(classId.get, classSecret.get).apply(request)
      status(result) mustBe Ok.header.status
      println(contentAsString(result))
    }
    "return status 409 if the survey of the class is in wrong status (Open,Closed,Calculating)" in {
      awaitInf(classModel.getStatus(classId.get)) mustBe SurveyStatus.Open
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )
      val result1: Future[Result] =
        controller.getResults(classId.get, classSecret.get).apply(request)
      status(result1) mustBe Conflict.header.status

      awaitInf(classModel.updateStatus(classId.get, SurveyStatus.Closed))
      val result2: Future[Result] =
        controller.getResults(classId.get, classSecret.get).apply(request)
      status(result2) mustBe Conflict.header.status

      awaitInf(classModel.updateStatus(classId.get, SurveyStatus.Calculating))
      val result3: Future[Result] =
        controller.getResults(classId.get, classSecret.get).apply(request)
      status(result3) mustBe Conflict.header.status
    }
    "return status 404 if classSecret or id is wrong" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )

      // wrong id
      val result: Future[Result] =
        controller.getResults(99, classSecret.get).apply(request)
      status(result) mustBe NotFound.header.status
      // wrong secret
      val result2: Future[Result] =
        controller.getResults(classId.get, "wrong secret").apply(request)
      status(result2) mustBe NotFound.header.status
    }
    "return status 401 if teacherSecret is wrong" in {
      val result: Future[Result] =
        controller
          .getResults(classId.get, schoolClass.classSecret)
          .apply(
            FakeRequest().withHeaders(
              Headers("teacherSecret" -> "wrong secret")
            )
          )
      status(result) mustBe Unauthorized.header.status
    }
    "return status 400 if no teacherSecret is provided" in {
      val result: Future[Result] =
        controller
          .getResults(classId.get, schoolClass.classSecret)
          .apply(FakeRequest())
      status(result) mustBe BadRequest.header.status
    }
  }

}
