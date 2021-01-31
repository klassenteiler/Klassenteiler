package models

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import utils.MockDatabase
import models.{StudentModel, SchoolClassModel, SchoolClassDB}
import scala.concurrent.ExecutionContext

class MergingModelSpec
    extends PlaySpec
    with MockDatabase
    with BeforeAndAfterEach {

  // we have access to db because DatabaseCleanerOnEachTest itself implements the trait play.api.db.slick.HasDatabaseConfigProvider
  val classModel: SchoolClassModel = new SchoolClassModel(db)
  val studentModel = new StudentModel(db)
  val mergingModel = new MergingModel(db)
  val relModel = new RelationshipModel(db)

  var classId: Int = _
  var selfReportedStudent1Id: Int = _
  var selfReportedStudent2Id: Int = _
  var friendReportedStudentId: Int = _

  val selfReportedStudent1: StudentCC =
    StudentCC(None, "selfreport1", "encName", true, None)

  val selfReportedStudent2: StudentCC =
    StudentCC(None, "selfreport2", "encName", true, None)

  val friendReportedStudent: StudentCC =
    StudentCC(None, "friendReportedStudent", "encName", false, None)

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
    println("Before Test: SchoolclassId is " + classId)

    selfReportedStudent1Id = awaitInf(
      studentModel.createStudent(selfReportedStudent1, classId)
    ).get
    selfReportedStudent2Id = awaitInf(
      studentModel.createStudent(selfReportedStudent2, classId)
    ).get
    friendReportedStudentId = awaitInf(
      studentModel.createStudent(friendReportedStudent, classId)
    ).get

    val rel1: RelationshipCC =
      RelationshipCC(classId, selfReportedStudent1Id, friendReportedStudentId)

    val success: Boolean =
      awaitInf(relModel.createRelationship(rel1))
  }

  "The MergingModel's changeName " should {
    "update the hashedName of a selfreported student" in {
      val studentId: Option[Int] =
        awaitInf(
          studentModel.getByHash(selfReportedStudent1.hashedName, classId)
        )
      studentId.get mustBe selfReportedStudent1Id

      val success1: Boolean =
        awaitInf(mergingModel.changeName(selfReportedStudent1Id, "test"))
      success1 mustBe true

      val studentId2: Option[Int] =
        awaitInf(
          studentModel.getByHash(selfReportedStudent1.hashedName, classId)
        )
      studentId2.isEmpty mustBe true // we should not be able to find the student by the old hash anymore

      val studentId3: Option[Int] =
        awaitInf(studentModel.getByHash("test", classId))
      studentId3.get mustBe selfReportedStudent1Id
    }
  }

  "The MergingModel's rewireAndDelete" should {
    "update all relations with the old id as target" in {
      // in this test we change all incoming ties to the friendreported student to the second
      // selfreported student

      val allRelations1: Seq[(Int, Int)] =
        awaitInf(relModel.getAllRelationIdsOfClass(classId))
      allRelations1 mustBe Seq(
        (selfReportedStudent1Id, friendReportedStudentId)
      )

      // nonexistent selfReportedid
      val success1: Boolean =
        awaitInf(mergingModel.rewireAndDelete(friendReportedStudentId, 99))
      success1 mustBe false
      // nonexistent friendId
      val success2: Boolean =
        awaitInf(mergingModel.rewireAndDelete(99, selfReportedStudent2Id))
      success2 mustBe false

      val success3: Boolean =
        awaitInf(
          mergingModel.rewireAndDelete(
            friendReportedStudentId,
            selfReportedStudent2Id
          )
        )
      success3 mustBe true


      val allRelations2: Seq[(Int, Int)] =
        awaitInf(relModel.getAllRelationIdsOfClass(classId))
      allRelations2 mustBe Seq(
        (selfReportedStudent1Id, selfReportedStudent2Id)
      )
    }
  }

}
