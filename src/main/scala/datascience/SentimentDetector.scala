package datascience

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations

import scala.collection.mutable.ListBuffer

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import java.util.Properties


object SentimentDetector extends java.io.Serializable {

  val nlp_props = new Properties()
  nlp_props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment")

  def run(message: String): (Double,String) = {

    // NLP properties and pipelone creation
    val pipeline = new StanfordCoreNLP(nlp_props)

    // Get annotation
    val annotations = pipeline.process(message)


    // Variables NLP
    var listSentiments: ListBuffer[Double] = ListBuffer()
    var ListsizesWord: ListBuffer[Int] = ListBuffer()



    // Every word in tweet get score.
    for (tweetWord <- annotations.get(classOf[CoreAnnotations.SentencesAnnotation])) {
      val tree = tweetWord.get(classOf[SentimentCoreAnnotations.AnnotatedTree])
      val tweetSentimentInt = RNNCoreAnnotations.getPredictedClass(tree)
      val tweetWordString = tweetWord.toString

      //  Add sentiment & word size into global senitment tweet.
      listSentiments += tweetSentimentInt.toDouble
      ListsizesWord += tweetWordString.length


    }

    // TODO Multiplier size = snetiment

    // Calculate total sentiment
    val sumSentimentWithLenghtList = (listSentiments, listSentiments)
      .zipped.map((sentimentInt, sizeWord) => sentimentInt * sizeWord)

    val sumSentiment = listSentiments.sum // Get sum of sentiment score
    val sumSentimentWithLenght = sumSentimentWithLenghtList.sum // Get sum of (sentiment * sentiment.size) score

    var weightedSentiment = sumSentimentWithLenght / sumSentiment // Final score



   val sentiment = if (weightedSentiment <= 0.0)
      "NOT_UNDERSTOOD"
    else if (weightedSentiment < 1.6)
      "NEGATIVE"
    else if (weightedSentiment <= 2.0)
      "NEUTRAL"
    else if (weightedSentiment < 5.0)
      "POSITIVE"
    else "NOT_UNDERSTOOD"
    (weightedSentiment,sentiment)
  }
}


