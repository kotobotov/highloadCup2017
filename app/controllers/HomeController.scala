package controllers

import java.io.File
import javax.inject._

import model._
import play.api.{Logger, NoHttpFiltersComponents}
import play.api.libs.json.{JsError, JsObject, Json, _}
import play.api.libs.json.Reads._
import play.api.mvc._
import utils.{AgeConverter, FileWork}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable
import scala.collection.parallel.mutable.ParSeq
import scala.concurrent.Future

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with NoHttpFiltersComponents {

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  ///root/public/javascripts/


  FileWork.unZipIt("/tmp/data/data.zip", "/root/resource/")

  val files = if (new File("/tmp/data/data.zip" ).exists()) FileWork.getListOfFiles("/root/resource/").par
              else FileWork.getListOfFiles("C:\\inetpub\\play\\Obuchenie\\hlcupdocs\\data\\FULL\\data").par

Logger.info("file exist: "+ new File("/tmp/data/data.zip" ).exists())
//Logger.info("get data from - "+ files.toString())
val currentTime = System.currentTimeMillis()
  val users = new mutable.OpenHashMap[Int,User](1100000)
    files.filter(_.getName.contains("users"))
                              .foreach{ file => (Json.parse( scala.io.Source.fromFile(file)("UTF-8").getLines.mkString) \ "users")
                                                .as[ParSeq[User]]
                                                .foreach(item => users.put(item.id, item)) }
  Logger.info("total users: "+ users.size + "time: "+ {(currentTime - System.currentTimeMillis())/1000})

  def toInt(s: String): Int = {
    {try {
      Some(s.toInt)
    } catch {
      case e: Exception => None
    }}.getOrElse(0)
  }

  val locations = new mutable.OpenHashMap[Int,Location](900000)
  files.filter(_.getName.contains("locations"))
  .foreach{ file => (Json.parse( scala.io.Source.fromFile(file)("UTF-8").getLines.mkString) \ "locations")
                    .as[ParSeq[Location]]
                    .foreach(item => locations.put(item.id, item)) }
  Logger.info("localions users: "+ locations.size+ "time: "+ {(currentTime - System.currentTimeMillis())/1000})



  val visits = new mutable.OpenHashMap[Int,Visit](10100000)
  files.filter(_.getName.contains("visits"))
  .foreach{ file => (Json.parse( scala.io.Source.fromFile(file)("UTF-8").getLines.mkString) \ "visits")
                    .as[ParSeq[Visit]]
                    .foreach(item => visits.put(item.id, item))
  }

  Logger.info("visits: "+ visits.size+ "time: "+ {(currentTime - System.currentTimeMillis())/1000})


  var visitsByUser = new mutable.OpenHashMap[Int, mutable.SortedSet[Visit]](1100000)
  var visitsByLocation = new mutable.OpenHashMap[Int, mutable.SortedSet[Visit]](900000)




  visits.foreach { case (id, visit) =>
    if (visitsByLocation.contains(visit.location)) visitsByLocation(visit.location).+=(visit) else visitsByLocation.put(visit.location, mutable.SortedSet(visit))
    if (visitsByUser.contains(visit.user)) visitsByUser(visit.user).+=(visit) else visitsByUser.put(visit.user, mutable.SortedSet(visit))
  }
  Logger.info("dobavili visitBy loc i users - time: "+ {(currentTime - System.currentTimeMillis())/1000})

  def userInfo(id: String) = Action {
    val myId = toInt(id)
    if (myId>0 && users.contains(myId)) Ok(Json.toJson(users(myId)))
    else NotFound("")
  }

  def locationInfo(id: String) = Action {
    val myId = toInt(id)
    if (myId>0 &&locations.contains(myId)) Ok(Json.toJson(locations(myId)))
    else NotFound("")
  }

  def visitInfo(id: String) = Action {
    val myId = toInt(id)
    if (myId>0 &&visits.contains(myId)) Ok(Json.toJson(visits(myId)))
    else NotFound("")
  }

  def userVisits(id: String) = Action { implicit request: Request[AnyContent] =>
    val param = request.queryString.map(item => (item._1, item._2.head))
    //Logger.info(param.toString)
    val myId = toInt(id)
    if (myId==0 || !users.contains(myId)) NotFound("") else {
      if (checkGenderError(param)) BadRequest("") else {
      val otvet = if (visitsByUser.contains(myId)) visitsByUser(myId) else mutable.SortedSet.empty[Visit]


      def parseArgument(par: List[(String, String)])(data: mutable.SortedSet[Visit]): mutable.SortedSet[Visit] = par match {
        case x :: xs => parseArgument(xs)(eval(x, data))
        case Nil     => data
      }

      def eval(inputPar: (String, String), data: mutable.SortedSet[Visit]) = inputPar match {
        case ("fromDate", startDate: String)                                        =>val date=startDate.toLong
          data.dropWhile(_.visited_at <= date)
        case ("toDate", endDate: String)                                            => val date = endDate.toLong
          data.takeWhile(_.visited_at < date )
        case ("country", country: String)  =>   data.filter(item => locations(item.location).country == country)
        case ("toDistance", distance: String)                                       => val dist = distance.toLong
          data.filter(item => locations(item.location).distance < dist)
        //case _                                                                      => data
      }


      val finalResult =  parseArgument(param.toList)(otvet).toSeq
      Ok(
        """{"visits":[""".stripMargin + {if(finalResult.size > 0) finalResult
                                                                  .map(item => raw"""{"mark": ${item.mark }, "visited_at": ${item.visited_at }, "place":"${locations(item.location).place }" }""").mkString(",")  else ""}+ "]}")
    }}
  }


  def checkGenderError(param: Map[String, String]): Boolean = {
  if (param.contains("gender")) { param("gender") match {
      case ("m") => false
      case ("f") => false
      case (_) => true
    }} else false
  }

  def averageEvaluation(id: String) = Action { implicit request: Request[AnyContent] =>

    val param = request.queryString.map(item => (item._1, item._2.head))
    //Logger.info(param.toString)


    val myId = toInt(id)
    if (myId==0 || !locations.contains(myId)) NotFound("") else {
      if (checkGenderError(param)) BadRequest("") else {
        val otvet = if (visitsByLocation.contains(myId)) visitsByLocation(myId) else mutable.SortedSet.empty[Visit]


        def parseArgument(par: List[(String, String)])(data: mutable.SortedSet[Visit]): mutable.SortedSet[Visit] = par match {
          case x :: xs => parseArgument(xs)(eval(x, data))
          case Nil     => data
        }

        def eval(inputPar: (String, String), data: mutable.SortedSet[Visit]) = inputPar match {
          case ("fromDate", startDate: String) => val date = startDate.toLong
            data.dropWhile(_.visited_at <= date)
          case ("toDate", endDate: String)     => val date = endDate.toLong
            data.takeWhile(_.visited_at < date)
          case ("fromAge", startAge: String)   => val date = startAge.toLong
            data.filter(item => users(item.user).birth_date < AgeConverter.toUnixTime(date))
          case ("toAge", endAge: String)       => val date = endAge.toLong
            data.filter(item => users(item.user).birth_date > AgeConverter.toUnixTime(date))
          case ("gender", gender: String)      => if ((gender == "m") || (gender == "f")) data.filter(item => users(item.user).gender == gender) else mutable.SortedSet.empty[Visit]
          //case _                               => data
        }


        val finalResult = parseArgument(param.toList)(otvet).toSeq
        val raiting = if (finalResult.size > 0) {
          finalResult.map(item => (item.mark, 1)).foldLeft(0, 0) { case ((acc1, acc2), (a, b)) => (acc1 + a, acc2 + b) } match {
            case (total: Int, size: Int) if size > 0 => total.toDouble / size.toDouble
            case _                                   => 0.0
          }
        } else 0.0
        Ok( s"""{"avg":""" + "%.5f".formatLocal(java.util.Locale.US, raiting) + "}")
      } }
  }



  def addIdToJson(id:Int) =(__).json.update(
    __.read[JsObject].map{ o => o ++ Json.obj( "id" -> JsNumber(id) ) }
  )


  def updateUser(id:String) = Action { implicit request: Request[AnyContent] =>
    val params = (request.body.asJson).get.as[JsObject].value.toList

    def parseArgument(par: List[(String, JsValue)])(data: User): User = par match {
      case x :: xs => parseArgument(xs)(eval(x, data))
      case Nil     => data
    }

    def eval(inputPar: (String, JsValue), data: User) = inputPar match {
      case ("first_name", first_name: JsValue) => data.copy(first_name = first_name.as[String])
      case ("last_name", last_name: JsValue)     => data.copy(last_name = last_name.as[String])
      case ("birth_date", birth_date: JsValue)   => data.copy(birth_date = birth_date.as[Long])
      case ("email", email: JsValue)       => data.copy(email=email.as[String])
      case ("gender", gender: JsValue)      => data.copy(gender=gender.as[String])
      case _                               => data
    }
    val myId = toInt(id)
    if (myId>0 &&users.contains(myId)) {
          val currentUser = users(myId)
          users(myId) = parseArgument(params)(currentUser)
          Ok(Json.obj())
        }
        else NotFound("")
  }


  def updateLocation(id:String) = Action { implicit request: Request[AnyContent] =>
    val params = request.body.asJson.get.as[JsObject].value.toList

    def parseArgument(par: List[(String, JsValue)])(data: Location): Location = par match {
      case x :: xs => parseArgument(xs)(eval(x, data))
      case Nil     => data
    }

    def eval(inputPar: (String, JsValue), data: Location) = inputPar match {
      case ("distance", distance: JsValue) => data.copy(distance = distance.as[Int])
      case ("city", city: JsValue)     => data.copy(city = city.as[String])
      case ("place", place: JsValue)   => data.copy(place = place.as[String])
      case ("country", country: JsValue)       => data.copy(country=country.as[String])
      case _                               => data
    }
    val myId = toInt(id)
    if (myId>0 &&locations.contains(myId)) {
      val current = locations(myId)
      locations(myId) = parseArgument(params)(current)
      Ok(Json.obj())
    }
    else NotFound("")
  }


  def updateVisit(id:String) = Action { implicit request: Request[AnyContent] =>
    val params = (request.body.asJson).get.as[JsObject].value.toList

    def parseArgument(par: List[(String, JsValue)])(data: Visit): Visit = par match {
      case x :: xs => parseArgument(xs)(eval(x, data))
      case Nil     => data
    }

    def eval(inputPar: (String, JsValue), data: Visit) = inputPar match {
      case ("user", user: JsValue) => data.copy(user = user.as[Int])
      case ("location", location: JsValue)     => data.copy(location = location.as[Int])
      case ("visited_at", visited_at: JsValue)   => data.copy(visited_at = visited_at.as[Long])
      case ("mark", mark: JsValue)       => data.copy(mark=mark.as[Int])
      case _                               => data
    }
    val myId = toInt(id)
    if (myId>0 &&visits.contains(myId)) {
      val current = visits(myId)
      val newVisit = parseArgument(params)(current)
      Future {
        visitsByLocation(current.location).-=(current)
        visitsByUser(current.user).-=(current)
        visits(myId) = newVisit
        visitsByLocation(newVisit.location).+=(newVisit)
        visitsByUser(newVisit.user).+=(newVisit)
      }


      Ok(Json.obj())
    }
    else NotFound("")
  }



  def createUser() = Action { implicit request: Request[AnyContent] =>
    val result = request.body.asJson.get.validate[User]
    val current = result.fold(
      errors => {
        BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toJson(errors)))
      },
      item => {
        if (users.contains(item.id)) BadRequest("")
        else {users.put(item.id, item)
        Ok(Json.obj())}
      }
    )
    current
  }


  def createLocation() = Action { implicit request: Request[AnyContent] =>
    val result = request.body.asJson.get.validate[Location]
    val current = result.fold(
      errors => {
        BadRequest("")
      },
      item => {
        if (locations.contains(item.id)) BadRequest("")
        else {locations.put(item.id, item)
        Ok(Json.obj())}
      }
    )
    current
  }


  def createVisit() = Action { implicit request: Request[AnyContent] =>
    val result = request.body.asJson.get.validate[Visit]
    val current = result.fold(
      errors => {
        BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toJson(errors)))
      },
      item => {
        if (visits.contains(item.id)) BadRequest("")
        else {
          Future{
            visits.put(item.id, item)
            if (visitsByLocation.contains(item.location)) visitsByLocation(item.location).+=(item) else visitsByLocation.put(item.location, mutable.SortedSet(item))
            if (visitsByUser.contains(item.user)) visitsByUser(item.user).+=(item) else visitsByUser.put(item.user, mutable.SortedSet(item))
          }
          Ok(Json.obj())}
      }
    )
    current
  }



}
