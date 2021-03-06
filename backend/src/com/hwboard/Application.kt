package com.hwboard

import com.github.kittinunf.fuel.util.decodeBase64ToString
import com.hwboard.BackendWS.handleConnect
import com.hwboard.BackendWS.handleDisconnect
import com.hwboard.auth.DiscordAuth
import com.hwboard.auth.Jwt.verifyAndDecode
import com.hwboard.auth.createJwt
import com.hwboard.database.HomeworkDB
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.defaultResource
import io.ktor.http.content.files
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json

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
      call.respondRedirect("/", false)
    }
    get("/discord/auth") {
      call.respondRedirect(DiscordAuth.oauthUrl, false)
    }
    get("/view") {
      call.response.cookies.append(
        Cookie(
          "user_sess",
          DiscordUser("Anonymous", "00000").createJwt(),
          httpOnly = true,
//              1 month
          maxAge = 2_629_746,
          path = "/"
        )
      )
      call.respondRedirect("/", false)
    }
    get("/logout") {
      call.response.cookies.appendExpired("user_sess")
      call.respondRedirect("/discord/auth")
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
    static("/frontend") {
      if (isJar) resources("/assets/bundle")
      else files("../frontend/build/bundle")
    }
    static {
      if (isJar) defaultResource("/assets/index.html")
      else defaultResource("/web/index.html")
    }
    webSocket("/websocket") {
      handleConnect(call, this)
      for (frame in incoming) {
        when (frame) {
          is Frame.Text -> {
            val text = frame.readText()
            val message = Json.parse(WebsocketMessage.serializer(), text)
            BackendWS.handle(message, this)
          }
        }
      }
      handleDisconnect(this)
    }
  }
}
