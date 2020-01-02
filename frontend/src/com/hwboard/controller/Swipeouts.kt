package com.hwboard.controller

import com.hwboard.FrontendWS
import com.hwboard.WebsocketMessage
import pl.treksoft.jquery.JQueryEventObject
import kotlin.browser.document
import pl.treksoft.jquery.jQuery as jq

object Swipeouts {
  fun init() {
    jq(document).on("click",".swipeout-delete-button"){ event: JQueryEventObject, _: Any ->
      val id = jq(event.target).parents(".hwitem").attr("homeworkid")
      println(id)
      FrontendWS.send(WebsocketMessage.DeleteHomework(id))
      return@on Unit
    }
  }
}
