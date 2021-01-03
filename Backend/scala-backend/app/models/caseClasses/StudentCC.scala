package models

final case class StudentCC(
    id: Option[Int],
    // classId: Option[Int] in database
    hashedName: String,
    encryptedName: String,
    selfReported: Boolean,
    groupBelonging: Option[Int],
)
