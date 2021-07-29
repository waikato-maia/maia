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
