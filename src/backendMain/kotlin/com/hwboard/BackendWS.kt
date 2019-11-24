package com.hwboard

import com.hwboard.WebsocketMessage.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.UnstableDefault

object BackendWS {
  private val users = mutableMapOf<User, WebSocketSession>()
  @UnstableDefault
  suspend fun handle(message: WebsocketMessage, context: WebSocketSession) {
    when (message) {
      is Message -> {
        val recipientSession = users[message.recipient]
            ?: return send(Error("Recipient not found"), context)
        send(message, recipientSession)
      }
      is Auth -> {
        users[message.user] = context
        send(message, context)
        broadcast(Connect(message.user))
      }
    }
  }

  @UnstableDefault
  suspend fun handleDisconnect(context: WebSocketSession) {
    val user = users.filter { (_, v) -> v == context }.keys.first()
    users.remove(user)
    broadcast(Disconnect(user))
  }

  @UnstableDefault
  suspend fun broadcast(message: WebsocketMessage) {
    users.forEach { (_, session) -> runBlocking { send(message, session) } }
  }

  @UnstableDefault
  suspend fun send(message: WebsocketMessage, context: WebSocketSession) {
    val messageWrapper = MessageWrapper(message)
    context.outgoing.send(Frame.Text(json.stringify(MessageWrapper.serializer(), messageWrapper)))
  }

}