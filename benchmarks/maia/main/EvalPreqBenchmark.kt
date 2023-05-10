/*
 * EvalPreqBenchmark.kt
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
package maia.main

import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Mode
import kotlinx.benchmark.Scope
import moa.classifiers.AbstractClassifier
import moa.classifiers.trees.HoeffdingTree
import moa.core.Utils
import moa.streams.InstanceStream
import moa.streams.generators.RandomRBFGenerator
import maia.ml.dataset.DataStream
import maia.ml.dataset.moa.MOADataStream
import maia.ml.dataset.moa.dataStreamToInstanceStream
import maia.ml.dataset.moa.materalizeMOAClass
import maia.ml.dataset.type.standard.Nominal
import maia.ml.dataset.view.viewAsDataBatch
import maia.ml.learner.Learner
import maia.ml.learner.LearnerHarness
import maia.ml.learner.moa.MOALearner
import maia.util.assertType
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit


enum class HandleErrorsBy {
    THROW,
    IGNORE,
    INCLUDE
}

enum class Framework {
    MOA,
    MAIA
}

const val PRINT_EVERY = 10_000
val HANDLE_ERRORS_BY: HandleErrorsBy = HandleErrorsBy.INCLUDE

/**
 * TODO
 */
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
open class EvalPreqBenchmark {

    @State(Scope.Benchmark)
    open class Params {

        @Param("100000")
        var maxInstances: Int = 0

        @Param("10")
        var numAttrs: Int = 0

        @Param("2", "6", "10")
        var numClasses: Int = 0

        @Param("50")
        var numCentroids: Int = 0

        @Param("false")
        var withHarness: Boolean = false

        @Param
        var runFramework: Framework = Framework.MOA

        @Param
        var sourceFramework: Framework = Framework.MOA

        @Param
        var learnerFramework: Framework = Framework.MOA
    }

    @Benchmark
    fun benchmark(params: Params, blackhole : Blackhole) {
        when (params.runFramework) {
            Framework.MOA -> runInMOA(
                when (params.learnerFramework) {
                    Framework.MOA -> moaLearner()
                    Framework.MAIA -> maiaLearnerForMOA()
                },
                when (params.sourceFramework) {
                    Framework.MOA -> moaSource(params)
                    Framework.MAIA -> maiaSourceForMOA(params)
                },
                params
            )
            Framework.MAIA -> runInMAIA(
                when (params.learnerFramework) {
                    Framework.MOA -> moaLearnerForMAIA(params)
                    Framework.MAIA -> maiaLearner(params)
                },
                when (params.sourceFramework) {
                    Framework.MOA -> moaSourceForMAIA(params)
                    Framework.MAIA -> maiaSource(params)
                },
                params
            )
        }
    }

    fun moaLearner(): HoeffdingTree {
        return materalizeMOAClass(
            "moa.classifiers.trees.HoeffdingTree"
        )
    }

    fun moaSource(params: Params): RandomRBFGenerator {
        return materalizeMOAClass(
            "moa.streams.generators.RandomRBFGenerator " +
                    "-r 1 " +
                    "-i 1 " +
                    "-c ${params.numClasses} " +
                    "-a ${params.numAttrs} " +
                    "-n ${params.numCentroids}"
        )
    }

    fun maiaLearner(params: Params): Learner<DataStream<*>> {
        val learner = maia.ml.learner.standard.hoeffdingtree.HoeffdingTree()
        return if (params.withHarness)
            LearnerHarness(learner, DataStream::class)
        else
            learner
    }

    fun maiaSource(params: Params): DataStream<*> {
        return maia.ml.dataset.standard.RandomRBFGenerator(
            1,
            1,
            params.numClasses,
            params.numAttrs,
            params.numCentroids
        )
    }

    fun moaLearnerForMAIA(params: Params): Learner<DataStream<*>> {
        val learner = MOALearner(moaLearner())
        return if (params.withHarness)
            LearnerHarness(learner, DataStream::class)
        else
            learner
    }

    fun moaSourceForMAIA(params: Params) = MOADataStream(moaSource(params)) { _, _, _ -> true }

    fun maiaLearnerForMOA(): Nothing = throw Exception("Not required")

    fun maiaSourceForMOA(params: Params): InstanceStream = dataStreamToInstanceStream(
        maiaSource(params)
    )

    fun runInMOA(
        learner: AbstractClassifier,
        stream: InstanceStream,
        params: Params
    ) {
        learner.modelContext = stream.header

        var instancesProcessed : Long = 0

        while (stream.hasMoreInstances() && instancesProcessed < params.maxInstances) {
            val trainInst = stream.nextInstance()
            val prediction = learner.getVotesForInstance(trainInst)

            val actualClass = trainInst.data.classValue()
            val predictedClass = Utils.maxIndex(prediction).toDouble()

            if (instancesProcessed % PRINT_EVERY == 0L)
                printPrediction(predictedClass, actualClass)

            learner.trainOnInstance(trainInst)
            instancesProcessed++
        }
    }

    fun runInMAIA(
        learner: Learner<DataStream<*>>,
        stream: DataStream<*>,
        params: Params
    ) {
        learner.initialise(stream)

        var instancesProcessed : Long = 0

        for (row in stream.rowIterator()) {
            if (instancesProcessed >= params.maxInstances) break

            val prediction = learner.predict(row)

            val actualClass = row.getValue(assertType<Nominal<*, *, *, *, *>>(row.headers[params.numAttrs].type).labelRepresentation)
            val predictedClass = prediction.getValue(assertType<Nominal<*, *, *, *, *>>(prediction.headers[0].type).labelRepresentation)

            if (instancesProcessed % PRINT_EVERY == 0L)
                printPrediction(predictedClass, actualClass)

            learner.train(row.viewAsDataBatch())
            instancesProcessed++
        }
    }

    fun <T> printPrediction(predicted: T, actual: T) {
        println("Predicted: $predicted; Actual: $actual [${predicted == actual}]")
    }
}
