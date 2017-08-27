package model

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import scala.collection.mutable

/**
  * Created by Kotobotov.ru on 19.08.2017.
  */
case class Location(distance: Int, city: String, place: String, id: Int, country: String)

object Location {
  var allLocation = mutable.HashMap.empty[String, Int]
  implicit val locationWriter = Json.writes[Location]
  implicit val locationReader: Reads[Location] = (
    (JsPath \ "distance").read[Int] and
    (JsPath \ "city").read[String](maxLength[String](50)) and
    (JsPath \ "place").read[String] and
    (JsPath \ "id").read[Int] and
    (JsPath \ "country").read[String](maxLength[String](50))
    )(Location.apply _ )

  def encode(location: String) = {
    allLocation(location)
  }
}