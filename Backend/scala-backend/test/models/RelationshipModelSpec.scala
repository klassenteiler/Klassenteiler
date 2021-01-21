package models

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import utils.MockDatabase
import models._
import scala.concurrent.ExecutionContext

class RelationshipModelSpec
    extends PlaySpec
    with MockDatabase
    with BeforeAndAfterEach {

  // we have access to db because DatabaseCleanerOnEachTest itself implements the trait play.api.db.slick.HasDatabaseConfigProvider
  val relationshipModel: RelationshipModel = new RelationshipModel(db)
  val classModel: SchoolClassModel = new SchoolClassModel(db)
  val studentModel: StudentModel = new StudentModel(db)
  var classId: Option[Int] = None
  // self reported students
  var srStudent1Id: Option[Int] = None
  var srStudent2Id: Option[Int] = None
  var alterstudentId: Option[Int] = None

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
    classId = createdSchoolClass.id
    println("Before Test: SchoolclassId is " + classId.get)

    val selfReportedStudent1: StudentCC =
      StudentCC(None, "selfReportedStudent1", "encName", true, None)
    srStudent1Id =
      // awaitInf is a helper defined in MockDatabase
      awaitInf(studentModel.createStudent(selfReportedStudent1, classId.get))

    val selfReportedStudent2: StudentCC =
      StudentCC(None, "selfReportedStudent2", "encName", true, None)
    srStudent2Id = awaitInf(
      studentModel.createStudent(selfReportedStudent2, classId.get)
    )

    val alterStudent: StudentCC =
      StudentCC(None, "alterStudent", "encName2", false, None)
    alterstudentId = awaitInf(
      studentModel.createStudent(alterStudent, classId.get)
    )
  }

  "The Relationship Model" should {
    "create relations from a self-reported student to a not self-reported student" in {
      val rel1: RelationshipCC =
        RelationshipCC(classId.get, srStudent1Id.get, alterstudentId.get)

      val success: Boolean =
        awaitInf(relationshipModel.createRelationship(rel1))
      success mustBe true

      // we do allow for duplicate relationships:
      val success2: Boolean =
        awaitInf(relationshipModel.createRelationship(rel1))
      success2 mustBe true
    }
    "create relations from a self-reported student to another self-reported student" in {
      val rel1: RelationshipCC =
        RelationshipCC(classId.get, srStudent1Id.get, srStudent2Id.get)

      val success: Boolean =
        awaitInf(relationshipModel.createRelationship(rel1))
      success mustBe true
    }

    "not create relations from one student to the same student" in {
        val rel1: RelationshipCC =
        RelationshipCC(classId.get, srStudent1Id.get, srStudent1Id.get)

      val success: Boolean =
        awaitInf(relationshipModel.createRelationship(rel1))
      success mustBe false
    }
    "return all relationships by classId" in {
      val rel1: RelationshipCC =
        RelationshipCC(classId.get, srStudent1Id.get, alterstudentId.get)

      val success: Boolean =
        awaitInf(relationshipModel.createRelationship(rel1))

      val allRelations1: Seq[(Int, Int)] =
        awaitInf(relationshipModel.getAllRelationIdsOfClass(classId.get))
      allRelations1 mustBe Seq(
        (srStudent1Id.get, alterstudentId.get)
      )

      val rel2: RelationshipCC =
        RelationshipCC(classId.get, srStudent1Id.get, srStudent2Id.get)

      val success2: Boolean =
        awaitInf(relationshipModel.createRelationship(rel2))

      val allRelations2: Seq[(Int, Int)] =
        awaitInf(relationshipModel.getAllRelationIdsOfClass(classId.get))
      allRelations2 mustBe Seq(
        (srStudent1Id.get, alterstudentId.get),
        (srStudent1Id.get, srStudent2Id.get)
      )
    }
  }
}
