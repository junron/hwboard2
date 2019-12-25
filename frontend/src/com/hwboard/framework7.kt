package com.hwboard

import externals.ctor
import externals.require
import kotlin.browser.window
import kotlin.js.json

object Framework7 {
  private val Panel = require("framework7/components/panel/panel.js")

  val Framework7: (dynamic) -> dynamic = {
    ctor(require("framework7").default, it)
  }

  fun init(): dynamic{
    val app = Framework7(
      json(
        // App root element
        "root" to "#app",
        "theme" to "md",
        // App Name
        "name" to "Hwboard",
        // App id
        "id" to "web.hwboard.hwboard2",
        "pushState" to true,
        // Enable swipe panel
        "view" to json(
          "pushState" to true,
          "mdSwipeBack" to true
        ),
        "routes" to listOf(
          json(
            "path" to "/about/",
            "url" to "about.html"
          )
        )
      )
    )
    app.loadModule(Panel.default)
    app.views.create(".view-main")
    return app
  }
}
