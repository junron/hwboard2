package com.hwboard

import com.hwboard.WebsocketMessage.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import kotlin.browser.window

object FrontendWS {
  private lateinit var webSocket: WebSocket
  private lateinit var user: User
  private val callbacks = mutableListOf<(Unit) -> Unit>()
  private val messageCallbacks = mutableListOf<(WebsocketMessage) -> Unit>()

  @UnstableDefault
  fun websocketConnect() {
    val webSocketConnection = WebSocket(
      window.location.protocol.replace("http", "ws") +
          "//" + window.location.host.replace("9090", "8080") + "/websocket"
    )
    webSocketConnection.onopen = {
      webSocket = webSocketConnection
      window.asDynamic()["websocket"] = webSocket
      window.asDynamic()["frontendWs"] = FrontendWS
      null
    }
    webSocketConnection.onmessage = ::handle
  }

  @UnstableDefault
  private fun handle(event: MessageEvent) {
    GlobalScope.launch {
      val data = event.data as? String ?: return@launch
      when (val message = Json.parse(WebsocketMessage.serializer(), data)) {
        is Message -> {
          println(message)
        }
        is Connect -> {
          println("${message.user.name} connected")
        }
        is Disconnect -> {
          println("${message.user.name} disconnected")
        }
        is Error -> {
          println("Error occurred: ${message.message}")
        }
        is AuthError -> {
          window.location.href = "/discord/auth"
        }
        is Auth -> {
          println("Authenticated as ${message.user.name}")
          user = message.user
          State.user = user
          callbacks.forEach { it(Unit) }
        }
        else -> {
          messageCallbacks.forEach { it(message) }
        }
      }
    }
  }

  @UnstableDefault
  fun send(message: WebsocketMessage) {
    if (!::webSocket.isInitialized) return
    webSocket.send(Json.stringify(WebsocketMessage.serializer(), message))
  }

  fun onConnect(callback: (Unit) -> Unit) {
    callbacks += callback
  }

  fun onMessage(callback: (WebsocketMessage) -> Unit) {
    messageCallbacks += callback
  }

  fun onDisconnect(callback: (Any) -> Unit){
    webSocket.onclose = callback
  }

  @UnstableDefault
  fun addHomework(homework: Homework) {
    send(AddHomework(homework))
  }
}
