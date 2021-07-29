package māia.main

import māia.ml.dataset.arff.ARFFLoader
import māia.ml.dataset.util.formatString
import māia.ml.dataset.view.readOnlyViewRows
import māia.ml.dataset.view.viewAsDataBatch
import māia.ml.learner.standard.ZeroRLearner


fun main() {

    // Create a loader for the Iris dataset
    val loader = ARFFLoader {
        filename = "/home/csterlin/Downloads/weka-3-9-4-azul-zulu-linux/weka-3-9-4/data/iris.arff"
    }

    // Load the dataset
    val irisDataset = loader.load()

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
