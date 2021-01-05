package models

trait PartitionAlgo {
  val searchIterations: Int = 10000000
  def computePartition(students: Array[Int], edges: Array[(Int, Int)]): (Array[Int], Array[Int])
}