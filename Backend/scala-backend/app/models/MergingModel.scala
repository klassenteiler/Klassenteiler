package models

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext // the execution context is needed for concurrent execution
import models.Tables._
import scala.concurrent.Future

class MergingModel(db: Database)(implicit ec: ExecutionContext) {

  val studentModel = new StudentModel(db)
  val relationModel = new RelationshipModel(db)

  // possibly rewires and deletes a student and updates the name of another student
  def renameAndMerge(
      classId: Int,
      studentId: Int,
      hashedName: String,
      encryptedName: String
  ): Future[Boolean] = {

    // it is possible that there exists a friendreported name with the target hash
    // we first look for it in the database
    val studentWithCorrectNameId: Future[Option[Int]] =
      studentModel.getByHash(hashedName, classId)
    studentWithCorrectNameId.flatMap(frID => {
      var success: Future[Boolean] = 
        if (!frID.isEmpty) {
          // in case there already exists a friendreported student with that name
          // we rewire all incoming ties and delete the student
          rewireAndDelete(frID.get, studentId)
        }else {
          Future.successful(true)
        }
      success.flatMap(succs => {
        if (succs) {
          changeName(studentId, hashedName, encryptedName) // return
        }else{
            Future.successful(false) // return
        }
      })

    })
  }

  // rewires all incoming ties that have friendRepStudentId as target to selfId and deletes student with friendRepStudentId
  def rewireAndDelete(friendRepStudentId: Int, selfRepStudentId: Int): Future[Boolean] = {
    // old,     new
    val success: Future[Boolean] =
      relationModel.rewireRelations(friendRepStudentId, selfRepStudentId)
    success.flatMap(succs => {
      if (succs) {
        studentModel.removeStudent(friendRepStudentId)
      } else {
        Future.successful(false)
      }
    })

  }

  // sometimes the frontend cannot deliver the id of a student because it doesnt know the id yet
  // In theses cases we first find the id of the student with the corresponding name and then call rewireAndDelete
  def findRewireAndDelete(
      classId: Int,
      friendRepStudentId: Int,
      selfRepStudentHash: String
  ): Future[Boolean] = {
    val selfId: Future[Option[Int]] = studentModel.getByHash(selfRepStudentHash, classId)
    selfId.flatMap(id => {
      if (!id.isEmpty) {
        rewireAndDelete(friendRepStudentId, id.get)
      } else {
        Future.successful(false)
      }
    })
  }

  // updates the hash of a student. This is purposefully not part of studentmodel, because it should not be used
  // outside of this mergin process, as certain checks need to be run before updating the name to guarantee uniqueness
  // todo: I would like to set this to private but then the test dont have access anymore
  def changeName(studentId: Int, newHashedName: String, newEncryptedName: String): Future[Boolean] = {
    val affectedRows: Future[Int] = db.run(
      Student
        .filter(_.id === studentId)
        .map(row => (row.hashedname, row.encryptedname))
        .update(newHashedName, newEncryptedName)
    )
    affectedRows.map(rows => rows == 1)
  }

}
