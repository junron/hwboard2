package com.hwboard

import com.github.kittinunf.fuel.util.decodeBase64ToString
import com.hwboard.BackendWS.handleDisconnect
import com.hwboard.auth.DiscordAuth
import com.hwboard.auth.DiscordUser
import com.hwboard.auth.Jwt
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.Cookie
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.*
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.websocket.webSocket
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import pl.treksoft.kvision.remote.kvisionInit

@UnstableDefault
fun Application.main() {
  kvisionInit()

  val isJar = {}.javaClass.getResource("/assets/index.html") != null
  routing {
    get("/discord/token") {
      val code = call.parameters["code"] ?: return@get
      val token = DiscordAuth.getToken(code)
      val user = DiscordAuth.authenticate(token)
      call.response.cookies.append(
          Cookie(
              "user_sess",
              user.createJwt(),
              httpOnly = true,
//              1 month
              maxAge = 2_629_746,
              path = "/"
          )
      )
      call.respondRedirect("/verify", false)
    }
    get("/discord/auth") {
      call.respondRedirect(DiscordAuth.oauthUrl, false)
    }
    get("/verify") {
      val token = call.request.cookies["user_sess"]
      if (token != null) {
        val payload = Jwt.verify(token).payload.decodeBase64ToString()
        if (payload != null) {
          val authenticatedUser = Json.parse(DiscordUser.serializer(), payload)
          val user = DiscordAuth.getAuthorization(authenticatedUser)
          call.respondText { user.toString() }
          return@get
        }
      }
      call.respondText { "Token not found" }
    }
    static("/static") {
      if (isJar) resources("/assets")
      else files("bundle")
    }
    static {
      if (isJar) defaultResource("/assets/index.html")
      else default("resources/main/index.html")
    }
    webSocket("/") {
      for (frame in incoming) {
        when (frame) {
          is Frame.Text -> {
            val text = frame.readText()
            val message = json.parse(MessageWrapper.serializer(), text)
            BackendWS.handle(message.m, this)
          }
        }
      }
      handleDisconnect(this)
    }
  }
}
