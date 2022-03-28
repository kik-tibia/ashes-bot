package com.kiktibia.ashesbot

import com.typesafe.config.ConfigFactory

object Config {
  private val root = ConfigFactory.load().getConfig("ashes-bot")

  val token: String = root.getString("token")
  val guildId: String = root.getString("guild-id")
  val newMemberChannelId: String = root.getString("new-member-channel-id")

  val dataDir: String = root.getString("data-dir")
}
