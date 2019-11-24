package com.hwboard

import com.hwboard.FrontendWS.websocketConnect
import kotlinx.serialization.UnstableDefault
import pl.treksoft.kvision.Application
import pl.treksoft.kvision.form.text.Text
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.panel.root
import pl.treksoft.kvision.startApplication

class App : Application() {
  private val stuff = mapOf(
      "Bustimings" to "m781470592-b26d2c71d0fefffab83c8245",
      "NUSH bot" to "m781616019-e680875f52ae18e8b8b172d1",
      "espace" to "m780418761-90a17bb00d6fd030bbf7327b"
  )

  @UnstableDefault
  override fun start() {
    root("kvapp") {
      val nameInput = Text(type = TextInputType.TEXT, label = "Name: ")
      nameInput.setEventListener {
        keydown = {
          if (it.keyCode == 13) {
            FrontendWS.auth(nameInput.value ?: "")
          }
        }
      }
      val messageInput = Text(type = TextInputType.TEXT, label = "Message: ")
      messageInput.setEventListener {
        keydown = {
          if (it.keyCode == 13) {
            val message = messageInput.value ?: ""
            FrontendWS.sendMessage(message, User("user2"))
          }
        }
      }
      add(nameInput)
      add(messageInput)
    }
    websocketConnect()
  }
}

fun main() {
  startApplication(::App)
}
