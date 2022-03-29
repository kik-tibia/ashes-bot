package com.kiktibia.ashesbot.util

import com.typesafe.scalalogging.StrictLogging
import net.dv8tion.jda.api.EmbedBuilder

object EmbedHelper extends StrictLogging {

  // Discord doesn't let you have more than
  def addMultiFields(embed: EmbedBuilder, fieldName: String, values: List[String], inline: Boolean): Unit = {
    var name = fieldName
    var field = ""
    values.foreach { v =>
      val currentField = field + "\n" + v
      if (currentField.length <= 1024) { // don't add field yet, there is still room
        field = currentField
      }
      else { // it's full, add the field
        embed.addField(name, field, inline)
        name = ""
        field = v
      }
    }
    embed.addField(name, field, inline)
    logger.info(s"Embed length: ${embed.length()}")
  }
}
