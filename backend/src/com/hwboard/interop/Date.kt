package com.hwboard.interop

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.toDate() = Date(this.format(DateTimeFormatter.ISO_DATE_TIME))

fun Date.toLocalDateTime(): LocalDateTime =
  LocalDateTime.parse(this.date, DateTimeFormatter.ISO_DATE_TIME)
