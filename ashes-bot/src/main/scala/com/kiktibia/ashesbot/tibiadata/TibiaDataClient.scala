package com.kiktibia.ashesbot.tibiadata

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal

import scala.concurrent.{ExecutionContextExecutor, Future}

class TibiaDataClient extends JsonSupport {

  implicit private val system: ActorSystem = ActorSystem()
  implicit private val executionContext: ExecutionContextExecutor = system.dispatcher

  private val guildUrl = "https://api.tibiadata.com/v3/guild/Ashes%20Remain"

  def getGuild: Future[GuildResponse] = {
    for {
      response <- Http().singleRequest(HttpRequest(uri = guildUrl))
      unmarshalled <- Unmarshal(response).to[GuildResponse]
    } yield unmarshalled
  }

}
