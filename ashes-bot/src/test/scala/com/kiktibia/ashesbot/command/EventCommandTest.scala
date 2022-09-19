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

  test("If someone has gained some levels, it should say so") {
    val fileUtils = mock[FileUtils]
    (fileUtils.getEventData _).expects(*).returns(List(
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"),"Kikaro", 500),
      EventData(ZonedDateTime.parse("2020-12-04T10:15:30Z"),"Kikaro", 501),
      EventData(ZonedDateTime.parse("2020-12-05T10:15:30Z"),"Kikaro", 502),
    ))

    val eventCommand = new EventCommand(fileUtils)
    val embed = eventCommand.buildEmbed(None)

    embed.getFields.asScala should have length Rank.ranks.length
    embed.getFields.asScala.foreach { field =>
      if (field.getName contains "Firestorm") {
        field.getValue should include("Kikaro")
        field.getValue should include("2 levels")
        field.getValue should include("500 to 502")
      }
      else field.getValue should include ("Nobody has gained any levels yet")
    }
  }

  test("If someone has died and gained negative levels, it shouldn't say anything") {
    val fileUtils = mock[FileUtils]
    (fileUtils.getEventData _).expects(*).returns(List(
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"),"Kikaro", 500),
      EventData(ZonedDateTime.parse("2020-12-04T10:15:30Z"),"Kikaro", 501),
      EventData(ZonedDateTime.parse("2020-12-05T10:15:30Z"),"Kikaro", 499),
    ))

    val eventCommand = new EventCommand(fileUtils)
    val embed = eventCommand.buildEmbed(None)

    embed.getFields.asScala should have length Rank.ranks.length
    embed.getFields.asScala.foreach { field =>
      field.getValue should include ("Nobody has gained any levels yet")
    }
  }

  test("If multiple people from different ranks have gained some levels, it should say so") {
    val fileUtils = mock[FileUtils]
    (fileUtils.getEventData _).expects(*).returns(List(
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"),"Kikaro", 500),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"),"Elder Kikaro", 300),
      EventData(ZonedDateTime.parse("2020-12-04T10:15:30Z"),"Kikaro", 501),
      EventData(ZonedDateTime.parse("2020-12-05T10:15:30Z"),"Kikaro", 502),
      EventData(ZonedDateTime.parse("2020-12-07T10:15:30Z"),"Elder Kikaro", 303),
    ))

    val eventCommand = new EventCommand(fileUtils)
    val embed = eventCommand.buildEmbed(None)

    embed.getFields.asScala should have length Rank.ranks.length
    embed.getFields.asScala.foreach { field =>
      if (field.getName contains "Wildfire") {
        field.getValue should include("Elder Kikaro")
        field.getValue should include("3 levels")
        field.getValue should include("300 to 303")
      }
      else if (field.getName contains "Firestorm") {
        field.getValue should include("Kikaro")
        field.getValue should include("2 levels")
        field.getValue should include("500 to 502")
      }
      else field.getValue should include ("Nobody has gained any levels yet")
    }
  }


  test("If someone passes into the next rank, it should show them in their original rank") {
    val fileUtils = mock[FileUtils]
    (fileUtils.getEventData _).expects(*).returns(List(
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"),"Kikaro", 500),
      EventData(ZonedDateTime.parse("2020-12-04T10:15:30Z"),"Kikaro", 700),
    ))

    val eventCommand = new EventCommand(fileUtils)
    val embed = eventCommand.buildEmbed(None)

    embed.getFields.asScala should have length Rank.ranks.length
    embed.getFields.asScala.foreach { field =>
      if (field.getName contains "Firestorm") {
        field.getValue should include("Kikaro")
        field.getValue should include("200 levels")
        field.getValue should include("500 to 700")
      }
      else field.getValue should include ("Nobody has gained any levels yet")
    }
  }

  test("If multiple people are in the same rank, they should be ordered by levels gained") {
    val fileUtils = mock[FileUtils]
    (fileUtils.getEventData _).expects(*).returns(List(
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"),"Kikaro", 500),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"),"Asd", 450),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"),"Zxc", 550),
      EventData(ZonedDateTime.parse("2020-12-04T10:15:30Z"),"Kikaro", 501),
      EventData(ZonedDateTime.parse("2020-12-04T10:15:30Z"),"Zxc", 552),
      EventData(ZonedDateTime.parse("2020-12-05T10:15:30Z"),"Kikaro", 502),
      EventData(ZonedDateTime.parse("2020-12-06T10:15:30Z"),"Zxc", 553),
      EventData(ZonedDateTime.parse("2020-12-07T10:15:30Z"),"Asd", 457),
    ))

    val eventCommand = new EventCommand(fileUtils)
    val embed = eventCommand.buildEmbed(None)

    embed.getFields.asScala should have length Rank.ranks.length
    embed.getFields.asScala.foreach { field =>
      if (field.getName contains "Firestorm") {
        field.getValue should include("Kikaro")
        field.getValue should include("Asd")
        field.getValue should include("Zxc")
        field.getValue.indexOf("Asd") should be < field.getValue.indexOf("Zxc")
        field.getValue.indexOf("Zxc") should be < field.getValue.indexOf("Kikaro")
      }
      else field.getValue should include ("Nobody has gained any levels yet")
    }
  }

  test("If multiple people in the same rank gained the same number of levels, they should be sorted by highest starting level first") {
    val fileUtils = mock[FileUtils]
    (fileUtils.getEventData _).expects(*).returns(List(
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"),"Kikaro", 500),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"),"Asd", 450),
      EventData(ZonedDateTime.parse("2020-12-03T10:15:30Z"),"Zxc", 550),
      EventData(ZonedDateTime.parse("2020-12-04T10:15:30Z"),"Kikaro", 503),
      EventData(ZonedDateTime.parse("2020-12-07T10:15:30Z"),"Asd", 453),
      EventData(ZonedDateTime.parse("2020-12-06T10:15:30Z"),"Zxc", 553),
    ))

    val eventCommand = new EventCommand(fileUtils)
    val embed = eventCommand.buildEmbed(None)

    embed.getFields.asScala should have length Rank.ranks.length
    embed.getFields.asScala.foreach { field =>
      if (field.getName contains "Firestorm") {
        field.getValue should include("Kikaro")
        field.getValue should include("Asd")
        field.getValue should include("Zxc")
        field.getValue.indexOf("Zxc") should be < field.getValue.indexOf("Kikaro")
        field.getValue.indexOf("Kikaro") should be < field.getValue.indexOf("Asd")
      }
      else field.getValue should include ("Nobody has gained any levels yet")
    }
  }

}
