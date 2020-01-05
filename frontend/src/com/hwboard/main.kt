package com.hwboard

import com.hwboard.FrontendWS.websocketConnect
import com.hwboard.controller.MainController
import com.hwboard.controller.Swipeouts
import kotlinx.serialization.UnstableDefault
import kotlin.browser.window

@UnstableDefault
fun main() {
  js("require('framework7/css/framework7.bundle.css')")
  js("require('style.css')")
  js("require('fonts.css')")
  js("require('homework.css')")
  window.asDynamic()["f7app"] = Framework7.init()
  window.asDynamic()["ws"] = FrontendWS
  window.asDynamic()["state"] = State

  websocketConnect()
  MainController.init()
  Swipeouts.init()
}
