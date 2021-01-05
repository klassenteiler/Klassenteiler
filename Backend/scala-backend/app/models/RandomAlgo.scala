package models

import scala.collection.mutable.ListBuffer
import util.Random

object RandomAlgo extends PartitionAlgo{

  // Method takes students as Int-array of student-ids, Edges as Int-tuple-array and the maximum number of search iterations
  // Computes the best found partition of the given students, such that the number of edges between the two groups are minimal.
  // Returns best found partition as tuple of Int-Arrays containing the student-ids
  def computePartition(students: Array[Int], edges: Array[(Int, Int)]) : (Array[Int], Array[Int]) = {

    // Remove multiple equivalent edges
    val uniqueEdges = edges.toSet.toArray
    val nrSus :Int = students.length
    val sizefirstHalfOfPartition :Int = nrSus / 2
    //val sizesecondHalfOfPartition :Int = (nrSus + 1) / 2
    //val nrEdges :Int = uniqueEdges.length

    // Temporary partitions
    val firstHalfOfPartition = ListBuffer[Int]()
    val secondHalfOfPartition = ListBuffer[Int]()

    // best found partition
    var bestfirstHalfOfPartition = List[Int]()
    val bestsecondHalfOfPartition = ListBuffer[Int]()
    var minimumCuts :Int = Integer.MAX_VALUE

    // create 'searchIterations' different partitions
    for(_ <- 0 to searchIterations) {

      // random indices for first half of the partition
      val indices :Array[Int] = Array.fill[Int](sizefirstHalfOfPartition)(Integer.MAX_VALUE)

      firstHalfOfPartition.clear()
      secondHalfOfPartition.clear()

      // fill indices array with unique indices
      for(i <- 0 until sizefirstHalfOfPartition) {
        indices(i) = getRandomIndexWithExclusion(0, students.length, indices.sortWith(_ < _))
      }

      // partition process
      for(i <- 0 until students.length) {
        if(indices.contains(i)) firstHalfOfPartition.addOne(students(i)) else secondHalfOfPartition.addOne(students(i))
      }

      // check if the current partition is better than the best found partition so far
      val cuts :Int = sumEdgesForPartition(firstHalfOfPartition.toArray, secondHalfOfPartition.toArray, uniqueEdges)
      if(cuts < minimumCuts) {
        minimumCuts = cuts
        bestfirstHalfOfPartition = firstHalfOfPartition.toList
      }
    }

    // finally create the second half of best found partition based on the first half of best found partition.
    for(i <- 0 until students.length) {
      if(!bestfirstHalfOfPartition.contains(i)) bestsecondHalfOfPartition.addOne(students(i))
    }

    // return Int-tuple
    (bestfirstHalfOfPartition.toArray, bestsecondHalfOfPartition.toArray)
  }

  // Method computes a random number within ['start';'end') but excludes given values from 'exclude' Int-array
  def getRandomIndexWithExclusion(start: Int, end: Int, exclude: Array[Int]): Int = {
    var random :Int = start + Random.nextInt(end - start + 1 - exclude.length)
    for (ex <- exclude) {
      if (random < ex) return random
      random += 1
    }
    random
  }

  // Method takes first and second half of partition as Int-arrays and the edges as Int-tuple-array.
  // It computes the number of crossing edges (cuts) for the given partition.
  def sumEdgesForPartition(firstHalfOfPartition: Array[Int], secondHalfOfPartition: Array[Int], edges: Array[(Int, Int)]) : Int = {
    var cuts :Int = 0
    for (edge <- edges) {
      if (firstHalfOfPartition.contains(edge._1) && secondHalfOfPartition.contains(edge._2))
      {
        cuts += 1
      }
    }
    cuts
  }
}
