/*
 * MainTestSource.kt
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

/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
fun main() {

    val inputs = TestInputs(
        5,
        5,
        100_000,
        1,
        1,
        1,
        100,
        100,
        50,
        false
    )

    val moa = measure(::MOASourceTest, inputs, HandleErrorsBy.THROW)
    val moana = measure(::MOANASourceTest, inputs, HandleErrorsBy.THROW)

    println("MOA: $moa")
    println("MOANA: $moana")

}

fun testSource(inputs: TestInputs, source: () -> Any?) {
    for (index in 0 until inputs.maxInstances) {
        printlnmulti(
            index,
            System.identityHashCode(source())
        )
    }
}

fun MOASourceTest(inputs : TestInputs) {
    val source = moaSource(inputs)

    testSource(inputs, source::nextInstance)
}

fun MOANASourceTest(inputs: TestInputs) {
    val source = moanaSource(inputs).rowIterator()

    testSource(inputs, source::next)
}
