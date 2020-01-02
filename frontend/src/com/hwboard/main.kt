package com.hwboard

import com.benasher44.uuid.uuid4
import com.hwboard.FrontendWS.websocketConnect
import com.hwboard.interop.moment.moment
import com.hwboard.interop.toDate
import externals.require
import kotlinx.serialization.UnstableDefault
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Date as JsDate
import com.hwboard.State.Homework
import com.hwboard.controller.MainController
import com.hwboard.controller.Swipeouts

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
