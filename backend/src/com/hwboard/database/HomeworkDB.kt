package com.hwboard.database

import com.hwboard.Homework
import com.hwboard.interop.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import java.io.File
import java.time.LocalDateTime

object HomeworkDB {
  private val file = if (File("data").exists())
    File("data/homework.json")
  else
    File("../data/homework.json")

  private val serializer = Homework.serializer().list

  private fun readFile() = file.readText()
  private fun writeFile(data: List<Homework>) = file.writeText(
    Json.indented.stringify(serializer, data)
  )

  operator fun get(id: String) =
    getAll().find { it.id == id }

  fun getAllCurrent() = getAll().filter {
    it.dueDate.toLocalDateTime().isAfter(LocalDateTime.now())
  }
  private fun getAll() = Json.indented.parse(serializer, readFile())

  operator fun minusAssign(id: String) {
    writeFile(
      getAll().filter { it.id != id }
    )
  }

  operator fun plusAssign(value: Homework) {
    writeFile(
      getAll() + value
    )
  }

  operator fun set(id: String, value: Homework) {
    writeFile(
      getAll().filter { it.id != id } + value
    )
  }
}
