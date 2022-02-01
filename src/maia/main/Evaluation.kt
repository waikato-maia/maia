package maia.main

import maia.ml.dataset.DataBatch
import maia.ml.dataset.type.standard.Nominal
import maia.ml.learner.AbstractLearner
import maia.util.assertType
import kotlin.system.measureTimeMillis

abstract class Evaluation(val data: DataBatch<*>, val learner: AbstractLearner<DataBatch<*>>){

    var classificationAccuracy = 0.0

    fun timeToBuild() {
        val timeInMillis = measureTimeMillis {
            // Initialise the learner on the dataset
            learner.initialise(data)

            // Train on the dataset
            learner.train(data)
        }

        //Print the time taken to build the model
        val timeInSeconds = timeInMillis/1000.toDouble()
        println("Time taken to build model: $timeInSeconds seconds")
    }

    abstract fun classificationAccuracy()
}