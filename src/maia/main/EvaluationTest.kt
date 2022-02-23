package maia.main

import maia.ml.dataset.DataBatch
import maia.ml.dataset.arff.load
import maia.ml.learner.standard.NaiveBayesLearner
import maia.ml.learner.standard.ZeroRLearner
import maia.ml.learner.standard.hoeffdingtree.AdaptiveRandomForest
import maia.ml.learner.standard.hoeffdingtree.HoeffdingTree
import maia.util.assertType
import maia.util.getResourceStatic


fun main() {
    //val filename = "/iris.arff"
    val filename = "/electricity-normalized.arff"
    //val filename = "/airlines.arff"
    //val filename = "/poker-hand-normalized.arff"

    // Load the dataset
    val datasetURL = getResourceStatic(filename)
        ?: throw Exception("Could not find the dataset ARFF file'")
    val dataset = assertType<DataBatch<*>>(load(datasetURL.file, true))

    // Create a learner instance
    //val learner = ZeroRLearner(dataset.numColumns-1)
    //val learner = NaiveBayesLearner(dataset.numColumns-1, false, false)
    val learner = HoeffdingTree(dataset.numColumns-1)
    //val learner = AdaptiveRandomForest(dataset.numColumns-1)

    //Create an evaluation instance
    //val evaluation = CrossValidation()
    val evaluation = PrequentialEvaluation()

    //Get time taken to build the model
    //println("Time taken to build the model: " + evaluation.timeToBuild(dataset, learner) + " seconds")

    //Get the classification accuracy
    //println("Classification Accuracy with Cross Validation: " + evaluation.classificationAccuracy(dataset, learner, 10) + " %")

    val classificationAccuracyList = evaluation.classificationAccuracy(dataset, learner, 100000)
    for (i in classificationAccuracyList) {
        println("Classification accuracy after processing " + i[0].toInt() + " instances: " + i[1] + " %")
    }
}