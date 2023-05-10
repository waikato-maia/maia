/*
 * HoeffdingTreesIdenticalTest.kt
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

import maia.ml.dataset.type.standard.Nominal
import moa.classifiers.AbstractClassifier
import moa.classifiers.trees.HoeffdingTree
import maia.ml.dataset.view.viewAsDataBatch
import maia.ml.learner.moa.MOALearner
import maia.util.assertType
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * TODO: What class does.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
class HoeffdingTreesIdenticalTest {

    inline fun assertStrings(
        learner: maia.ml.learner.standard.hoeffdingtree.HoeffdingTree,
        learner2: MOALearner
    ) {
        val learner2String = buildString { (learner2.source as AbstractClassifier).getModelDescription(this, 2) }
        val learnerString = buildString { learner.getModelDescription(this, 2) }

        assertEquals(learner2String, learnerString)
    }

    @Test
    fun testIdentical() {
        val maxInstances = 10_000_000
        val numAttr = 10
        val learner = maia.ml.learner.standard.hoeffdingtree.HoeffdingTree()
        val learner2 = MOALearner(HoeffdingTree().apply { prepareForUse() })
        val stream = maia.ml.dataset.standard.RandomRBFGenerator(2, 3, 5, numAttr, 50)

        learner.initialise(stream)
        learner2.initialise(stream)

        val actualClassRepr = assertType<Nominal<*, *, *, *, *>>(stream.headers[numAttr].type).labelRepresentation
        val predictedClassRepr = assertType<Nominal<*, *, *, *, *>>(learner.predictOutputHeaders[0].type).labelRepresentation
        val predictedClass2Repr = assertType<Nominal<*, *, *, *, *>>(learner2.predictOutputHeaders[0].type).labelRepresentation

        var instancesProcessed : Long = 0

        for (row in stream.rowIterator()) {
            if (instancesProcessed >= maxInstances) break

            val prediction = learner.predict(row)
            val prediction2 = learner2.predict(row)

            val actualClass = row.getValue(actualClassRepr)
            val predictedClass = prediction.getValue(predictedClassRepr)
            val predictedClass2 = prediction2.getValue(predictedClass2Repr)

            assertEquals(predictedClass2, predictedClass)

            val trainBatch = row.viewAsDataBatch()
            learner2.train(trainBatch)
            learner.train(trainBatch)

            instancesProcessed++

            if (instancesProcessed % 10_000 == 0L) {
                print("$instancesProcessed: ")
                println("Predicted: $predictedClass; Actual: $actualClass [${predictedClass == actualClass}]")
                assertStrings(learner, learner2)
            }
        }
    }
}
