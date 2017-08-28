package model

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import scala.collection.{mutable, SortedSet}

/**
  * Created by Kotobotov.ru on 19.08.2017.
  */
case class User(first_name:String,last_name: String,birth_date: Long,gender: Char,id: Int,email: String){
  var visits = mutable.SortedSet.empty[Visit]
}

object User {
  implicit val userWriter = Json.writes[User]
  //implicit val userReader = Json.reads[User]
  implicit val userReader: Reads[User] = (
    (JsPath \ "first_name").read[String](maxLength[String](50)) and
    (JsPath \ "last_name").read[String](maxLength[String](50)) and
    (JsPath \ "birth_date").read[Long] and
    (JsPath \ "gender").read[Char].map{case a:Char if (a=='f')||(a=='m') => a} and
    (JsPath \ "id").read[Int] and
    (JsPath \ "email").read[String](email keepAnd maxLength[String](100))
    )(User.apply _ )

}