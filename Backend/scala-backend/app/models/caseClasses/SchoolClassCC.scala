package models

final case class SchoolClassCC(
    id: Option[Int],
    className: String,
    schoolName: Option[String],
    classSecret: String,
    publicKey: String,
    teacherSecret: String,
    encryptedPrivateKey: String,
    surveyStatus: Option[Int]
)
