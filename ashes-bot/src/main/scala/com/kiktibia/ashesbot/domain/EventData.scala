package com.kiktibia.ashesbot.domain

import java.time.{LocalDateTime, ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter

case class EventData(date: ZonedDateTime, name: String, level: Int)

case object EventData {

  def fromString(s: String): EventData = {
    val split = s.split(",")
    val ldt = LocalDateTime.parse(split.head, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    val zdt = ldt.atZone(ZoneOffset.UTC)
    EventData(zdt, split(1), split(2).toInt)
  }

}
