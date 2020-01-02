package com.hwboard.database

import com.hwboard.Homework
import com.toddway.shelf.FileStorage
import com.toddway.shelf.KotlinxSerializer
import com.toddway.shelf.Shelf
import com.toddway.shelf.get
import java.io.File

object HomeworkDB {
  private val database = Shelf(FileStorage(
    if (File("data/homework").exists())
      File("data/homework")
    else
      File("../data/homework")
  ), KotlinxSerializer().apply {
    register(Homework.serializer())
  })

  operator fun get(id: String) =
    database.item(id).get<Homework>()

  fun getAll() = database.all().map { it.get<Homework>()!! }

  operator fun minusAssign(id: String) {
    database.item(id).remove()
    database.item(id).remove()
  }

  operator fun plusAssign(value: Homework) {
    database.item(value.id).put(value)
  }

  operator fun set(id: String, value: Homework) {
    database.item(id).put(value)
  }
}
