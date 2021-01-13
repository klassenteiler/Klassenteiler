package models

// import org.scalatest._
import org.scalatestplus.play.PlaySpec
import play.api.http.MimeTypes
// import play.api.test._
import play.api.db.slick.DatabaseConfigProvider

// import javax.inject._
// import scala.concurrent.ExecutionContext.global

// import play.api.inject._
// import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import utils.DatabaseCleanerOnEachTest
// import models.StudentModel.StudentModel
// import models.SchoolClassModel.SchoolClassModel
import models.{StudentModel, SchoolClassModel, SchoolClassDB}
import scala.concurrent.ExecutionContext
// testing with databases
import play.api.db.Databases
import play.api.db.Database
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

// das grundgerüst ist aus https://www.playframework.com/documentation/2.8.x/ScalaFunctionalTestingWithScalaTest

                                        //alternative: GuiceOneAppPerTest 
class DbExampleSpec
    extends PlaySpec with DatabaseCleanerOnEachTest {




  // Override fakeApplication if you need a Application with other than
  // default parameters.
  // shakti: this function is overwritting the function from GuiceOneApplicationPerTest which inside DatabaseCleanerOnEachTest
  override def fakeApplication(): Application = {
    GuiceApplicationBuilder().configure(
      // Map(
      //   "ehcacheplugin" -> "disabled",
      //     "slick.dbs.test.driver" -> "slick.driver.H2Driver$",
      //     "slick.dbs.test.db.driver" -> "org.h2.Driver",
      //     "slick.dbs.test.db.url" -> "jdbc:h2:mem:"
      //     )
          Map(
    "slick.dbs.default.profile"     -> "slick.jdbc.H2Profile$",
    "slick.dbs.default.driver"      -> "slick.driver.H2Driver$",
    "slick.dbs.default.db.driver"   -> "org.h2.Driver",
    "slick.dbs.default.db.url"      -> "jdbc:h2:mem:play;MODE=MYSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE",
    "slick.dbs.default.db.user"     -> "",
    "slick.dbs.default.db.password" -> ""
  )
    ).build()
  }

  "the database connection" should {
    "Test should work" in {
      println(">>>>>>> starting db test!")
      this.clearDatabase();

      implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

      // so we have access to db because DatabaseCleanerOnEachTest itself implements the trait play.api.db.slick.HasDatabaseConfigProvider
      val studentModel: StudentModel = new StudentModel(db)
      val classModel: SchoolClassModel = new SchoolClassModel(db)

      val schoolClass: SchoolClassDB = SchoolClassDB(None, "test", Some("AMG"), "clsSecret", "teachsecret","puKey", "encPrivateKey", Some(0))

      val created: SchoolClassCC = awaitInf(classModel.createSchoolClass(schoolClass))
      val Some(clsId) = created.id
      //directly unpack into id, lol scala syntax lol rofl
      println(clsId)

      awaitInf(studentModel.getNumberOfStudents(clsId)) mustBe 0 
      val stud: StudentCC = StudentCC(None, "hashedName", "encName", false, None)

      val createdS: Option[Int] =  awaitInf(studentModel.createStudent(stud, clsId))
      println(createdS)
      
      val stud2: StudentCC = StudentCC(None, "hashedName2", "encName2", false, None)
      val createdStwo: Option[Int] =  awaitInf(studentModel.createStudent(stud2, clsId))
      println(createdStwo)

      val num: Int = awaitInf(studentModel.getNumberOfStudents(clsId))// mustBe 2
      println(num)

      num mustBe 2
    }
  }

  // dies ist aus https://www.playframework.com/documentation/2.8.x/ScalaTestingWithDatabases
  // def withMyDatabase[T](block: Database => T) = {
  //     Databases.withInMemory(
  //         name = "test",
  //         urlOptions = Map(
  //             "MODE" -> "POSTGRESQL"
  //         ),
  //         config = Map(
  //             "logStatements" -> true
  //         )
  //     )(block)
  // }   

  // dass der wrapper so benutzt wird hab ich jetzt selbst abgeleitet, weil es nirgendwo explizit steht
  // ist aber ähnlich zu https://stackoverflow.com/questions/34858856/having-trouble-setting-up-in-memory-db-for-unit-tests-in-play-2-4-an-slick-3
  // habe den namen des tests und inhalt gelassen wie im beispiel, dann wird nämlich wenigstens getestet, ob das ändern der konfiguration
  // überhaupt funktionierte. nur das Hinzufügen des wrappers ist neu
  // "The GuiceOneAppPerSuite trait" must {
  //   "provide an Application" in withMyDatabase{ database =>
  //     app.configuration.getOptional[String]("ehcacheplugin") mustBe Some("disabled")
  //   }
  // }

// das wäre der kontextlose beispiel test
//   val appWithMemoryDatabase = new GuiceApplicationBuilder().configure(inMemoryDatabase("test")).build()
// "run an application" in new App(appWithMemoryDatabase) {

//   val Some(macintosh) = Computer.findById(21)

//   macintosh.name mustEqual "Macintosh"
//   macintosh.introduced.value mustEqual "1984-01-24"
// }

  def awaitInf[T](future: Future[T]): T  = {
    val res: T = Await.result(future, Duration.Inf)
    res
  }

}