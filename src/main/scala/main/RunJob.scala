package main

import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.log4j.Logger

/**
  * Created by Grégoire PORTIER.
  */
abstract class RunJob{


  private val PROP : PropertiesConfiguration = MainClass.PROP
  private val LOGGER : Logger = MainClass.LOGGER

  def run();

}