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
        if (user?.write == true && user == message.homework.lastEditPerson) {
          val sanitized = message.homework.sanitized()
          HomeworkDB += sanitized
          broadcast(HomeworkAdded(sanitized))
        }
      }
      is DeleteHomework -> {
        if (user?.write == true) {
          HomeworkDB -= message.id
          broadcast(HomeworkDeleted(message.id))
        }
      }
      is EditHomework -> {
        if (user?.write == true && user == message.homework.lastEditPerson) {
          val sanitized = message.homework.sanitized()
          HomeworkDB[message.homework.id] = sanitized
          broadcast(HomeworkEdited(sanitized))
        }
      }
      is LoadHomework -> {
        if (user?.read == true) {
          send(HomeworkLoaded(HomeworkDB.getAllCurrent()), context)
        }
      }
    }
  }

  private fun auth(context: WebSocketSession) =
    users.filterValues { sessions -> context in sessions }.keys.firstOrNull()


  @UnstableDefault
  suspend fun handleConnect(call: ApplicationCall, context: WebSocketSession) {
    val token = call.request.cookies["user_sess"] ?: return run {
      send(AuthError("Unauthenticated"), context)
      context.close(CloseReason(403, "Unauthenticated"))
    }
    val authenticatedUser = Jwt.verifyAndDecode(token, DiscordUser.serializer())
      ?: return run {
        send(AuthError("Unauthenticated"), context)
        context.close(CloseReason(403, "Unauthenticated"))
      }
    val user =
      if (authenticatedUser.id == "00000")
        authenticatedUser.copy(read = true)
      else
        DiscordAuth.getAuthorization(authenticatedUser)
    if (!user.read) return run {
      send(AuthError("Unauthorized"), context)
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
    val user = auth(context) ?: return
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
