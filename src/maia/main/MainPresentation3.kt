/*
 * MainPresentation3.kt
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
import maia.ml.dataset.DataBatch
import maia.ml.dataset.arff.load
import maia.ml.dataset.util.formatString
import maia.ml.dataset.view.readOnlyViewRows
import maia.ml.dataset.view.viewAsDataBatch
import maia.ml.learner.standard.ZeroRLearner
import maia.util.assertType
import maia.util.getResourceStatic


fun main() {

    // Load the dataset
    val irisURL = getResourceStatic("/iris.arff")
        ?: throw Exception("Could not find resource '/iris.arff'")
    val irisDataset = assertType<DataBatch<*>>(load(irisURL.file, true))

    // Create a read-only view of the data-set
    val irisView = irisDataset.readOnlyViewRows((75 until irisDataset.numRows).toList())

    // Create a learner instance
    val learner = ZeroRLearner(4)

    // Initialise the learner on the dataset
    learner.initialise(irisView)

    for (row in irisView.rowIterator()) {
        // View the row as a dataset
        val rowView = row.viewAsDataBatch()

        // Train on the row
        runBlocking { learner.train(rowView) }

        // Get the prediction for the row
        val prediction = learner.predict(row)

        // Print the prediction
        println(prediction.formatString())
    }

}
