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
import play.api.Logger



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

    private val logger: Logger = Logger(this.getClass())
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
                                // we read the Friend limit from the .env or set it to 5 if no value is provided
                                val limit: Int = sys.env.getOrElse("FRIEND_LIMIT", 5).toString.toInt
                                if (alters.length <= limit){
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
                                            Future.successful(Created(Json.obj("message" -> "success - created"))) //TODO maybe only report success once it is created
                                       
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
    def closeSurvey(implicit id: Int, classSecret: String) = Action.async { implicit request: Request[AnyContent] =>
        val body = {_: ClassTeacherCC => {
            classModel.getStatus(id).flatMap(status => {
                if (status == 0) {
                    classModel.updateStatus(id, 1).map(_ => Ok(Json.obj("message" -> "success - survey closed")))
                    // Future.successful(Ok(Json.obj("message" -> "success - survey closed")))
                } else Future.successful(Gone("Survey has wrong status"))
            })
        }}
        auth.withTeacherAuthentication(body)
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
                    val studentsOfClass: Future[Seq[Int]] = studentModel.getAllSelfReportedStudentIDs(id)
                    val relationsOfClass: Future[Seq[(Int, Int)]] = relModel.getAllRelationIdsOfClass(id)

                    for{
                        f1 <- studentsOfClass
                        f2 <- relationsOfClass
                    } yield (f1, f2)

                    val partition: Future[(Array[Int], Array[Int])] = studentsOfClass.zip(relationsOfClass).map{
                        case ((f1,f2)) => {
                            this.logger.info(s"starting to compute the partition for students ${f1.mkString(" ")}")
                            IterativeAlgo.computePartition(f1.toArray, f2.toArray)
                            }
                    }

                    partition.map(p => {
                        this.logger.info(s"partition was computed: p1=[${p._1.mkString(" ")}] p2=[${p._2.mkString(" ")}]")
                        val futureGroupOneSet: List[Future[Int]] = p._1.toList.map(id => studentModel.updateGroupBelonging(id, 1))
                        val futureGroupTwoSet: List[Future[Int]] = p._2.toList.map(id => studentModel.updateGroupBelonging(id, 2))

                        val futureAllGroupOne: Future[Seq[Int]] = Future.sequence(futureGroupOneSet)
                        val futureAllGroupTwo: Future[Seq[Int]] = Future.sequence(futureGroupTwoSet)

                        // these are just sanity checks and get rid of the sequence
                        val checkedFutureOne: Future[Boolean] = futureAllGroupOne.map((arr: Seq[Int])=>{
                            // this.logger.warn(s"group one set ${arr.mkString(" ")} ")
                            arr.forall(_==1) // one seems to mean successful
                        }
                        )
                        val checkedFutureTwo: Future[Boolean] = futureAllGroupTwo.map((arr: Seq[Int])=>{
                            // this.logger.warn(s"group two set ${arr.mkString(" ")} ")
                            arr.forall(_==1) // one seems to mean successful
                        }
                        )

                        val allUpdateComplete: Future[Boolean] = for {
                            oneOK <- checkedFutureOne
                            twoOK <- checkedFutureTwo
                        } yield (oneOK && twoOK)
                        // val allUpdateComplete: Future[Tuple2[Boolean, Boolean]] = checkedFutureOne.zipWith(checkedFutureTwo)

                        val classStatusUpdateFuture: Future[Unit] = allUpdateComplete.flatMap((ok:Boolean)=>{
                            this.logger.info("students were updated in the database")
                            if(!ok) {
                                this.logger.error("Aperantly setting the groupBelonging did not work, should throw error")
                                throw new Exception("Aperantly setting the groupBelonging did not work") 
                            }
                            else {
                                classModel.updateStatus(id, 3).map(v => {
                                    assert(v==3)
                                    this.logger.info("class status was updated")
                                    })
                            }
                        })
                    })

                    classModel.updateStatus(id, 2)
                    Future.successful(Ok(Json.obj("message" -> "success - started calculating")))
                } else Future.successful(Gone("Survey has wrong status"))
            })
        }}
        //Future.successful(Ok("todo"))
        auth.withTeacherAuthentication(body)
    }

    // GET /getResult/:id/:classSecret
    // checkt ob der status von der relevanten schoolclass korrekt ist und queried die studenttable mit classid == id
    // returns Array[StudentCC] 
    def getResults(implicit id: Int, classSecret: String) = Action.async { implicit request: Request[AnyContent] =>
        val body = {_:ClassTeacherCC =>
            classModel.getStatus(id).flatMap(status => {
                if(id != 3){
                    val allStudents: Future[Seq[StudentCC]] = studentModel.getStudents(id)
                    allStudents.map(students =>{
                        Ok(Json.toJson(students))
                    })
                    
                }else Future.successful(Gone("Results are not ready yet"))
            })
        }

        auth.withTeacherAuthentication(body)
    } 

}
