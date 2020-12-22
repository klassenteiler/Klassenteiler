package models

final case class StudentCC(
    classId: Int,
    hashedName: String,
    encryptedName: String,
    selfReported: Boolean,
    groupBelonging: Int,
)
