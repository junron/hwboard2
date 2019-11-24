package com.hwboard.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.hwboard.ConfigLoader
import kotlinx.serialization.UnstableDefault

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
  fun verify(token: String) = verifier.verify(token)!!
}
