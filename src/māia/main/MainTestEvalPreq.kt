/*
 * MainTestEvalPreq.kt
 * Copyright (C) 2021 University of Waikato, Hamilton, New Zealand
 *
 * This file is part of MĀIA.
 *
 * MĀIA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MĀIA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MĀIA.  If not, see <https://www.gnu.org/licenses/>.
 */
package māia.main

import moa.classifiers.AbstractClassifier
import moa.core.Utils
import moa.streams.InstanceStream
import moa.streams.generators.RandomRBFGenerator
import māia.ml.dataset.DataStream
import māia.ml.dataset.moa.MOADataStream
import māia.ml.dataset.moa.dataStreamToInstanceStream
import māia.ml.dataset.moa.materalizeMOAClass
import māia.ml.dataset.view.viewAsDataBatch
import māia.ml.learner.Learner
import māia.ml.learner.LearnerHarness
import māia.ml.learner.moa.MOALearner
import māia.ml.learner.moa.appletree.HoeffdingTree
import māia.util.enumerate
import kotlin.system.measureNanoTime
import kotlin.time.ExperimentalTime
import kotlin.time.nanoseconds

val WARM_UP_RUNS = arrayOf(5)
val MEASURED_RUNS = arrayOf(5)

private val MAX_INSTANCES = arrayOf(100_000)
private val PRINT_EVERY = arrayOf(10_000)

private val MODEL_SEED = arrayOf(1)
private val INST_SEED = arrayOf(1)
private val NUM_CLASSES = arrayOf(2,6,10)
private val NUM_ATTR = arrayOf(10)
private val NUM_CENTROIDS = arrayOf(50)

private val WITH_HARNESS = arrayOf(false)

private val HANDLE_ERRORS_BY: HandleErrorsBy = HandleErrorsBy.INCLUDE

enum class HandleErrorsBy {
    THROW,
    IGNORE,
    INCLUDE
}

data class TestInputs(
    val warmUpRuns: Int,
    val measuredRuns: Int,
    val maxInstances: Int,
    val printEvery: Int,
    val modelSeed: Int,
    val instSeed: Int,
    val numClasses: Int,
    val numAttr: Int,
    val numCentroids: Int,
    val withHarness: Boolean
)

data class TestOutputs(
    val MOASourceMOALearnerInMOATestTime: String,
    val MOANASourceMOALearnerInMOATestTime: String,
    val MOASourceMOANALearnerInMOATestTime: String,
    val MOANASourceMOANALearnerInMOATestTime: String,
    val MOASourceMOALearnerInMOANATestTime: String,
    val MOANASourceMOALearnerInMOANATestTime: String,
    val MOASourceMOANALearnerInMOANATestTime: String,
    val MOANASourceMOANALearnerInMOANATestTime: String
)

fun main() {
    val results: ArrayList<Pair<TestInputs, TestOutputs>> = ArrayList()

    for (warmUpRuns in WARM_UP_RUNS)
        for (measuredRuns in MEASURED_RUNS)
            for (maxInstances in MAX_INSTANCES)
                for (printEvery in PRINT_EVERY)
                    for (modelSeed in MODEL_SEED)
                        for (instSeed in INST_SEED)
                            for (numClasses in NUM_CLASSES)
                                for (numAttr in NUM_ATTR)
                                    for (withHarness in WITH_HARNESS)
                                        for (numCentroids in NUM_CENTROIDS) {
                                            val inputs = TestInputs(
                                                warmUpRuns,
                                                measuredRuns,
                                                maxInstances,
                                                printEvery,
                                                modelSeed,
                                                instSeed,
                                                numClasses,
                                                numAttr,
                                                numCentroids,
                                                withHarness
                                            )
                                            val outputs = test(inputs)
                                            results.add(Pair(inputs, outputs))
                                        }

    printlnmulti(
        "warmUpRuns",
        "measuredRuns",
        "maxInstances",
        "printEvery",
        "modelSeed",
        "instSeed",
        "numClasses",
        "numAttr",
        "numCentroids",
        "withHarness",
        "MOASourceMOALearnerInMOATestTime",
        "MOANASourceMOALearnerInMOATestTime",
        "MOASourceMOANALearnerInMOATestTime",
        "MOANASourceMOANALearnerInMOATestTime",
        "MOASourceMOALearnerInMOANATestTime",
        "MOANASourceMOALearnerInMOANATestTime",
        "MOASourceMOANALearnerInMOANATestTime",
        "MOANASourceMOANALearnerInMOANATestTime"
    )
    for ((inputs, outputs) in results) {
        printlnmulti(
            inputs.warmUpRuns,
            inputs.measuredRuns,
            inputs.maxInstances,
            inputs.printEvery,
            inputs.modelSeed,
            inputs.instSeed,
            inputs.numClasses,
            inputs.numAttr,
            inputs.numCentroids,
            inputs.withHarness,
            outputs.MOASourceMOALearnerInMOATestTime,
            outputs.MOANASourceMOALearnerInMOATestTime,
            outputs.MOASourceMOANALearnerInMOATestTime,
            outputs.MOANASourceMOANALearnerInMOATestTime,
            outputs.MOASourceMOALearnerInMOANATestTime,
            outputs.MOANASourceMOALearnerInMOANATestTime,
            outputs.MOASourceMOANALearnerInMOANATestTime,
            outputs.MOANASourceMOANALearnerInMOANATestTime
        )
    }

}

fun printlnmulti(vararg args: Any?) {
    for ((index, arg) in args.enumerate()) {
        print(arg)
        if (index != args.size - 1) print(",")
    }
    println()
}

@OptIn(ExperimentalTime::class)
fun measure(
    func: (TestInputs) -> Unit,
    inputs: TestInputs,
    handleErrorsBy : HandleErrorsBy = HANDLE_ERRORS_BY
): String {
    val errorHandler = when (handleErrorsBy) {
        HandleErrorsBy.THROW -> ::handleErrorByRaise
        HandleErrorsBy.IGNORE -> ::handleErrorByIgnore
        HandleErrorsBy.INCLUDE -> ::handleErrorByInclude
    }

    return errorHandler {
        // Warm up
        for (i in 0 until inputs.warmUpRuns) func(inputs)

        // Measure
        (0L until inputs.measuredRuns).sumOf {
            measureNanoTime { func(inputs) }
        }.div(inputs.measuredRuns).nanoseconds.inMilliseconds.toString()
    }
}

fun handleErrorByRaise(block: () -> String): String {
    return block()
}

fun handleErrorByInclude(block: () -> String): String {
    return try {
        block()
    } catch (e: Exception) {
        e.message ?: "Exception has no message"
    }
}

fun handleErrorByIgnore(block: () -> String): String {
    return try {
        block()
    } catch (e: Exception) {
        ""
    }
}

fun test(inputs : TestInputs): TestOutputs {
    println("MOASourceMOANALearnerInMOANATest")
    val MOASourceMOANALearnerInMOANATestTime = measure(::MOASourceMOANALearnerInMOANATest, inputs)

    println("MOANASourceMOALearnerInMOANATest")
    val MOANASourceMOALearnerInMOANATestTime = measure(::MOANASourceMOALearnerInMOANATest, inputs)

    println("MOANASourceMOANALearnerInMOANATest")
    val MOANASourceMOANALearnerInMOANATestTime = measure(::MOANASourceMOANALearnerInMOANATest, inputs)

    println("MOASourceMOALearnerInMOATest")
    val MOASourceMOALearnerInMOATestTime = measure(::MOASourceMOALearnerInMOATest, inputs)

    println("MOASourceMOALearnerInMOANATest")
    val MOASourceMOALearnerInMOANATestTime = measure(::MOASourceMOALearnerInMOANATest, inputs)

    println("MOASourceMOANALearnerInMOATest")
    val MOASourceMOANALearnerInMOATestTime = measure(::MOASourceMOANALearnerInMOATest, inputs)

    println("MOANASourceMOANALearnerInMOATest")
    val MOANASourceMOANALearnerInMOATestTime = measure(::MOANASourceMOANALearnerInMOATest, inputs)

    println("MOANASourceMOALearnerInMOATest")
    val MOANASourceMOALearnerInMOATestTime = measure(::MOANASourceMOALearnerInMOATest, inputs)

    println()

    return TestOutputs(
        MOASourceMOALearnerInMOATestTime,
        MOANASourceMOALearnerInMOATestTime,
        MOASourceMOANALearnerInMOATestTime,
        MOANASourceMOANALearnerInMOATestTime,
        MOASourceMOALearnerInMOANATestTime,
        MOANASourceMOALearnerInMOANATestTime,
        MOASourceMOANALearnerInMOANATestTime,
        MOANASourceMOANALearnerInMOANATestTime
    )
}

fun <T> printPrediction(predicted: T, actual: T) {
    println("Predicted: $predicted; Actual: $actual [${predicted == actual}]")
}

fun moaLearner(): HoeffdingTree {
    return materalizeMOAClass<HoeffdingTree>(
        HoeffdingTree::class.java,
        "māia.ml.learner.moa.appletree.HoeffdingTree"
    )
}

fun moaSource(inputs : TestInputs): RandomRBFGenerator {
    return materalizeMOAClass(
        RandomRBFGenerator::class.java,
        "moa.streams.generators.RandomRBFGenerator " +
                "-r ${inputs.modelSeed} " +
                "-i ${inputs.instSeed} " +
                "-c ${inputs.numClasses} " +
                "-a ${inputs.numAttr} " +
                "-n ${inputs.numCentroids}"
    )
}

fun moanaLearner(inputs: TestInputs): Learner<DataStream<*>> {
    val learner = māia.ml.learner.standard.hoeffdingtree.HoeffdingTree()
    return if (inputs.withHarness)
        LearnerHarness(learner, DataStream::class)
    else
        learner
}

fun moanaSource(inputs: TestInputs): DataStream<*> {
    return māia.ml.dataset.standard.RandomRBFGenerator(
        inputs.modelSeed,
        inputs.instSeed,
        inputs.numClasses,
        inputs.numAttr,
        inputs.numCentroids
    )
}

fun moaLearnerForMOANA(inputs : TestInputs): Learner<DataStream<*>> {
    val learner = MOALearner(moaLearner())
    return if (inputs.withHarness)
        LearnerHarness(learner, DataStream::class)
    else
        learner
}

fun moaSourceForMOANA(inputs : TestInputs) = MOADataStream(moaSource(inputs)) { _, _, _ -> true }

fun moanaLearnerForMOA(): Nothing = throw Exception("Not required")

fun moanaSourceForMOA(inputs : TestInputs): InstanceStream = dataStreamToInstanceStream(
    moanaSource(inputs)
)

fun runInMOA(
    learner: AbstractClassifier,
    stream: InstanceStream,
    inputs: TestInputs
) {
    learner.modelContext = stream.header

    var instancesProcessed : Long = 0

    while (stream.hasMoreInstances() && instancesProcessed < inputs.maxInstances) {
        val trainInst = stream.nextInstance()
        val prediction = learner.getVotesForInstance(trainInst)

        val actualClass = trainInst.data.classValue()
        val predictedClass = Utils.maxIndex(prediction).toDouble()

        if (instancesProcessed % inputs.printEvery == 0L)
            printPrediction(predictedClass, actualClass)

        learner.trainOnInstance(trainInst)
        instancesProcessed++
    }
}

fun runInMOANA(
    learner: Learner<DataStream<*>>,
    stream: DataStream<*>,
    inputs: TestInputs
) {
    learner.initialise(stream)

    var instancesProcessed : Long = 0

    for (row in stream.rowIterator()) {
        if (instancesProcessed >= inputs.maxInstances) break

        val prediction = learner.predict(row)

        val actualClass = row.getColumn(inputs.numAttr)
        val predictedClass = prediction.getColumn(0)

        if (instancesProcessed % inputs.printEvery == 0L)
            printPrediction(predictedClass, actualClass)

        learner.train(row.viewAsDataBatch())
        instancesProcessed++
    }
}

class NotImplementedYet(
    reason: String
): Exception("Not implemented yet: $reason")

fun MOASourceMOALearnerInMOATest(inputs: TestInputs) {
    runInMOA(
        moaLearner(),
        moaSource(inputs),
        inputs
    )
}

fun MOASourceMOALearnerInMOANATest(inputs: TestInputs) {
    runInMOANA(
        moaLearnerForMOANA(inputs),
        moaSourceForMOANA(inputs),
        inputs
    );
}

fun MOANASourceMOANALearnerInMOANATest(inputs: TestInputs) {
    runInMOANA(
        moanaLearner(inputs),
        moanaSource(inputs),
        inputs
    )
}

fun MOASourceMOANALearnerInMOATest(inputs: TestInputs) {
    runInMOA(
        moanaLearnerForMOA(),
        moaSource(inputs),
        inputs
    )
}

fun MOASourceMOANALearnerInMOANATest(inputs: TestInputs) {
    runInMOANA(
        moanaLearner(inputs),
        moaSourceForMOANA(inputs),
        inputs
    )
}

fun MOANASourceMOANALearnerInMOATest(inputs: TestInputs) {
    runInMOA(
        moanaLearnerForMOA(),
        moanaSourceForMOA(inputs),
        inputs
    )
}

fun MOANASourceMOALearnerInMOANATest(inputs: TestInputs) {
    runInMOANA(
        moaLearnerForMOANA(inputs),
        moanaSource(inputs),
        inputs
    )
}

fun MOANASourceMOALearnerInMOATest(inputs: TestInputs) {
    runInMOA(
        moaLearner(),
        moanaSourceForMOA(inputs),
        inputs
    )
}
