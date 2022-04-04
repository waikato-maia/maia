---
layout: page
title: Getting Started
include_in_header: true
---

# How to start

Obtaining MĀIA is as easy as cloning the GitHub repo:

```
git clone https://github.com/waikato-maia/maia.git
```

Run the following script to test that all modules have been sucessfully
cloned:

```
cd maia
./test-all
```

## What to do next

### Read the overview of how MĀIA operates

See [this page](overview.md) for an overview of MĀIA's core modules and
principles.

### Create and run your own experiment

For example:

```kotlin
// Import required modules
import maia.ml.dataset.arff.load
import maia.ml.dataset.util.formatString
import maia.ml.learner.standard.ZeroRLearner

fun main() {
    // Load a training dataset
    val trainingDataset = load("/path/to/training_dataset.arff")

    // Create a learner instance
    val learner = ZeroRLearner(targetIndex = 4)

    // Initialise the learner on the dataset
    learner.initialise(trainingDataset)

    // Train the learner with the dataset
    learner.train(trainingDataset)

    // Load a test dataset
    val testDataset = load("/path/to/test_dataset.arff")

    // Print the prediction for each test instance
    testDataset.rowIterator().forEach { testRow ->
        println(learner.predict(testRow).formatString())
    }
}
```

or use a built-in evaluation:

```kotlin
import maia.ml.dataset.arff.load
import maia.ml.evaluation.evaluate
import maia.ml.evaluation.standard.evaluation.EvaluatePrequential
import maia.ml.evaluation.standard.evaluator.ClassificationPerformanceEvaluator
import maia.ml.learner.standard.hoeffdingtree.HoeffdingTree

fun main() {

    // Create an evaluation
    val evaluation = EvaluatePrequential(
        load("/path/to/training_dataset.arff")
    )

    // Create an evaluator
    val evaluator = ClassificationPerformanceEvaluator(
        precisionRecall = true,
        precisionPerClass = true,
        recallPerClass = true,
        f1PerClass = true
    )

    // Create a learner
    val learner = HoeffdingTree()

    // Perform the evaluation
    val metrics = arrayOf(learner).evaluate(
        evaluation,
        evaluator
    )

    // Print the results of the evaluation
    println(metrics)
}
```
