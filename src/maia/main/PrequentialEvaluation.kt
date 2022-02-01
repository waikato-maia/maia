package maia.main

import maia.ml.dataset.DataBatch
import maia.ml.dataset.type.standard.Nominal
import maia.ml.dataset.view.viewAsDataBatch
import maia.ml.learner.AbstractLearner
import maia.util.assertType

class PrequentialEvaluation(val dataset: DataBatch<*>, val learnerType: AbstractLearner<DataBatch<*>>) : Evaluation(dataset, learnerType) {
    override fun classificationAccuracy() {
        learner.initialise(data)

        // Get the desired representations of the actual and predicted classes
        val actualClassRepr = assertType<Nominal<*, *, *, *>>(data.headers[data.numColumns-1].type).canonicalRepresentation
        val predictedClassRepr = assertType<Nominal<*, *, *, *>>(learner.predictOutputHeaders[0].type).canonicalRepresentation

        //Initialise variables
        var instancesCorrect = 0
        var totalInstances = 0

        for (row in data.rowIterator()) {
            totalInstances ++

            //Get the actual class for the row
            val actualClass = row.getValue(actualClassRepr)

            //Get the prediction for the row
            val predictedClass: String
            //Check if this is the first row being processed
            if (totalInstances == 1) {
                predictedClass = row.getValue(actualClassRepr)
            }
            else {
                predictedClass = learner.predict(row).getValue(predictedClassRepr)
            }

            //Test if this instance has been classified correctly
            if (actualClass == predictedClass) {
                instancesCorrect ++
            }

            //Recalculate the classification accuracy
            classificationAccuracy += instancesCorrect/totalInstances.toDouble() * 100
            classificationAccuracy /= 2

            //Train the model with this instance
            val trainBatch = row.viewAsDataBatch()
            learner.train(trainBatch)
        }

        println("Classification Accuracy with Prequential Evaluation: $classificationAccuracy %")
    }
}