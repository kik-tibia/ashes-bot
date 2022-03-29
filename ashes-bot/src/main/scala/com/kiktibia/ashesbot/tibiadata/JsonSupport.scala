package com.kiktibia.ashesbot.tibiadata

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val guildhallFormat: RootJsonFormat[Guildhall] = jsonFormat3(Guildhall)
  implicit val memberFormat: RootJsonFormat[Member] = jsonFormat7(Member)
  implicit val guildFormat: RootJsonFormat[Guild] = jsonFormat18(Guild)
  implicit val guildsFormat: RootJsonFormat[Guilds] = jsonFormat1(Guilds)
  implicit val informationFormat: RootJsonFormat[Information] = jsonFormat2(Information)
  implicit val guildResponseFormat: RootJsonFormat[GuildResponse] = jsonFormat2(GuildResponse)
}
