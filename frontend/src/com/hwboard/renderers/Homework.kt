package com.hwboard.renderers

import com.hwboard.*
import kotlin.browser.window
import kotlin.reflect.typeOf

fun Homework.render(sortType: SortType, admin: Boolean): String {
  val Date = externals.require("sugar-date").Date
  val baseHomeworkRender = """
<li class="hwitem swipeout" homeworkId="$id">
<div class="swipeout-content item-content">
<div class="item-media">
<i class="material-icons" style="color:
${when {
    displayDate() == "today" -> "red"
    displayDate() == "tomorrow" -> "#ab47bc"
    else -> "#00000063"
  }}" aria-hidden="true">&#xe873;</i>
</div>
<div class="item-inner">
<div class="item-title">
$text
<div class="item-footer">
${when (sortType) {
    SortType.Date -> {
      """
        <div class="chip" style="background-color:#26c6da">
        <div class="chip-label" style="color:white">${subject.name}</div>
        </div>
      """.trimIndent()
    }
    SortType.Subject -> {
      """Due ${displayDate()}
      (${Date.format(Date.create(dueDate.date),"{d}/{M}")})
      """.trimIndent()
    }
  }}
  
${tags.render()}
</div>
</div>
</div>
</div>
<!--<div class="swipeout-actions-left swipeout-info-button">
<a class="swipeout-close swipeout-overswipe" style="background-color:#2196f3">Info</a>
</div>-->
""".trimIndent()
  return if (admin)
    baseHomeworkRender + """<div class="swipeout-actions-right">
    <!--<a class="swipeout-close swipeout-edit-button" style="background-color:#ff9800">Edit</a>-->
    <a class="swipeout-close swipeout-delete-button" style="background-color:#f44336">Delete</a>
    </div>
    </li>
    """.trimIndent()
  else
    "$baseHomeworkRender</li>"
}

fun Homework.displayDate(): String {
  val Date = externals.require("sugar-date").Date
  val date = Date.create(dueDate.date)
  val daysLeft = Date.daysFromNow(date)
  return when {
    Date.isToday(date) as Boolean -> "today"
    Date.isTomorrow(date) as Boolean -> "tomorrow"
    else -> "in $daysLeft days"
  }
}

fun List<Homework>.render(
  sortType: SortType,
  reversed: Boolean
): String {
  if (isEmpty()) return "<div style='text-align: center;font-size:2em;margin:0.67em'>No homework yay</div>"
  return when (sortType) {
    SortType.Subject -> {
      var result = ""
      val subjects = mutableListOf<Subject>()
      var sorted = this.sortedWith(
        compareBy(
          { it.subject.name },
          { it.dueDate.date }
        )
      )
      if (reversed) sorted = sorted.reversed()
      sorted.forEach {
        val subject = it.subject
        if (subject !in subjects) {
          if (subjects.isNotEmpty()) {
            result += "</ul></div>"
          }
          val subjectId = "hex${subject.toHex()}"
          result += """
              <div class="list-group">
              <ul id="$subjectId">
              <li style="padding-top:5px" class="list-group-title">${subject.name}</li>
          """.trimIndent()
          subjects += subject
        }
        result += it.render(SortType.Subject, State.user.write)
      }
      result
    }
    SortType.Date -> {
      var result = ""
      val dates = mutableListOf<String>()
      var sorted = this.sortedWith(
        compareBy(
          { it.dueDate.date },
          { it.subject.name }
        )
      )
      if (reversed) sorted = sorted.reversed()
      sorted.forEach {
        val date = it.displayDate()
        if (date !in dates) {
          if (dates.isNotEmpty()) {
            result += "</ul></div>"
          }
          result += """
              <div class="list-group">
              <ul id="$date">
              <li style="padding-top:5px" class="list-group-title">Due $date</li>
          """.trimIndent()
          dates += date
        }
        result += it.render(SortType.Date, State.user.write)
      }
      result
    }
  } + "</div></ul>"
}

fun List<Tag>.render(): String {
  var result = ""
  this.forEach {
    val tinycolor = externals.require("tinycolor2")
    val textColor = if ((tinycolor.readability(it.color, "#fff") as Double) < 2.0)
      "black"
    else
      "white"
    result += """
      <div class="chip" style="background-color:${it.color};color:$textColor">
       <div class="chip-label">${it.name}</div>
       </div>
    """.trimIndent()
  }
  return result
}
