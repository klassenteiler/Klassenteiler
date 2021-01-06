package models

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext // the execution context is needed for concurrent execution
import models.Tables._
import scala.concurrent.Future


class SchoolClassModel(db: Database)(implicit ec: ExecutionContext) {
  
  def createSchoolClass(
      schoolClassDB: SchoolClassDB
  ): Future[SchoolClassCC] =  {   
    db.run(
      // this is the table schoolclass and we add another entry | with keyword 'returning' we specify that we want to get the id of the inserted object and the object 
      // back and we update the object with the new id
      Schoolclass returning Schoolclass.map(_.id) into ((schoolclass,id) => schoolclass.copy(id=id)) += SchoolclassRow(
        -1, // this is a flag that tells the db to autoincrement the id
        schoolClassDB.className,
        schoolClassDB.schoolName,
        schoolClassDB.classSecret,
        schoolClassDB.publicKey,
        schoolClassDB.teacherSecret,
        schoolClassDB.encryptedPrivateKey
        // surveystatus is not set and initialized to Some(0)
      )
    ).map{entry => SchoolClassCC(Some(entry.id), entry.classname, entry.schoolname, entry.classsecret, entry.publickey, entry.surveystatus)} // return
  }

  def removeSchoolclass(classId: Int): Future[Boolean] = {
    //delete returns the number of rows affected so we can map that to a boolean by checking whether its larger than 0
    db.run(Schoolclass.filter(_.id === classId).delete).map(count => count > 0) 
  }

  def getSchoolClass(classId: Int): Future[SchoolClassCC] = {
    // for this easy query we split the code up into its individual chunks and provide all types to understand whats going on
    val q: slick.lifted.Query[Schoolclass,SchoolclassRow,Seq] = Schoolclass.filter(_.id === classId)
    val action: slick.sql.FixedSqlStreamingAction[Seq[SchoolclassRow],SchoolclassRow,slick.dbio.Effect.Read] = q.result
    val intermediateResult: Future[Seq[models.Tables.SchoolclassRow]] = db.run(action)
    val result: Future[SchoolclassRow] = intermediateResult.map(r => r.head)
    
    val schoolClass: Future[SchoolClassCC] = result.map(entry => SchoolClassCC(Some(entry.id), entry.classname, entry.schoolname, entry.classsecret, entry.publickey, entry.surveystatus))
    schoolClass // return
  }

  def validateAccess(classId: Int, classSecret: String): Future[Boolean] = {
    // we check whether there is exactly one schoolclass with the relevant id and classsecret
    db.run(Schoolclass.filter(x => (x.id === classId && x.classsecret === classSecret)).result).map(rows => rows.length == 1)
  }

  def getTeacher(classId: Int, teacherSecret: String): Future[Option[ClassTeacherCC]] = {
    val schoolClassOption: Future[Option[SchoolclassRow]] = db.run(Schoolclass.filter(x => (x.id === classId && x.teachersecret === teacherSecret)).result).map(rows => rows.headOption)
    
    schoolClassOption.map(sc => sc match {
      case Some(x) =>
        Some(ClassTeacherCC(Some(x.id), x.encryptedprivatekey, x.teachersecret)) // return
      case None => None // return
    })
  }

  // returns surveystatus
  def getStatus(classId: Int): Future[Int] = {
    db.run(Schoolclass.filter(_.id === classId).result).map(r => r.head.surveystatus.get) 
  }

  def updateStatus(classId: Int, status: Int): Future[Int] = {
    db.run(Schoolclass.filter(_.id === classId).map(row => (row.surveystatus)).update((Some(status))))
  }
}

