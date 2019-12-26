package com.hwboard

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault

@Serializable
sealed class WebsocketMessage {
  @Serializable
  data class Error(val message: String) : WebsocketMessage()

  @Serializable
  data class Connect(val user: User) : WebsocketMessage()

  @Serializable
  data class Disconnect(val user: User) : WebsocketMessage()

  @Serializable
  data class Auth(val user: User) : WebsocketMessage()

  @Serializable
  data class Message(val message: String, val sender: User, val recipient: User) : WebsocketMessage()
}

@Serializable
sealed class User{
  abstract val name: String
}

@Serializable
@UnstableDefault
data class DiscordUser(
  @SerialName("username")
  override val name: String,
  val id: String,
  val read: Boolean = false,
  val write: Boolean = false,
  val superuser: Boolean = false
): User()
