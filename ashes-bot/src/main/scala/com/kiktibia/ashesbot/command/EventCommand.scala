package com.kiktibia.ashesbot.command

import com.kiktibia.ashesbot.domain.{CharData, EventData, Rank}
import com.kiktibia.ashesbot.util.{EmbedHelper, FileUtils}
import com.typesafe.scalalogging.StrictLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.{Commands, OptionData, SlashCommandData}
import Rank.ranks

import scala.jdk.CollectionConverters._

object EventCommand extends StrictLogging {

  val command: SlashCommandData = Commands.slash("event", "get info of event")
    .addOptions(new OptionData(OptionType.STRING, "rank", "The rank to show")
      .addChoices(ranksAsChoices()))

  def handleEvent(event: SlashCommandInteractionEvent): MessageEmbed = {
    logger.info("event command called")
    val requestedRank: Option[String] = event.getInteraction.getOptions.asScala.find(_.getName == "rank").map(_.getAsString)

    val eventData: List[EventData] = FileUtils.getEventData(None)

    val charData = eventDataToCharData(eventData).filter(_.gained > 0).sortWith(charDataSort)

    val groupedCharData = charData.groupBy { c =>
      Rank.levelToRank(c.startLevel)
    }.map { case (rank, value) => (rank.name, value) }

    val embed = new EmbedBuilder()
    embed.setTitle("Level event update").setColor(16753451)
    requestedRank match {
      case Some(rank) =>
        val rankMessages = groupedCharData.getOrElse(rank, List.empty).map(rankMessage)
        EmbedHelper.addMultiFields(embed, s":fire: $rank :fire:", rankMessages, false)
      case None =>
        ranks.map(_.name).foreach { rank =>
          val rankMessages = groupedCharData.getOrElse(rank, List.empty).take(5).map(rankMessage)
          EmbedHelper.addMultiFields(embed, s":fire: $rank :fire:", rankMessages, false)
        }
    }
    embed.build()
  }

  private def ranksAsChoices() = {
    Rank.ranks.map { rank =>
      new Choice(rank.name, rank.name)
    }
  }.asJava

  private def eventDataToCharData(eventData: List[EventData]): List[CharData] = {
    val reversedEventData = eventData.reverse
    val charNames = eventData.map(_.name).toSet
    charNames.map { name =>
      val startLevel = eventData.filter(_.name == name).head.level
      val endLevel = reversedEventData.filter(_.name == name).head.level
      CharData(name, startLevel, endLevel, endLevel - startLevel)
    }.toList
  }

  // sort char data by who gained the most levels, breaking ties by who is the highest level
  private def charDataSort(c1: CharData, c2: CharData): Boolean = {
    if (c1.gained == c2.gained) c1.startLevel > c2.startLevel
    else c1.gained > c2.gained
  }

  private def rankMessage(c: CharData): String = {
    val levels = if (c.gained == 1) "level" else "levels"
    s"**${c.name}**: ${c.gained} $levels (${c.startLevel} to ${c.endLevel})"
  }

}
