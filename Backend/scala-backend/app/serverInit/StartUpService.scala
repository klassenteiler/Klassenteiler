package serverInit

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import javax.inject._

import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.ExecutionContext
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import models._
import scala.concurrent.Future
import controllers.SurveyController
import play.api.Logger

@Singleton
class StartUpService @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    val surveyController: SurveyController
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  private val logger: Logger = Logger(this.getClass())
  private val classModel = new SchoolClassModel(db)

  println("~~~~~~~Starting Application~~~~~~~")

  val allClasses: Future[Seq[SchoolClassCC]] = classModel.getAllClasses()
  val calculatingClasses: Future[Seq[SchoolClassCC]] =
    allClasses.map(sequence =>
      sequence.filter(_.surveyStatus.get == SurveyStatus.Calculating) // we
    )

  calculatingClasses.map(sequence =>
    sequence.map(schoolClass => {
      surveyController.startPartitionAlgorithm(schoolClass.id.get)
      this.logger.info(
        s"restarted calculation of class ${schoolClass.className} with id ${schoolClass.id.get}"
      )
    })
  )

}

class EagerLoaderModule extends AbstractModule {
  override def configure() = {
    bind(classOf[StartUpService]).asEagerSingleton
  }
}
