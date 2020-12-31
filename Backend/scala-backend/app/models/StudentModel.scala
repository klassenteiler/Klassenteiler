package models

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext // the execution context is needed for concurrent execution
import models.Tables._
import scala.concurrent.Future

class StudentModel(db: Database)(implicit ec: ExecutionContext) {

  def createStudent(studentCC: StudentCC
  ): Future[Boolean] = ???

  def removeStudent(studentId: Int): Future[Boolean] = ???

  def getStudent(studentId: Int): Future[StudentCC] = ???

  def getNumberOfStudents(classId: Int): Future[Int] = {
    db.run(Student.filter(x => (x.classid === classId && x.selfreported === true)).result).map(rows => rows.length)
  }

}
