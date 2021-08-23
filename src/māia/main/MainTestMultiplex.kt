/*
 * MainTestMultiplex.kt
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
import māia.topology.node.standard.Constant
import māia.topology.node.standard.Delay
import māia.topology.node.standard.ItemCounter
import māia.topology.node.standard.Printer
import māia.topology.node.standard.routing.LetPassForRange
import māia.topology.node.standard.routing.Multiplex
import māia.topology.node.standard.test.DummyHotSource


/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
fun main() {


    buildTopology {

        val delay = Delay<String> {
            name = "delay"
            timeMillis = 500
        }

        val constant = Constant<String> {
            name = "constant"
            constant = "Constant String"
        }

        val pass = LetPassForRange<Int> {
            name = "pass"
            endInclusive = 20
        }

        val multiplex = Multiplex<String> {
            name = "multiplex"
            size = 4
            closeOnSelectClose = true
        }

        val source = DummyHotSource {
            name = "source"
            intervalMillis = 1000
        }

        val counter = ItemCounter {
            name = "counter"
            start = 0
            stop = multiplex.configuration.size
            resetOnStop = true
        }

        for (i in 0 until multiplex.configuration.size) {
            multiplex.outputs[i] - Printer {
                name = "p$i"
            }
        }

        source - counter - pass - multiplex.selectInput
        constant - delay - multiplex

    }.execute()

}
