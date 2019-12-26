package com.hwboard

import com.benasher44.uuid.Uuid
import com.hwboard.interop.Date
import kotlinx.serialization.Serializable

data class Homework(
  val id: Uuid,
  val subject: Subject,
  val dueDate: Date,
  val text: String,
  val tags: List<Tag>,
  val lastEditPerson: User,
  val lastEditTime: Date
)

@Serializable
data class Subject(val name: String)

@Serializable
data class Tag(val name: String, val color: String)
