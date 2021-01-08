package models

final case class SchoolClassCC(
    id: Option[Int],
    className: String,
    schoolName: Option[String],
    classSecret: String,
    publicKey: String,
    surveyStatus: Option[Int]
)

object SurveyStatus extends Enumeration {
  type status = Int

  val Open          = 0
  val Closed        = 1
  val Calculating   = 2
  val Done          = 3
  val Uninitialized = null
}