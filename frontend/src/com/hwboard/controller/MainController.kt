package com.hwboard.controller

import com.hwboard.FrontendWS.onConnect
import com.hwboard.FrontendWS.onMessage
import com.hwboard.FrontendWS.send
import com.hwboard.State
import com.hwboard.State.Homework
import com.hwboard.State.Homework.homework
import com.hwboard.WebsocketMessage.*
import kotlinx.serialization.UnstableDefault

@UnstableDefault
object MainController {
  private fun loadHomework() {
    send(LoadHomework)
  }

  fun init() {
    onConnect {
      loadHomework()
    }
    onMessage { message ->
      when (message) {
        is HomeworkAdded -> {
          println("Added homework ${message.homework}")
          homework += message.homework
        }
        is HomeworkEdited -> {
          println("Edited homework ${message.homework}")
          homework.map { if(it.id == message.homework.id) message.homework else it }
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
