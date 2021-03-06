package models

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import utils.MockDatabase
import models.{StudentModel, SchoolClassModel, SchoolClassDB}
import scala.concurrent.ExecutionContext

class SchoolClassModelSpec
    extends PlaySpec
    with MockDatabase
    with BeforeAndAfterEach {

  val classModel: SchoolClassModel = new SchoolClassModel(db)

  val schoolClass1: SchoolClassDB = SchoolClassDB(
    None, // id
    "test", // className
    Some("AMG"), // schoolName
    "clsSecret", // classSecret
    "teachsecret", // teacherSecret
    "puKey", // public Key
    "encPrivateKey", // encryptedPrivateKey
    Some(0) // SurveyStatus
  )
  var createdClass1: SchoolClassCC = _

  override def beforeEach(): Unit = {
    this.clearDatabase();

    // one schoolclass is added in every test so we do it here to avoid duplicate code
    createdClass1 = awaitInf(classModel.createSchoolClass(schoolClass1))
  }

  "The SchoolClassModels" should {
    "create new school classes" in {

      createdClass1.id.get mustBe 1
      createdClass1.className mustBe "test"

      // adding a second class to check whether IDs increments
      val schoolClass2: SchoolClassDB = SchoolClassDB(
        None, // id
        "test2", // className
        Some("AMG"), // schoolName
        "clsSecret", // classSecret
        "teachsecret", // teacherSecret
        "puKey", // public Key
        "encPrivateKey", // encryptedPrivateKey
        Some(0) // SurveyStatus
      )
      val createdClass2: SchoolClassCC =
        awaitInf(classModel.createSchoolClass(schoolClass2))
      createdClass2.id.get mustBe 2
      createdClass2.className mustBe "test2"

      // adding a third class to check whether identical information to first class still creates a new class
      val schoolClass3: SchoolClassDB = SchoolClassDB(
        None, // id
        "test", // className
        Some("AMG"), // schoolName
        "clsSecret", // classSecret
        "teachsecret", // teacherSecret
        "puKey", // public Key
        "encPrivateKey", // encryptedPrivateKey
        Some(0) // SurveyStatus
      )
      val createdClass3: SchoolClassCC =
        awaitInf(classModel.createSchoolClass(schoolClass3))
      createdClass3.id.get mustBe 3
      createdClass3.className mustBe "test"
    }
    "return a schoolclassCC by id" in {
      val returnedClass: SchoolClassCC =
        awaitInf(classModel.getSchoolClass(createdClass1.id.get))
      returnedClass.id.get mustBe 1
      returnedClass.className mustBe "test"

      // request nonexistent class
      an[java.util.NoSuchElementException] mustBe thrownBy(
        awaitInf(classModel.getSchoolClass(2))
      )

    }
    "return all schoolclasses" in {
      val allClasses1: Seq[SchoolClassCC] =
        awaitInf(classModel.getAllClasses())
      allClasses1.length mustBe 1
      allClasses1(0).id.get mustBe 1
      allClasses1(0).className mustBe "test"

      // add another class
      val schoolClass2: SchoolClassDB = SchoolClassDB(
        None, // id
        "test2", // className
        Some("AMG"), // schoolName
        "clsSecret", // classSecret
        "teachsecret", // teacherSecret
        "puKey", // public Key
        "encPrivateKey", // encryptedPrivateKey
        Some(0) // SurveyStatus
      )
      val createdClass2: SchoolClassCC =
        awaitInf(classModel.createSchoolClass(schoolClass2))
      createdClass2.id.get mustBe 2

      val allClasses2: Seq[SchoolClassCC] =
        awaitInf(classModel.getAllClasses())
      allClasses2.length mustBe 2
      allClasses2(1).id.get mustBe 2
      allClasses2(1).className mustBe "test2"
    }
    "return the ids of all calculating classes" in {
      val calculatingClasses1: Seq[Int] =
        awaitInf(classModel.getCalculatingClassesIds())
      calculatingClasses1.length mustBe 0 // class is in status OPEN

      val numberOfChangedClasses: Int =
        awaitInf(
          classModel.updateStatus(
            createdClass1.id.get,
            SurveyStatus.Calculating
          )
        )
      numberOfChangedClasses mustBe 1

      val calculatingClasses2: Seq[Int] =
        awaitInf(classModel.getCalculatingClassesIds())
      calculatingClasses2.length mustBe 1
      calculatingClasses2(0) mustBe createdClass1.id.get

      // add another class
      val schoolClass2: SchoolClassDB = SchoolClassDB(
        None, // id
        "test2", // className
        Some("AMG"), // schoolName
        "clsSecret", // classSecret
        "teachsecret", // teacherSecret
        "puKey", // public Key
        "encPrivateKey", // encryptedPrivateKey
        Some(0) // SurveyStatus
      )
      val createdClass2: SchoolClassCC =
        awaitInf(classModel.createSchoolClass(schoolClass2))
      createdClass2.id.get mustBe 2

      val calculatingClasses3: Seq[Int] =
        awaitInf(classModel.getCalculatingClassesIds())
      calculatingClasses3.length mustBe 1

      val numberOfChangedClasses2: Int =
        awaitInf(
          classModel.updateStatus(
            createdClass2.id.get,
            SurveyStatus.Calculating
          )
        )
      numberOfChangedClasses mustBe 1

      val calculatingClasses4: Seq[Int] =
        awaitInf(classModel.getCalculatingClassesIds())
      calculatingClasses4.length mustBe 2
      calculatingClasses4(1) mustBe createdClass2.id.get

    }
    "validate that a class with id and secret exists" in {
      val validatedSuccess: Boolean = awaitInf(
        classModel.validateAccess(
          createdClass1.id.get,
          schoolClass1.classSecret
        )
      )
      validatedSuccess mustBe true

      // wrong secret but existing id
      val validatedFailure1: Boolean =
        awaitInf(
          classModel.validateAccess(createdClass1.id.get, "wrongSecret")
        )
      validatedFailure1 mustBe false
      // wrong id but existing secret
      val validatedFailure2: Boolean =
        awaitInf(classModel.validateAccess(2, schoolClass1.classSecret))
      validatedFailure2 mustBe false

    }
    "return the teacher of a class if the teacher secret is correct" in {
      val authenticatedTeacher: Option[ClassTeacherCC] = awaitInf(
        classModel.getTeacher(
          createdClass1.id.get,
          schoolClass1.teacherSecret
        )
      )
      authenticatedTeacher.get.id.get mustBe 1

      // wrong secret
      val notAuthenticatedTeacher1: Option[ClassTeacherCC] = awaitInf(
        classModel.getTeacher(createdClass1.id.get, "wrong Secret")
      )
      notAuthenticatedTeacher1.isEmpty mustBe true

      // wrong id
      val notAuthenticatedTeacher2: Option[ClassTeacherCC] = awaitInf(
        classModel.getTeacher(2, schoolClass1.teacherSecret)
      )
      notAuthenticatedTeacher2.isEmpty mustBe true
    }
    "return the status of a class by id" in {
      // initial state is 0
      val surveyStatus: Int =
        awaitInf(classModel.getStatus(createdClass1.id.get))
      surveyStatus mustBe SurveyStatus.Open

      // nonexisting id
      an[java.util.NoSuchElementException] mustBe thrownBy(
        awaitInf(classModel.getStatus(2))
      )

    }
    "update the surveyStatus of a class if id is correct" in {
      val surveyStatus: Int =
        awaitInf(classModel.getStatus(createdClass1.id.get))
      surveyStatus mustBe SurveyStatus.Open

      val NumberOfChangedClasses: Int =
        awaitInf(
          classModel.updateStatus(createdClass1.id.get, SurveyStatus.Closed)
        )
      val surveyStatus1: Int =
        awaitInf(classModel.getStatus(createdClass1.id.get))
      surveyStatus1 mustBe SurveyStatus.Closed
    }
    "return the number of classes that are in status x" in {
      val numOfOpenClasses =
        awaitInf(classModel.getNumOfClasses(SurveyStatus.Open))
      numOfOpenClasses mustBe 1
      val numOfDoneClasses =
        awaitInf(classModel.getNumOfClasses(SurveyStatus.Done))
      numOfDoneClasses mustBe 0
      awaitInf(
        classModel.updateStatus(createdClass1.id.get, SurveyStatus.Done)
      )
      val numOfOpenClasses2 =
        awaitInf(classModel.getNumOfClasses(SurveyStatus.Open))
      numOfOpenClasses2 mustBe 0
      val numOfDoneClasses2 =
        awaitInf(classModel.getNumOfClasses(SurveyStatus.Done))
      numOfDoneClasses2 mustBe 1
    }
  }
}
