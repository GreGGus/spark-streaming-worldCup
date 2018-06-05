package main

/**
  * Created by Grégoire PORTIER.
  */

import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.log4j.{LogManager, Logger}


/**
  * Run MainClass with :
  *
  * ${SPARK_HOME}/bin/spark-submit \
  * --class main.MainClass \
  * --master yarn \
  * UBER.jar <package.class>
  */
object MainClass {

  //val spark: SparkSession = Setup.getSparkSession("Spark Jon")
  //val sc: SparkContext = spark.sparkContext
  val PROP: PropertiesConfiguration = new PropertiesConfiguration()
  val LOGGER: Logger = LogManager.getRootLogger


  def main(args: Array[String]) {


    //Launch Script in args

    implicit def newify[T](className: String) = Class.forName(className).newInstance.asInstanceOf[T]
    args.foreach{ nom_class =>
      val runJob:RunJob = nom_class;
      runJob.run()
    }
  }
}
