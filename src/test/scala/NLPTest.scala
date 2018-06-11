import org.scalatest.{FunSpec, Matchers}
import datascience.SentimentDetector
import streaming.Country

class NLPTest extends FunSpec with Matchers {
 describe("test sentiment nlp") {
  it("doit avoir le sentiment : POSITIVE") {
    val input = "On a super équipe, vive la France !"
    val sentiment =  SentimentDetector.run(input)._1
    sentiment should be(3)
  }
 }

  describe("test team matching ") {
    it("La France a perdu ce soir ..") {
      val input = "On a super équipe, vive la France !"
      val sentiment =  Country.findCountryToScore(input)
      sentiment should be("France")
    }
  }
}
