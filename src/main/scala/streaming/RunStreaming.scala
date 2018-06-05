package streaming

import main.{MainClass, RunJob}
import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.elasticsearch.spark._

import datascience.SentimentDetector

class RunStreaming extends RunJob {
  private val PROP: PropertiesConfiguration = MainClass.PROP


  object ConversionUtils extends Serializable {
    // quoted_status_truncated
    import org.apache.spark.sql.functions._

    def convert: (String => String) = {
      case null => ""
      case tweet => tweet.replace("\n", "")
    }
    def nerValueUdf=udf((oldV:String,newV:String)=>{
      var value =""
      if(newV==""){
        value=oldV
      }else{value=newV}
      value
    })

    val convertUdf = udf(convert)

    val regex_emoji = "(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|[\uD83C\uDD70\uD83C\uDD71\uD83C\uDD7E\uD83C\uDD7F\uD83C\uDD8E\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|[\u0023\u002A\u0030-\u0039]\uFE0F?\u20E3|[\u2194-\u2199\u21A9-\u21AA]\uFE0F?|[\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55]\uFE0F?|[\u2934\u2935]\uFE0F?|[\u3030\u303D]\uFE0F?|[\u3297\u3299]\uFE0F?|[\uD83C\uDE01\uD83C\uDE02\uD83C\uDE1A\uD83C\uDE2F\uD83C\uDE32-\uD83C\uDE3A\uD83C\uDE50\uD83C\uDE51]\uFE0F?|[\u203C\u2049]\uFE0F?|[\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE]\uFE0F?|[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|[\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA]\uFE0F?)"

    def cleanTweet: (String => String) = { line: String =>
       line.replaceAll("RT [@]\\w*:\\s", "")
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
    }

    val cleanTweetUdf = udf(cleanTweet)


  }



  def run(): Unit = {

    // SparkConf with ES connexion
    val conf = new SparkConf()
      .setAppName("Spark Streaming World-Cup")
      .set("es.nodes","localhost")
      .set("es.port","9200")



    // Streaming Context for Twitter4J
    val ssc = new StreamingContext(conf, Seconds(30))


    // Get json object from twitter stream with hashtag filter
    val worldCupHasthtag=Array("fifaworldcup", "WorldCup ")
    val tweets = TwitterUtils.createStream(ssc, None, worldCupHasthtag)

    tweets.foreachRDD { (rdd, time) =>
      println("==================== BATCH SIZE           : ",rdd.count())
      rdd.map(t => {
        println("t.getText",t.getText)
        val k = SentimentDetector.run(t.getText)
        println("sum score",k)

        Map(
          "user" -> t.getUser.getScreenName,
          "created_at" -> t.getCreatedAt.getTime.toString,
          "location" -> Option(t.getGeoLocation).map(geo => {
            s"${geo.getLatitude},${geo.getLongitude}"
          }),
          "text" -> t.getText,
          "hashtags" -> t.getHashtagEntities.map(_.getText),
          "retweet" -> t.getRetweetCount,
          "language" -> t.getLang,
          "sentiment" -> "todo",
          "sentiment_score" -> SentimentDetector.run(t.getText)
        )
      }).saveToEs("worldcup/tweet")
      println("INDEX ES SEEMS OK")
    }


    ssc.start()
    ssc.awaitTermination()

  }
}