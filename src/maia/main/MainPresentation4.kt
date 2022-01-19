/*
 * MainPresentation4.kt
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

import maia.configure.initialise
import maia.ml.dataset.DataRow
import maia.ml.dataset.DataStream
import maia.ml.learner.standard.ConfigurableZeroRLearnerFactory
import maia.ml.learner.standard.ZeroRConfiguration
import maia.topology.buildTopology
import maia.topology.node.standard.Printer
import maia.topology.node.standard.Sequential
import maia.topology.node.standard.ml.dataset.ARFFSource
import maia.topology.node.standard.ml.dataset.BatchViewRows
import maia.topology.node.standard.ml.dataset.FormatDataRow
import maia.topology.node.standard.ml.dataset.InitialiseOnFirst
import maia.topology.node.standard.ml.dataset.IterateRows
import maia.topology.node.standard.ml.learner.LearnerNode
import maia.topology.node.standard.ml.learner.NewLearner
import maia.topology.node.standard.routing.LetPassForRange
import maia.topology.node.standard.routing.Split
import maia.util.getResourceStatic


fun main() {

    val topology = buildTopology {

        /*
         * Load the Iris dataset, iterate over its rows, and
         * filter out the first 75 rows
         */

        val irisURL = getResourceStatic("/iris.arff")
            ?: throw Exception("Could not find resource '/iris.arff'")
        val source = ARFFSource {
            name = "source"
            filename = irisURL.file
        }

        val toRows = IterateRows {
            name = "rows"
        }

        val pass = LetPassForRange<DataRow> {
            name = "pass"
            start = 75
            endInclusive = 111111
            invert = false
        }

        source - toRows - pass

        /*
         * Create a learner and pass it to a learner node
         */

        val learnerConfig = initialise<ZeroRConfiguration> {
            targetIndex = 4
        }

        val learnerFactory = NewLearner {
            name = "learnerFactory"
            factoryClass = ConfigurableZeroRLearnerFactory::class
            learnerConfiguration = learnerConfig
        }

        val learnerNode = LearnerNode {
            name = "learner"
        }

        learnerFactory - learnerNode.learnerInput

        // View each row as a dataset, initialise the learner on the
        // first row, and pass each row to first train and then predict

        val batchViewRows = BatchViewRows {
            name = "bvr"
        }

        val init = InitialiseOnFirst<DataStream<*>> {
            name = "init"
        }

        val seq = Sequential<DataStream<*>> {
            name = "seq"
        }

        pass - batchViewRows - init - seq
        init.initialise - learnerNode.initialise
        seq.output1 - learnerNode.train
        seq.output2 - learnerNode.predictionInput

        /*
         * Split the prediction from the predictor, format it as
         * a string and print it
         */

        val split = Split<DataRow, DataRow> {
            name = "split"
        }

        val format = FormatDataRow {
            name = "format"
        }

        val printer = Printer {
            name = "printer"
            prefix = "Prediction - "
            suffix = " is the class"
        }


        learnerNode.predictionOutput - split
        split.second - format - printer

    }

    topology.execute()
}
