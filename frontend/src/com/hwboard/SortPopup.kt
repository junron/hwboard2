package com.hwboard

import com.hwboard.State.Sort
import com.hwboard.State.app
import com.hwboard.utils.getCheckedValue
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.asList
import org.w3c.dom.get
import kotlin.browser.document

object SortPopup {
  fun init() {
    document.querySelectorAll("input[type=radio].sort-radio")
      .asList().forEach {
        (it as HTMLInputElement).checked = false
      }
    val sortType = Sort.sortType.serialize()
    val sortOrder = Sort.sortOrder
    (document.querySelector("input[type=radio][name=type][value='$sortType']")
        as HTMLInputElement).checked = true
    (document.querySelector("input[type=radio][name=order][value='$sortOrder']")
        as HTMLInputElement).checked = true

    app.loadModule(Framework7.importF7Module("input"))
    app.loadModule(Framework7.importF7Module("checkbox"))
    app.loadModule(Framework7.importF7Module("grid"))
    app.loadModule(Framework7.importF7Module("radio"))

    document.querySelector(".page-current #sort-confirm")
      ?.addEventListener("click", {
        Sort.setSortType(
          document.querySelectorAll("input[type=radio][name=type]")
            .getCheckedValue()
        )
        Sort.sortOrder = document.querySelectorAll("input[type=radio][name=order]")
          .getCheckedValue()?.toInt() ?: 0
        State.Homework.rerender()
        if ((document.querySelector("#sort-set-default") as HTMLInputElement).checked)
          Sort.saveState()
      })
  }
}
