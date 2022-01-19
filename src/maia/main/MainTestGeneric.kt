/*
 * MainTestGeneric.kt
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
package maia.main

import maia.util.createType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class Test {}

fun Test.bla() : String {
    return "bla $this"
}

fun bla2(i : Test) : String {
    return "bla2 $i"
}

fun main() {

    val t = createType<Test.() -> String>(listOf(
            KTypeProjection.invariant(Test::class.createType()),
            KTypeProjection.invariant(String::class.createType())
    ))
    val t2 = createType<(Test) -> String>(listOf(
            KTypeProjection.invariant(Test::class.createType()),
            KTypeProjection.invariant(String::class.createType())
    ))
    val t3 = Test::bla::class.createType(listOf(
            KTypeProjection.invariant(Test::class.createType()),
            KTypeProjection.invariant(String::class.createType())
    ))
    println(t == t2)

}
