package com.hwboard.interop

import kotlin.js.Date as JsDate


fun JsDate.toDate() = Date(this.toISOString())
fun Date.toJsDate() = JsDate(JsDate.parse(this.date))
