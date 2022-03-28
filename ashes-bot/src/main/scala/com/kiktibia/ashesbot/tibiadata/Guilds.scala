package com.kiktibia.ashesbot.tibiadata

case class Guildhall(
                      name: String,
                      world: String,
                      paid_until: String
                    )

case class Member(
                   name: String,
                   title: Option[String],
                   rank: String,
                   vocation: String,
                   level: Double,
                   joined: String,
                   status: String
                 )

case class Guild(
                  name: String,
                  world: String,
                  logo_url: Option[String],
                  description: Option[String],
                  guildhalls: List[Guildhall],
                  active: Boolean,
                  founded: String,
                  open_applications: Boolean,
                  homepage: Option[String],
                  in_war: Boolean,
                  disband_date: Option[String],
                  disband_condition: Option[String],
                  players_online: Double,
                  players_offline: Double,
                  members_total: Double,
                  members_invited: Double,
                  members: List[Member],
                  invites: Option[String]
                )

case class Guilds(
                   guild: Guild
                 )

case class Information(
                        api_version: Double,
                        timestamp: String
                      )

case class GuildResponse(
                          guilds: Guilds,
                          information: Information
                        )
