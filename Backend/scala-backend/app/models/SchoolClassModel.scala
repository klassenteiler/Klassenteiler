package models

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext // the execution context is needed for concurrent execution
import models.Tables._
import scala.concurrent.Future


class SchoolClassModel(db: Database)(implicit ec: ExecutionContext) {
  
  def createSchoolClass(
      schoolClassCC: SchoolClassCC
  ): Future[Boolean] = {
    db.run(
      //this is the table schoolclass and we add another entry
      Schoolclass += SchoolclassRow(
        -1,
        schoolClassCC.className,
        schoolClassCC.schoolName,
        schoolClassCC.classSecret,
        schoolClassCC.publicKey,
        schoolClassCC.teacherSecret,
        schoolClassCC.encryptedPrivateKey,
        schoolClassCC.surveyStatus
      )
    ).flatMap(addCount => Future(addCount > 0))
    // translate the return value of the transaction (number of rows affected) into a boolean
  }

  def removeSchoolclass(classId: Int): Future[Boolean] = ???

  def getSchoolClass(classId: Int): Future[SchoolClassCC] = ???
}
