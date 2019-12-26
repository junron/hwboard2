package com.hwboard

import com.benasher44.uuid.uuid4
import com.hwboard.FrontendWS.websocketConnect
import com.hwboard.interop.moment.moment
import com.hwboard.interop.toDate
import kotlinx.serialization.UnstableDefault
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Date as JsDate


@UnstableDefault
fun main() {
  js("require('framework7/css/framework7.bundle.css')")
  js("require('style.css')")
  js("require('fonts.css')")
  js("require('homework.css')")
  window.asDynamic()["f7app"] = Framework7.init()
  window.asDynamic()["ws"] = FrontendWS

  websocketConnect()
  FrontendWS.onConnect {
    val homework = listOf(
      Homework(
        uuid4().toString(),
        Subject("English"),
        moment().add(3, "hours").toDate().toDate(),
        "Hello, world",
        listOf(Tag("Graded", "red")),
        State.user,
        JsDate().toDate()
      ),
      Homework(
        uuid4().toString(),
        Subject("English"),
        moment().add(1, "days").toDate().toDate(),
        "Hello, world",
        listOf(Tag("Graded", "red")),
        State.user,
        JsDate().toDate()
      ),
      Homework(
        uuid4().toString(),
        Subject("English"),
        moment().add(9, "days").toDate().toDate(),
        "Hello, world",
        listOf(Tag("Graded", "red")),
        State.user,
        JsDate().toDate()
      ),
      Homework(
        uuid4().toString(),
        Subject("Math"),
        moment().add(9, "weeks").toDate().toDate(),
        "Hello, world",
        listOf(Tag("Graded", "red")),
        State.user,
        JsDate().toDate()
      )
    )
    document.getElementById("hwboard-homework-list")
      ?.innerHTML = homework.render(SortType.Subject, false)
    State.app?.swipeout.init()
  }
}
