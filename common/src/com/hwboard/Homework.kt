package com.hwboard

import com.hwboard.interop.Date
import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import kotlinx.serialization.toUtf8Bytes

@Serializable
data class Homework(
  @ContextualSerialization
  val id: String,
  val subject: Subject,
  val dueDate: Date,
  val text: String,
  val tags: List<Tag>,
  val lastEditPerson: User,
  val lastEditTime: Date
)

enum class SortType {
  Subject, Date;

  companion object {
    fun deserialize(name: String) =
      when(name){
        "Due date" -> Date
        "Subject name" -> Subject
        else -> null
      }
  }
  fun serialize(): String {
    return when(this){
      Subject -> "Subject name"
      Date -> "Due date"
    }
  }
}

@Serializable
data class Subject(val name: String) {
  fun toHex() {
    name.toUtf8Bytes().joinToString("") {
      it.toInt().toString(16).padStart(2, '0')
    }
  }
}

@Serializable
data class Tag(val name: String, val color: String)
