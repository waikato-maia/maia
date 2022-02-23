package maia.main

import maia.ml.dataset.DataBatch
import maia.ml.dataset.type.standard.Nominal
import maia.ml.dataset.view.readOnlyViewRows
import maia.ml.learner.AbstractLearner
import maia.util.assertType
import java.util.Random

class CrossValidation : Evaluation(){
    //Stratifies a set of instances according to its nominal class value
    fun stratify(dataset: DataBatch<*>, folds: Int): DataBatch<*> {
        val actualClassRepr = assertType<Nominal<*, *, *, *>>(dataset.headers[dataset.numColumns-1].type).canonicalRepresentation
        var datasetList = (0 until dataset.numRows).toMutableList()
        //Sort by class
        var index = 1
        while (index <= dataset.numRows) {
            val row1 = dataset.getRow(datasetList[index])
            for (i in index until dataset.numRows) {
                val row2 = dataset.getRow(datasetList[i])
                if (row1.getValue(actualClassRepr) == row2.getValue(actualClassRepr)) {
                    datasetList = swap(datasetList, index, i)
                    index++
                }
            }
            index++
        }
        //Create stratified batch
        var newDatasetList: MutableList<Int> = mutableListOf()
        var start = 0
        var j: Int
        while (newDatasetList.count() < dataset.numRows) {
            j = start
            while(j < dataset.numRows) {
                newDatasetList.add(datasetList[j])
                j += folds
            }
            start++
        }

        return dataset.readOnlyViewRows(newDatasetList)
    }

    //Shuffles the instances in the data batch so that they are ordered randomly
    fun randomize(instancesList: MutableList<Int>, random: Random): MutableList<Int> {
        var instancesListCopy: MutableList<Int> = mutableListOf()
        instancesListCopy.addAll(instancesList)

        for (j in instancesListCopy.count() - 1 downTo 1) {
            instancesListCopy = swap(instancesListCopy, j, random.nextInt(j + 1))
        }
        return instancesListCopy
    }

    //Swaps two instances in the data batch
    fun swap(instancesList: MutableList<Int>, i: Int, j: Int): MutableList<Int> {
        val temp = instancesList[i]
        instancesList[i] = instancesList[j]
        instancesList[j] = temp

        return instancesList
    }

    //Gets the training dataset for a fold
    fun getTrainingDataset(dataset: DataBatch<*>, folds: Int, currentFold: Int, random: Random): DataBatch<*> {
        var numRowsForFold = dataset.numRows/folds
        var offset: Int

        if (currentFold < dataset.numRows % folds) {
            numRowsForFold++
            offset = currentFold
        }
        else {
            offset = dataset.numRows % folds
        }

        val firstRow = currentFold * (dataset.numRows / folds) + offset

        var train = (0 until firstRow).toList() + ((firstRow+numRowsForFold) until (dataset.numRows)).toList()
        train = randomize(train as MutableList<Int>, random)

        return dataset.readOnlyViewRows(train)
    }

    //Gets the testing dataset for a fold
    fun getTestingDataset(dataset: DataBatch<*>, folds: Int, currentFold: Int): DataBatch<*> {
        var numRowsForFold = dataset.numRows / folds
        var offset: Int

        if (currentFold < dataset.numRows % folds) {
            numRowsForFold++
            offset = currentFold
        }
        else {
            offset = dataset.numRows % folds
        }

        val numRows = numRowsForFold
        val firstRow = currentFold * (dataset.numRows / folds) + offset
        val test = dataset.readOnlyViewRows((firstRow until (firstRow + numRows)).toList())
        return test
    }

    fun classificationAccuracy(unstratifiedDataset: DataBatch<*>, learner: AbstractLearner<DataBatch<*>>, folds: Int): Double {
        //Stratify the dataset
        val dataset = stratify(unstratifiedDataset, folds)

        learner.initialise(dataset)
        val actualClassRepr = assertType<Nominal<*, *, *, *>>(dataset.headers[dataset.numColumns-1].type).canonicalRepresentation
        val predictedClassRepr = assertType<Nominal<*, *, *, *>>(learner.predictOutputHeaders[0].type).canonicalRepresentation

        //For each fold, use the other k-1 folds for training and this fold for testing
        for(i in 0 until folds) {
            val trainingDataset = getTrainingDataset(dataset, folds, i, Random(1))
            val testingDataset = getTestingDataset(dataset, folds, i)

            learner.initialise(trainingDataset)
            learner.train(trainingDataset)

            //Evaluate using this sub-dataset
            var instancesCorrect = 0

            for (row in testingDataset.rowIterator()) {
                // Get the actual class of the row
                val actualClass = row.getValue(actualClassRepr)
                // Get the prediction for the row
                val prediction = learner.predict(row).getValue(predictedClassRepr)

                //Test if this instance has been classified correctly
                if(actualClass == prediction){
                    instancesCorrect++
                }
            }
            //Calculate the classification accuracy for this fold
            classificationAccuracy += instancesCorrect/testingDataset.numRows.toDouble() * 100

        }
        //Average the results
        classificationAccuracy /= folds
        return classificationAccuracy
    }
}