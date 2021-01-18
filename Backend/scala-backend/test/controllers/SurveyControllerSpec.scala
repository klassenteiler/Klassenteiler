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
import models._

/** Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class SurveyControllerSpec extends PlaySpec with MockDatabase with Injecting {

  // dbConfigProvider comes from MockDatabase, stubControllerComponents from Helpers
  val authenticationController =
    new AuthenticationController(dbConfigProvider, stubControllerComponents())
  val controller = new SurveyController(
    dbConfigProvider,
    stubControllerComponents(),
    authenticationController
  )

  "SurveyController /submitStudentSurvey" should {
    "return status 201 and contain message if request is correct" in {}
    "return status 403 if student with that name already exists" in {}
    "return status 400 if too many friends are tried to be added" in {}
    "return status 415 if json body is in wrong format" in {}
    "return status 400 if no body is provided" in {}
    "return status 410 if the survey of the class is in wrong status" in {}
    "return status 404 if classSecret or id is wrong" in {}
    "return status 401 if teacherSecret is wrong" in {}
    "return status 400 if no teacherSecret is provided" in {}
  }

  "SurveyController /closeSurvey" should {
    "return status 200, contain message and update status if request is correct" in {}
    "return status 410 if the survey of the class is in wrong status" in {}
    "return status 404 if classSecret or id is wrong" in {}
    "return status 401 if teacherSecret is wrong" in {}
    "return status 400 if no teacherSecret is provided" in {}
  }

  "SurveyController /startCalculating" should {
    "return status 200, contain message and update statuses if request is correct" in {}
    "return status 410 if the survey of the class is in wrong status" in {}
    "return status 404 if classSecret or id is wrong" in {}
    "return status 401 if teacherSecret is wrong" in {}
    "return status 400 if no teacherSecret is provided" in {}
  }

    "SurveyController /getResults" should {
    "return status 200 and array of students if request is correct" in {}
    "return status 409 if the survey of the class is already closed" in {}
    "return status 404 if classSecret or id is wrong" in {}
    "return status 401 if teacherSecret is wrong" in {}
    "return status 400 if no teacherSecret is provided" in {}
  }

}
