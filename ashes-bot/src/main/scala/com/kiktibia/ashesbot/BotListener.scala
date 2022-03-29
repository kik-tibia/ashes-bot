package com.kiktibia.ashesbot

import com.kiktibia.ashesbot.command.InfoCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class BotListener extends ListenerAdapter {

  override def onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = {
    event.getName match {
      case "info" =>
        handleInfo(event)

      case _ =>
    }
  }

  private def handleInfo(event: SlashCommandInteractionEvent): Unit = {
    val embed = InfoCommand.handleEvent(event)
    event.replyEmbeds(embed).queue()
  }

}
