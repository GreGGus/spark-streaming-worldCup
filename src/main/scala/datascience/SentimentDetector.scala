package datascience
import java.util.Properties

import edu.stanford.nlp.pipeline.StanfordCoreNLP

import scala.collection.mutable.ListBuffer

class SentimentDetector {

  val nlp_props = new Properties()
  nlp_props.setProperty("annotators", "tokenize, ssplit, sentiment")

  def run(message: String): String = {

    // NLP properties and pipelone creation
    val pipeline = new StanfordCoreNLP(nlp_props)


    // Get annotation
    val annotation = pipeline.process(message)



  }
}
