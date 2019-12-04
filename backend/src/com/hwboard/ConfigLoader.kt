package com.hwboard

import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import java.io.File

object ConfigLoader {
  @UnstableDefault
  fun load(): HwboardConfig {
    val file =
      if (File("config.json").exists())
        File("config.json")
      else
        File("../config.json")
    val data = file.readText().trim()
    return Json.nonstrict.parse(HwboardConfig.serializer(), data)
  }
}

@Serializable
data class HwboardConfig(val jwtSecret: String, val discord: DiscordConfig)

@Serializable
data class DiscordConfig(
  val clientId: String,
  val clientSecret: String,
  val redirectUrl: String,
  val botToken: String,
  val guildId: String,
  val adminRole: String
)
