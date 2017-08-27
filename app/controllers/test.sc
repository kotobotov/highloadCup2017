import play.api.libs.json.Json

Json.parse(scala.io.Source.fromFile("\\..\\..\\data\\users_1.json")("UTF-8").getLines.mkString)