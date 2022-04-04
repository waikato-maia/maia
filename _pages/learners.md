---
layout: page
title: Learners
include_in_header: false
---

# MĀIA Learners

The `maia.ml.learner` package (from the `maia-learner` module) contains the
interface for defining machine-learning algorithms in MĀIA. The following
sections describe the pieces of MĀIA's learner framework.

## Learner Interface and Base Class

The `maia.ml.learner.Learner` interface defines how a learning algorithm
interacts with the greater MĀIA eccosystem. The generic type parameter defines
the type of data-set that the learner can work with, and should in practice
always be either `maia.ml.dataset.DataStream<*>` or
`maia.ml.dataset.DataBatch<*>`. This determines whether the learner requires a
data-batch to learn from (i.e. it performs methods over the entire dataset that
a single-pass data-stream can't provide) or if it can perform its algorithm on a
data-stream.

An abstract base-class, `maia.ml.learner.AbstractLearner`, is provided which
performs a number of common functions when implementing learners. Although not
strictly necessary, you should prefer this base-class over the raw interface
where possible.

## Learner Harness

The class `maia.ml.dataset.LearnerHarness` takes a learner and wraps it with a
number of safety mechanisms which ensure that the learner is behaving in the
intended manner. If you are implementing a new learning algorithm, and it is
not working as you expect, the first technique to use to identify the problem
is to test the learner with this harness.

## Learner Types

All learners belong to a type-system based on the
`maia.ml.learner.type.LearnerType` class hierarchy. The type of a learner
determines whether it is capable of working with a given data-set structure in
a prescribed manner (or, if not, why not). This allows for automatic
determination of suitable learners for a given set of data.

Each `Learner` sub-class has three types:

### Class Learner Type

The `classLearnerType` property determines the types of data that the class can
be used with, in the absence of any constructor parameters. This property is
statically set in the companion object of the learner's class, using the
`LearnerClass::classLearnerType.override` method of the property. This can be
used to determine if a class of learners is applicable to a given dataset,
without having to create an instance of the learner.

### Uninitialised Learner Type

The `uninitialisedType` property determines the types of data that an instance
of the learner can handle *with respect to* the constructor arguments provided.
By default, this is equal to the `classLearnerType`, but should be overridden by
learners which narrow the range of use-cases upon construction. If the property
is overridden, the uninitialised type must be a sub-type of the class type.

### Initialised Learner Type

Once initialised (see [learner lifecycle](#initialisation) below), the 
`initialisedType` provides the type the learner implements in the context of the
data provided. This must be a sub-type of the uninitialised type, and
further-more, must not be a union type (i.e. the type must be fully determined).

### Adding Learner Types

Each learner-type is essentially a name given to a set of boolean predicates
on the structure of a data-set. There are 3 ways of adding new learner-types:

#### Intersection

Using the `intersectionOf` function, a new learner-type is produced which is
equivalent to all requirements of the passed in types. For example, given
`SingleTarget` is a learner-type which declares that the learner produces 
exactly 1 predicted attribute, and `Classifier` which declares that the learner
only produces nominal predicted attributes, we can define a learner-type
`SingleTargetClassifier` as `intersectionOf(SingleTarget, Classifier)`.

The learner-type produced by an intersection is a sub-type of all learner-types
participating in the intersection.

#### Union

The `unionOf` function produces learner-types which could be one (or more) of
many possible types. This is useful for class/uninitialised learner-types,
which may not be determined until they see the training headers during
initialisation.

All learner-types participating in the union type are sub-types of the union
itself.

#### Extension

Extending a learner-type is performed via the `extend` method, passing a
function which performs the actual check on the input/output headers. It is not
possible to extend intersection or union learner-types.

The extended type is equivalent to the base type with the additional check
performed on top, and as such is a sub-type of the base-type.

To create a brand new learner-type, extend the base of the type hierarchy,
`maia.ml.learner.type.AnyLearnerType`.

## Learner Lifecycle

There are three main phases of the lifecycle of a learner in MĀIA:

### Initialisation

Initialisation is the process of informing the learning algorithm of the
structure of the data it will be learning from. This is achieved by calling the
`initialise` method, passing the headers that the learner will be training on. A
learner should, upon initialisation, reset any learning state, determine its 
initialised learner type ([see above](#initialised-learner-type)), and determine
the subset of the provided headers that it will use for making predictions. It 
should also determine the headers of the predictions that it will produce, which
should not overlap with the prediction input-headers, but can include any
number of arbitrary additional headers.

### Training

Training is the process of updating the learner's internal state with example
data. This is performed by passing a data-set to the `train` method. The type
of the data-set should match the generic type parameter of the learner, usually
`maia.ml.dataset.DataStream<*>` or `maia.ml.dataset.DataBatch<*>`.

### Prediction

Prediction is the process of attempting to generate the values of certain
attributes, based on the examples seen thus far (during training). This is done
via the `predict` method, which is passed a data-row to create predictions for.
The row should match the structure of the predict input-headers as determined
during initialisation, and the data-row returned from `predict` should match
the structure of the predict output-headers, also determined during 
initialisation.
