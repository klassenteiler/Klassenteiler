package models

import scala.collection.mutable.ListBuffer
import util.Random
import org.apache.commons.math3.util.CombinatoricsUtils

object IterativeAlgo extends PartitionAlgo {

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

    var cnt: Int = 0
    var iterator: java.util.Iterator[Array[Int]] = CombinatoricsUtils.combinationsIterator(nrSus, nrSus/2)
    while(iterator.hasNext()) {
      var nextCombination: Array[Int] = iterator.next()
      firstHalfOfPartition.clear()
      secondHalfOfPartition.clear()

      // partition process (for this iteration)
      for(i <- 0 until students.length) {
        if(nextCombination.contains(i)) firstHalfOfPartition.addOne(students(i)) else secondHalfOfPartition.addOne(students(i))
      }

      // check if the current partition is better than the best found partition so far
      val cuts :Int = sumEdgesForPartition(firstHalfOfPartition.toArray, secondHalfOfPartition.toArray, uniqueEdges)
      println("Combination: " + firstHalfOfPartition.mkString(" "))
      println("cuts: " + cuts)

      if(cuts < minimumCuts) {
        minimumCuts = cuts
        bestfirstHalfOfPartition = firstHalfOfPartition.toList
      }
    }

    // finally create the second half of best found partition based on the first half of best found partition.
    for(studentDBid <- students) {
      if(!bestfirstHalfOfPartition.contains(studentDBid)) bestsecondHalfOfPartition.addOne(studentDBid)
    }

    // return Int-tuple
    (bestfirstHalfOfPartition.toArray, bestsecondHalfOfPartition.toArray)
  }

  // Method takes first and second half of partition as Int-arrays and the edges as Int-tuple-array.
  // It computes the number of crossing edges (cuts) for the given partition.
  def sumEdgesForPartition(firstHalfOfPartition: Array[Int], secondHalfOfPartition: Array[Int], edges: Array[(Int, Int)]) : Int = {
    var cuts :Int = 0
    println(edges.mkString(", "))
    for (edge <- edges) {
      if ((firstHalfOfPartition.contains(edge._1) && secondHalfOfPartition.contains(edge._2)) ||
        (firstHalfOfPartition.contains(edge._2) && secondHalfOfPartition.contains(edge._1))) {
        cuts += 1
        println("++")
      }
    }
    cuts
  }
}
