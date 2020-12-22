package models

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext // the execution context is needed for concurrent execution
import models.Tables._
import scala.concurrent.Future

class RelationshipModel(db: Database)(implicit ec: ExecutionContext) {
  def createRelationship(relationshipCC: RelationshipCC): Future[Boolean] = ???

  def removeStudent(relationshipId: Int): Future[Boolean] = ???

  def getStudent(relationshipId: Int): Future[RelationshipCC] = ???
}
