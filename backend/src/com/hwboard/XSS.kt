package com.hwboard

import com.benasher44.uuid.uuid4
import org.owasp.encoder.Encode

fun Homework.sanitized(): Homework {
  val isValid =
    Regex("/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\$/").matchEntire(id) != null
  return this.copy(
    id = if (isValid) id else uuid4().toString(),
    subject = Subject(Encode.forHtml(subject.name)),
    text = Encode.forHtml(text)
  )
}
