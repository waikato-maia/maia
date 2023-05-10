/*
 * MainImperative2.kt
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

import kotlinx.coroutines.runBlocking
import maia.configure.initialise
import maia.ml.dataset.DataBatch
import maia.ml.dataset.DataRow
import maia.ml.dataset.DataStream
import maia.ml.dataset.arff.load
import maia.ml.dataset.util.formatString
import maia.ml.dataset.view.readOnlyViewRows
import maia.ml.dataset.view.viewAsDataBatch
import maia.ml.learner.standard.ConfigurableDummyIncrementalLearnerFactory
import maia.ml.learner.standard.DummyIncrementalLearnerConfiguration
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
import maia.util.assertType
import maia.util.getResourceStatic


fun main() {

    val irisURL = getResourceStatic("/iris.arff")
        ?: throw Exception("Could not find resource '/iris.arff'")
    val irisDataset = assertType<DataBatch<*>>(load(irisURL.file, true))

    val learnerConfig = initialise<DummyIncrementalLearnerConfiguration> {
        target = 4
    }

    val learner = ConfigurableDummyIncrementalLearnerFactory(learnerConfig).create()

    learner.initialise(irisDataset)

    val irisView = irisDataset.readOnlyViewRows((0 until irisDataset.numRows).toList())

    for (row in irisView.rowIterator()) {
        val trainView = row.viewAsDataBatch()
        runBlocking { learner.train(trainView) }
        println(learner.predict(row).formatString())
    }

    println()

    val topo = buildTopology {

        val source = ARFFSource {
            name = "source"
            filename = irisURL.file
            batch = true
        }

        val toRows = IterateRows {
            name = "rows"
        }

        val pass = LetPassForRange<DataBatch<*>> {
            name = "pass"
            start = 0
            endInclusive = 111111
            invert = false
        }

        val batchViewRows = BatchViewRows {
            name = "bvr"
        }

        val init = InitialiseOnFirst<DataStream<*>> {
            name = "init"
        }

        val seq = Sequential<DataBatch<*>> {
            name = "seq"
        }

        val learnerFactory = NewLearner {
            name = "learnerFactory"
            factoryClass = ConfigurableDummyIncrementalLearnerFactory::class
            learnerConfiguration = learnerConfig
        }

        val learnerNode = LearnerNode {
            name = "learner"
        }

        val printer = Printer {
            name = "printer"
            suffix = " is the class"
        }

        val split = Split<DataRow, DataRow> {
            name = "split"
        }

        val format = FormatDataRow {
            name = "format"
        }

        learnerFactory - learnerNode.learnerInput
        source - init - toRows - batchViewRows - pass - seq
        seq.output1 - learnerNode.train
        seq.output2 - learnerNode.predictionInput
        init.initialise - learnerNode.initialise
        learnerNode.predictionOutput - split
        split.second - format - printer

    }

    topo.execute()
}
