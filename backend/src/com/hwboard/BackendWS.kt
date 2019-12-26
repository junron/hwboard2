package com.hwboard

import com.hwboard.WebsocketMessage.*
import com.hwboard.auth.DiscordAuth
import com.hwboard.auth.Jwt
import io.ktor.application.ApplicationCall
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json

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
      is HomeworkMessage ->{
        println(message.homework)
      }
    }
  }

  @UnstableDefault
  suspend fun handleConnect(call: ApplicationCall, context: WebSocketSession) {
    val token = call.request.cookies["user_sess"] ?: return context.close(CloseReason(403, "Unauthenticated"))
    val authenticatedUser = Jwt.verifyAndDecode(token, DiscordUser.serializer())
      ?: return context.close(CloseReason(403, "Unauthenticated"))
    val user = DiscordAuth.getAuthorization(authenticatedUser)
    if (!user.read) return context.close(CloseReason(401, "Unauthorized"))
    users[user] = context
    send(Auth(user), context)
    broadcast(Connect(user))
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
    context.outgoing.send(Frame.Text(Json.stringify(WebsocketMessage.serializer(), message)))
  }

}
