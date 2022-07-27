package com.kiktibia.ashesbot

import com.kiktibia.ashesbot.command.{EventCommand, RankupsCommand, WinnersCommand}
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class BotListener extends ListenerAdapter {

  override def onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = {
    event.getName match {
      case "event" =>
        handleEvent(event)
      case "winners" =>
        handleWinners(event)
      case "rankups" =>
        handleRankups(event)
      case _ =>
    }
  }

  private def handleEvent(event: SlashCommandInteractionEvent): Unit = {
    val embed = EventCommand.handleEvent(event)
    event.replyEmbeds(embed).queue()
  }

  private def handleWinners(event: SlashCommandInteractionEvent): Unit = {
    val embed = WinnersCommand.handleEvent()
    event.replyEmbeds(embed).queue()
  }

  private def handleRankups(event: SlashCommandInteractionEvent): Unit = {
    val embed = RankupsCommand.handleEvent()
    event.replyEmbeds(embed).queue()
  }

}
