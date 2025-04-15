package com.kiktibia.ashesbot.command

import com.kiktibia.ashesbot.domain.Rank.ranks
import com.kiktibia.ashesbot.domain.{CharData, EventData, Rank}
import com.kiktibia.ashesbot.util.{EmbedHelper, FileUtils}
import com.typesafe.scalalogging.StrictLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.{Commands, OptionData, SlashCommandData}

import scala.jdk.CollectionConverters._

class PayoutsCommand(override val fileUtils: FileUtils) extends StrictLogging with Command {

  override val command: SlashCommandData = Commands.slash("payouts", "get payouts of event")
    .addOptions(new OptionData(OptionType.STRING, "event-id", "The id of the event to query"))

  override def handleEvent(event: SlashCommandInteractionEvent): MessageEmbed = {
    logger.info("payouts command called")

    val requestedId: Option[String] = event.getInteraction.getOptions.asScala.find(_.getName == "event-id")
      .map(_.getAsString)

    val eventData: List[EventData] = requestedId match {
      case None => fileUtils.getPreviousEventData()
      case _ => fileUtils.getEventData(requestedId)
    }
    val charData = eventDataToCharData(eventData)
    val month = getMonth(eventData)

    val groupedCharData = charData.groupBy { c => Rank.levelToRank(c.startLevel) }.map { case (rank, value) =>
      (rank.name, value)
    }

    val embed = new EmbedBuilder()
    embed.setTitle(s"$month Level event payouts").setColor(embedColour)

    val prizeMessages = ranks.map(_.name).flatMap { rank =>
      calculatePrizes(groupedCharData, rank).map { winner =>
        s"guild player transfer ${winner.prize} to ${winner.name}"
      }
    }
    EmbedHelper.addMultiFields(embed, s":fire: Payouts :fire:", prizeMessages, false)

    embed.build()
  }

}
