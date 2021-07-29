package māia.main

import māia.topology.buildTopology
import māia.topology.node.standard.Delay
import māia.topology.node.standard.Printer
import māia.topology.node.standard.routing.Alternate
import māia.topology.node.standard.routing.Buffer
import māia.topology.node.standard.routing.LetPassForRange
import māia.topology.node.standard.routing.Zip
import māia.topology.node.standard.test.DummyHotSource
import māia.topology.node.standard.test.InstantCloseSink
import māia.topology.node.standard.test.InstantCloseSource


/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
fun main() {

    buildTopology {

        val icSource = InstantCloseSource<String> {
            name = "icSource"
        }

        val icPrinter = Printer {
            name = "ic printer"
        }

        icSource - icPrinter

        val source = DummyHotSource {
            name = "source"
            intervalMillis = 500
        }

        val buffer = Buffer<Long> {
            name = "buffer"
            size = 1000
        }

        val delay = Delay<Long> {
            name = "delay"
            timeMillis = 1000
        }

        val pass = LetPassForRange<Long> {
            name = "pass"
            start = 0
            endInclusive = 60
        }

        val alternate = Alternate<Long> {
            name = "al"
            numOutputs = 3
            start = 0
            skipClosed = false
        }

        for (i in 0 until alternate.configuration.numOutputs) {
            alternate.outputs[i] - LetPassForRange {
                start = 0
                endInclusive = (i + 1) * 4L
            } - Printer {
                name = "p$i"
                suffix = "\n"
            }
        }

        val zip = Zip<Long, Long> {
            name = "zip"
        }

        val zipPrinter = Printer {
            name = "zip printer"
        }

        val sink = InstantCloseSink {
            name = "sink"
        }

        alternate.outputs[0] - zip.inputA
        alternate.outputs[1] - zip.inputB
        zip - sink

        source - buffer - delay - pass - alternate
    }.execute()

}
