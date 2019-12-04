package externals

external fun require(module: String): dynamic


@Suppress("unused")
fun ctor(d: dynamic, vararg args: dynamic): dynamic = js("new d(args);")
