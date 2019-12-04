package com.hwboard

import com.github.kittinunf.fuel.util.decodeBase64ToString
import com.hwboard.BackendWS.handleConnect
import com.hwboard.BackendWS.handleDisconnect
import com.hwboard.auth.DiscordAuth
import com.hwboard.auth.DiscordUser
import com.hwboard.auth.Jwt.verifyAndDecode
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.*
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.serialization.UnstableDefault

val ok = HttpStatusCode(200, "T0sgQk9PTUVS".decodeBase64ToString()!!)
val isJar = {}.javaClass.getResource("/assets/index.html") != null

@UnstableDefault
fun Application.main() {
  install(WebSockets)
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
        val authenticatedUser = verifyAndDecode(token, DiscordUser.serializer())
        if (authenticatedUser != null) {
          val user = DiscordAuth.getAuthorization(authenticatedUser)
          call.respondText(status = ok) { user.toString() }
          return@get
        }
      }
      call.respondText(status = HttpStatusCode.NotFound) { "Token not found" }
    }
    static("/static") {
      if (isJar) resources("/assets")
      else files("bundle")
    }
    static {
      if (isJar) defaultResource("/assets/index.html")
      else default("resources/main/index.html")
    }
    webSocket("/websocket") {
      handleConnect(call, this)
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
