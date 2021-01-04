package models

import org.scalatestplus.play._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class PartitioningAlgoTest extends PlaySpec {
  "Partitioning algorithm" should {

   "8 SuS to [0,1,2,3] and [4,5,6,7] " in {
     val nrSus = 8
     var susSet = (0 until nrSus).toArray
     var edges = Array(
       (0, 1), (0, 2), (0, 3),
       (2, 3),
       (3, 2),
       (5, 6),
       (6, 5),
       (7, 6)
     )
     var partition = IterativeAlgo.computePartition(susSet, edges.toArray)
     println("first group: " + partition._1.mkString(" "))
     println("second group: " + partition._2.mkString(" "))
     println("(One Group should be [0, 1, 2, 3])")

     val firstMatches :Boolean = (partition._1.contains(0) && partition._1.contains(1)
       && partition._1.contains(2) && partition._1.contains(3))

     val secondMatches :Boolean = (partition._2.contains(0) && partition._2.contains(1)
       && partition._2.contains(2) && partition._2.contains(3))

     (firstMatches || secondMatches) mustBe true
   }

    "12 SuS to [0,2,4,6,8,10] and [1,3,5,7,9,11] " in {
      val nrSus = 12
      var susSet = (0 until nrSus).toArray
      var edges = Array(
        (0, 2),
        (2, 4),
        (4, 6),
        (6, 8),
        (8, 10),
        (10, 0)
      )
      var partition = IterativeAlgo.computePartition(susSet, edges.toArray)
      println("first group: " + partition._1.mkString(" "))
      println("second group: " + partition._2.mkString(" "))
      println("(One Group should be [0, 2, 4, 6, 8, 10])")

      val firstMatches :Boolean = (partition._1.contains(0) && partition._1.contains(2)
        && partition._1.contains(4) && partition._1.contains(6) && partition._1.contains(8)
        && partition._1.contains(10))

      val secondMatches :Boolean = (partition._2.contains(0) && partition._2.contains(2)
        && partition._2.contains(4) && partition._2.contains(6) && partition._2.contains(8)
        && partition._2.contains(10))

      (firstMatches || secondMatches) mustBe true
    }

    //Test not deterministic because of random generated edges. Uncomment to test large classes.
    // Adjust for your purpose
    "20 SuS - random edges " in {
      // be careful with high numbers 27 -->~ 8 Minutes, 30 --> 20 Minutes
      val nrSus = 20
      var susSet = (0 until nrSus).toArray
      var adjacentMatrix = Array.tabulate(nrSus,nrSus)(computeRandomCellValue)
      var edges :Array[(Int, Int)] = createEdges(adjacentMatrix, nrSus)
      var partition = IterativeAlgo.computePartition(susSet, edges.toArray)

      true mustBe true // Adjust for your purpose!
    }
  }

  // Helper method to compute a random
  def computeRandomCellValue(i: Int, j: Int): Boolean =
    (math.random < 0.25)

  // Helper method to create edges based on a adjacency matrix
  def createEdges(adjacencyMatrix :Array[Array[Boolean]], nrSus :Int): Array[(Int, Int)] = {
    val edges: ListBuffer[(Int, Int)] = ListBuffer()
    var i, j = 0
    for (i <- 0 until nrSus) {
      {
        for (j <- 0 until nrSus) {
          if (i != j && adjacencyMatrix(i)(j)) {
            edges.addOne((i, j))
          }
        }
      }
    }
    edges.toArray
  }
}