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

class EventCommand(override val fileUtils: FileUtils) extends StrictLogging with Command {

  override val command: SlashCommandData = Commands.slash("event", "get info of event")
    .addOptions(new OptionData(OptionType.STRING, "rank", "The rank to show")
      .addChoices(ranksAsChoices()))

  override def handleEvent(event: SlashCommandInteractionEvent): MessageEmbed = {
    logger.info("event command called")
    val requestedRank: Option[String] = event.getInteraction.getOptions.asScala.find(_.getName == "rank").map(_.getAsString)
    buildEmbed(requestedRank)
  }

  def buildEmbed(requestedRank: Option[String]): MessageEmbed = {
    val eventData: List[EventData] = fileUtils.getEventData(None)
    val charData = eventDataToCharData(eventData)
    val month = getMonth(eventData)

    val groupedCharData = charData.groupBy { c =>
      Rank.levelToRank(c.startLevel)
    }.map { case (rank, value) => (rank.name, value) }

    val embed = new EmbedBuilder()
    embed.setTitle(s"$month Level event update").setColor(embedColour)
    requestedRank match {
      case Some(rank) =>
        addRankFieldToEmbed(groupedCharData, embed, rank, None)
      case None =>
        ranks.map(_.name).foreach { rank =>
          addRankFieldToEmbed(groupedCharData, embed, rank, Some(5))
        }
    }
    embed.build()
  }

  private def addRankFieldToEmbed(groupedCharData: Map[String, List[CharData]], embed: EmbedBuilder, rank: String, limit: Option[Int]): Unit = {
    val rankCharData = groupedCharData.getOrElse(rank, List.empty)
    val rankMessages = limit match {
      case Some(l) => rankCharData.take(l).map(rankMessage)
      case None => rankCharData.map(rankMessage)
    }

    val fieldValue = rankMessages match {
      case Nil => List("Nobody has gained any levels yet.")
      case messages => messages
    }

    EmbedHelper.addMultiFields(embed, s":fire: $rank :fire:", fieldValue, false)
  }

  private def ranksAsChoices() = {
    Rank.ranks.map { rank =>
      new Choice(rank.name, rank.name)
    }
  }.asJava

  private def rankMessage(c: CharData): String = {
    val levels = if (c.gained == 1) "level" else "levels"
    s"**${c.name}**: ${c.gained} $levels (${c.startLevel} to ${c.endLevel})"
  }

}
