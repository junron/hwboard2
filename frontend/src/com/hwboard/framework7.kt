package com.hwboard

import com.hwboard.State.app
import externals.require
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.asList
import kotlin.browser.document
import kotlin.browser.localStorage
import kotlin.browser.window
import kotlin.js.json

object Framework7 {
  private val Panel = require("framework7/components/panel/panel.js")
  private val Swipeout = require("framework7/components/swipeout/swipeout.js")
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
              println("ok")
              //          prevDataHash = "";
//          if(Framework7App.ptr){
//            ptr = Framework7App.ptr.get('.page-current .ptr-content');
//            ptr.on("refresh",async (_,done)=>{
//              $("#hwboard-homework-list").html("<h2 class=homework-reload-status>Reloading homework...</h2>");
//              setTimeout(async ()=>{
//                await loadHomework(true);
//                done();
//              },300);
//            });
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
//    app.loadModule(Popup.default)
    app.views.create(".view-main")
    State.app = app
    return app
  }
}
