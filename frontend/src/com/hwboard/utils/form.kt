package com.hwboard.utils

import org.w3c.dom.HTMLInputElement
import org.w3c.dom.NodeList
import org.w3c.dom.asList
import pl.treksoft.jquery.JQuery

fun List<HTMLInputElement>.getCheckedValue(): String? =
  this.firstOrNull { it.checked }?.value

fun NodeList.getCheckedValue() =
  this.asList().map { it as HTMLInputElement }.getCheckedValue()


fun JQuery.addFloating(){
  parents().addClass("item-input-with-value")
}

fun JQuery.removeFloating(){
  parents().removeClass("item-input-with-value")
}
