package models

final case class SchoolClassCC(
    id: Int,
    className: String,
    schoolName: Option[String],
    classSecret: String,
    publicKey: String,
    teacherSecret: String,
    encryptedPrivateKey: String,
    surveyStatus: Option[Int]
)
