package com.hwboard

import com.hwboard.SortType.Companion.deserialize
import com.hwboard.renderers.render
import kotlin.browser.document
import kotlin.browser.localStorage

object State {
  var app: dynamic = null
  lateinit var user: User

  object Sort {
    var sortType = deserialize(localStorage.getItem("sortType") ?: "") ?: SortType.Date
      private set

    fun setSortType(value: String?) {
      sortType = deserialize(value ?: "") ?: SortType.Date
    }

    var sortOrder = localStorage.getItem("sortOrder")?.toIntOrNull() ?: 0

    fun saveState() {
      localStorage.setItem("sortType", sortType.serialize())
      localStorage.setItem("sortOrder", sortOrder.toString())
    }
  }

  object Homework {
    var homework = listOf<com.hwboard.Homework>()

    fun rerender() {
      document.getElementById("hwboard-homework-list")
        ?.innerHTML = this.homework.render(Sort.sortType, Sort.sortOrder == 1)
      app?.swipeout.init()
    }
  }
}
