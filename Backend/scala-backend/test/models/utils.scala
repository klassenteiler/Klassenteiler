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
    extends HasDatabaseConfigProvider[JdbcProfile] 
    with GuiceOneAppPerTest
     {
       this: GuiceOneAppPerTest with TestSuite => 
       // this funny thing only allows the DAtabaseCleanerOnEachTest trait to be mixed into some class that also inherits from GuiceOneAppPerTest with TestSuite

  override lazy val dbConfigProvider: DatabaseConfigProvider =
    app.injector.instanceOf[DatabaseConfigProvider]
    // app comes from GuiceOneAppPerTest
    // db comes from HasDatabaseConfigProvider
  

  def clearDatabase(): Unit = {
    Try(dropTables())
    createTables()
  }

  private def createTables() = {
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