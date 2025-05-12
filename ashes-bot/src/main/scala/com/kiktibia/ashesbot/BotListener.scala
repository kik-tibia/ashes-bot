package com.kiktibia.ashesbot

import com.kiktibia.ashesbot.command.Command
import com.typesafe.scalalogging.StrictLogging
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.EmbedBuilder

class BotListener(commands: List[Command]) extends ListenerAdapter with StrictLogging {

  override def onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = {
    commands.find(_.command.getName == event.getName) match {
      case Some(command) =>
        // val embed = command.handleEvent(event)
        val embed = new EmbedBuilder().addField(
          "Ashes Bot has been disabled",
          "Read here to find out why: https://discord.com/channels/891452957806190604/897094918667911198/1369506105868292102 \nSorry for the inconvenience.",
          false
        ).build()
        event.replyEmbeds(embed).queue()
      case None => logger.warn(s"Command not found: ${event.getName}")
    }
  }

}
