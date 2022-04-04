---
layout: page
title: Overview
include_in_header: true
---

# Overview of MĀIA

MĀIA is a machine-learning library written in Kotlin for use on Android
devices. It is designed to provide a type-safe environment for constructing
ML services without compromising on performance. There are 2 main branches of
the MĀIA module structure:

- Machine Learning
  - [Datasets](datasets.md) - abstractions for sources of instance-based data.
  - [Learners](learners.md) - structures for the definition of learning 
                              algorithms and their capabilities/compatibilities.
- Execution Graphs
  - [Configuration](configuration.md) - Type-safe document structure for
                                        repeatable object instantiation.
  - [Topology](topology.md) - Asynchronous execution graphs.
