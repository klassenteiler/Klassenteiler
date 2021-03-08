package models

final case class ClassCounts(
    openClasses: Int,
    closedClasses: Int,
    calculatingClasses: Int,
    doneClasses: Int
)