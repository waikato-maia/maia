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

class CrossValidation(val dataset: DataBatch<*>, val learnerType: AbstractLearner<DataBatch<*>>, val folds: Int) : Evaluation(dataset, learnerType){
    override fun classificationAccuracy() {
        learner.initialise(data)

        var notEvaluatedDataset = data.readOnlyViewRows((0 until data.numRows).toList())
        val numEvalRows = data.numRows / folds //Select 10% of the rows for evaluation

        // Get the desired representations of the actual and predicted classes
        val actualClassRepr = assertType<Nominal<*, *, *, *>>(data.headers[data.numColumns-1].type).canonicalRepresentation
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
            val evalDataset = data.readOnlyViewRows(evalSubset.toList())

            //Get the training dataset
            val trainDataset = data.readOnlyViewRows(
                (0 until data.numRows)
                    .filter { it !in evalSubset }
            )

            //Extract the evaluation dataset from the data which the next evaluation dataset will be chosen from
            notEvaluatedDataset = data.readOnlyViewRows(
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