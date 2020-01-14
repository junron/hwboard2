package com.hwboard.controller

import com.hwboard.FrontendWS.onConnect
import com.hwboard.FrontendWS.onDisconnect
import com.hwboard.FrontendWS.onMessage
import com.hwboard.FrontendWS.send
import com.hwboard.State
import com.hwboard.State.Homework
import com.hwboard.State.Homework.homework
import com.hwboard.WebsocketMessage.*
import kotlinx.serialization.UnstableDefault
import pl.treksoft.jquery.jQuery

@UnstableDefault
object MainController {
  private fun loadHomework() {
    send(LoadHomework)
  }

  fun init() {
    onConnect {
      if (!State.user.write) jQuery("#fab-add-homework").hide()
      jQuery("#connection-status").text("Connected")
      loadHomework()

      onDisconnect {
        jQuery("#connection-status").text("Disconnected")
      }
    }
    onMessage { message ->
      when (message) {
        is HomeworkAdded -> {
          println("Added homework ${message.homework}")
          homework += message.homework
        }
        is HomeworkEdited -> {
          println("Edited homework ${message.homework}")
          homework.map { if (it.id == message.homework.id) message.homework else it }
        }
        is HomeworkDeleted -> {
          println("Deleted homework ${homework.find { it.id == message.id }}")
          homework = homework.filter { it.id != message.id }
        }
        is HomeworkLoaded -> {
          homework = message.homework
          println("Loaded homework $homework")
        }
        else -> return@onMessage
      }
      Homework.rerender()
    }
  }
}
