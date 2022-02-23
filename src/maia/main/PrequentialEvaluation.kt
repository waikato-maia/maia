package maia.main

import maia.ml.dataset.DataBatch
import maia.ml.dataset.DataStream
import maia.ml.dataset.type.standard.Nominal
import maia.ml.dataset.view.viewAsDataBatch
import maia.ml.learner.AbstractLearner
import maia.util.assertType

class PrequentialEvaluation : Evaluation() {
    fun classificationAccuracy(dataset: DataBatch<*>, learner: AbstractLearner<DataBatch<*>>, numInstances: Int): MutableList<MutableList<Double>> {
        learner.initialise(dataset)

        // Get the desired representations of the actual and predicted classes
        val actualClassRepr = assertType<Nominal<*, *, *, *>>(dataset.headers[dataset.numColumns-1].type).canonicalRepresentation
        val predictedClassRepr = assertType<Nominal<*, *, *, *>>(learner.predictOutputHeaders[0].type).canonicalRepresentation

        //Initialise variables
        var instancesCorrect = 0
        var totalInstances = 0
        val classificationAccuracyList: MutableList<MutableList<Double>> = mutableListOf()

        for (row in dataset.rowIterator()) {
            totalInstances ++

            //Get the actual class for the row
            val actualClass = row.getValue(actualClassRepr)

            //Get the prediction for the row
            val predictedClass = learner.predict(row).getValue(predictedClassRepr)

            //Test if this instance has been classified correctly
            if (actualClass == predictedClass) {
                instancesCorrect ++
            }

            //If it is appropriate, calculate the classification accuracy
            if (totalInstances % numInstances == 0) {
                //Recalculate the classification accuracy
                classificationAccuracy = instancesCorrect/totalInstances.toDouble() * 100
                classificationAccuracyList.add(mutableListOf(totalInstances.toDouble(), classificationAccuracy))
            }

            //Train the model with this instance
            val trainBatch = row.viewAsDataBatch()
            learner.train(trainBatch)
        }
        //Recalculate the classification accuracy
        classificationAccuracy = instancesCorrect/totalInstances.toDouble() * 100
        classificationAccuracyList.add(mutableListOf(totalInstances.toDouble(), classificationAccuracy))
        return classificationAccuracyList
    }
}