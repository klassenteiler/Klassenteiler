package models

final case class ClassTeacherCC(
    id: Option[Int],
    encryptedPrivateKey: String,
    teacherSecret: String
)
