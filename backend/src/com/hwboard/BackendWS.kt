package com.hwboard

import com.hwboard.WebsocketMessage.*
import com.hwboard.auth.DiscordAuth
import com.hwboard.auth.Jwt
import com.hwboard.database.HomeworkDB
import io.ktor.application.ApplicationCall
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json

object BackendWS {
  private val users = mutableMapOf<User, List<WebSocketSession>>()
  @UnstableDefault
  suspend fun handle(message: WebsocketMessage, context: WebSocketSession) {
    val user = auth(context)
    when (message) {
      is AddHomework -> {
        if (user.write && user == message.homework.lastEditPerson) {
          HomeworkDB += message.homework.sanitized()
          broadcast(HomeworkAdded(message.homework.sanitized()))
        }
      }
      is DeleteHomework -> {
        if (user.write) {
          HomeworkDB -= message.id
          broadcast(HomeworkDeleted(message.id))
        }
      }
      is EditHomework -> {
        if (user.write && user == message.homework.lastEditPerson) {
          HomeworkDB[message.homework.id] = message.homework.sanitized()
          broadcast(HomeworkEdited(message.homework.sanitized()))
        }
      }
      is LoadHomework -> {
        if (user.read) {
          send(HomeworkLoaded(HomeworkDB.getAll()), context)
        }
      }
    }
  }

  private fun auth(context: WebSocketSession) =
    users.filterValues { sessions -> context in sessions }.keys.first()


  @UnstableDefault
  suspend fun handleConnect(call: ApplicationCall, context: WebSocketSession) {
    val token = call.request.cookies["user_sess"] ?: return context.close(CloseReason(403, "Unauthenticated"))
    val authenticatedUser = Jwt.verifyAndDecode(token, DiscordUser.serializer())
      ?: return run {
        send(Error("Unauthenticated"), context)
        context.close(CloseReason(403, "Unauthenticated"))
      }
    val user = DiscordAuth.getAuthorization(authenticatedUser)
    if (!user.read) return run {
      send(Error("Unauthorized"), context)
      context.close(CloseReason(401, "Unauthorized"))
    }
    users[user] = if (users[user] == null)
      listOf(context)
    else
      users[user]!! + context

    send(Auth(user), context)
    broadcast(Connect(user))
  }

  @UnstableDefault
  suspend fun handleDisconnect(context: WebSocketSession) {
    val user = auth(context)
    users.remove(user)
    broadcast(Disconnect(user))
  }

  @UnstableDefault
  suspend fun broadcast(message: WebsocketMessage) {
    for (webSocketSession in users.values.flatten()) {
      send(message, webSocketSession)
    }
  }

  @UnstableDefault
  suspend fun send(message: WebsocketMessage, context: WebSocketSession) {
    context.outgoing.send(Frame.Text(Json.stringify(WebsocketMessage.serializer(), message)))
  }

}
