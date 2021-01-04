package models

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext // the execution context is needed for concurrent execution
import models.Tables._
import scala.concurrent.Future

class StudentModel(db: Database)(implicit ec: ExecutionContext) {

  // returns the id of the student inserted
  def createStudent(studentCC: StudentCC, classId: Int): Future[Option[Int]] = {
    
    val resultRows: Future[Seq[StudentRow]] = db.run(Student.filter(_.hashedname === studentCC.hashedName).result)
    val userExists: Future[Boolean] = resultRows.map(rows => rows.length != 0)
    val studentId: Future[Int] = resultRows.map(rows => rows.head.id)
    val alreadySelfReported: Future[Boolean]= resultRows.map(rows => rows.head.selfreported)
    
    userExists.flatMap(exists => {
      // if the student already exists we need to differentiate between two cases
      if (exists) {
        alreadySelfReported.flatMap(sr => {
          // if the existing student was already self reported and the current one is too
          // we return None to signal a wrong input
          if(sr && studentCC.selfReported) Future.successful(None)//return None because student already exists
          // if the new entry is self reported but the old one is not, we need to update the value
          else if (!sr && studentCC.selfReported) {
            println("User already existed, updating selfReport status")
            studentId.flatMap(sId => {
            // update selfreported of student
              db.run(Student.filter(_.id === sId).map(row => (row.selfreported)).update((true)))
              Future.successful(Some(sId))  //return
            })
          // else we don't need to do anything and just return the id
          }else studentId.map(sId => Some(sId))
        })
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

  def getStudent(studentId: Int): Future[StudentCC] = ???

  def getNumberOfStudents(classId: Int): Future[Int] = {
    db.run(Student.filter(x => (x.classid === classId && x.selfreported === true)).result).map(rows => rows.length)
  }

  def getAllStudentsOfClass(classId: Int): Future[Seq[Int]] = {
    db.run(Student.filter(x => (x.classid === classId && x.selfreported === true)).result).map(rows => rows.map(_.id))
  }

  def updateStatus(studentId: Int, group: Int) = {
    db.run(Student.filter(_.id === studentId).map(row => (row.groupbelonging)).update((Some(group))))
  }
}
