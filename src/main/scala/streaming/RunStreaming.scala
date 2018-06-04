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


    System.setProperty("twitter4j.oauth.consumerKey", "Qik97WOKFcprdDsn8gIxIRNBq")
    System.setProperty("twitter4j.oauth.consumerSecret", "5uySxUisYq5TpI5gF1yyT0LFhFvC3K7kgl0TH0iQLlS2br7c3e")
    System.setProperty("twitter4j.oauth.accessToken", "2563228074-BFFvpLCadTVTncSu5Gf4xFAJLuUnKMYm26gjBNn")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "vQG4NJdDpAH6C4332NMw4bJ5j0G230GD2zzFVqfyJYI7u")


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
