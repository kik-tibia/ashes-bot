package com.kiktibia.ashesbot.tibiadata

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.apache.commons.text.StringEscapeUtils
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val strFormat: RootJsonFormat[String] = new RootJsonFormat[String] {
    override def write(obj: String): JsValue = new JsString(obj)

    override def read(json: JsValue): String = StringEscapeUtils
      .unescapeHtml4(JsonConvertNoCustomImplicits.convert(json))
  }

  implicit val guildhallFormat: RootJsonFormat[Guildhall] = jsonFormat3(Guildhall)
  implicit val memberFormat: RootJsonFormat[Member] = jsonFormat7(Member)
  implicit val inviteFormat: RootJsonFormat[Invite] = jsonFormat2(Invite)
  implicit val guildFormat: RootJsonFormat[Guild] = jsonFormat18(Guild)
  implicit val apiFormat: RootJsonFormat[Api] = jsonFormat3(Api)
  implicit val statusFormat: RootJsonFormat[Status] = jsonFormat1(Status)
  implicit val informationFormat: RootJsonFormat[Information] = jsonFormat3(Information)
  implicit val guildResponseFormat: RootJsonFormat[GuildResponse] = jsonFormat2(GuildResponse)
}

// This is needed because you can't just call json.convertTo[String] inside strFormat above because you get a stack overflow because it calls back on itself
object JsonConvertNoCustomImplicits extends SprayJsonSupport with DefaultJsonProtocol {
  def convert(json: JsValue): String = json.convertTo[String]
}
