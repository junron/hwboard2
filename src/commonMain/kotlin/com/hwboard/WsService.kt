package com.hwboard

import com.hwboard.WebsocketMessage.*
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule


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
data class User(val name: String)


val messageModule = SerializersModule {
  polymorphic<WebsocketMessage> {
    Disconnect::class with Disconnect.serializer()
    Connect::class with Connect.serializer()
    Message::class with Message.serializer()
    Auth::class with Auth.serializer()
    Error::class with Error.serializer()
  }
}

val json = Json(context = messageModule)

@Serializable
data class MessageWrapper(@Polymorphic val m: WebsocketMessage)