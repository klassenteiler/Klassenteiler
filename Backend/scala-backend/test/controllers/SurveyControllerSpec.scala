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

  implicit val studentWrites = Json.writes[StudentCC]
  implicit val MergeCommandsCCWrites = Json.writes[MergeCommandsCC]

  var classId: Int = _
  var classSecret: String = _
  val student1: StudentCC =
    StudentCC(None, "baseStudent", "encName", true, None)
  var student1Id: Int = _

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
    classId = createdSchoolClass.id.get
    classSecret = createdSchoolClass.classSecret

    student1Id = awaitInf(studentModel.createStudent(student1, classId)).get
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

  val jsonWithEgoNotSelfReported: JsValue = Json.obj(
    "me" ->
      Json.obj(
        "encryptedName" -> "asdf",
        "hashedName" -> "ego",
        "selfReported" -> false
      ),
    "friends" -> Json.arr(
      Json.obj(
        "encryptedName" -> "asdfadsf",
        "hashedName" -> "alter1",
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
        controller.submitSurvey(classId, classSecret).apply(request)
      status(result) mustBe Created.header.status
      contentAsString(result) mustBe Json
        .obj(
          "message" -> "success - created"
        )
        .toString
      val allStudents: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId))
      allStudents.length mustBe 4 // one student is already in database
      // see beforeEach

      val allRelations: Seq[(Int, Int)] =
        awaitInf(relModel.getAllRelationIdsOfClass(classId))
      allRelations.length mustBe 2

    }
    "return status 403 if student with that name already exists" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(json)
      val result1: Future[Result] =
        controller.submitSurvey(classId, classSecret).apply(request)
      status(result1) mustBe Created.header.status

      // sending the same request again
      val result2: Future[Result] =
        controller.submitSurvey(classId, classSecret).apply(request)
      status(result2) mustBe Forbidden.header.status

      val allStudents: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId))
      allStudents.length mustBe 4 // one student is already in database
      // see beforeEach

      val allRelations: Seq[(Int, Int)] =
        awaitInf(relModel.getAllRelationIdsOfClass(classId))
      allRelations.length mustBe 2
    }
    "return status 400 if too many friends are tried to be added" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(overLimitJson)
      val result1: Future[Result] =
        controller.submitSurvey(classId, classSecret).apply(request)
      status(result1) mustBe BadRequest.header.status
    }
    "return status 415 if json body is in wrong format" in {
      val body: JsObject = Json.obj("value" -> "wrong")
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(body)
      val result1: Future[Result] =
        controller.submitSurvey(classId, classSecret).apply(request)
      status(result1) mustBe UnsupportedMediaType.header.status
    }
    "return status 400 if no body is provided" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
      val result: Future[Result] =
        controller.submitSurvey(classId, classSecret).apply(request)
      status(result) mustBe BadRequest.header.status
    }
    "return status 410 if the survey of the class is in wrong status" in {
      classModel.updateStatus(classId, SurveyStatus.Closed)
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(json)
      val result1: Future[Result] =
        controller.submitSurvey(classId, classSecret).apply(request)
      status(result1) mustBe Gone.header.status

      classModel.updateStatus(classId, SurveyStatus.Calculating)
      val result2: Future[Result] =
        controller.submitSurvey(classId, classSecret).apply(request)
      status(result2) mustBe Gone.header.status

      classModel.updateStatus(classId, SurveyStatus.Done)
      val result3: Future[Result] =
        controller.submitSurvey(classId, classSecret).apply(request)
      status(result3) mustBe Gone.header.status
    }
    "return status 404 if classSecret or id is wrong" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(json)
      // wrong id
      val result: Future[Result] =
        controller.submitSurvey(99, classSecret).apply(request)
      status(result) mustBe NotFound.header.status
      // wrong secret
      val result2: Future[Result] =
        controller.submitSurvey(classId, "wrong secret").apply(request)
      status(result2) mustBe NotFound.header.status
    }
    "set self-reported status of source student to true before creating student" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(jsonWithEgoNotSelfReported)
      val result: Future[Result] =
        controller.submitSurvey(classId, classSecret).apply(request)
      status(result) mustBe Created.header.status
      contentAsString(result) mustBe Json
        .obj(
          "message" -> "success - created"
        )
        .toString
      val allStudents: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId))
      allStudents(0).selfReported mustBe true
    }
  }

  "SurveyController /closeSurvey" should {
    "return status 200, contain message and update status if request is correct" in {

      awaitInf(classModel.getStatus(classId)) mustBe SurveyStatus.Open
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
          .withHeaders(
            Headers("teacherSecret" -> schoolClass.teacherSecret)
          )
      val result: Future[Result] =
        controller.closeSurvey(classId, classSecret).apply(request)
      status(result) mustBe Ok.header.status
      contentAsString(result) mustBe Json
        .obj(
          "message" -> "success - survey closed"
        )
        .toString
      awaitInf(classModel.getStatus(classId)) mustBe SurveyStatus.Closed
    }

    "return status 410 if the survey of the class is in wrong status" in {

      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )

      awaitInf(classModel.updateStatus(classId, SurveyStatus.Closed))
      val result1: Future[Result] =
        controller.closeSurvey(classId, classSecret).apply(request)
      status(result1) mustBe Gone.header.status

      awaitInf(classModel.updateStatus(classId, SurveyStatus.Calculating))
      val result2: Future[Result] =
        controller.closeSurvey(classId, classSecret).apply(request)
      status(result2) mustBe Gone.header.status

      awaitInf(classModel.updateStatus(classId, SurveyStatus.Done))
      val result3: Future[Result] =
        controller.closeSurvey(classId, classSecret).apply(request)
      status(result3) mustBe Gone.header.status
    }
    "return status 404 if classSecret or id is wrong" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )

      // wrong id
      val result: Future[Result] =
        controller.closeSurvey(99, classSecret).apply(request)
      status(result) mustBe NotFound.header.status
      // wrong secret
      val result2: Future[Result] =
        controller.closeSurvey(classId, "wrong secret").apply(request)
      status(result2) mustBe NotFound.header.status
    }
    "return status 401 if teacherSecret is wrong" in {
      val result: Future[Result] =
        controller
          .closeSurvey(classId, schoolClass.classSecret)
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
          .closeSurvey(classId, schoolClass.classSecret)
          .apply(FakeRequest())
      status(result) mustBe BadRequest.header.status
    }
  }

  "SurveyController /startCalculating" should {
    "return status 200, contain message and update statuses if request is correct" in {
      // empty body:

      val mergeObject: MergeCommandsCC = MergeCommandsCC(
        Seq(),
        Seq(),
        Seq(),
        Seq()
      )

      awaitInf(classModel.updateStatus(classId, SurveyStatus.Closed))
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
          .withHeaders(
            Headers("teacherSecret" -> schoolClass.teacherSecret)
          )
          .withJsonBody(Json.toJson(mergeObject))
      val result: Future[Result] =
        controller.startCalculating(classId, classSecret).apply(request)
      status(result) mustBe Ok.header.status
      val surveyStatus: Int = awaitInf(classModel.getStatus(classId))
      surveyStatus must (equal(SurveyStatus.Calculating) or equal(
        SurveyStatus.Done
      ))
      contentAsString(result) mustBe Json
        .obj(
          "message" -> "success - started calculating"
        )
        .toString

    }
    "merge wrongly typed friendreported student with correctly typed selfreported Student [CASE 1]" in {
      awaitInf(classModel.updateStatus(classId, SurveyStatus.Closed))
      // one selfreported student is already in database
      // create a misstyped student and a correct student
      val friendReportedStudent: StudentCC =
        StudentCC(None, "wrongName", "encName", false, None)
      val selfReportedStudent: StudentCC =
        StudentCC(None, "correctName", "encName2", true, None)
      val friendReportedStudentId: Int =
        awaitInf(studentModel.createStudent(friendReportedStudent, classId)).get
      friendReportedStudentId mustBe 2 // second student has id 2
      val selfReportedStudentId: Int =
        awaitInf(studentModel.createStudent(selfReportedStudent, classId)).get
      selfReportedStudentId mustBe 3 // third student has id 3

      // create relation from first student to friendreported student, which will be rewired
      val relation: RelationshipCC =
        RelationshipCC(classId, student1Id, friendReportedStudentId)
      val success: Boolean =
        awaitInf(relModel.createRelationship(relation))
      success mustBe true

      val mergeObject: MergeCommandsCC = MergeCommandsCC(
        Seq( // isAliasOf
          Tuple2[Int, String](
            friendReportedStudentId,
            selfReportedStudent.hashedName // "correctName"
          )
        ),
        Seq(), // studentsToAdd
        Seq(), // studentsToDelete
        Seq() // studentsToRename
      )
      val numOfStudentsBefore: Int =
        awaitInf(studentModel.getStudents(classId)).length
      numOfStudentsBefore mustBe 3

      // sending the request
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
          .withHeaders(
            Headers("teacherSecret" -> schoolClass.teacherSecret)
          )
          .withJsonBody(Json.toJson(mergeObject))
      val result: Result =
        awaitInf(
          controller.startCalculating(classId, classSecret).apply(request)
        )
      status(Future.successful(result)) mustBe Ok.header.status

      val numOfStudentsAfter: Int =
        awaitInf(studentModel.getStudents(classId)).length
      numOfStudentsAfter mustBe 2 // we deleted the friendreported student

      val allRelations: Seq[(Int, Int)] =
        awaitInf(relModel.getAllRelationIdsOfClass(classId))
      allRelations mustBe Seq(
        (student1Id, selfReportedStudentId)
      )

    }
    "merge a correctly and wrongly typed friendreported student with wrongly typed selfreported Student [CASE 2/B]" in {
      awaitInf(classModel.updateStatus(classId, SurveyStatus.Closed))
      // one selfreported student is already in database
      // create a misstyped student and a correct student
      val friendRepCorrect: StudentCC =
        StudentCC(None, "correctName", "encName", false, None)
      val friendRepWrong: StudentCC =
        StudentCC(None, "wrongName", "encName2", false, None)
      val selfReportedStudent: StudentCC =
        StudentCC(None, "anotherWrongName", "encName3", true, None)
      val friendRepCorrectId: Int =
        awaitInf(
          studentModel.createStudent(friendRepCorrect, classId)
        ).get
      friendRepCorrectId mustBe 2
      val friendRepWrongId: Int =
        awaitInf(
          studentModel.createStudent(friendRepWrong, classId)
        ).get
      friendRepWrongId mustBe 3
      val selfReportedStudentId: Int =
        awaitInf(studentModel.createStudent(selfReportedStudent, classId)).get
      selfReportedStudentId mustBe 4

      // for brevity reasons I am going to use student 1 to create two test relations. this is of course unrealistic,
      //  because it would mean the same student send a relation to the same person twice, once with incorrect spelling

      // create relation from first student to friendreported student, which will be rewired
      val relation1: RelationshipCC =
        RelationshipCC(classId, student1Id, friendRepCorrectId)
      val success1: Boolean =
        awaitInf(relModel.createRelationship(relation1))
      success1 mustBe true
      // create relation from first student to second friendreported student, which will be rewired
      val relation2: RelationshipCC =
        RelationshipCC(classId, student1Id, friendRepWrongId)
      val success2: Boolean =
        awaitInf(relModel.createRelationship(relation2))
      success2 mustBe true

      val mergeObject: MergeCommandsCC = MergeCommandsCC(
        Seq( // isAliasOf
          Tuple2[Int, String](
            friendRepWrongId,
            friendRepCorrect.hashedName // "correctName"
          )
        ),
        Seq(),
        Seq(),
        Seq( // studentsToRename
          StudentCC(
            Some(selfReportedStudentId),
            friendRepCorrect.hashedName, //"correctName"
            "encName3",
            true,
            None
          )
        )
      )

      // sanity checks to see whether database actually changes
      val studentIdBefore: Int = awaitInf(
        studentModel.getByHash(friendRepCorrect.hashedName, classId)
      ).get
      // if we look for the correct hash in the database, it should lead to the friendreported Student
      studentIdBefore mustBe friendRepCorrectId

      val allRelationsBefore: Seq[(Int, Int)] =
        awaitInf(relModel.getAllRelationIdsOfClass(classId))
      allRelationsBefore mustBe Seq(
        (
          student1Id,
          friendRepCorrectId
        ),
        (
          student1Id,
          friendRepWrongId
        )
      )

      val numOfStudentsBefore: Int =
        awaitInf(studentModel.getStudents(classId)).length
      numOfStudentsBefore mustBe 4

      // sending the request
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
          .withHeaders(
            Headers("teacherSecret" -> schoolClass.teacherSecret)
          )
          .withJsonBody(Json.toJson(mergeObject))
      val result: Result =
        awaitInf(
          controller.startCalculating(classId, classSecret).apply(request)
        )
      status(Future.successful(result)) mustBe Ok.header.status

      val updatedStudentId: Int = awaitInf(
        studentModel.getByHash(friendRepCorrect.hashedName, classId)
      ).get
      // if we look for the correct hash in the database, it should now lead to the selfreported student's id
      updatedStudentId mustBe selfReportedStudentId

      val allRelationsAfter: Seq[(Int, Int)] =
        awaitInf(relModel.getAllRelationIdsOfClass(classId))
      allRelationsAfter mustBe Seq(
        (
          student1Id,
          selfReportedStudentId
        ),
        (
          student1Id,
          selfReportedStudentId
        ) // its twice the same because we used the same student to create both relations
      )

      val numOfStudentsAfter: Int =
        awaitInf(studentModel.getStudents(classId)).length
      numOfStudentsAfter mustBe 2 // because it was 4 and we deleted two

    }
    "add selfreported student if a student didnt enter their own info [CASE 3]" in {
      awaitInf(classModel.updateStatus(classId, SurveyStatus.Closed))
      val friendRepCorrectName: StudentCC =
        StudentCC(None, "correctName", "encName", false, None)

      val friendRepCorrectNameId: Int =
        awaitInf(
          studentModel.createStudent(friendRepCorrectName, classId)
        ).get
      friendRepCorrectNameId mustBe 2

      val mergeObject: MergeCommandsCC = MergeCommandsCC(
        Seq(),
        Seq( //studentsToAdd
          StudentCC(
            Some(friendRepCorrectNameId),
            friendRepCorrectName.hashedName, //"correctName"
            "encName",
            true,
            None
          )
        ),
        Seq(),
        Seq()
      )

      val allStudentsBefore: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId))

      allStudentsBefore(1).id.get mustBe friendRepCorrectNameId
      allStudentsBefore(1).selfReported mustBe false

      // sending the request
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
          .withHeaders(
            Headers("teacherSecret" -> schoolClass.teacherSecret)
          )
          .withJsonBody(Json.toJson(mergeObject))

      val result: Result =
        awaitInf(
          controller.startCalculating(classId, classSecret).apply(request)
        )
      status(Future.successful(result)) mustBe Ok.header.status

      val allStudentsAfter: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId))

      // we added a new selfreported student with the same hash, which automatically sets selfReported to true
      allStudentsAfter(1).id.get mustBe friendRepCorrectNameId
      allStudentsAfter(1).selfReported mustBe true
    }
    "add selfreported student if nobody entered their info [CASE 4]" in {
      awaitInf(classModel.updateStatus(classId, SurveyStatus.Closed))
      val mergeObject: MergeCommandsCC = MergeCommandsCC(
        Seq(),
        Seq( //studentsToAdd
          StudentCC(
            None,
            "aName",
            "encName",
            true,
            None
          )
        ),
        Seq(),
        Seq()
      )

      val numStudentsBefore: Int =
        awaitInf(studentModel.getStudents(classId)).length

      numStudentsBefore mustBe 1 // only the baseStudent is in the db

      // sending the request
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
          .withHeaders(
            Headers("teacherSecret" -> schoolClass.teacherSecret)
          )
          .withJsonBody(Json.toJson(mergeObject))

      val result: Result =
        awaitInf(
          controller.startCalculating(classId, classSecret).apply(request)
        )
      status(Future.successful(result)) mustBe Ok.header.status

      val numStudentsAfter: Int =
        awaitInf(studentModel.getStudents(classId)).length

      numStudentsAfter mustBe 2
    }
    "add selfreported student and merge a correctly and a wrongly typed friendreported student into it [CASE A]" in {
      awaitInf(classModel.updateStatus(classId, SurveyStatus.Closed))
      val friendRepCorrect: StudentCC =
        StudentCC(None, "correctName", "encName", false, None)
      val friendRepWrong: StudentCC =
        StudentCC(None, "wrongName", "encName2", false, None)
      val friendRepCorrectId: Int =
        awaitInf(
          studentModel.createStudent(friendRepCorrect, classId)
        ).get
      friendRepCorrectId mustBe 2
      val friendRepWrongId: Int =
        awaitInf(
          studentModel.createStudent(friendRepWrong, classId)
        ).get
      friendRepWrongId mustBe 3

      // for brevity reasons I am going to use student 1 to create two test relations. this is of course unrealistic,
      //  because it would mean the same student send a relation to the same person twice, once with incorrect spelling

      // create relation from first student to friendreported student, which will be rewired
      val relation1: RelationshipCC =
        RelationshipCC(classId, student1Id, friendRepCorrectId)
      val success1: Boolean =
        awaitInf(relModel.createRelationship(relation1))
      success1 mustBe true
      // create relation from first student to second friendreported student, which will be rewired
      val relation2: RelationshipCC =
        RelationshipCC(classId, student1Id, friendRepWrongId)
      val success2: Boolean =
        awaitInf(relModel.createRelationship(relation2))
      success2 mustBe true

      val mergeObject: MergeCommandsCC = MergeCommandsCC(
        Seq( // isAliasOf
          Tuple2[Int, String](
            friendRepWrongId,
            friendRepCorrect.hashedName // "correctName"
          )
        ),
        Seq( //studentsToAdd
          StudentCC(
            Some(friendRepCorrectId),
            friendRepCorrect.hashedName, //"correctName"
            "encName",
            true,
            None
          )
        ),
        Seq(),
        Seq()
      )

      val numStudentsBefore: Int =
        awaitInf(studentModel.getStudents(classId)).length
      numStudentsBefore mustBe 3 // base student + two friendreported

      val allRelationsBefore: Seq[(Int, Int)] =
        awaitInf(relModel.getAllRelationIdsOfClass(classId))
      allRelationsBefore mustBe Seq(
        (
          student1Id,
          friendRepCorrectId
        ),
        (
          student1Id,
          friendRepWrongId
        )
      )

      // sending the request
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
          .withHeaders(
            Headers("teacherSecret" -> schoolClass.teacherSecret)
          )
          .withJsonBody(Json.toJson(mergeObject))

      val result: Result =
        awaitInf(
          controller.startCalculating(classId, classSecret).apply(request)
        )
      status(Future.successful(result)) mustBe Ok.header.status

      val numStudentsAfter: Int =
        awaitInf(studentModel.getStudents(classId)).length
      numStudentsAfter mustBe 2 // we delete one of the friendreported and set the other to selfReported = true

      val allStudents: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId))
      allStudents(1).selfReported mustBe true

      val allRelationsAfter: Seq[(Int, Int)] =
        awaitInf(relModel.getAllRelationIdsOfClass(classId))
      allRelationsAfter mustBe Seq(
        (
          student1Id,
          friendRepCorrectId
        ),
        (
          student1Id,
          friendRepCorrectId
        ) // its twice the same because we used the same student to create both relations
      )
    }
    "add selfreported student and merge a wrongly typed friendreported student into it [CASE C]" in {
      awaitInf(classModel.updateStatus(classId, SurveyStatus.Closed))
      val friendRepWrong: StudentCC =
        StudentCC(None, "wrongName", "encName2", false, None)
      val friendRepWrongId: Int =
        awaitInf(
          studentModel.createStudent(friendRepWrong, classId)
        ).get
      friendRepWrongId mustBe 2

      val relation: RelationshipCC =
        RelationshipCC(classId, student1Id, friendRepWrongId)
      val success: Boolean =
        awaitInf(relModel.createRelationship(relation))
      success mustBe true

      val mergeObject: MergeCommandsCC = MergeCommandsCC(
        Seq( // isAliasOf
          Tuple2[Int, String](
            friendRepWrongId,
            "correctName"
          )
        ),
        Seq( //studentsToAdd
          StudentCC(
            None,
            "correctName",
            "encName",
            true,
            None
          )
        ),
        Seq(),
        Seq()
      )

      val allStudentsBefore: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId))

      allStudentsBefore(1).id.get mustBe friendRepWrongId
      allStudentsBefore(1).hashedName mustBe friendRepWrong.hashedName
      allStudentsBefore(1).selfReported mustBe false

      // sending the request
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
          .withHeaders(
            Headers("teacherSecret" -> schoolClass.teacherSecret)
          )
          .withJsonBody(Json.toJson(mergeObject))

      val result: Result =
        awaitInf(
          controller.startCalculating(classId, classSecret).apply(request)
        )
      status(Future.successful(result)) mustBe Ok.header.status

      val allStudentsAfter: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId))

      allStudentsAfter(1).hashedName mustBe "correctName"
      allStudentsAfter(1).selfReported mustBe true
      allStudentsAfter.length mustBe 2 // we added one, but also deleted one
    }
    "delete students" in {
      awaitInf(classModel.updateStatus(classId, SurveyStatus.Closed))
      val mergeObject: MergeCommandsCC = MergeCommandsCC(
        Seq(),
        Seq(),
        Seq(student1Id),
        Seq()
      )

      val numStudentsBefore: Int =
        awaitInf(studentModel.getStudents(classId)).length

      numStudentsBefore mustBe 1 // only the baseStudent is in the db

      // sending the request
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
          .withHeaders(
            Headers("teacherSecret" -> schoolClass.teacherSecret)
          )
          .withJsonBody(Json.toJson(mergeObject))

      val result: Result =
        awaitInf(
          controller.startCalculating(classId, classSecret).apply(request)
        )
      status(Future.successful(result)) mustBe Ok.header.status

      val numStudentsAfter: Int =
        awaitInf(studentModel.getStudents(classId)).length

      numStudentsAfter mustBe 0
    }
    "return 400 if the user send invalid merging commands by not handling all self-reported students" in {
      awaitInf(classModel.updateStatus(classId, SurveyStatus.Closed))
      val friendReportedStudent: StudentCC =
        StudentCC(None, "test", "encName", false, None)
      val friendReportedStudentId: Int =
        awaitInf(
          studentModel.createStudent(friendReportedStudent, classId)
        ).get

      val mergeObject: MergeCommandsCC = MergeCommandsCC(
        Seq(),
        Seq(),
        Seq(),
        Seq()
      )

      // sending the request
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
          .withHeaders(
            Headers("teacherSecret" -> schoolClass.teacherSecret)
          )
          .withJsonBody(Json.toJson(mergeObject))

      val result: Result =
        awaitInf(
          controller.startCalculating(classId, classSecret).apply(request)
        )
      status(Future.successful(result)) mustBe BadRequest.header.status
    }
    "return 400 if the user send invalid merging commands by adding and deleting the same student" in {
      awaitInf(classModel.updateStatus(classId, SurveyStatus.Closed))
      val mergeObject: MergeCommandsCC = MergeCommandsCC(
        Seq(),
        Seq(
          StudentCC(
            None,
            student1.hashedName,
            student1.encryptedName,
            true,
            None
          )
        ), // studentsToAdd
        Seq(student1Id), // studentsToDelete
        Seq()
      )

      // sending the request
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest()
          .withHeaders(
            Headers("teacherSecret" -> schoolClass.teacherSecret)
          )
          .withJsonBody(Json.toJson(mergeObject))

      val result: Result =
        awaitInf(
          controller.startCalculating(classId, classSecret).apply(request)
        )
      status(Future.successful(result)) mustBe BadRequest.header.status
    }

    "return status 410 if the survey of the class is in wrong status (Open,Calculating,Done)" in {
      // status 0
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )
      val result1: Future[Result] =
        controller.startCalculating(classId, classSecret).apply(request)
      status(result1) mustBe Gone.header.status

      awaitInf(classModel.updateStatus(classId, SurveyStatus.Calculating))
      val result2: Future[Result] =
        controller.startCalculating(classId, classSecret).apply(request)
      status(result2) mustBe Gone.header.status

      awaitInf(classModel.updateStatus(classId, SurveyStatus.Done))
      val result3: Future[Result] =
        controller.startCalculating(classId, classSecret).apply(request)
      status(result3) mustBe Gone.header.status

    }
    "return status 404 if classSecret or id is wrong" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )

      // wrong id
      val result: Future[Result] =
        controller.startCalculating(99, classSecret).apply(request)
      status(result) mustBe NotFound.header.status
      // wrong secret
      val result2: Future[Result] =
        controller.startCalculating(classId, "wrong secret").apply(request)
      status(result2) mustBe NotFound.header.status
    }
    "return status 401 if teacherSecret is wrong" in {
      val result: Future[Result] =
        controller
          .startCalculating(classId, schoolClass.classSecret)
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
          .startCalculating(classId, schoolClass.classSecret)
          .apply(FakeRequest())
      status(result) mustBe BadRequest.header.status
    }
  }

  "SurveyController /getResults" should {
    "return status 200 and array of students if request is correct" in {
      awaitInf(classModel.updateStatus(classId, SurveyStatus.Done))
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )
      val result: Future[Result] =
        controller.getResults(classId, classSecret).apply(request)
      status(result) mustBe Ok.header.status
    }
    "return status 409 if the survey of the class is in wrong status (Open,Closed,Calculating)" in {
      awaitInf(classModel.getStatus(classId)) mustBe SurveyStatus.Open
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )
      val result1: Future[Result] =
        controller.getResults(classId, classSecret).apply(request)
      status(result1) mustBe Conflict.header.status

      awaitInf(classModel.updateStatus(classId, SurveyStatus.Closed))
      val result2: Future[Result] =
        controller.getResults(classId, classSecret).apply(request)
      status(result2) mustBe Conflict.header.status

      awaitInf(classModel.updateStatus(classId, SurveyStatus.Calculating))
      val result3: Future[Result] =
        controller.getResults(classId, classSecret).apply(request)
      status(result3) mustBe Conflict.header.status
    }
    "return status 404 if classSecret or id is wrong" in {
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withHeaders(
          Headers("teacherSecret" -> schoolClass.teacherSecret)
        )

      // wrong id
      val result: Future[Result] =
        controller.getResults(99, classSecret).apply(request)
      status(result) mustBe NotFound.header.status
      // wrong secret
      val result2: Future[Result] =
        controller.getResults(classId, "wrong secret").apply(request)
      status(result2) mustBe NotFound.header.status
    }
    "return status 401 if teacherSecret is wrong" in {
      val result: Future[Result] =
        controller
          .getResults(classId, schoolClass.classSecret)
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
          .getResults(classId, schoolClass.classSecret)
          .apply(FakeRequest())
      status(result) mustBe BadRequest.header.status
    }
  }

}
