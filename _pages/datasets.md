---
layout: page
title: Datasets
include_in_header: false
---

# MĀIA Datasets

Datasets in MĀIA are a collection of related instances of some phenomenon. Each
instance is represented by a row in the dataset, whereas each measurable
attribute of the phenomenon is represented by a column. A cell of the dataset
(i.e. the intersection of a row and column) is therefore the value of an
attribute (column) for a particular instance (row).

Datasets are represented by a number of interfaces which describe the dataset's
structure, but not its implementation. Therefore, many sources of data can be
used as datasets by implementing the relevant interfaces.

## Streaming Data vs. Batch Data

MĀIA makes a distinction between streaming sources of data and batch sources.
A streaming source is one where the data is retrieved row-by-row, and the
entire dataset is not expected to be held in memory all at once. It is an
expectation that a streaming source of data may not be able to reproduce a row
of data after it has been initially consumed. The base interface for streaming
datasets is `maia.ml.dataset.DataStream`. The method for getting instance data
(rows) from a data-stream is to use the `rowIterator` method, which will
return an iterator that will yield rows as they are produced by the data-source.

On the other hand, a batch of data is one where all rows can be held in memory
at once. This allows for repeatable access to the instance data, in
random-access order. The base interface for data-batches is
`maia.ml.dataset.DataBatch`, and data can be accessed via the `getValue` or
`getRow` methods.

The `DataBatch` interface inherits from the `DataStream` interface, which
represents the fact that in-memory datasets can be treated as streaming datasets
if required.

## Mutability

There are two kinds of mutability to consider in regard to datasets in MĀIA:
value-mutability and structural-mutability. Value-mutability means that the
value of an attribute for an instance (i.e. the value in a cell of the dataset)
can be modified. Structural-mutability means that rows and/or columns can be
added/removed from a dataset. Which types of mutability a dataset or data-row
provides are expressed by including the interfaces in `maia.ml.dataset.mutable`
in the dataset implementation's inheritance hierarchy.

## Metadata

All datasets must provide a meta-data instance, which details information that
applies to the dataset as a whole. The meta-data is provide via an instance of
the `maia.ml.dataset.DataMetadata` interface, which currently on prescribes
that the dataset is given a name.

## Headers, Types and Representations

All datasets must provide a set of headers to describe the attributes 
represented by their columns, accessed via the `headers` property. This is
achieved by creating an instance of
`maia.ml.dataset.headers.MutableDataColumnHeaders` and calling its mutating
methods to add the relevant columns. As the instance is an owned and mutable
object, its `readOnlyView` property can be used to get a read-only reference
to the headers for external provision.

If the dataset is a data-batch that has a mutable column-structure, the headers
object should be maintained by adding/deleting/changing the headers as the
relevant columns are likewise modified.

Each header in the headers collection has 4 attributes:

- `index`, the header's position in the collection,
- `name`, the name of the attribute/column
- `type`, the type of data stored in the column (see below), and,
- `isTarget`, the dataset's determination of whether the column represents
              feature data or target data.

### Types

The type of a dataset column in MĀIA is represented by an instance of the
`maia.ml.dataset.type.DataType` class. This class is part of a 3-layer
inheritance hierarchy:

- The base-class `maia.ml.dataset.type.DataType` itself,
- An intermediary class which represents the abstract notion of the data-type,
  and prescribes how to interact with the type itself, and,
- An implementation class which can read data of this type from a particular
  type of dataset.

The standard data-types which come with MĀIA can be found in the
`maia.ml.dataset.type.standard` package in the `maia-standard-types` module.

### Representations

Each data-type provides a number of representations (each inheriting from
`maia.ml.dataset.type.DataRepresentation`), which act as keys for getting data
from a dataset in particular forms. Every data-type has a "canonical"
representation, but may provide other representations where appropriate.

### Example

By way of example, imagine you are trying to see if a nominal attribute of some
row is equal to some particular class:

```kotlin
// Base imports from maia-dataset module
import maia.ml.dataset.DataRow
import maia.ml.dataset.headers.DataColumnHeaders
import maia.ml.dataset.type.DataType

// Type-specific imports from maia-standard-types
import maia.ml.dataset.type.standard.Nominal
import maia.ml.dataset.type.standard.NominalCanonicalRepresentation
import maia.ml.dataset.type.standard.NominalIndexRepresentation

fun classIs(
    row : DataRow, // The instance we are looking at the attribute for
    attributeIndex : Int, // The index of the attribute that we expect is nominal
    expectedClass : String, // The class we are testing for
    byString : Boolean // Whether to do string comparisons or index comparisons
) : Boolean {
    // Get the headers of the data-row
    val headers : DataColumnHeaders = row.headers

    // Get the type of the specified attribute
    val datatype : DataType<*, *> = headers[attributeIndex].type

    // Check that the type is nominal. The Nominal class specifies the
    // properties of nominal types, but datatype will be a sub-class which
    // knows how to access nominal data from whatever implementation of DataRow
    // the headers came from. However, we don't need to know the implementation
    // details, just to use them to get the value for this row.
    if (datatype !is Nominal<*, *, *, *>) return false
    
    // For a string comparison...
    if (byString) {
        // Get the canonical representation of the nominal type, which is
        // represented by strings containing the class names.
        val representation: NominalCanonicalRepresentation<*, *> = 
            datatype.canonicalRepresentation
        
        // Use the representation object to request the data from the row
        // in its string class-name form
        val actualClass: String = row.getValue(representation)
        
        // Check if it is what we expected
        return actualClass == expectedClass
        
    } else {
        // Perform the comparison by index. For the case of an individual row,
        // this may not provide much performance impact, but if we were
        // comparing multiple rows, the performance boost of comparing integers
        // instead of strings may be significant.

        // Get the index representation of the nominal type, which is
        // represented by integers indexing the Nominal type's ordering
        // of the classes.
        val representation: NominalIndexRepresentation<*, *> =
            datatype.indexRepresentation
        
        // Get the index of the expected class from the data-type
        val expectedClassIndex: Int = datatype.indexOf(expectedClass)

        // Use the representation object to request the data from the row
        // in its integer index form
        val actualClassIndex: Int = row.getValue(representation)

        // Check if it is what we expected
        return actualClassIndex == expectedClassIndex
    }
    
}
```

This example shows:

- Getting the headers from a data-structure (in this case, a `DataRow`, but it
  also applies to `DataStream` and `DataBatch` objects),
- Getting and checking the type of an attribute. If we knew the type of the
  attribute was nominal (from some other logic), we could also assert this
  to the compiler using `val datatype : Nominal<*, *, *, *> =
  headers[attributeIndex].type as Nominal<*, *, *, *>`, which would throw if
  that assertion was incorrect.
- Selecting a representation of that attribute from the possible representations
  of the attribute's type. In this case, we used a boolean parameter to decide
  whether to access the class-name (canonical) representation or the index
  representation.
- Getting representation-agnostic data from the attribute's type (the index
  of the expected class).
- Using a representation to get data from a data-structure in the required
  format.

## Rows

The `maia.ml.dataset.DataRow` interface describes a single row from a dataset.
Data-rows also provide access to the headers of the data-set they came from (or
their own set of headers if it is a stand-alone row).

## Views

The `maia.ml.dataset.view` package provides a number of classes for viewing
subsets of the rows/columns of a data-set as their own dataset, for providing
read-only access to mutable data-sets, etc.
