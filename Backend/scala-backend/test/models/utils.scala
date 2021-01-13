package utils

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
// import slick.Tables
import slick.jdbc.SetParameter.SetUnit
import slick.jdbc.{JdbcProfile, SQLActionBuilder}
import models.Tables
import org.scalatest.TestSuite
import org.scalatestplus.play.guice.GuiceOneAppPerTest
// import utils.extensions._

import scala.util.Try

trait DatabaseCleanerOnEachTest
    extends HasDatabaseConfigProvider[JdbcProfile] // we need this so that we have access to the field 'db'
    with GuiceOneAppPerTest // we need this sot aht we have acces to the field 'app'
     {
       this: GuiceOneAppPerTest with TestSuite => 
       // this funny thing only allows the DAtabaseCleanerOnEachTest trait to be mixed into some class that also inherits from GuiceOneAppPerTest with TestSuite
       // this is an enforcement from GuceOneAppPerTest with this we basically push the check down in the hierarchy so that the compiler does not shit it's pants

  // with this we get a dbConfigProvider from the dependency injection of the testing. i.e. what kind of database we have depends on GuiceOneAppPerTest. Recall that we overwritte
  // the definition of that one somewhere else (i.e. the method fakeApplication)
  override lazy val dbConfigProvider: DatabaseConfigProvider =
    app.injector.instanceOf[DatabaseConfigProvider] // this seems to be the way to get dependencies in unit tests
    // app comes from GuiceOneAppPerTest
    // db comes from HasDatabaseConfigProvider
  

  def clearDatabase(): Unit = {
    Try(dropTables())
    createTables()
  }

  private def createTables() = {
    // tables is the code generated tables that contain our schema
    Tables.schema.createStatements.toList.foreach { query =>
      Await.result(db.run(SQLActionBuilder(List(query), SetUnit).asUpdate),  Duration.Inf)
    }
  }

  private def dropTables() = {
    Tables.schema.dropStatements.toList.reverse.foreach { query =>
      Await.result(db.run(SQLActionBuilder(List(query), SetUnit).asUpdate), Duration.Inf)
    }
  }

}