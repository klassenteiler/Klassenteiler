import org.scalatest._
import org.scalatestplus.play._
import play.api.http.MimeTypes
import play.api.test._


import play.api.inject._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api._
import play.api.inject.guice._

// testing with databases
import play.api.db.Databases
import play.api.db.Database

// das grundgerüst ist aus https://www.playframework.com/documentation/2.8.x/ScalaFunctionalTestingWithScalaTest

                                        //alternative: GuiceOneAppPerTest 
class ExampleSpec extends PlaySpec with GuiceOneAppPerSuite {

  // Override fakeApplication if you need a Application with other than
  // default parameters.
  override def fakeApplication(): Application = {
    GuiceApplicationBuilder().configure(
      Map("ehcacheplugin" -> "disabled",
          "slick.dbs.test.driver" -> "slick.driver.H2Driver$",
          "slick.dbs.test.db.driver" -> "org.h2.Driver",
          "slick.dbs.test.db.url" -> "jdbc:h2:mem:")
    ).build()
  }

  // dies ist aus https://www.playframework.com/documentation/2.8.x/ScalaTestingWithDatabases
  def withMyDatabase[T](block: Database => T) = {
      Databases.withInMemory(
          name = "test",
          urlOptions = Map(
              "MODE" -> "POSTGRESQL"
          ),
          config = Map(
              "logStatements" -> true
          )
      )(block)
  }   

  // dass der wrapper so benutzt wird hab ich jetzt selbst abgeleitet, weil es nirgendwo explizit steht
  // ist aber ähnlich zu https://stackoverflow.com/questions/34858856/having-trouble-setting-up-in-memory-db-for-unit-tests-in-play-2-4-an-slick-3
  // habe den namen des tests und inhalt gelassen wie im beispiel, dann wird nämlich wenigstens getestet, ob das ändern der konfiguration
  // überhaupt funktionierte. nur das Hinzufügen des wrappers ist neu
  "The GuiceOneAppPerSuite trait" must {
    "provide an Application" in withMyDatabase{ database =>
      app.configuration.getOptional[String]("ehcacheplugin") mustBe Some("disabled")
    }
  }

// das wäre der kontextlose beispiel test
//   val appWithMemoryDatabase = new GuiceApplicationBuilder().configure(inMemoryDatabase("test")).build()
// "run an application" in new App(appWithMemoryDatabase) {

//   val Some(macintosh) = Computer.findById(21)

//   macintosh.name mustEqual "Macintosh"
//   macintosh.introduced.value mustEqual "1984-01-24"
// }

}