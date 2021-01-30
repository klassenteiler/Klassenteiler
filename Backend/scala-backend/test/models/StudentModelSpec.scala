package models

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import utils.MockDatabase
import models.{StudentModel, SchoolClassModel, SchoolClassDB}
import scala.concurrent.ExecutionContext

class StudentModelSpec
    extends PlaySpec
    with MockDatabase
    with BeforeAndAfterEach {


  // we have access to db because DatabaseCleanerOnEachTest itself implements the trait play.api.db.slick.HasDatabaseConfigProvider
  val studentModel: StudentModel = new StudentModel(db)
  val classModel: SchoolClassModel = new SchoolClassModel(db)
  var classId: Int = _

  override def beforeEach(): Unit = {
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

    val createdSchoolClass: SchoolClassCC =
      awaitInf(classModel.createSchoolClass(schoolClass))
    classId = createdSchoolClass.id.get
    println("Before Test: SchoolclassId is "+ classId)
  }

  "The Student Model" should {
    "create Students" in {
      val student1: StudentCC = StudentCC(None, "hashedName", "encName", true, None)

      val student1Id: Option[Int] =
        // awaitInf is a helper defined in MockDatabase
        awaitInf(studentModel.createStudent(student1, classId)) 
      student1Id.get mustBe 1 // first student has id 1

      val student2: StudentCC =
        StudentCC(None, "hashedName2", "encName2", true, None)
      
      val student2Id: Option[Int] =
        awaitInf(studentModel.createStudent(student2, classId))
      student2Id.get mustBe 2 // second student has id 2

      val num: Int = awaitInf(studentModel.getNumberOfStudents(classId))

      num mustBe 2
    }
    "return None if student is created with name that already exists" in {
      val student1: StudentCC =
        StudentCC(None, "uniqueName", "encName", true, None)

      val student1Id: Option[Int] =
        awaitInf(studentModel.createStudent(student1, classId))
      student1Id.get mustBe 1 // first student has id 1

      val student2: StudentCC =
        StudentCC(None, "uniqueName", "encName2", true, None)
      val student2Id: Option[Int] =
        awaitInf(studentModel.createStudent(student2, classId))
      student2Id.isEmpty mustBe true
    }
    "update the old student selfReported status if new student is self-reported and old one is not" in {
      val student1: StudentCC =
        StudentCC(None, "uniqueName", "encName", false, None)

      val student1Id: Option[Int] =
        awaitInf(studentModel.createStudent(student1, classId))
      student1Id.get mustBe 1 // first student has id 1
      val allStudents: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId))
      allStudents.length mustBe 1 
      val firstStudent: StudentCC = allStudents(0) // since there is only one student we can access the first element
      firstStudent.selfReported mustBe false

      // student with same name but this time the entry is self-reported
      val student2: StudentCC =
        StudentCC(None, "uniqueName", "encName2", true, None)
      val student2Id: Option[Int] =
        awaitInf(studentModel.createStudent(student2, classId))
      student2Id.get mustBe 1 // same id

      val allStudents2: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId))
      allStudents.length mustBe 1 
      val firstStudent2: StudentCC = allStudents2(0)
      firstStudent2.selfReported mustBe true // now the value must be updated
    }
    "should return the studentID of old student if he is already self reported" in {
       val student1: StudentCC =
        StudentCC(None, "uniqueName", "encName", true, None)

      val student1Id: Option[Int] =
        awaitInf(studentModel.createStudent(student1, classId))
      student1Id.get mustBe 1 // first student has id 1

      val student2: StudentCC =
        StudentCC(None, "uniqueName", "encName2", false, None)
      val student2Id: Option[Int] =
        awaitInf(studentModel.createStudent(student2, classId))
      student2Id.get mustBe 1 // same id
    }
    "count only self-reported students" in {
      val numOfStudents0: Int = awaitInf(studentModel.getNumberOfStudents(classId))

      numOfStudents0 mustBe 0

      val student1: StudentCC = StudentCC(None, "hashedName", "encName", true, None)
      val student2: StudentCC =
        StudentCC(None, "hashedName2", "encName2", false, None)

      val student1Id: Option[Int] =
        awaitInf(studentModel.createStudent(student1, classId))
      student1Id.get mustBe 1 // first student has id 1

      val student2Id: Option[Int] =
        awaitInf(studentModel.createStudent(student2, classId))
      student2Id.get mustBe 2 // second student has id 2

      val numOfStudents1: Int = awaitInf(studentModel.getNumberOfStudents(classId))

      numOfStudents1 mustBe 1
    }
    "return all students" in {
      //also those with self-reported = false
      val allStudents: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId))
      allStudents.length mustBe 0

      val student1: StudentCC = StudentCC(None, "hashedName", "encName", true, None)

      val numOfStudentsId: Option[Int] =
        awaitInf(studentModel.createStudent(student1, classId))
      numOfStudentsId.get mustBe 1 // first student has id 1

      val student2: StudentCC =
        StudentCC(None, "hashedName2", "encName2", false, None)
      val student2Id: Option[Int] =
        awaitInf(studentModel.createStudent(student2, classId))
      student2Id.get mustBe 2 // second student has id 2

      val allStudents2: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId))
      allStudents2.length mustBe 2
    }
    "return ids of only self-reported students" in {
      val allStudentIds1: Seq[Int] =
        awaitInf(studentModel.getAllSelfReportedStudentIDs(classId))

      allStudentIds1.length mustBe 0

      val student1: StudentCC =
        StudentCC(None, "hashedName", "encName", false, None)
      val student2: StudentCC =
        StudentCC(None, "hashedName2", "encName2", true, None)

      val student1Id: Option[Int] =
        awaitInf(studentModel.createStudent(student1, classId))
      student1Id.get mustBe 1 // first student has id 1

      val student2Id: Option[Int] =
        awaitInf(studentModel.createStudent(student2, classId))
      student2Id.get mustBe 2 // second student has id 2

      val allStudentIds2: Seq[Int] =
        awaitInf(studentModel.getAllSelfReportedStudentIDs(classId))
      allStudentIds2.length mustBe 1
      allStudentIds2(
        0
      ) mustBe 2 // there is only one element in the list and it must be of the second student
    }
    "update group belonging of students" in {
      val student1: StudentCC =
        StudentCC(None, "uniqueName", "encName", false, None)
      val student1Id: Option[Int] =
        awaitInf(studentModel.createStudent(student1, classId))
      student1Id.get mustBe 1 // first student has id 1

      val allStudents: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId))
      val firstStudent: StudentCC = allStudents(0)
      firstStudent.groupBelonging mustBe None // none is default value of groupBelonging

      val updatedStudents: Int =
        awaitInf(studentModel.updateGroupBelonging(1, 2))
      updatedStudents mustBe 1 // indicates success of operation

      val allStudents2: Seq[StudentCC] =
        awaitInf(studentModel.getStudents(classId))
      val firstStudent2: StudentCC = allStudents2(0)
      firstStudent2.groupBelonging.get mustBe 2 // now the value must be updated

      val updatedStudents2: Int = awaitInf(
        studentModel.updateGroupBelonging(2, 2)
      ) // updating a non-existent student
      updatedStudents2 mustBe 0
    }
    "return all selfreported students by classID" in{
      val allSelfReportedStudents1: Seq[StudentCC] =
        awaitInf(studentModel.getAllSelfReportedStudents(classId))

      allSelfReportedStudents1.length mustBe 0

      val student1: StudentCC =
        StudentCC(None, "hashedName", "encName", false, None)
      val student2: StudentCC =
        StudentCC(None, "hashedName2", "encName2", true, None)

      val student1Id: Option[Int] =
        awaitInf(studentModel.createStudent(student1, classId))
      student1Id.get mustBe 1 // first student has id 1

      val student2Id: Option[Int] =
        awaitInf(studentModel.createStudent(student2, classId))
      student2Id.get mustBe 2 // second student has id 2

      val allSelfReportedStudents2: Seq[StudentCC] =
        awaitInf(studentModel.getAllSelfReportedStudents(classId))
      allSelfReportedStudents2.length mustBe 1
      allSelfReportedStudents2(0).id.get mustBe 2 // there is only one element in the list and it must be of the second student
    }
    "return all friendreported students by classID" in {
      val allFriendReportedStudents1: Seq[StudentCC] =
        awaitInf(studentModel.getAllFriendReportedStudents(classId))

      allFriendReportedStudents1.length mustBe 0

      val student1: StudentCC =
        StudentCC(None, "hashedName", "encName", false, None)
      val student2: StudentCC =
        StudentCC(None, "hashedName2", "encName2", true, None)

      val student1Id: Option[Int] =
        awaitInf(studentModel.createStudent(student1, classId))
      student1Id.get mustBe 1 // first student has id 1

      val student2Id: Option[Int] =
        awaitInf(studentModel.createStudent(student2, classId))
      student2Id.get mustBe 2 // second student has id 2

      val allFriendReportedStudents2: Seq[StudentCC] =
        awaitInf(studentModel.getAllFriendReportedStudents(classId))
      allFriendReportedStudents2.length mustBe 1
      allFriendReportedStudents2(0).id.get mustBe 1 
    }
  }
}
