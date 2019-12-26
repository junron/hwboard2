package com.hwboard

import com.benasher44.uuid.uuid4
import com.hwboard.FrontendWS.websocketConnect
import com.hwboard.interop.toDate
import kotlinx.serialization.UnstableDefault
import kotlin.browser.window
import kotlin.js.Date


@UnstableDefault
fun main() {
  js("require('framework7/css/framework7.bundle.css')")
  js("require('style.css')")
  js("require('fonts.css')")
  window.asDynamic()["f7app"] = Framework7.init()
  window.asDynamic()["ws"] = FrontendWS

  websocketConnect()
  FrontendWS.onConnect {
    println("test")
    println(Homework(
      uuid4(),
      Subject("English"),
      Date().toDate(),
      "Hello, world",
      listOf(Tag("Graded","red")),
      State.user,
      Date().toDate()
    ))
  }
}
