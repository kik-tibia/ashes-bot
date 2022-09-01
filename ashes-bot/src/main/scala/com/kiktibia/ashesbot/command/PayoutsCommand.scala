package com.kiktibia.ashesbot.command

import com.kiktibia.ashesbot.domain.Rank.ranks
import com.kiktibia.ashesbot.domain.{EventData, Rank}
import com.kiktibia.ashesbot.util.{EmbedHelper, FileUtils}
import com.typesafe.scalalogging.StrictLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.{Commands, OptionData, SlashCommandData}

import scala.jdk.CollectionConverters._

object PayoutsCommand extends StrictLogging with Command {

  val command: SlashCommandData = Commands.slash("payouts", "get payouts of event")
    .addOptions(new OptionData(OptionType.STRING, "event-id", "The id of the event to query"))

  def handleEvent(event: SlashCommandInteractionEvent): MessageEmbed = {
    logger.info("payouts command called")

    val requestedId: Option[String] = event.getInteraction.getOptions.asScala.find(_.getName == "event-id").map(_.getAsString)

    val eventData: List[EventData] = FileUtils.getEventData(requestedId)
    val charData = eventDataToCharData(eventData)
    val month = getMonth(eventData)

    val groupedCharData = charData.groupBy { c =>
      Rank.levelToRank(c.startLevel)
    }.map { case (rank, value) => (rank.name, value) }

    val embed = new EmbedBuilder()
    embed.setTitle(s"$month Level event payouts").setColor(16753451)

    val prizeMessages = ranks.map(_.name).flatMap { rank =>
      val scores = groupedCharData.getOrElse(rank, List.empty)
      val top3 = scores.take(3)
      val winners = top3 ++ scores.drop(3).takeWhile(_.gained == top3.last.gained)
      val payoutsWithIndex = winners.zipWithIndex

      winners.map { winner =>
        val tiedWith = payoutsWithIndex.filter(_._1.gained == winner.gained)
        val numTiedWith = tiedWith.length
        val numPrizesToShare = tiedWith.count(_._2 <= 2)
        val prizeMoney = 1000000 * numPrizesToShare / numTiedWith
        // s"**${winner.name}**: ${winner.gained} levels, $prizeString"
        s"guild player transfer $prizeMoney to ${winner.name}"
      }
    }
    EmbedHelper.addMultiFields(embed, s":fire: Payouts :fire:", prizeMessages, false)

    embed.build()
  }

}
