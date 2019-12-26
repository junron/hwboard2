package com.hwboard.auth

import com.auth0.jwt.JWT
import com.github.kittinunf.fuel.httpGet
import com.hwboard.ConfigLoader
import com.hwboard.DiscordUser
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.jetbrains.hub.oauth2.client.jersey.oauth2Client
import java.net.URI
import kotlin.random.Random

@UnstableDefault
object DiscordAuth {
  private val config = ConfigLoader.load().discord
  val oauthUrl = oauth2Client().codeFlowURI(
    authEndpoint = URI("https://discordapp.com/api/oauth2/authorize"),
    clientID = config.clientId,
    redirectURI = URI("${config.redirectUrl}/discord/token"),
    scope = listOf("identify"),
//      Hex state
    state = Random.nextBytes(16).joinToString("") {
      java.lang.String.format("%02x", it)
    }).toString()

  fun getToken(code: String): String {
    val (clientId, clientSecret, redirectUrl) = config
    val token = oauth2Client().codeFlow(
      tokenEndpoint = URI("https://discordapp.com/api/oauth2/token"),
      code = code,
      redirectURI = URI("$redirectUrl/discord/token"),
      clientID = clientId,
      clientSecret = clientSecret
    )
    return token.accessToken
  }

  fun authenticate(token: String): DiscordUser {
    val (_, _, result) =
      "https://discordapp.com/api/users/@me"
        .httpGet()
        .header("Authorization", "Bearer $token")
        .header("User-Agent", "DiscordBot (localhost,1.0)")
        .responseString()
    val data = result.component1() ?: return DiscordUser("anonymous", "unknown")
    return Json.nonstrict.parse(DiscordUser.serializer(), data)
  }

  fun getAuthorization(user: DiscordUser): DiscordUser {
    val (_, _, guildMemberResult) =
      "https://discordapp.com/api/guilds/${config.guildId}/members/${user.id}"
        .httpGet()
        .header("Authorization", "Bot ${config.botToken}")
        .header("User-Agent", "DiscordBot (localhost,1.0)")
        .responseString()
    val memberData = guildMemberResult.component1() ?: return user.copy()
    val guildMember = Json.nonstrict.parse(GuildMember.serializer(), memberData)
    val isAdmin = guildMember.roles.contains(config.adminRole)
    return user.copy(
      read = true,
      write = isAdmin,
      superuser = false,
      name = guildMember.nick ?: user.name
    )
  }
}

@UnstableDefault
fun DiscordUser.createJwt() = Jwt.sign(
  JWT.create()
    .withClaim("username", name)
    .withClaim("id", id)
)

@Serializable
data class GuildMember(val nick: String?, val roles: List<String>)
