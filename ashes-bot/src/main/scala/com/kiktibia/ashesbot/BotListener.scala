package com.kiktibia.ashesbot

import com.kiktibia.ashesbot.command.EventCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class BotListener extends ListenerAdapter {

  override def onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = {
    event.getName match {
      case "event" =>
        handleEvent(event)

      case _ =>
    }
  }

  private def handleEvent(event: SlashCommandInteractionEvent): Unit = {
    val embed = EventCommand.handleEvent(event)
    event.replyEmbeds(embed).queue()
  }

}
