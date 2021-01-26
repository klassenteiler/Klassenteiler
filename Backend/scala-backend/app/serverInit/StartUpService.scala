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



@Singleton
class StartUpService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {
    println("~~~~~~~Starting Application~~~~~~~")
    private val classModel = new SchoolClassModel(db)




}


class EagerLoaderModule extends AbstractModule {
    override def configure() = {
        bind(classOf[StartUpService]).asEagerSingleton
    }
}