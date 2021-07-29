package māia.main

import kotlin.reflect.KClass

class TestObj

const val NaNMask: Long = 0x7FF8000000000000
const val NaNUnmask: Long = NaNMask.inv()

fun NaNBox(value: Int): Double {
    return Double.fromBits(value.toLong() or NaNMask)
}

fun NaNUnbox(value: Double): Int {
    return (value.toRawBits() and NaNUnmask).toInt()
}


fun main() {

    println(System.identityHashCode(2))
    println(System.identityHashCode(2))

    println(NaNBox(0))

    val t = TestObj()
    val id = System.identityHashCode(t)

    println(id)

    val idBoxed = NaNBox(id)

    val test: Double? = idBoxed

    val cls : KClass<out TestObj> = t::class

    println(System.identityHashCode(t::class))
    println(System.identityHashCode(t::class))

    println(idBoxed)

    val idUnboxed = NaNUnbox(idBoxed)

    println(idUnboxed)
}
