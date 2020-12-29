package models

final case class ClassTeacherCC(
    id: Int,
    encryptedPrivateKey: String,
    teacherSecret: String
)
