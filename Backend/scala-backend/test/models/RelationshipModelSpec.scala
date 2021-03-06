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
  var classId: Int = _
  // self reported students
  var srStudent1Id: Int = _
  var srStudent2Id: Int = _
  var alterstudentId: Int = _

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

    val selfReportedStudent1: StudentCC =
      StudentCC(None, "selfReportedStudent1", "encName", true, None)
    srStudent1Id =
      // awaitInf is a helper defined in MockDatabase
      awaitInf(studentModel.createStudent(selfReportedStudent1, classId)).get

    val selfReportedStudent2: StudentCC =
      StudentCC(None, "selfReportedStudent2", "encName", true, None)
    srStudent2Id = awaitInf(
      studentModel.createStudent(selfReportedStudent2, classId)
    ).get

    val alterStudent: StudentCC =
      StudentCC(None, "alterStudent", "encName2", false, None)
    alterstudentId = awaitInf(
      studentModel.createStudent(alterStudent, classId)
    ).get
  }

  "The Relationship Model" should {
    "create relations from a self-reported student to a not self-reported student" in {
      val rel1: RelationshipCC =
        RelationshipCC(classId, srStudent1Id, alterstudentId)

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
        RelationshipCC(classId, srStudent1Id, srStudent2Id)

      val success: Boolean =
        awaitInf(relationshipModel.createRelationship(rel1))
      success mustBe true
    }

    "not create relations from one student to the same student" in {
      val rel1: RelationshipCC =
        RelationshipCC(classId, srStudent1Id, srStudent1Id)

      val success: Boolean =
        awaitInf(relationshipModel.createRelationship(rel1))
      success mustBe false
    }
    "return all relationships by classId" in {
      val rel1: RelationshipCC =
        RelationshipCC(classId, srStudent1Id, alterstudentId)

      val success: Boolean =
        awaitInf(relationshipModel.createRelationship(rel1))

      val allRelations1: Seq[(Int, Int)] =
        awaitInf(relationshipModel.getAllRelationIdsOfClass(classId))
      allRelations1 mustBe Seq(
        (srStudent1Id, alterstudentId)
      )

      val rel2: RelationshipCC =
        RelationshipCC(classId, srStudent1Id, srStudent2Id)

      val success2: Boolean =
        awaitInf(relationshipModel.createRelationship(rel2))

      val allRelations2: Seq[(Int, Int)] =
        awaitInf(relationshipModel.getAllRelationIdsOfClass(classId))
      allRelations2 mustBe Seq(
        (srStudent1Id, alterstudentId),
        (srStudent1Id, srStudent2Id)
      )
    }
    "override all occurences of an id in the 'target' field of a relationship with a new id" in {

      // rewiring a relation that does not exist yet
      val rewiringSuccess1: Boolean =
        awaitInf(
          relationshipModel.rewireRelations(alterstudentId, srStudent2Id)
        )
      // rewiringSuccess1 mustBe false
      // this now 'fails' silently, i.e. no rows are changed, but true is returned

      val rel1: RelationshipCC =
        RelationshipCC(classId, srStudent1Id, alterstudentId)

      val creationSuccess: Boolean =
        awaitInf(relationshipModel.createRelationship(rel1))
      creationSuccess mustBe true

      val allRelations1: Seq[(Int, Int)] =
        awaitInf(relationshipModel.getAllRelationIdsOfClass(classId))
      allRelations1 mustBe Seq(
        (srStudent1Id, alterstudentId)
      )

      val rewiringSuccess2: Boolean =
        awaitInf(
          relationshipModel.rewireRelations(alterstudentId, srStudent2Id)
        )
      rewiringSuccess2 mustBe true

      val allRelations2: Seq[(Int, Int)] =
        awaitInf(relationshipModel.getAllRelationIdsOfClass(classId))
      allRelations2 mustBe Seq(
        (srStudent1Id, srStudent2Id)
      )
    }
  }
}
