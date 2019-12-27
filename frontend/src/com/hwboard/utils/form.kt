package com.hwboard.utils

import org.w3c.dom.HTMLInputElement
import org.w3c.dom.NodeList
import org.w3c.dom.asList

fun List<HTMLInputElement>.getCheckedValue(): String? =
  this.firstOrNull { it.checked }?.value

fun NodeList.getCheckedValue() =
  this.asList().map { it as HTMLInputElement }.getCheckedValue()
