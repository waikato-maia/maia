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
