// run only one test with e.g. sbt testOnly models.StudentModelSpec

package utils

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import slick.jdbc.SetParameter.SetUnit
import slick.jdbc.{JdbcProfile, SQLActionBuilder}
import models.Tables
import org.scalatest.TestSuite
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import scala.util.Try
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder


trait MockDatabase
    extends HasDatabaseConfigProvider[
      JdbcProfile
    ] // we need this so that we have access to the field 'db'
    with GuiceOneAppPerSuite // we need this sot aht we have acces to the field 'app'
    {
  this: GuiceOneAppPerSuite with TestSuite =>
  // this funny thing only allows the DAtabaseCleanerOnEachTest trait to be mixed into some class that also inherits from TestSuite (i.e. only use this trait in PlaySpec test suits)
  // this is an enforcement from GuceOneAppPerTest with this we basically push the check down in the hierarchy so that the compiler does not shit it's pants

  // with this we get a dbConfigProvider from the dependency injection of the testing. i.e. what kind of database we have depends on GuiceOneAppPerTest. Recall that we overwritte
  // the definition of that one somewhere else (i.e. the method fakeApplication)
  override lazy val dbConfigProvider: DatabaseConfigProvider =
    app.injector.instanceOf[
      DatabaseConfigProvider
    ] // this seems to be the way to get dependencies in unit tests
  // app comes from GuiceOneAppPerSuite
  // db comes from HasDatabaseConfigProvider

  // we need this because for instantiating the models
  implicit val ec: scala.concurrent.ExecutionContext =
    scala.concurrent.ExecutionContext.global

  // this overrides the method of GuiceOneAppPerSuite, which delivers the app needed for testing
  override def fakeApplication(): Application = {
    GuiceApplicationBuilder()
      .configure(
        Map(
          "slick.dbs.default.profile" -> "slick.jdbc.H2Profile$",
          "slick.dbs.default.driver" -> "slick.driver.H2Driver$",
          "slick.dbs.default.db.driver" -> "org.h2.Driver",
          "slick.dbs.default.db.url" -> "jdbc:h2:mem:play;MODE=MYSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE",
          "slick.dbs.default.db.user" -> "",
          "slick.dbs.default.db.password" -> ""
        )
      )
      .build()
  }

  def clearDatabase(): Unit = {
    Try(dropTables())
    createTables()
  }

  private def createTables() = {
    // tables is the code generated tables that contain our schema
    Tables.schema.createStatements.toList.foreach { query =>
      awaitInf(
        db.run(SQLActionBuilder(List(query), SetUnit).asUpdate)
      )
    }
  }

  private def dropTables() = {
    Tables.schema.dropStatements.toList.reverse.foreach { query =>
      awaitInf(
        db.run(SQLActionBuilder(List(query), SetUnit).asUpdate)
      )
    }
  }

  def awaitInf[T](future: Future[T]): T = {
    val res: T = Await.result(future, Duration.Inf)
    res
  }

}
