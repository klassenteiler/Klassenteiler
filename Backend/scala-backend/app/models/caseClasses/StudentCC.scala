package models

final case class StudentCC(
    classId: Option[Int],
    hashedName: String,
    encryptedName: String,
    selfReported: Boolean,
    groupBelonging: Option[Int],
)
