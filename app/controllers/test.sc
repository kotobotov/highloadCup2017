import java.io.File

import model.User
import play.api.libs.json.Json


def getListOfFiles(dir: String):List[File] = {
  val d = new File(dir)
  if (d.exists && d.isDirectory) {
    d.listFiles.filter(_.isFile).toList
  } else {
    List[File]()
  }
}

val files = getListOfFiles("C:\\inetpub\\play\\Obuchenie\\highload\\resource").filter(_.getName.contains("locations"))

