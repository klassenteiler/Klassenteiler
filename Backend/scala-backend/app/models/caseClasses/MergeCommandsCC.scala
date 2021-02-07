package models

final case class MergeCommandsCC(
    isAliasOf: Seq[Tuple2[Int,String]],
    studentsToAdd: Seq[StudentCC],
    studentsToDelete: Seq[Int],
    studentsToRename: Seq[StudentCC]
)