/*
 * TestNanBoxing.kt
 * Copyright (C) 2021 University of Waikato, Hamilton, New Zealand
 *
 * This file is part of MĀIA.
 *
 * MĀIA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MĀIA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MĀIA.  If not, see <https://www.gnu.org/licenses/>.
 */
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
