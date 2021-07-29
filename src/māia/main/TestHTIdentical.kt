package māia.main

import moa.classifiers.AbstractClassifier
import māia.ml.dataset.DataStream
import māia.ml.dataset.view.viewAsDataBatch
import māia.ml.learner.Learner
import māia.ml.learner.moa.MOALearner
import māia.ml.learner.standard.hoeffdingtree.HoeffdingTree

/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */

fun main() {
    val inputs = TestInputs(
        1,
        1,
        10_000_000,
        10_000,
        2,
        3,
        5,
        10,
        50,
        false
    )

    run(
        moanaLearner(inputs),
        moaLearnerForMOANA(inputs),
        moanaSource(inputs),
        inputs
    )
}

fun run(
    learner: Learner<DataStream<*>>,
    learner2: Learner<DataStream<*>>,
    stream: DataStream<*>,
    inputs: TestInputs
) {
    learner.initialise(stream)
    learner2.initialise(stream)

    var instancesProcessed : Long = 0

    for (row in stream.rowIterator()) {
        if (instancesProcessed >= inputs.maxInstances) break

        val prediction = learner.predict(row)
        val prediction2 = learner2.predict(row)

        val actualClass = row.getColumn(inputs.numAttr)
        val predictedClass = prediction.getColumn(0)
        val predictedClass2 = prediction2.getColumn(0)

        if (predictedClass != predictedClass2) {
            val prediction = learner.predict(row).getColumn(0)
            val prediction2 = learner2.predict(row).getColumn(0)
            throw Exception("Predictions differ at $instancesProcessed")
        }

        val trainBatch = row.viewAsDataBatch()
        learner2.train(trainBatch)
        learner.train(trainBatch)

        instancesProcessed++

        if (instancesProcessed % inputs.printEvery == 0L) {
            print("$instancesProcessed: ")
            printPrediction(predictedClass, actualClass)

            val learner2String = buildString { ((learner2 as MOALearner).source as AbstractClassifier).getModelDescription(this, 2) }
            val learnerString = buildString { (learner as HoeffdingTree).getModelDescription(this, 2) }

            if (learnerString != learner2String) {
                println(learnerString)
                println(learner2String)
                throw Exception("Serialisations differ at $instancesProcessed")
            }
        }
    }
}
