package com.kiktibia.ashesbot.command

import com.kiktibia.ashesbot.domain.{EventData, Rank}
import com.kiktibia.ashesbot.util.{EmbedHelper, FileUtils}
import com.typesafe.scalalogging.StrictLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.{Commands, OptionData, SlashCommandData}

import scala.jdk.CollectionConverters._

class RankupsCommand(override val fileUtils: FileUtils) extends StrictLogging with Command {

  override val command: SlashCommandData = Commands
    .slash("rankups", "get a list of characters that have advanced to the next rank")
    .addOptions(new OptionData(OptionType.STRING, "event-id", "The id of the event to query"))

  override def handleEvent(event: SlashCommandInteractionEvent): MessageEmbed = {
    logger.info("rankups command called")

    val requestedId: Option[String] = event.getInteraction.getOptions.asScala.find(_.getName == "event-id")
      .map(_.getAsString)

    val eventData: List[EventData] = requestedId match {
      case None => fileUtils.getPreviousEventData()
      case _ => fileUtils.getEventData(requestedId)
    }
    val charData = eventDataToCharData(eventData)
    val month = getMonth(eventData)

    val rankupMessages = charData.flatMap { c =>
      val startRank = Rank.levelToRank(c.startLevel).id
      val endRank = Rank.levelToRank(c.endLevel).id
      if (startRank != endRank) Some(s"**${c.name}**: ${c.startLevel} to ${c.endLevel}") else None
    }

    val embed = new EmbedBuilder()
    embed.setTitle(s"$month Level event rankups").setColor(embedColour)

    EmbedHelper.addMultiFields(embed, "Rankups", rankupMessages, false)

    embed.build()
  }

}
