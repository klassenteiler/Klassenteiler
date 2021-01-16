package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import utils.MockDatabase
import scala.concurrent.Future
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._

/** Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class SchoolClassControllerSpec
    extends PlaySpec
    with MockDatabase
    with Injecting {

  // dbConfigProvider comes from MockDatabase, stubControllerComponents from Helpers
  val authenticationController =
    new AuthenticationController(dbConfigProvider, stubControllerComponents())
  val controller = new SchoolClassController(
    dbConfigProvider,
    stubControllerComponents(),
    authenticationController
  )

  "SchoolClassController" should {
    "return status 400 if no body is provided" in {
      val result: Future[Result] =
        controller.createSchoolClass().apply(FakeRequest())
      status(result) mustBe BadRequest.header.status //i.e. 400
    }
    "return status .. if format of body is wrong" in {
      val body: JsObject = Json.obj("value" -> "wrong")
      val request: FakeRequest[play.api.mvc.AnyContent] =
        FakeRequest().withJsonBody(body)
      println("och ne")
      val result: Future[Result] = controller.createSchoolClass().apply(request)
      status(result) mustBe UnsupportedMediaType.header.status //i.e. 400
    }

  }
}
