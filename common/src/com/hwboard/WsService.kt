package com.hwboard

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault

@Serializable
sealed class WebsocketMessage {
  @Serializable
  data class Error(val message: String) : WebsocketMessage()

  @Serializable
  data class AuthError(val message: String) : WebsocketMessage()

  @Serializable
  data class Connect(val user: User) : WebsocketMessage()

  @Serializable
  data class Disconnect(val user: User) : WebsocketMessage()

  @Serializable
  data class Auth(val user: User) : WebsocketMessage()

  @Serializable
  data class Message(val message: String, val sender: User, val recipient: User) : WebsocketMessage()

  //  Homework requests
  @Serializable
  data class AddHomework(val homework: Homework) : WebsocketMessage()

  @Serializable
  data class EditHomework(val homework: Homework) : WebsocketMessage()

  @Serializable
  data class DeleteHomework(val id: String) : WebsocketMessage()

  @Serializable
  object LoadHomework : WebsocketMessage()

  //  Homework responses
  @Serializable
  data class HomeworkAdded(val homework: Homework) : WebsocketMessage()

  @Serializable
  data class HomeworkEdited(val homework: Homework) : WebsocketMessage()

  @Serializable
  data class HomeworkDeleted(val id: String) : WebsocketMessage()

  @Serializable
  data class HomeworkLoaded(val homework: List<Homework>) : WebsocketMessage()
}

@Serializable
sealed class User {
  abstract val name: String
  abstract val read: Boolean
  abstract val write: Boolean
  abstract val superuser: Boolean
}

@Serializable
@UnstableDefault
data class DiscordUser(
  @SerialName("username")
  override val name: String,
  val id: String,
  override val read: Boolean = false,
  override val write: Boolean = false,
  override val superuser: Boolean = false
) : User()
