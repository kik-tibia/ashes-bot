package com.kiktibia.ashesbot

import akka.actor.Cancellable
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}
import akka.stream.{Attributes, Supervision}
import com.kiktibia.ashesbot.tibiadata.{GuildResponse, Member, TibiaDataClient}
import com.kiktibia.ashesbot.util.FileUtils
import com.typesafe.scalalogging.StrictLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.TextChannel

import scala.concurrent.Future
import scala.concurrent.duration._

class NewMemberStream(newMemberChannel: TextChannel) extends StrictLogging {

  private val tibiaDataClient = new TibiaDataClient()

  private val logAndResumeDecider: Supervision.Decider = { e =>
    logger.error("An exception has occurred in the NewMemberStream:", e)
    Supervision.Resume
  }
  private val logAndResume: Attributes = supervisionStrategy(logAndResumeDecider)

  private lazy val sourceTick = Source.tick(2.seconds, 60.seconds, ())

  private lazy val getGuildFromTibiaData = Flow[Unit].mapAsync(1) { _ =>
    logger.info("Running stream")
    tibiaDataClient.getGuild
  }.withAttributes(logAndResume)

  private lazy val determineNewMembers = Flow[GuildResponse].mapAsync(1) { guildResponse =>
    val members = guildResponse.guilds.guild.members
    val namesFromTibiaData: Set[String] = members.map(_.name).toSet
    val namesFromFile: Set[String] = FileUtils.getMembers.toSet
    val newMembers: Set[String] = namesFromTibiaData -- namesFromFile
    logger.info(s"${newMembers.size} new members")
    Future.successful((newMembers, members))
  }.withAttributes(logAndResume)

  private lazy val writeNewMembersToFile = Flow[(Set[String], List[Member])].mapAsync(1) { case (newMembers, guildMembers) =>
    FileUtils.writeNewMembers(newMembers.toList)
    Future.successful((newMembers, guildMembers))
  }.withAttributes(logAndResume)

  private lazy val mentionNewMembersOnDiscord = Flow[(Set[String], List[Member])].mapAsync(1) { case (newMembers, guildMembers) =>
    if (newMembers.nonEmpty) {
      val newMemberMessages = newMembers.flatMap(m => guildMembers.find(_.name == m)).map {m =>
        s"**${m.name}** (${m.level.toInt} ${m.vocation}) just joined Ashes Remain!"
      }
      val embed = new EmbedBuilder().setTitle("New members").setDescription(newMemberMessages.mkString("\n")).setColor(16753451).build()
      newMemberChannel.sendMessageEmbeds(embed).queue()
    }
    logger.info("Stream finished")
    Future.successful()
  }.withAttributes(logAndResume)

  lazy val stream: RunnableGraph[Cancellable] =
    sourceTick via
      getGuildFromTibiaData via
      determineNewMembers via
      writeNewMembersToFile via
      mentionNewMembersOnDiscord to Sink.ignore
}
