package streaming

import main.{MainClass, RunJob}
import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.elasticsearch.spark._


class RunStreaming extends RunJob {
  private val PROP: PropertiesConfiguration = MainClass.PROP


  def run(): Unit = {

    // SparkConf with ES connexion
    val conf = new SparkConf()
      .setAppName("Spark Streaming World-Cup")
      .set("es.nodes","localhost")
      .set("es.port","9200")


    System.setProperty("twitter4j.oauth.consumerKey", "")
    System.setProperty("twitter4j.oauth.consumerSecret", "")
    System.setProperty("twitter4j.oauth.accessToken", "")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "")


    // Streaming Context for Twitter4J
    val ssc = new StreamingContext(conf, Seconds(13))


    // Get json object from twitter stream with hashtag filter
    val worldCupHasthtag=Array("fifaworldcup", "WorldCup ")
    val tweets = TwitterUtils.createStream(ssc, None, worldCupHasthtag)

    tweets.foreachRDD { (rdd, time) =>
      rdd.map(t => {
        Map(
          "user" -> t.getUser.getScreenName,
          "created_at" -> t.getCreatedAt.getTime.toString,
          "location" -> Option(t.getGeoLocation).map(geo => {
            s"${geo.getLatitude},${geo.getLongitude}"
          }),
          "text" -> t.getText,
          "hashtags" -> t.getHashtagEntities.map(_.getText),
          "retweet" -> t.getRetweetCount,
          "language" -> t.getLang.toString(),
          "sentiment" -> "todo"         )
      }).saveToEs("test/tweet")
    }


    ssc.start()
    ssc.awaitTermination()

  }
}
