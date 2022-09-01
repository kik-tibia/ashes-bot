package com.kiktibia.ashesbot.command

import com.kiktibia.ashesbot.domain.{CharData, EventData}

import java.time.format.TextStyle
import java.util.Locale

trait Command {

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

}
