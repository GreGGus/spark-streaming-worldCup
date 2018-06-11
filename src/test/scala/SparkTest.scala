import com.holdenkarau.spark.testing.{RDDComparisons, SharedSparkContext, StreamingSuiteBase}
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream._
import org.junit.runner.RunWith
import org.scalactic.Equality
import org.scalatest.exceptions.TestFailedException
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner


class SparkTest extends FunSuite with SharedSparkContext with RDDComparisons {

//  println("START TEST")
//  System.setProperty("hadoop.home.dir", "C:\\hadoop-2.6.5")
//
//  test("test RDDComparisons") {
//    val expectedRDD = sc.parallelize(Seq(1, 2, 3))
//    val resultRDD = sc.parallelize(Seq(3, 2, 1))
//
//    assert(None === compareRDD(expectedRDD, resultRDD)) // succeed
//    assert(None === compareRDDWithOrder(expectedRDD, resultRDD)) // Fail
//
//    assertRDDEquals(expectedRDD, resultRDD) // succeed
//    assertRDDEqualsWithOrder(expectedRDD, resultRDD) // Fail
//  }
}
