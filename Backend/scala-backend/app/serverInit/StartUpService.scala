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

  val calculatingClasses: Future[Seq[Int]] =
    classModel.getCalculatingClassesIds()

  calculatingClasses.map(sequence =>
    sequence.foreach(id => {
      surveyController.startPartitionAlgorithm(id)
      this.logger.info(
        s"restarted calculation of class with id ${id}"
      )
    })
  )

}

class EagerLoaderModule extends AbstractModule {
  override def configure() = {
    bind(classOf[StartUpService]).asEagerSingleton
  }
}
