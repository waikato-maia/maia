package maia.main

import maia.ml.dataset.DataBatch
import maia.ml.dataset.type.standard.Nominal
import maia.ml.dataset.view.readOnlyViewRows
import maia.ml.learner.AbstractLearner
import maia.util.SubsetNumber
import maia.util.assertType
import maia.util.nextBigInteger
import maia.util.numSubsets
import kotlin.random.Random

class CrossValidation(val dataset1: DataBatch<*>, val learner1: AbstractLearner<DataBatch<*>>, val folds: Int) : Evaluation(dataset1, learner1){
    override fun classificationAccuracy() {
        learner.initialise(dataset)

        var notEvaluatedDataset = dataset.readOnlyViewRows((0 until dataset.numRows).toList())
        val numEvalRows = dataset.numRows / folds //Select 10% of the rows for evaluation

        // Get the desired representations of the actual and predicted classes
        val actualClassRepr = assertType<Nominal<*, *, *, *>>(dataset.headers[dataset.numColumns-1].type).canonicalRepresentation
        val predictedClassRepr = assertType<Nominal<*, *, *, *>>(learner.predictOutputHeaders[0].type).canonicalRepresentation

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
                val actualClass = row.getValue(actualClassRepr)

                // Get the prediction for the row
                val prediction = learner.predict(row).getValue(predictedClassRepr)

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
}