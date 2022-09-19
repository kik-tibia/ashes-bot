package com.kiktibia.ashesbot.command

import com.kiktibia.ashesbot.domain.{CharData, EventData}
import com.kiktibia.ashesbot.util.FileUtils
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

import java.time.format.TextStyle
import java.util.Locale

trait Command {

  val fileUtils: FileUtils

  val command: SlashCommandData

  val embedColour = 16753451

  def handleEvent(event: SlashCommandInteractionEvent): MessageEmbed

  def getMonth(eventData: List[EventData]) = {
    eventData.head.date.getMonth.getDisplayName(TextStyle.FULL, Locale.UK)
  }

  def eventDataToCharData(eventData: List[EventData]): List[CharData] = {
    val reversedEventData = eventData.reverse
    val charNames = eventData.map(_.name).toSet
    charNames.map { name =>
      val startLevel = eventData.filter(_.name == name).head.level
      val endLevel = reversedEventData.filter(_.name == name).head.level
      CharData(name, startLevel, endLevel, endLevel - startLevel)
    }.toList.filter(_.gained > 0).sortWith(charDataSort)
  }

  // sort char data by who gained the most levels, breaking ties by who is the highest level
  private def charDataSort(c1: CharData, c2: CharData): Boolean = {
    if (c1.gained == c2.gained) c1.startLevel > c2.startLevel
    else c1.gained > c2.gained
  }

  def calculatePrizes(groupedCharData: Map[String, List[CharData]], rank: String): List[PrizeWinner] = {
    val scores = groupedCharData.getOrElse(rank, List.empty)
    val top3 = scores.take(3)
    val winners = top3 ++ scores.drop(3).takeWhile(_.gained == top3.last.gained)
    val payoutsWithIndex = winners.zipWithIndex

    winners.map { winner =>
      val tiedWith = payoutsWithIndex.filter(_._1.gained == winner.gained)
      val numTiedWith = tiedWith.length
      val numPrizesToShare = tiedWith.count(_._2 <= 2)
      val prizeMoney = 1000000 * numPrizesToShare / numTiedWith
      PrizeWinner(winner.name, winner.gained, prizeMoney)
    }
  }

}

case class PrizeWinner(name: String, gained: Int, prize: Int)
