package com.kiktibia.ashesbot

import akka.actor.ActorSystem
import com.kiktibia.ashesbot.command.InfoCommand
import com.typesafe.scalalogging.StrictLogging
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.{Activity, Guild}

object BotApp extends App with StrictLogging {

  implicit private val actorSystem: ActorSystem = ActorSystem()

  logger.info("Starting up")

  private val jda = JDABuilder.createDefault(Config.token)
    .addEventListeners(new BotListener())
    .setActivity(Activity.playing("testing stuff"))
    .build()

  jda.awaitReady()
  logger.info("JDA ready")

  private val guild: Guild = jda.getGuildById(Config.guildId)

  guild.updateCommands().addCommands(InfoCommand.command).complete()

  private val newMemberChannel = guild.getTextChannelById(Config.newMemberChannelId)
  private val newMemberStream = new NewMemberStream(newMemberChannel)
  newMemberStream.stream.run()

}
