package models

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext // the execution context is needed for concurrent execution
import models.Tables._
import scala.concurrent.Future

class StudentModel(db: Database)(implicit ec: ExecutionContext) {

  // returns the id of the student inserted
  def createStudent(studentCC: StudentCC, classId: Int): Future[Option[Int]] = {
    
    val resultRows: Future[Seq[StudentRow]] = db.run(Student.filter(_.hashedname === studentCC.hashedName).result)

    
    resultRows.flatMap(rows => {
      val userExists: Boolean = rows.length != 0
      // if the student already exists we need to differentiate between two cases
      if (userExists) {
        val studentId: Int = rows.head.id
        val alreadySelfReported: Boolean = rows.head.selfreported
        // if the existing student was already self reported and the current one is too
        // we return None to signal a wrong input
        if (alreadySelfReported && studentCC.selfReported) Future.successful(None)//return None because student already exists
        // if the new entry is self reported but the old one is not, we need to update the value
        else if (!alreadySelfReported && studentCC.selfReported) {
          // update selfreported of student
          db.run(Student.filter(_.id === studentId).map(row => (row.selfreported)).update((true)))
          Future.successful(Some(studentId))  //return
        // else we don't need to do anything and just return the id
        }else Future.successful(Some(studentId))

      // if the student with that name didn exist yet, we need to insert the student
      }else { 
        val studentId: Future[Int] = db.run(Student returning Student.map(_.id) += StudentRow(
          -1, // id is automatically set
          Some(classId), 
          studentCC.hashedName,
          studentCC.encryptedName,
          studentCC.selfReported,
          studentCC.groupBelonging
        ))
        studentId.map(x => Some(x)) // return
      }

    })
  }

  def removeStudent(studentId: Int): Future[Boolean] = ???

   def getStudents(classId: Int): Future[Seq[StudentCC]] = {
    db.run(Student.filter(_.classid === classId).result).map(rows => rows.map(entry => {
        StudentCC(
          Some(entry.id),
          entry.hashedname,
          entry.encryptedname,
          entry.selfreported,
          entry.groupbelonging
        )
      })
    )
  }

  def getNumberOfStudents(classId: Int): Future[Int] = {
    db.run(Student.filter(x => (x.classid === classId && x.selfreported === true)).result).map(rows => rows.length)
  }

  def getAllSelfReportedStudentIDs(classId: Int): Future[Seq[Int]] = {
    db.run(Student.filter(x => (x.classid === classId && x.selfreported === true)).result).map(rows => rows.map(_.id))
  }

  // i think it returns the value that was updated, so again group
  def updateGroupBelonging(studentId: Int, group: Int): Future[Int] = {
    val updateOp: Future[Int] = db.run(Student.filter(_.id === studentId).map(row => (row.groupbelonging)).update((Some(group))))
    return updateOp
  }
}
