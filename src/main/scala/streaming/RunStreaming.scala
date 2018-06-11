package streaming

import main.{MainClass, RunJob}
import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.elasticsearch.spark._
import datascience.SentimentDetector

import scala.collection.mutable.ListBuffer


object Country extends Serializable {
  val wordCupTeam: Set[String] = Set("argentina", "australia", "belgium", "brazil",
    "colombia", "costar rica", "croatia", "denmark", "egypt", "england", "france", "germany",
    "iceland", "iran", "japan", "korea", "mexico", "morocco", "nigeria", "panama",
    "peru", "poland", "portugal", "russia", "saudi arabia", "senegal", "serbia", "spain", "sweden", "switzerland", "tunisia", "uruguay")

  def findCountryToScore(tweet: String): String = {
    var teamList: ListBuffer[String] = ListBuffer()

    tweet.split(" ").exists(word => {
      if (wordCupTeam contains word.toLowerCase) {
        teamList += word
        true
      } else false
    })
    val result = if (teamList.isEmpty) " " else teamList.head
    result
  }
}

class RunStreaming extends RunJob {
  private val PROP: PropertiesConfiguration = MainClass.PROP


  def run(): Unit = {

    // SparkConf with ES connexion
    val conf = new SparkConf()
      .setAppName("Spark Streaming World-Cup")
      .set("es.nodes", "localhost")
      .set("es.port", "9200")




    // Streaming Context for Twitter4J
    val ssc = new StreamingContext(conf, Seconds(120))


    // Get json object from twitter stream with hashtag filter
    val worldCupHasthtag = Array("fifaworldcup", "WorldCup")
    val tweets = TwitterUtils.createStream(ssc, None, worldCupHasthtag)

    val regex_emoji = "(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|[\uD83C\uDD70\uD83C\uDD71\uD83C\uDD7E\uD83C\uDD7F\uD83C\uDD8E\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|[\u0023\u002A\u0030-\u0039]\uFE0F?\u20E3|[\u2194-\u2199\u21A9-\u21AA]\uFE0F?|[\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55]\uFE0F?|[\u2934\u2935]\uFE0F?|[\u3030\u303D]\uFE0F?|[\u3297\u3299]\uFE0F?|[\uD83C\uDE01\uD83C\uDE02\uD83C\uDE1A\uD83C\uDE2F\uD83C\uDE32-\uD83C\uDE3A\uD83C\uDE50\uD83C\uDE51]\uFE0F?|[\u203C\u2049]\uFE0F?|[\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE]\uFE0F?|[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|[\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA]\uFE0F?)"


    tweets.foreachRDD { (rdd, time) =>
      println("==================== BATCH SIZE           : ", rdd.count())
      rdd.map(t => {
        var team: String = ""

        val tweetClean = t.getText.replaceAll("RT [@]\\w*:\\s", "")
          .replaceAll("#", "")
          .replaceAll(",", "")
          .replaceAll("\n", "")
          .replaceAll("|", "")
          .replaceAll("&lt", "")
          .replaceAll("&amp", "")
          .replaceAll("\\@\\S*", "")
          .replaceAll("https://t.co/\\w*", "")
          .replaceAll(regex_emoji, "")
          .trim()
        print("=========  tweetClean ", tweetClean)

        val tweetTeam = Country.findCountryToScore(tweetClean)

        val sentimentResultTuple =SentimentDetector.run(tweetClean)

        Map(
          "user" -> t.getUser.getScreenName,
          "created_at" -> t.getCreatedAt.getTime.toString,
          "location" -> Option(t.getGeoLocation).map(geo => {
            s"${geo.getLatitude},${geo.getLongitude}"
          }),
          "text" -> tweetClean,
          "hashtags" -> t.getHashtagEntities.map(_.getText),
          "retweet" -> t.getRetweetCount,
          "language" -> t.getLang,
          "sentiment" -> sentimentResultTuple._2,
          "team" -> tweetTeam,
          "sentiment_score" -> sentimentResultTuple._1
        )
      }).saveToEs("worldcup/tweet")
    }


    ssc.start()
    ssc.awaitTermination()

  }
}