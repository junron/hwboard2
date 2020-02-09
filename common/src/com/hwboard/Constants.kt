package com.hwboard

object Constants {
  val subjects = listOf(
    "Math",
    "English",
    "Chinese",
    "Higher chinese",
    "CS",
    "Physics",
    "Chemistry",
    "PE"
  ).sorted()

  val tags = listOf(
    Tag("Graded", "red"),
    Tag("Project", "#ffcc00"),
    Tag("Optional", "#4cd964"),
    Tag("Assessment", "#f18e33")
  )
}
