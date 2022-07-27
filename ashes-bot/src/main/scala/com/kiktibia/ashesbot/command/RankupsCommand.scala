package com.kiktibia.ashesbot.command

import com.kiktibia.ashesbot.domain.{CharData, EventData, Rank}
import com.kiktibia.ashesbot.util.{EmbedHelper, FileUtils}
import com.typesafe.scalalogging.StrictLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.commands.build.{Commands, SlashCommandData}

object RankupsCommand extends StrictLogging with Command {

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

}
