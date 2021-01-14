package models

import org.scalatestplus.play.PlaySpec
import utils.MockDatabase
import models.{StudentModel, SchoolClassModel, SchoolClassDB}
import scala.concurrent.ExecutionContext

class StudentModelSpec extends PlaySpec with MockDatabase {

  implicit val ec: scala.concurrent.ExecutionContext =
    scala.concurrent.ExecutionContext.global
  // so we have access to db because DatabaseCleanerOnEachTest itself implements the trait play.api.db.slick.HasDatabaseConfigProvider
  val studentModel: StudentModel = new StudentModel(db)
  val classModel: SchoolClassModel = new SchoolClassModel(db)

  "The Student Model" should {
    "create Students" in {
      this.clearDatabase();

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

      val created: SchoolClassCC =
        awaitInf(classModel.createSchoolClass(schoolClass))
      val clsId = created.id.get

      // awaitInf is a helper defined in MockDatabase
      val stud: StudentCC = StudentCC(None, "hashedName", "encName", true, None)

      val createdS: Option[Int] =
        awaitInf(studentModel.createStudent(stud, clsId))
      createdS.get mustBe 1 // first student has id 1

      val stud2: StudentCC =
        StudentCC(None, "hashedName2", "encName2", true, None)
      val createdStwo: Option[Int] =
        awaitInf(studentModel.createStudent(stud2, clsId))
      createdStwo.get mustBe 2 // second student has id 2

      val num: Int = awaitInf(studentModel.getNumberOfStudents(clsId))

      num mustBe 2
    }
    "return None if student is created with name that already exists" in {
      this.clearDatabase();

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

      val created: SchoolClassCC =
        awaitInf(classModel.createSchoolClass(schoolClass))
      val clsId = created.id.get

      val stud1: StudentCC = StudentCC(None, "uniqueName", "encName", true, None)

      val createdS: Option[Int] =
        awaitInf(studentModel.createStudent(stud1, clsId))
      createdS.get mustBe 1 // first student has id 1

      val stud2: StudentCC =
        StudentCC(None, "uniqueName", "encName2", true, None)
      val createdStwo: Option[Int] =
        awaitInf(studentModel.createStudent(stud2, clsId))
      createdStwo.isEmpty mustBe true
    }
    "update the old student selfReported status if new student is self-reported and old one is not" in {
      this.clearDatabase();

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

      val created: SchoolClassCC =
        awaitInf(classModel.createSchoolClass(schoolClass))
      val clsId = created.id.get

      val stud1: StudentCC = StudentCC(None, "uniqueName", "encName", false, None)

      val createdS: Option[Int] =
        awaitInf(studentModel.createStudent(stud1, clsId))
      createdS.get mustBe 1 // first student has id 1
       val allStudents: Seq[StudentCC] = awaitInf(studentModel.getStudents(clsId))
       val firstStudent: StudentCC = allStudents(0)
       firstStudent.selfReported mustBe false

      // student with same name but this time the entry is self-reported
      val stud2: StudentCC =
        StudentCC(None, "uniqueName", "encName2", true, None)
      val createdStwo: Option[Int] =
        awaitInf(studentModel.createStudent(stud2, clsId))
       createdStwo.get mustBe 1 // same id

       val allStudents2: Seq[StudentCC] = awaitInf(studentModel.getStudents(clsId))
       val firstStudent2: StudentCC = allStudents2(0)
       firstStudent2.selfReported mustBe true // now the value must be updated
    }
    "should return the studentID of old student if he is already self reported" in {
      this.clearDatabase();

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

      val created: SchoolClassCC =
        awaitInf(classModel.createSchoolClass(schoolClass))
      val clsId = created.id.get

      val stud1: StudentCC = StudentCC(None, "uniqueName", "encName", true, None)

      val createdS: Option[Int] =
        awaitInf(studentModel.createStudent(stud1, clsId))
      createdS.get mustBe 1 // first student has id 1

      // student with same name but this time the entry is self-reported
      val stud2: StudentCC =
        StudentCC(None, "uniqueName", "encName2", false, None)
      val createdStwo: Option[Int] =
        awaitInf(studentModel.createStudent(stud2, clsId))
       createdStwo.get mustBe 1 // same id
    }
    "count only self-reported students" in {
      this.clearDatabase();

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
      val created: SchoolClassCC =
        awaitInf(classModel.createSchoolClass(schoolClass))
      val clsId = created.id.get

      val num0: Int = awaitInf(studentModel.getNumberOfStudents(clsId))

      num0 mustBe 0

      // awaitInf is a helper defined in MockDatabase
      val stud: StudentCC = StudentCC(None, "hashedName", "encName", true, None)
      val stud2: StudentCC =
        StudentCC(None, "hashedName2", "encName2", false, None)

      val createdS: Option[Int] =
        awaitInf(studentModel.createStudent(stud, clsId))
      createdS.get mustBe 1 // first student has id 1

      val createdStwo: Option[Int] =
        awaitInf(studentModel.createStudent(stud2, clsId))
      createdStwo.get mustBe 2 // second student has id 2

      val num: Int = awaitInf(studentModel.getNumberOfStudents(clsId))

      num mustBe 1
    }
    "return all students" in {
      this.clearDatabase();

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

      val created: SchoolClassCC =
        awaitInf(classModel.createSchoolClass(schoolClass))
      val clsId = created.id.get

      val allStudents: Seq[StudentCC] = awaitInf(studentModel.getStudents(clsId))
      allStudents.length mustBe 0

      // awaitInf is a helper defined in MockDatabase
      val stud: StudentCC = StudentCC(None, "hashedName", "encName", true, None)

      val createdS: Option[Int] =
        awaitInf(studentModel.createStudent(stud, clsId))
      createdS.get mustBe 1 // first student has id 1

      val stud2: StudentCC =
        StudentCC(None, "hashedName2", "encName2", true, None)
      val createdStwo: Option[Int] =
        awaitInf(studentModel.createStudent(stud2, clsId))
      createdStwo.get mustBe 2 // second student has id 2

      val num: Int = awaitInf(studentModel.getNumberOfStudents(clsId))
      num mustBe 2
      
      val allStudents2: Seq[StudentCC] = awaitInf(studentModel.getStudents(clsId))
      allStudents2.length mustBe 2
    }
    "return ids of only self-reported students" in {
      this.clearDatabase();

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
      val created: SchoolClassCC =
        awaitInf(classModel.createSchoolClass(schoolClass))
      val clsId = created.id.get

      val allStudentIds1: Seq[Int] = awaitInf(studentModel.getAllSelfReportedStudentIDs(clsId))

      allStudentIds1.length mustBe 0

      // awaitInf is a helper defined in MockDatabase
      val stud: StudentCC = StudentCC(None, "hashedName", "encName", false, None)
      val stud2: StudentCC = StudentCC(None, "hashedName2", "encName2", true, None)

      val createdS: Option[Int] =
        awaitInf(studentModel.createStudent(stud, clsId))
      createdS.get mustBe 1 // first student has id 1

      val createdStwo: Option[Int] =
        awaitInf(studentModel.createStudent(stud2, clsId))
      createdStwo.get mustBe 2 // second student has id 2

      val allStudentIds2: Seq[Int] = awaitInf(studentModel.getAllSelfReportedStudentIDs(clsId))
      allStudentIds2.length mustBe 1
      allStudentIds2(0) mustBe 2 // there is only one element in the list and it must be of the second student
    }
    "update group belonging of students" in {
      this.clearDatabase();

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

      val created: SchoolClassCC =
        awaitInf(classModel.createSchoolClass(schoolClass))
      val clsId = created.id.get

      val stud1: StudentCC = StudentCC(None, "uniqueName", "encName", false, None)
      val createdS: Option[Int] =
        awaitInf(studentModel.createStudent(stud1, clsId))
      createdS.get mustBe 1 // first student has id 1

       val allStudents: Seq[StudentCC] = awaitInf(studentModel.getStudents(clsId))
       val firstStudent: StudentCC = allStudents(0)
       firstStudent.groupBelonging mustBe None // none is default value of groupBelonging

      val updatedStudents: Int = awaitInf(studentModel.updateGroupBelonging(1, 2))
      updatedStudents mustBe 1 // indicates success of operation

       val allStudents2: Seq[StudentCC] = awaitInf(studentModel.getStudents(clsId))
       val firstStudent2: StudentCC = allStudents2(0)
       firstStudent2.groupBelonging.get mustBe 2 // now the value must be updated

       val updatedStudents2: Int = awaitInf(studentModel.updateGroupBelonging(2, 2))  // updating a non-existent student
      updatedStudents2 mustBe 0
    }
  }
}
