package com.hwboard.controller

import com.hwboard.*
import com.hwboard.Constants.subjects
import com.hwboard.Constants.tags
import com.hwboard.State.app
import com.hwboard.interop.toDate
import com.hwboard.utils.removeFloating
import com.willowtreeapps.fuzzywuzzy.diffutils.FuzzySearch
import kotlinx.serialization.UnstableDefault
import pl.treksoft.jquery.JQueryEventObject
import kotlin.browser.document
import kotlin.js.Date
import kotlin.js.json
import pl.treksoft.jquery.jQuery as jq

object EditHomework {
  private val Date = externals.require("sugar-date").Date
  var homeworkEdited: Homework? = null

  fun beforeInit() {
    app.loadModule(Framework7.importF7Module("smart-select").default)
    app.loadModule(Framework7.importF7Module("checkbox").default)
    app.loadModule(Framework7.importF7Module("popup").default)
  }

  @UnstableDefault
  fun init() {
    app.input.init()
    reset()
    initSubjectAutocomplete()
    initTags()
    initDate()
    updateButtonStatus()
    jq(document).on("click", ".page-current #update-homework-button") { _: JQueryEventObject, _: Any ->
      if (homeworkEdited == null) {
        FrontendWS.addHomework(getHomework())
        app.views.main.router.back()
      }
    }
    jq(document).on("input", ".page-current input") { _: JQueryEventObject, _: Any ->
      updateButtonStatus()
    }
  }

  private fun initDate() {
    jq(document).on("input", ".page-current #dueDate") { _: JQueryEventObject, _: Any ->
      val date = getDate()
      if (Date.isValid(date) as Boolean && Date.isFuture(date) as Boolean) {
        jq(".page-current .date-input").removeClass("item-input-invalid")
        jq(".page-current #date-input-info").text(Date.short(date) as String)
      } else {
        jq(".page-current .date-input").addClass("item-input-invalid")
        jq(".page-current #due-date-validation-err").text("Invalid date")
      }
    }
  }

  private fun initTags() {
    app.smartSelect.create(
      json(
        "el" to "#selectTagsElem",
        "openIn" to "popup",
        "on" to json(
          "open" to {
            jq(".view.view-main").append(jq(".popup-backdrop.backdrop-in"))
          }
        )
      )
    )
    jq("#selectTagsElem select").html(tags.joinToString("") {
      "<option>${it.name}</option>"
    })
  }

  private fun initSubjectAutocomplete() {
    app.loadModule(Framework7.importF7Module("autocomplete").default)
    app.loadModule(Framework7.importF7Module("list-index").default)
    app.autocomplete.create(
      json(
        "openIn" to "dropdown",
        "source" to fun(query: String, render: (Array<String>) -> Unit) {
          if (query.isBlank()) return render(subjects.toTypedArray())
          return render(
            FuzzySearch.extractSorted(query, subjects, 70)
              .map { it.string!! }.toTypedArray()
          )
        },
        "inputEl" to "#subject-name"
      )
    )
  }

  private fun isValid() =
    jq("#subject-name").`val`().toString().isNotBlank() &&
        jq("#subject-name").`val`().toString() in subjects &&
        jq("#homework-name").`val`().toString().isNotBlank() &&
        Date.isValid(getDate()) as Boolean && Date.isFuture(getDate()) as Boolean

  private fun updateButtonStatus() {
    with(jq(".page-current #update-homework-button")) {
      if (isValid()) {
        this.removeAttr("disabled")
        this.removeClass("disabled")
      } else {
        this.attr("disabled", "true")
        this.addClass("disabled")
      }
    }
  }

  private fun getHomework() =
    Homework(
      "",
      Subject(jq("#subject-name").`val`().toString()),
      (getDate() as Date).toDate(),
      jq("#homework-name").`val`().toString().trim(),
      jq(".page-current #selectTagsElem .item-inner .item-after").text().split(", ")
        .mapNotNull { tagName ->
          tags.find { it.name == tagName }
        },
      State.user,
      Date().toDate()
    )

  private fun getDate() = Date.endOfDay(Date.create(jq(".page-current #dueDate").`val`().toString()))


  private fun reset() {
    listOf(".page-current #subject-name", ".page-current #dueDate", ".page-current #homework-name")
      .forEach { jq(it).removeFloating() }
    homeworkEdited = null
  }
}
