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

  implicit val mergeInterfaceReads = Json.reads[MergeInterface]

  private val classModel = new SchoolClassModel(db)
  private val studentModel = new StudentModel(db)
  private val relModel = new RelationshipModel(db)
  private val mergeModel = new MergingModel(db)

  private val logger: Logger = Logger(this.getClass())
  // POST /submitStudentSurvey/:id/:classSecret
  // fügt student zu student table hinzu
  // fügt friends und student zu relations table hinzu
  def submitSurvey(implicit
      classId: Int,
      classSecret: String
  ): play.api.mvc.Action[play.api.mvc.AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      val body = { () =>
        {
          // if the survey is not open anymore we return 410
          classModel
            .getStatus(classId)
            .flatMap(status => {
              if (status == SurveyStatus.Open) {
                request.body.asJson match {
                  case Some(content) => {
                    try {
                      // extract json values from JsObject
                      val egoJs: JsValue = content("me")
                      val altersJs: JsValue = content("friends")

                      // parse the Json Values
                      val egoOption: JsResult[StudentCC] =
                        Json.fromJson[StudentCC](egoJs)
                      val altersOption: JsResult[Seq[StudentCC]] =
                        Json.fromJson[Seq[StudentCC]](altersJs)

                      var ego: StudentCC = egoOption.get
                      // set selfReported to true (since StudentCC is a case class and thus immutable we have to create a new instance)
                      ego = StudentCC(
                        None,
                        ego.hashedName,
                        ego.encryptedName,
                        true,
                        None
                      )
                      val alters: Seq[StudentCC] = altersOption.get
                      // we read the Friend limit from the .env or set it to 5 if no value is provided
                      val limit: Int =
                        sys.env.getOrElse("FRIEND_LIMIT", 5).toString.toInt
                      if (alters.length <= limit) {
                        val creationSuccess: Future[Boolean] =
                          createStudents(classId, ego, alters)

                        creationSuccess.map(success =>
                          if (success) {
                            Created(
                              Json.obj("message" -> "success - created")
                            )
                          } else {
                            Forbidden(
                              "Student with this name already submitted survey"
                            )
                          }
                        )
                      } else {
                        Future.successful(BadRequest("Over friend limit (5)"))
                      }
                    } catch {
                      // in case the json does not contain "me" or "friends" objects or json format is wrong
                      case e: java.util.NoSuchElementException =>
                        Future.successful(
                          UnsupportedMediaType("Wrong JSON format") //return
                        )
                    }
                  }
                  case None => Future.successful(BadRequest("Empty Body"))
                }
              } else Future.successful(Gone("Survey is already closed"))

            })

        }
      }

      // check whether classSecret is correct
      auth.withClassAuthentication(body)
  }

  // helper method used by submitSurvey and closeSurvey
  def createStudents(
      classId: Int,
      ego: StudentCC,
      alters: Seq[StudentCC]
  ): Future[Boolean] = {
    val sourceId: Future[Option[Int]] =
      studentModel.createStudent(ego, classId)
    sourceId.flatMap(sId =>
      sId match {
        case Some(sId) => {
          // enter each alter and create a relationship with its id
          val creationSuccesses: Seq[Future[Boolean]] =
            alters.map(alter => {
              val targetStudentId: Future[Option[Int]] =
                studentModel.createStudent(alter, classId)
              targetStudentId.flatMap(tId => {
                val relationship = RelationshipCC(
                  classId = classId,
                  sourceId = sId,
                  targetId = tId.get
                )
                relModel.createRelationship(relationship)
              })
            })

          // make a future out of a list of futures
          val creationsSuccessesList: Future[Seq[Boolean]] =
            Future.sequence(creationSuccesses)

          // flatten the list of booleans into one boolean
          val creationsSucceeded: Future[Boolean] =
            creationsSuccessesList.map((arr: Seq[Boolean]) => {
              arr.forall(_ == true)
            })

          creationsSucceeded // return

        }
        case None => {
          Future.successful(
            false // return
          )
        }
      }
    )
  }

  // PUT /closeSurvey/:id/:classSecret
  // setzt survey status von schoolclass mit der relevanten classId und
  def closeSurvey(implicit classId: Int, classSecret: String) = Action.async {
    implicit request: Request[AnyContent] =>
      val body = { _: ClassTeacherCC =>
        {
          classModel
            .getStatus(classId)
            .flatMap(status => {
              if (status == SurveyStatus.Open) {
                request.body.asJson match {
                  case Some(content) => {
                    val mergingJsonOption: JsResult[MergeInterface] =
                      Json.fromJson[MergeInterface](content)

                    if (mergingJsonOption.isSuccess) {
                      val mergingSuccess: Future[Boolean] =
                        mergeStudents(classId, mergingJsonOption.get)

                      mergingSuccess.flatMap(success => {
                        classModel
                          .updateStatus(classId, SurveyStatus.Closed)
                          .map(_ =>
                            Ok(
                              Json.obj("message" -> "success - survey closed")
                            ) //return
                          )
                      })
                    } else {
                      Future.successful(
                        UnsupportedMediaType("Wrong JSON format") //return
                      )
                    }
                  }
                  case None =>
                    Future.successful(BadRequest("Empty Body")) //return
                }

              } else Future.successful(Gone("Survey has wrong status")) //return
            })
        }
      }
      auth.withTeacherAuthentication(body)
  }

  def mergeStudents(
      classId: Int,
      mergingObject: MergeInterface
  ): Future[Boolean] = {

    //1. create new
    val creationSuccessList: Seq[Future[Boolean]] =
      mergingObject.studentsToAdd.map(student =>
        createStudents(classId, student, Seq())
      )

    val creationSuccess: Future[Boolean] = Future
      .sequence(creationSuccessList)
      .map((arr: Seq[Boolean]) => {
        arr.forall(_ == true)
      })

    // 2. rename
    creationSuccess.flatMap(cSuccess => {
      if (cSuccess) {
        val renameSuccessesList: Seq[Future[Boolean]] =
          mergingObject.studentsToRename.map(student =>
            mergeModel.updateStudent(
              classId,
              student.id.get,
              student.hashedName
            )
          )
        val renameSuccess: Future[Boolean] = Future
          .sequence(renameSuccessesList)
          .map((arr: Seq[Boolean]) => {
            arr.forall(_ == true)
          })
        //3. delete
        renameSuccess.flatMap(rSuccess => {
          if (rSuccess) {
            val deletionSuccessesList: Seq[Future[Boolean]] =
              mergingObject.studentsToDelete.map(id =>
                studentModel.removeStudent(id)
              )
            val deletionSuccess: Future[Boolean] = Future
              .sequence(deletionSuccessesList)
              .map((arr: Seq[Boolean]) => {
                arr.forall(_ == true)
              })

            //4. isAliasOf
            deletionSuccess.flatMap(dSuccess => {
              if (dSuccess) {
                val aliasOfSuccessesList: Seq[Future[Boolean]] =
                  mergingObject.isAliasOf.map(aliasTuple =>
                    mergeModel.findRewireAndDelete(
                      classId,
                      aliasTuple._1,
                      aliasTuple._2
                    )
                  )
                val aliasOfSuccess: Future[Boolean] = Future
                  .sequence(aliasOfSuccessesList)
                  .map((arr: Seq[Boolean]) => {
                    arr.forall(_ == true)
                  })
                aliasOfSuccess // return
              } else {
                Future.successful(false)
              }
            })
          } else {
            Future.successful(false)
          }
        })
      } else {
        Future.successful(false)
      }
    })

  }

  // PUT
  // setzt survey status auf 2 ('calculating')
  // ruft alle relationships auf
  // ruft internen algorithmus auf
  // ändert alle groupbelonging einträge in der students datenbank
  // setzt survey status auf 3 (done)
  def startCalculating(implicit classId: Int, classSecret: String) =
    Action.async { implicit request: Request[AnyContent] =>
      val body = { _: ClassTeacherCC =>
        {
          classModel
            .getStatus(classId)
            .flatMap(status => {
              if (status == SurveyStatus.Closed) {
                startPartitionAlgorithm(classId)
                classModel.updateStatus(classId, SurveyStatus.Calculating)
                Future.successful(
                  Ok(Json.obj("message" -> "success - started calculating"))
                )
              } else Future.successful(Gone("Survey has wrong status"))
            })
        }
      }
      auth.withTeacherAuthentication(body)
    }

  def startPartitionAlgorithm(classId: Int): Unit = {
    // todo: check that all students have selfReported == true
    val studentsOfClass: Future[Seq[Int]] =
      studentModel.getAllSelfReportedStudentIDs(classId)
    val relationsOfClass: Future[Seq[(Int, Int)]] =
      relModel.getAllRelationIdsOfClass(classId)

    val suSAndRelations: Future[(Seq[Int], Seq[(Int, Int)])] = for {
      f1 <- studentsOfClass
      f2 <- relationsOfClass
    } yield (f1, f2)

    // studentsOfClass.zip(relationsOfClass)
    val partition: Future[(Array[Int], Array[Int])] =
      suSAndRelations.map {
        case ((f1, f2)) => {
          this.logger.info(
            s"starting to compute the partition for students ${f1.mkString(" ")}"
          )
          IterativeAlgo.computePartition(f1.toArray, f2.toArray)
        }
      }

    partition.map(p => {
      this.logger.info(s"partition was computed: p1=[${p._1
        .mkString(" ")}] p2=[${p._2.mkString(" ")}]")
      val futureGroupOneSet: List[Future[Int]] = p._1.toList
        .map(id => studentModel.updateGroupBelonging(id, 1))
      val futureGroupTwoSet: List[Future[Int]] = p._2.toList
        .map(id => studentModel.updateGroupBelonging(id, 2))

      val futureAllGroupOne: Future[Seq[Int]] =
        Future.sequence(futureGroupOneSet)
      val futureAllGroupTwo: Future[Seq[Int]] =
        Future.sequence(futureGroupTwoSet)

      // these are just sanity checks and get rid of the sequence
      val checkedFutureOne: Future[Boolean] =
        futureAllGroupOne.map((arr: Seq[Int]) => {
          // this.logger.warn(s"group one set ${arr.mkString(" ")} ")
          arr.forall(_ == 1) // one seems to mean successful
        })
      val checkedFutureTwo: Future[Boolean] =
        futureAllGroupTwo.map((arr: Seq[Int]) => {
          // this.logger.warn(s"group two set ${arr.mkString(" ")} ")
          arr.forall(_ == 1) // one seems to mean successful
        })

      val allUpdateComplete: Future[Boolean] = for {
        oneOK <- checkedFutureOne
        twoOK <- checkedFutureTwo
      } yield (oneOK && twoOK)
      // val allUpdateComplete: Future[Tuple2[Boolean, Boolean]] = checkedFutureOne.zipWith(checkedFutureTwo)

      val classStatusUpdateFuture: Future[Unit] =
        allUpdateComplete.flatMap((ok: Boolean) => {
          this.logger.info("students were updated in the database")
          if (!ok) {
            this.logger.error(
              "Aperantly setting the groupBelonging did not work, should throw error"
            )
            throw new Exception(
              "Aperantly setting the groupBelonging did not work"
            )
          } else {
            classModel
              .updateStatus(classId, SurveyStatus.Done)
              .map(v => {
                this.logger.info("class surveyStatus was updated")
              })
          }
        })
    })
  }

  // GET /getResult/:id/:classSecret
  // checkt ob der status von der relevanten schoolclass korrekt ist und queried die studenttable mit classid == id
  // returns Array[StudentCC]
  def getResults(implicit classId: Int, classSecret: String) = Action.async {
    implicit request: Request[AnyContent] =>
      val body = { _: ClassTeacherCC =>
        classModel
          .getStatus(classId)
          .flatMap(status => {
            if (status == SurveyStatus.Done) {
              val allStudents: Future[Seq[StudentCC]] =
                studentModel.getStudents(classId)
              allStudents.map(students => {
                Ok(Json.toJson(students))
              })

            } else Future.successful(Conflict("Results are not ready yet"))
          })
      }

      auth.withTeacherAuthentication(body)
  }
}
