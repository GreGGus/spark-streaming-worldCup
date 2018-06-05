import org.scalatest.{FunSpec, Matchers}
import datascience.SentimentDetector

class NLPTest extends FunSpec with Matchers {
 describe("test nlp") {
  it("doit avoir le sentiment : POSITIVE") {
    val input = "On a super équipe, vive la France !"
    val sentiment =  SentimentDetector.run(input)
    sentiment should be(3)
  }
 }

}
