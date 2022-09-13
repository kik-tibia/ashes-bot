package com.kiktibia.ashesbot

import com.kiktibia.ashesbot.command.Command
import com.typesafe.scalalogging.StrictLogging
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class BotListener(commands: List[Command]) extends ListenerAdapter with StrictLogging {

  override def onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = {
    commands.find(_.command.getName == event.getName) match {
      case Some(command) =>
        val embed = command.handleEvent(event)
        event.replyEmbeds(embed).queue()
      case None =>
        logger.warn(s"Command not found: ${event.getName}")
    }
  }


}
