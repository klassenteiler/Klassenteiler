package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import models._

import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.ExecutionContext
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Future



@Singleton
class SurveyController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    val cc: ControllerComponents,
    val auth: AuthenticationController
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile] {
  
    implicit val studentReads = Json.reads[StudentCC]
    implicit val studentWrites = Json.writes[StudentCC]

    private val classModel = new SchoolClassModel(db)
    private val studentModel = new StudentModel(db)
    private val relModel = new RelationshipModel(db)
    // POST /submitStudentSurvey/:id/:classSecret
    // fügt student zu student table hinzu
    // fügt friends und student zu relations table hinzu
    def submitSurvey(implicit id: Int, classSecret: String): play.api.mvc.Action[play.api.mvc.AnyContent] = Action.async { implicit request: Request[AnyContent] =>
        val body = {() => {
            // if the survey is not open anymore we return 410
            classModel.getStatus(id).flatMap(status => {
                if (status == 0){
                    request.body.asJson match {
                        case Some(content) => {
                            // extract json values from JsObject
                            val egoJs: JsValue = content("me")
                            val altersJs: JsValue = content("friends")      

                            // parse the Json Values
                            val egoOption: JsResult[StudentCC] = Json.fromJson[StudentCC](egoJs)
                            val altersOption: JsResult[Seq[StudentCC]] = Json.fromJson[Seq[StudentCC]](altersJs)
                            
                            // if the parsing was successful
                            if (egoOption.isSuccess && altersOption.isSuccess) {
                                val ego: StudentCC = egoOption.get
                                val alters: Seq[StudentCC] = altersOption.get

                                if (alters.length <= 5){
                                    val sourceId: Future[Option[Int]] = studentModel.createStudent(ego, id)
                                    sourceId.flatMap(sId => sId match {
                                        case Some(sId) => {
                                            // enter each alter and store the id of the student objects
                                            val targetIds: Seq[Future[Option[Int]]] = alters.map(alter => studentModel.createStudent(alter, id))
                                            // create a new relationship with the alter as target and ego as source
                                            targetIds.foreach(targetId => {
                                                targetId.map(tId => {
                                                    val relationship = RelationshipCC(classId=id, sourceId=sId, targetId=tId.get) //tId is an option
                                                    relModel.createRelationship(relationship)
                                                })
                                            })
                                            Future.successful(Created(Json.obj("message" -> "success")))
                                       
                                        }
                                        case None => {
                                            Future.successful(Unauthorized("Student with this name already submitted survey"))
                                        }
                                    })
                                }else {
                                    Future.successful(BadRequest("Over friend limit (5)"))
                                }
                                
                            }else {
                                Future.successful(UnsupportedMediaType("Wrong JSON format"))
                            }
                        }
                        case None => Future.successful(BadRequest("Empty Body"))
                    }
                }else Future.successful(Gone("Survey is already closed"))

            })
            
        }}

        // check whether classSecret is correct
        auth.withClassAuthentication(body)
    }

    // PUT /closeSurvey/:id/:classSecret
    // setzt survey status von schoolclass mit der relevanten id und 
    def closeSurvey(id: Int, classSecret: String) = Action { implicit request: Request[AnyContent] =>
        Ok("todo")
    }

    // PUT
    // setzt survey status auf 2 ('calculating') 
    // ruft alle relationships auf
    // ruft internen algorithmus auf
    // ändert alle groupbelonging einträge in der students datenbank
    // setzt survey status auf 3 (done)
    def startCalculating(implicit id: Int, classSecret: String) = Action.async { implicit request: Request[AnyContent] =>
        val body = {_: ClassTeacherCC => {
            classModel.getStatus(id).flatMap(status => {
                if (status == 1) {
                    val studentsOfClass: Future[Seq[Int]] = studentModel.getAllStudentsOfClass(id)
                    val relationsOfClass: Future[Seq[(Int, Int)]] = relModel.getAllRelationsOfClass(id)

                    for{
                        f1 <- studentsOfClass
                        f2 <- relationsOfClass
                    } yield (f1, f2)

                    val partition: Future[(Array[Int], Array[Int])] = studentsOfClass.zip(relationsOfClass).map{
                        case ((f1,f2)) => IterativeAlgo.computePartition(f1.toArray, f2.toArray)
                    }

                    partition.map(p => p._1.map(id => studentModel.updateGroupBelonging(id, 1)))
                    partition.map(p => p._2.map(id => studentModel.updateGroupBelonging(id, 2)))

                    classModel.updateStatus(id, 2)
                    Future.successful(Ok("status: startCalculating (2)"))
                } else Future.successful(Gone("Survey has wrong status"))
            })
        }}
        //Future.successful(Ok("todo"))
        auth.withTeacherAuthentication(body)
    }

    // GET /getResult/:id/:classSecret
    // checkt ob der status von der relevanten schoolclass korrekt ist und queried die studenttable mit classid == id
    // returns Array[StudentCC] 
    def getResults(id: Int, classSecret: String) = Action { implicit request: Request[AnyContent] =>
        Ok("todo")
    } 

}
