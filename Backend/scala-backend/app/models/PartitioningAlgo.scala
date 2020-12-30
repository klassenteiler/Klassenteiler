package models

import scala.collection.mutable.ListBuffer
import util.Random

object PartitioningAlgo {
  def computePartition(students: Array[Int], edges: Array[(Int, Int)], searchIterations: Int) : (Array[Int], Array[Int]) = {
    val nrSus = students.length
    val sizeFirstPartition = nrSus / 2
    //val sizeSecondPartition = (nrSus + 1) / 2
    //val nrEdges = edges.length

    val firstPartition = ListBuffer[Int]()
    val secondPartition = ListBuffer[Int]()

    var minimumCuts = Integer.MAX_VALUE
    var bestFirstPartition = List[Int]()
    val bestSecondPartition = ListBuffer[Int]()

    for(_ <- 0 to searchIterations) {
      val indices = Array.fill[Int](sizeFirstPartition)(Integer.MAX_VALUE)
      firstPartition.clear()
      secondPartition.clear()

      for(i <- 0 until sizeFirstPartition) {
        indices(i) = getRandomIndexWithExclusion(0, students.length, indices.sortWith(_ < _))
      }

      for(i <- 0 until students.length) {
        if(indices.contains(i)) firstPartition.addOne(students(i)) else secondPartition.addOne(students(i))
      }

      val cuts = sumEdgesForPartition(firstPartition.toArray, secondPartition.toArray, edges)
      if(cuts < minimumCuts) {
        minimumCuts = cuts
        bestFirstPartition = firstPartition.toList
      }
    }

    for(i <- 0 until students.length) {
      if(!bestFirstPartition.contains(i)) bestSecondPartition.addOne(students(i))
    }
    (bestFirstPartition.toArray, bestSecondPartition.toArray)
  }

  def getRandomIndexWithExclusion(start: Int, end: Int, exclude: Array[Int]): Int = {
    var random = start + Random.nextInt(end - start + 1 - exclude.length)
    for (ex <- exclude) {
      if (random < ex) return random
      random += 1
    }
    random
  }

  def sumEdgesForPartition(firstPartition: Array[Int], secondPartition: Array[Int], edges: Array[(Int, Int)]) : Int = {
    var cuts = 0
    for (edge <- edges) {
      if (firstPartition.contains(edge._1) && secondPartition.contains(edge._2) ||
        secondPartition.contains(edge._1) && firstPartition.contains(edge._2))
        {
          cuts += 1
        }
    }
    cuts
  }
}
