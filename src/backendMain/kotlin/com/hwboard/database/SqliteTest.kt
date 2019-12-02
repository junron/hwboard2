package com.hwboard.database

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.Connection

object Users : IntIdTable() {
  val name = varchar("name", 100).index()
  val read = bool("read")
//  val write = bool("write")
//  val superuser = bool("superuser")
}

class TestUser(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<TestUser>(Users)

  var name by Users.name
  var read by Users.read
}


fun main() {
  Database.connect(
      "jdbc:sqlite:${File(".").absolutePath}/test.db",
      driver = "org.sqlite.JDBC"
  )
  TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
  transaction {
    addLogger(StdOutSqlLogger)
    SchemaUtils.create(Users)
    TestUser.new {
      name = "jro"
      read = true
    }
    println(TestUser.all().forEach { println(it.name) })
  }

}
