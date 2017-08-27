package model

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/**
  * Created by Kotobotov.ru on 19.08.2017.
  */
case class Visit(user: Int,location: Int,visited_at: Long,id: Int,mark: Int)

object Visit {
  implicit val ordering = Ordering.fromLessThan[Visit](_.visited_at < _.visited_at)
  implicit val visitWriter = Json.writes[Visit]
  implicit val visitReader: Reads[Visit] = (
    (JsPath \ "user").read[Int] and
    (JsPath \ "location").read[Int] and
    (JsPath \ "visited_at").read[Long] and
    (JsPath \ "id").read[Int] and
    (JsPath \ "mark").read[Int](min[Int](0) keepAnd max[Int](5))
    )(Visit.apply _ )
}