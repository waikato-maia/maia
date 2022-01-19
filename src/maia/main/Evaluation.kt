package maia.main

import maia.ml.dataset.DataBatch
import maia.ml.dataset.arff.load
import maia.ml.dataset.type.standard.Nominal
import maia.ml.dataset.util.formatString
import maia.ml.dataset.view.DataBatchView
import maia.ml.dataset.view.readOnlyViewRows
import maia.ml.dataset.view.viewAsDataBatch
import maia.ml.learner.AbstractLearner
import maia.ml.learner.Learner
import maia.ml.learner.standard.NaiveBayesLearner
import maia.ml.learner.standard.ZeroRLearner
import maia.ml.learner.standard.hoeffdingtree.HoeffdingTree
import maia.ml.learner.type.AnyLearnerType
import maia.util.SubsetNumber
import maia.util.assertType
import maia.util.getResourceStatic
import maia.util.nextBigInteger
import maia.util.numSubsets
import java.math.BigInteger
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun timeToBuild(dataset: DataBatch<*>, learner: AbstractLearner<DataBatch<*>>) {
    val timeInMillis = measureTimeMillis {
        // Initialise the learner on the dataset
        learner.initialise(dataset)

        // Train on the dataset
        learner.train(dataset)
    }

    //Print the time taken to build the model
    val timeInSeconds = timeInMillis/1000.toDouble()
    println("Time taken to build model: $timeInSeconds seconds")
}

fun crossValidation(dataset: DataBatch<*>, learner: AbstractLearner<DataBatch<*>>, folds: Int) {
    //Initialise the classification accuracy
    var classificationAccuracy = 0.0

    var notEvaluatedDataset = dataset.readOnlyViewRows((0 until dataset.numRows).toList())
    val numEvalRows = dataset.numRows / folds //Select 10% of the rows for evaluation

    // Get the desired representations of the actual and predicted classes
    val trainRepr = assertType<Nominal<*, *, *, *>>(dataset.headers[dataset.numColumns-1].type).canonicalRepresentation
    val evalRepr = assertType<Nominal<*, *, *, *>>(learner.predictOutputHeaders[0].type).canonicalRepresentation

    //Repeat the cross-validation process for each fold
    repeat(folds) {
        //Initialise count variable
        var instancesCorrect = 0

        //Get the evaluation dataset
        val numEvalCombs = numSubsets(notEvaluatedDataset.numRows, numEvalRows) // no. of possible subsets that could be selected
        val evalSubsetSelection = Random.nextBigInteger(numEvalCombs) //select one of the possible subset numbers
        val evalSubsetNumber = SubsetNumber.create(evalSubsetSelection, notEvaluatedDataset.numRows, numEvalRows) //create that subset
        val evalSubset = evalSubsetNumber.toSubset()
        val evalDataset = dataset.readOnlyViewRows(evalSubset.toList())

        //Get the training dataset
        val trainDataset = dataset.readOnlyViewRows(
            (0 until dataset.numRows)
                .filter { it !in evalSubset }
        )

        //Extract the evaluation dataset from the data which the next evaluation dataset will be chosen from
        notEvaluatedDataset = dataset.readOnlyViewRows(
            (0 until notEvaluatedDataset.numRows)
                .filter { it !in evalSubset }
        )

        // Train on the train split
        learner.train(trainDataset)

        for (row in evalDataset.rowIterator()) {
            // Get the actual class of the row
            val actualClass = row.getValue(trainRepr)

            // Get the prediction for the row
            val prediction = learner.predict(row).getValue(evalRepr)

            if(actualClass == prediction){
                instancesCorrect++
            }
        }

        //Calculate the classification accuracy for this fold
        classificationAccuracy += instancesCorrect/evalDataset.numRows.toDouble() * 100
    }

    //Print the classification accuracy
    classificationAccuracy /= folds
    println("Classification Accuracy with Cross Validation: $classificationAccuracy %")
}

fun prequentialEvaluation(dataset: DataBatch<*>, learner: AbstractLearner<DataBatch<*>>, ) {
    // Get the desired representations of the actual and predicted classes
    val actualClassRepr = assertType<Nominal<*, *, *, *>>(dataset.headers[dataset.numColumns-1].type).canonicalRepresentation
    val predictedClassRepr = assertType<Nominal<*, *, *, *>>(learner.predictOutputHeaders[0].type).canonicalRepresentation

    //Initialise variables
    var instancesCorrect = 0
    var totalInstances = 0
    var classificationAccuracy = 0.0

    for (row in dataset.rowIterator()) {
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

fun main() {
    val filename = "/electricity-normalized.arff"

    // Load the dataset
    val datasetURL = getResourceStatic(filename)
        ?: throw Exception("Could not find the dataset ARFF file'")
    val dataset = assertType<DataBatch<*>>(load(datasetURL.file, true))

    // Create a learner instance
    val learner = ZeroRLearner(dataset.numColumns-1)
    //val learner = NaiveBayesLearner(dataset.numColumns-1, false, false)
    //val learner = HoeffdingTree()

    //Get time taken to build model
    timeToBuild(dataset, learner)

    // Initialise the learner on the dataset
    learner.initialise(dataset)

    //Perform 10-fold cross validation
    val folds = 10
    crossValidation(dataset, learner, folds)

    //Perform prequential evaluation
    prequentialEvaluation(dataset, learner)
}