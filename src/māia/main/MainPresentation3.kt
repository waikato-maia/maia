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
package māia.main

import māia.ml.dataset.DataBatch
import māia.ml.dataset.arff.load
import māia.ml.dataset.util.formatString
import māia.ml.dataset.view.readOnlyViewRows
import māia.ml.dataset.view.viewAsDataBatch
import māia.ml.learner.standard.ZeroRLearner
import māia.util.assertType


fun main() {

    // Load the dataset
    val irisDataset = assertType<DataBatch<*>>(load("/home/csterlin/Downloads/weka-3-9-4-azul-zulu-linux/weka-3-9-4/data/iris.arff", true))

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
        learner.train(rowView)

        // Get the prediction for the row
        val prediction = learner.predict(row)

        // Print the prediction
        println(prediction.formatString())
    }

}
