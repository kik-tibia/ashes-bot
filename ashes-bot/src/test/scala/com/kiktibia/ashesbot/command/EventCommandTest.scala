package com.kiktibia.ashesbot.command

import com.kiktibia.ashesbot.domain.{EventData, Rank}
import com.kiktibia.ashesbot.util.FileUtils
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.time.ZonedDateTime
import scala.jdk.CollectionConverters._

class EventCommandTest extends AnyFunSuite with Matchers with MockFactory {

  test("If nobody has gained any levels, it should say so") {
    val fileUtils = mock[FileUtils]
    (fileUtils.getEventData _).expects(*).returns(List(
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"),"Kikaro", 500),
    ))

    val eventCommand = new EventCommand(fileUtils)
    val embed = eventCommand.buildEmbed(None)

    embed.getFields.asScala should have length Rank.ranks.length
    embed.getFields.asScala.foreach { field =>
      field.getValue should include ("Nobody has gained any levels yet")
    }
  }

}
