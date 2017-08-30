package utils

/**
  * Created by Kotobotov.ru on 30.08.2017.
  */
object AgeConverter {
val currentTime = System.currentTimeMillis()/1000
val secondsPerYaer = 31471200L
val additor = 86400L
 def toUnixTime(input:Long)={
  //currentTime-(secondsPerYaer*input)+(additor*((input+2)/4))
  currentTime-(secondsPerYaer*input)
 }
}
