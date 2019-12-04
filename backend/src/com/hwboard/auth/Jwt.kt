package com.hwboard.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.github.kittinunf.fuel.util.decodeBase64ToString
import com.hwboard.ConfigLoader
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json

object Jwt {
  @UnstableDefault
  private val secret = ConfigLoader.load().jwtSecret
  @UnstableDefault
  private val algorithm = Algorithm.HMAC512(secret)
  @UnstableDefault
  private val verifier = JWT.require(algorithm).build()

  @UnstableDefault
  fun sign(token: JWTCreator.Builder) = token.sign(algorithm)!!

  @UnstableDefault
  fun <T> verifyAndDecode(token: String, serializer: DeserializationStrategy<T>): T? = verifier.verify(token)?.payload?.decodeBase64ToString()?.let {
    Json.parse(serializer, it)
  }
}
