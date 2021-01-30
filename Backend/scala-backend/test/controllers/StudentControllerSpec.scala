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
class StudentControllerSpec extends PlaySpec with MockDatabase with Injecting {
  // dbConfigProvider comes from MockDatabase, stubControllerComponents from Helpers
  val authenticationController =
    new AuthenticationController(dbConfigProvider, stubControllerComponents())
  val controller = new StudentController(
    dbConfigProvider,
    stubControllerComponents(),
    authenticationController
  )

  implicit val StudentReads = Json.reads[StudentCC]

  // clear Database and insert one schoolClass and three students
  this.clearDatabase();
  val classModel: SchoolClassModel = new SchoolClassModel(db)
  val studentModel: StudentModel = new StudentModel(db)
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

  val student1: StudentCC =
    StudentCC(None, "hashedName", "encName", true, None)
  val student2: StudentCC =
    StudentCC(None, "hashedName2", "encName2", false, None)
  val student3: StudentCC =
    StudentCC(None, "hashedName3", "encName3", true, None)

  val student1Id: Option[Int] =
    awaitInf(studentModel.createStudent(student1, classId))

  val student2Id: Option[Int] =
    awaitInf(studentModel.createStudent(student2, classId))

  val student3Id: Option[Int] =
    awaitInf(studentModel.createStudent(student3, classId))

  "StudentController's getSignups" should {
    "return number of self-reported students" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
          .withHeaders(Headers("teacherSecret" -> schoolClass.teacherSecret))
      val result: Future[Result] =
        controller.getSignups(classId, schoolClass.classSecret).apply(request)
      status(result) mustBe Ok.header.status
      contentAsString(result) mustBe Json
        .obj(
          "value" -> 2
        )
        .toString
    }
    "return status 404 if classSecret or id is wrong" in {
      val result1: Future[Result] =
        controller
          .getSignups(99, schoolClass.classSecret) // wrong id
          .apply(
            FakeRequest().withHeaders(
              Headers("teacherSecret" -> schoolClass.teacherSecret)
            )
          )
      status(result1) mustBe NotFound.header.status

      val result2: Future[Result] =
        controller
          .getSignups(classId, "wrong Secret")
          .apply(
            FakeRequest().withHeaders(
              Headers("teacherSecret" -> schoolClass.teacherSecret)
            )
          )
      status(result2) mustBe NotFound.header.status
    }
    "return status 401 if teacherSecret is wrong" in {
      val result: Future[Result] =
        controller
          .getSignups(classId, schoolClass.classSecret)
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
          .getSignups(classId, schoolClass.classSecret)
          .apply(FakeRequest())
      status(result) mustBe BadRequest.header.status
    }
  }
  "StudentController's getSelfReportedStudents" should {
    "return only the selfReportedStudents" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
          .withHeaders(Headers("teacherSecret" -> schoolClass.teacherSecret))
      val result: Future[Result] =
        controller.getSelfReportedStudents(classId, schoolClass.classSecret).apply(request)
      status(result) mustBe Ok.header.status
      val resultBody: String =
        contentAsString(result) // automatically waits for future
      val jsBody: JsValue = Json.parse(resultBody)
      val students: Seq[StudentCC] = Json.fromJson[Seq[StudentCC]](jsBody).get
      students.length mustBe 2
    }
  }
  "StudentController's getFriendReportedStudents" should {
    "return only the friendreported Students" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
          .withHeaders(Headers("teacherSecret" -> schoolClass.teacherSecret))
      val result: Future[Result] =
        controller.getFriendReportedStudents(classId, schoolClass.classSecret).apply(request)
      status(result) mustBe Ok.header.status
      val resultBody: String =
        contentAsString(result) // automatically waits for future
      val jsBody: JsValue = Json.parse(resultBody)
      val students: Seq[StudentCC] = Json.fromJson[Seq[StudentCC]](jsBody).get
      students.length mustBe 1
    }
  }
}
