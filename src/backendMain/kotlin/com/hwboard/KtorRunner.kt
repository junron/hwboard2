package com.hwboard

import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.UnstableDefault

//Only for development
@UnstableDefault
fun main() {
  embeddedServer(
      Netty,
      watchPaths = listOf("build/classes/kotlin/backend/main"),
      port = 8080,
      module = Application::main
  ).apply { start(wait = true) }
}