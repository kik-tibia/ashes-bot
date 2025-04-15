package com.kiktibia.ashesbot.command

import com.kiktibia.ashesbot.domain.{EventData, Rank}
import com.kiktibia.ashesbot.util.FileUtils
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.time.ZonedDateTime
import scala.jdk.CollectionConverters._

class WinnersCommandTest extends AnyFunSuite with Matchers with MockFactory {

  test("It should give 1kk to 1st and 750k to 2nd") {
    val fileUtils = mock[FileUtils]
    (fileUtils.getPreviousEventData _).expects().returns(List(
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Kikaro", 500),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Asd", 450),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Zxc", 550),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Loser", 420),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Kikaro", 510),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Asd", 455),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Zxc", 552),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Loser", 420)
    ))

    val winnersCommand = new WinnersCommand(fileUtils)
    val embed = winnersCommand.buildEmbed(None)

    embed.getFields.asScala should have length Rank.ranks.length
    embed.getFields.asScala.foreach { field =>
      if (field.getName contains "Firestorm") {
        field.getValue should include("**Kikaro**: 10 levels, 1kk")
        field.getValue should include("**Asd**: 5 levels, 750k")
        field.getValue should not include ("Loser")
        field.getValue should not include ("Zxc")
        field.getValue.indexOf("Kikaro") should be < field.getValue.indexOf("Asd")
      }
    }
  }

  test("It should work across multiple ranks") {
    val fileUtils = mock[FileUtils]
    (fileUtils.getPreviousEventData _).expects().returns(List(
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Kikaro", 500),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Kikaro", 510),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Noob", 100),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Noob", 105)
    ))

    val winnersCommand = new WinnersCommand(fileUtils)
    val embed = winnersCommand.buildEmbed(None)

    embed.getFields.asScala should have length Rank.ranks.length
    embed.getFields.asScala.foreach { field =>
      if (field.getName contains "Spark") { field.getValue should include("**Noob**: 5 levels, 1kk") }
      if (field.getName contains "Firestorm") { field.getValue should include("**Kikaro**: 10 levels, 1kk") }
    }
  }

  test("It should handle a second place tie by awarding half the prize money to the tied players") {
    val fileUtils = mock[FileUtils]
    (fileUtils.getPreviousEventData _).expects().returns(List(
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Kikaro", 500),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Zxc", 550),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Vbn", 580),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Kikaro", 510),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Zxc", 552),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Vbn", 582)
    ))

    val winnersCommand = new WinnersCommand(fileUtils)
    val embed = winnersCommand.buildEmbed(None)

    embed.getFields.asScala should have length Rank.ranks.length
    embed.getFields.asScala.foreach { field =>
      if (field.getName contains "Firestorm") {
        field.getValue should include("**Kikaro**: 10 levels, 1kk")
        field.getValue should include("**Zxc**: 2 levels, 375k")
        field.getValue should include("**Vbn**: 2 levels, 375k")
        field.getValue.indexOf("Kikaro") should be < field.getValue.indexOf("Zxc")
      }
    }
  }

  test("Three people tied for first place should split two prize awards") {
    val fileUtils = mock[FileUtils]
    (fileUtils.getPreviousEventData _).expects().returns(List(
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Asd", 450),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Zxc", 550),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Vbn", 580),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Asd", 455),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Zxc", 555),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Vbn", 585)
    ))

    val winnersCommand = new WinnersCommand(fileUtils)
    val embed = winnersCommand.buildEmbed(None)

    embed.getFields.asScala should have length Rank.ranks.length
    embed.getFields.asScala.foreach { field =>
      if (field.getName contains "Firestorm") {
        field.getValue should include("**Asd**: 5 levels, 583k")
        field.getValue should include("**Zxc**: 5 levels, 583k")
        field.getValue should include("**Vbn**: 5 levels, 583k")
      }
    }
  }

  test("People in flame must gain at least 5 levels to qualify for a reward") {
    val fileUtils = mock[FileUtils]
    (fileUtils.getPreviousEventData _).expects().returns(List(
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Asd", 150),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Zxc", 160),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Vbn", 170),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Asd", 160),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Zxc", 164),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"), "Vbn", 171)
    ))

    val winnersCommand = new WinnersCommand(fileUtils)
    val embed = winnersCommand.buildEmbed(None)

    embed.getFields.asScala should have length Rank.ranks.length
    embed.getFields.asScala.foreach { field =>
      if (field.getName contains "Flame") {
        field.getValue should include("**Asd**: 10 levels, 1kk")
        field.getValue should not include ("Zxc")
        field.getValue should not include ("Vbn")
      }
    }
  }

}
