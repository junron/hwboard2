package com.hwboard

import com.hwboard.WebsocketMessage.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.UnstableDefault
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import kotlin.browser.window

object FrontendWS {
  private lateinit var webSocket: WebSocket
  private lateinit var user: User

  @UnstableDefault
  fun websocketConnect() {
    val webSocketLocal = WebSocket(
      window.location.protocol.replace("http", "ws") +
          "//" + window.location.host.replace("9090","8080") + "/websocket")
    webSocketLocal.onopen = {
      webSocket = webSocketLocal
      window.asDynamic()["websocket"] = webSocket
      window.asDynamic()["frontendWs"] = FrontendWS
      null
    }
    webSocketLocal.onmessage = ::handle
  }

  @UnstableDefault
  private fun handle(event: MessageEvent) {
    GlobalScope.launch {
      val data = event.data as? String ?: return@launch
      val (message) = json.parse(MessageWrapper.serializer(), data)
      when (message) {
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
        is Auth -> {
          println("Authenticated as ${message.user.name}")
          user = message.user
        }
      }
    }
  }

  @UnstableDefault
  fun auth(name: String) {
    send(Auth(User(name)))
  }

  @UnstableDefault
  fun sendMessage(message: String, recipient: User) {
    send(Message(message, user, recipient))
  }

  @UnstableDefault
  private fun send(message: WebsocketMessage) {
    if (!::webSocket.isInitialized) return
    val messageWrapper = MessageWrapper(message)
    webSocket.send(json.stringify(MessageWrapper.serializer(), messageWrapper))
  }
}
