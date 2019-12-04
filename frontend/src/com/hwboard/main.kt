package com.hwboard

import com.hwboard.FrontendWS.websocketConnect
import kotlinx.serialization.UnstableDefault
import kotlin.browser.window


@UnstableDefault
fun main() {
  js("require('framework7/css/framework7.bundle.css')")
  js("require('style.css')")
  window.asDynamic()["f7app"] = Framework7.init()
  window.asDynamic()["ws"] = FrontendWS
  websocketConnect()
}
