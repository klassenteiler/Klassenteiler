package models

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import utils.DatabaseCleanerOnEachTest
import play.api.db.slick.DatabaseConfigProvider

class AmIidiotSpec extends PlaySpec with DatabaseCleanerOnEachTest{
    "am I idiot" should {
        "fail" in {
            0 mustBe 1
        }
    }
}