/*
 * MainPresentation2.kt
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

import māia.topology.buildTopology
import māia.topology.node.standard.Printer
import māia.topology.node.standard.test.DummyHotSource
import māia.topology.node.standard.test.PassThrough


fun main() {

    val topology = buildTopology {

        val source = DummyHotSource {
            name = "source"
            intervalMillis = 20
        }

        val pass = PassThrough<Any?> {
            name = "pass"
        }

        val print = Printer {
            name = "print"
            prefix = "source produced: "
        }

        //print subscribesTo source

        //print.primaryInput subscribesTo source.primaryOutput

        source - pass - print

    }

    topology.execute()

}
