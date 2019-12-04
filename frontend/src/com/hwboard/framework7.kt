package com.hwboard

import externals.ctor
import kotlin.js.json

object Framework7 {
  val Framework7: (dynamic) -> dynamic = {
    ctor(externals.require("framework7").default, it)
  }
  fun init(): dynamic{
    val app = Framework7(
      json(
        // App root element
        "root" to "#app",
        // App Name
        "name" to "My App",
        // App id
        "id" to "com.myapp.test",
        // Enable swipe panel
        "panel" to json(
          "swipe" to "left"
        ),
        "routes" to listOf(
          json(
            "path" to "/about/",
            "url" to "about.html"
          )
        )
      )
    )
    app.views.create(".view-main")
    return app
  }
}
