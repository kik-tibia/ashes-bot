package com.kiktibia.ashesbot

import akka.actor.ActorSystem
import com.kiktibia.ashesbot.command.{EventCommand, PayoutsCommand, RankupsCommand, WinnersCommand}
import com.kiktibia.ashesbot.util.FileUtilsImpl
import com.typesafe.scalalogging.StrictLogging
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild

import scala.jdk.CollectionConverters._

object BotApp extends App with StrictLogging {

  logger.info("Starting up")

  implicit private val actorSystem: ActorSystem = ActorSystem()

  private val fileUtils = new FileUtilsImpl()

  private val eventCommand = new EventCommand(fileUtils)
  private val payoutsCommand = new PayoutsCommand(fileUtils)
  private val rankupsCommand = new RankupsCommand(fileUtils)
  private val winnersCommand = new WinnersCommand(fileUtils)
  private val commands = List(eventCommand, payoutsCommand, rankupsCommand, winnersCommand)

  private val botListener = new BotListener(commands)
  private val jda = JDABuilder.createDefault(Config.token)
    .addEventListeners(botListener)
    .build()

  jda.awaitReady()
  logger.info("JDA ready")

  private val guild: Guild = jda.getGuildById(Config.guildId)

  guild.updateCommands().addCommands(commands.map(_.command).asJava).complete()

  private val newMemberChannel = guild.getTextChannelById(Config.newMemberChannelId)

  private val newMemberStream = new NewMemberStream(newMemberChannel, fileUtils)
  newMemberStream.stream.run()

}
