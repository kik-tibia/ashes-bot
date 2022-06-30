package com.kiktibia.ashesbot.command

import com.kiktibia.ashesbot.domain.Rank.ranks
import com.kiktibia.ashesbot.domain.{CharData, EventData, Rank}
import com.kiktibia.ashesbot.util.{EmbedHelper, FileUtils}
import com.typesafe.scalalogging.StrictLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.{Commands, OptionData, SlashCommandData}

import scala.jdk.CollectionConverters._

object RankupsCommand extends StrictLogging {

  val command: SlashCommandData = Commands.slash("rankups", "get a list of characters that have advanced to the next rank")

  def handleEvent(): MessageEmbed = {
    logger.info("rankups command called")

    val eventData: List[EventData] = FileUtils.getEventData(None)
    val charData = eventDataToCharData(eventData).filter(_.gained > 0).sortWith(charDataSort)

    val rankupMessages = charData.flatMap { c =>
      val startRank = Rank.levelToRank(c.startLevel).id
      val endRank = Rank.levelToRank(c.endLevel).id
      if (startRank != endRank) Some(s"**${c.name}**: ${c.startLevel} to ${c.endLevel}")
      else None
    }

    val embed = new EmbedBuilder()
    embed.setTitle("Level event rankups").setColor(16753451)

    EmbedHelper.addMultiFields(embed, "Rankups", rankupMessages, false)

    embed.build()
  }

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

}
