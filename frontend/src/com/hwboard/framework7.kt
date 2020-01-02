package com.hwboard

import com.hwboard.controller.EditHomework
import com.hwboard.controller.SortPopup
import externals.require
import kotlin.browser.window
import kotlin.js.json

object Framework7 {
  private val Panel = require("framework7/components/panel/panel.js")
  private val Swipeout = require("framework7/components/swipeout/swipeout.js")
  private val Input = require("framework7/components/input/input.js")
  private val Popup = require("framework7/components/popup/popup.js")

  fun importF7Module(name: String) = require("framework7/components/$name/$name.js")

  fun init(): dynamic {

    window.asDynamic()["f7appConfig"] = json(
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
      "routes" to arrayOf(
        json(
          "name" to "home",
          "path" to "/",
          "reloadPrevious" to true,
          "animate" to false,
          "url" to "index.html",
          "on" to json(
            "pageBeforeIn" to { e: dynamic ->
              println(e)
            }
          ),
          "routes" to arrayOf(
            json(
              "name" to "sort",
              "path" to "/pages/sort/",
              "url" to js("require('pages/sort.html')").default,
              "on" to json(
                "pageAfterIn" to {
                  //Uncheck all
                  SortPopup.init()
                })
            ),
            json(
              "name" to "edit-homework",
              "path" to "/pages/edit-homework/",
              "url" to js("require('pages/edit-homework.html')").default,
              "on" to json(
                "pageAfterIn" to {
                  EditHomework.init()
                },
                "pageBeforeIn" to {
                  EditHomework.beforeInit()
                }
              )
            )

          )
        )
      )
    )


    val app = js(
      """
    var Framework7 = require("framework7").default;
    new Framework7(f7appConfig)
    """
    )
    app.loadModule(Panel.default)
    app.loadModule(Swipeout.default)
    app.loadModule(importF7Module("input").default)
    app.loadModule(importF7Module("grid").default)
//    app.loadModule(Popup.default)
    app.views.create(".view-main")
    State.app = app
    return app
  }
}
