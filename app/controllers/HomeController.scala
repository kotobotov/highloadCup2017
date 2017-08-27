package controllers

import javax.inject._

import model._
import play.api.{Environment, Logger, NoHttpFiltersComponents}
import play.api.libs.json._
import play.api.libs.json.{JsError, JsObject, Json}
import play.api.libs.json.Reads._
import play.api.mvc._

import scala.collection.mutable

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

  val users = mutable.HashMap((Json.parse(scala.io.Source.fromFile("C:\\inetpub\\play\\Obuchenie\\highload\\resource\\data\\users_1.json")("UTF-8").getLines.mkString) \ "users").as[Seq[User]].map(user => (user.id -> user)): _*)
  val visits = mutable.HashMap((Json.parse(scala.io.Source.fromFile("C:\\inetpub\\play\\Obuchenie\\highload\\resource\\data\\visits_1.json")("UTF-8").getLines.mkString) \ "visits").as[mutable.ArrayBuffer[Visit]].map(item => (item.id -> item)): _*)
  val locations = mutable.HashMap((Json.parse(scala.io.Source.fromFile("C:\\inetpub\\play\\Obuchenie\\highload\\resource\\data\\locations_1.json")("UTF-8").getLines.mkString) \ "locations").as[mutable.ArrayBuffer[Location]].map(item => (item.id -> item)): _*)
  var visitsByUser = mutable.HashMap.empty[Int, mutable.SortedSet[Visit]]
  var visitsByLocation = mutable.HashMap.empty[Int, mutable.SortedSet[Visit]]

  visits.foreach { case (id, visit) =>
    if (visitsByLocation.contains(visit.location)) visitsByLocation(visit.location).+=(visit) else visitsByLocation.put(visit.location, mutable.SortedSet(visit))
    if (visitsByUser.contains(visit.user)) visitsByUser(visit.user).+=(visit) else visitsByUser.put(visit.user, mutable.SortedSet(visit))
  }

  def userInfo(id: Int) = Action {
    //Logger.info(RelativePath)
    Ok(Json.toJson(users(id)))
  }

  def locationInfo(id: Int) = Action {
    Ok(Json.toJson(locations(id)))
  }

  def visitInfo(id: Int) = Action {
    Ok(Json.toJson(locations(id)))
  }

  def userVizits(id: Int) = Action { implicit request: Request[AnyContent] =>
    val param = request.queryString.map(item => (item._1, item._2.head)).toList
    Logger.info(param.toString)
    var otvet = visitsByUser(id)


    def parseArgument(par: List[(String, String)])(data: mutable.SortedSet[Visit]): mutable.SortedSet[Visit] = par match {
      case x :: xs => parseArgument(xs)(eval(x, data))
      case Nil     => data
    }

    def eval(inputPar: (String, String), data: mutable.SortedSet[Visit]) = inputPar match {
      case ("fromDate", startDate: String)                                        => data.dropWhile(_.visited_at <= startDate.toLong)
      case ("toDate", endDate: String)                                            => data.takeWhile(_.visited_at < endDate.toLong)
      case ("country", country: String) if Location.allLocation.contains(country) => data.filter(item => item.location == Location.encode(country))
      case ("toDistance", distance: String)                                       => data.filter(item => locations(item.location).distance < distance.toInt)
      case _                                                                      => data
    }


    val finalResult = parseArgument(param)(otvet)
    Ok(
      """{"visits":[""".stripMargin + finalResult.toSeq
                                      .map(item => raw"""{"mark": ${item.mark }, "visited_at": ${item.visited_at }, "place":"${locations(item.location).place }" }""").mkString(",") + "]}")

  }

  def averageEvaluation(id: Int) = Action { implicit request: Request[AnyContent] =>

    val param = request.queryString.map(item => (item._1, item._2.head)).toList
    Logger.info(param.toString)
    val otvet = visitsByLocation(id)


    def parseArgument(par: List[(String, String)])(data: mutable.SortedSet[Visit]): mutable.SortedSet[Visit] = par match {
      case x :: xs => parseArgument(xs)(eval(x, data))
      case Nil     => data
    }

    def eval(inputPar: (String, String), data: mutable.SortedSet[Visit]) = inputPar match {
      case ("fromDate", startDate: String) => data.dropWhile(_.visited_at <= startDate.toLong)
      case ("toDate", endDate: String)     => data.takeWhile(_.visited_at < endDate.toLong)
      case ("fromAge", startAge: String)   => data.filter(item => users(item.user).birth_date > startAge.toLong)
      case ("toAge", endAge: String)       => data.filter(item => users(item.user).birth_date < endAge.toLong)
      case ("gender", gender: String)      => data.filter(item => users(item.user).gender == gender)
      case _                               => data
    }


    val finalResult = parseArgument(param)(otvet)
    val raiting = finalResult.toSeq.map(item => (item.mark, 1)).foldLeft(0, 0) { case ((acc1, acc2), (a, b)) => (acc1 + a, acc2 + b) } match {
      case (total: Int, size: Int) if size > 0 => total.toDouble / size.toDouble
      case _                                   => 0
    }
    Ok( s"""{"avg":""" + "%.5f".formatLocal(java.util.Locale.US, raiting) + "}")
  }



  def addIdToJson(id:Int) =(__).json.update(
    __.read[JsObject].map{ o => o ++ Json.obj( "id" -> JsNumber(id) ) }
  )


  def updateUser(id:Int) = Action { implicit request: Request[AnyContent] =>

    Logger.info(request.body.asJson.get
                .transform(addIdToJson(id)).get.toString)
    val result = request.body.asJson.get
                 .transform(addIdToJson(id)).get
                 .validate[User]
    val current = result.fold(
      errors => {
        BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toJson(errors)))
      },
      item => {
        if (users.contains(item.id)) {
          users(item.id) = item
          Ok(Json.obj())
        }
        else NotFound("")
      }
    )
    current
  }


  def updateLocation(id:Int) = Action { implicit request: Request[AnyContent] =>
    val result = request.body.asJson.get.transform(addIdToJson(id)).get.validate[Location]
    val current = result.fold(
      errors => {
        BadRequest("")
      },
      item => {
        if (users.contains(item.id)) {
          locations(item.id) = item
          Ok(Json.obj())
        }
        else NotFound("")
      }
    )
    current
  }


  def updateVisit(id:Int) = Action { implicit request: Request[AnyContent] =>
    val result = request.body.asJson.get.transform(addIdToJson(id)).get.validate[Visit]
    val current = result.fold(
      errors => {
        BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toJson(errors)))
      },
      item => {
        if (users.contains(item.id)) {
          visits(item.id) = item
          Ok(Json.obj())
        }
        else NotFound("")
      }
    )
    current
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
        else {visits.put(item.id, item)
        Ok(Json.obj())}
      }
    )
    current
  }



}
