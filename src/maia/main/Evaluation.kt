package maia.main

import maia.ml.dataset.DataBatch
import maia.ml.dataset.DataStream
import maia.ml.learner.AbstractLearner
import kotlin.system.measureTimeMillis

abstract class Evaluation {
    var classificationAccuracy = 0.0

    fun timeToBuild(data: DataBatch<*>, learner: AbstractLearner<DataBatch<*>>): Double {
        val timeInMillis = measureTimeMillis {
            // Initialise the learner on the dataset
            learner.initialise(data)
            // Train on the dataset
            learner.train(data)
        }
        //Return the time taken to build the model
        return timeInMillis / 1000.toDouble()
    }
}