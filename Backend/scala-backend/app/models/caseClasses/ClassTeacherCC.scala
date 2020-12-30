package models

final case class ClassTeacherCC(
    id: Option[Int], //this is the classId
    encryptedPrivateKey: String,
    teacherSecret: String
)
