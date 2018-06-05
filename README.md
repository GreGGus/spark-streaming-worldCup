# Spark Streaming Twitter for World Cup NLP

In progress ...



Scrap Twitter Streaming API with Hashtag world cup filter.
Each tweets is used to analyse sentiment Standford with Spark.
Results are indexed into ElasticSearch


### Requierements

Spark2.2
ElasticSearch 5.6.3
apache.bahir.streaming (TwitterStream & Twitter4j)
Standford NLP


### Installation with Maven UBER JAR

> git clone
> cd spark-kstreaming-worldCup
> mvn install
> mvn package

> BUILD SUCCESS

Uber Jar with deps : spark-streaming-worldCup-1.0-SNAPSHOT-UBER.jar


### Test

In progress ..

### Running in Spark StandAlone

Our MainClass is main.MainClass which run class received in args.

> spark-submit --class main.MainClass --master local[*] spark-streaming-worldCup-1.0-SNAPSHOT-UBER.jar streaming.RunStreaming






