package com.kiktibia.ashesbot.util

import com.kiktibia.ashesbot.Config
import com.kiktibia.ashesbot.domain.EventData

import java.io.{BufferedWriter, File, FileWriter}
import scala.io.Source

object FileUtils {

  private val membersFile = new File(s"${Config.dataDir}/members/ashes-remain.dat")
  private val eventDir = new File(s"${Config.dataDir}/event")

  def getMembers: List[String] = {
    getLines(membersFile)
  }

  def writeNewMembers(members: List[String]): Unit = {
    val bw = new BufferedWriter(new FileWriter(membersFile, true))
    members.foreach(m => bw.write(s"$m\n"))
    bw.close()
  }

  def getEventData(idOpt: Option[String]): List[EventData] = {
    val datFiles: List[File] = eventDir.listFiles().toList.filter(_.getName.contains(".dat"))

    val datFile = idOpt match {
      // id provided -> use that file
      case Some(id) => datFiles.find(_.getName.startsWith(s"$id."))
      // id not provided -> find the latest file
      case None => datFiles.filter(_.getName.contains(".dat")).maxByOption(_.getName.split("\\.").head.toInt)
    }

    datFile match {
      case Some(file) => getLines(file).map(EventData.fromString)
      case None => List.empty // TODO handle some other way?
    }

  }

  private def getLines(file: File): List[String] = {
    val source = Source.fromFile(file)
    val lines = source.getLines().toList
    source.close()
    lines
  }

}
