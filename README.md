# iBench #

In the iBench project we develop a metadata generator for creating arbitrarily large and complex mappings, schemas and schema constraints. iBench can be used with a data generator to efficiently generate realistic data integration scenarios with varying degrees of size and complexity. iBench can be used to create benchmarks for different integration tasks including (virtual) data integration, data exchange, schema evolution, mapping operators like composition and inversion, and schema matching.

The user provides a configuration file specifying what types of meta-data and data to generate (e.g., schemas, data, constraints, mappings, ...) and properties of the generated scenario. iBench produces an XML file storing the meta-data generated based on the user configuration. If requested, iBench also generates instance data for generated schemas.

### Setup Guide ###

iBench is written in Java. To build the system you need [ant](http://ant.apache.org/). Simply run 

```
ant
```

in the main directory. This will build a jar file and create a build folder. The `mac` subfolder contains a script for running iBench on Mac OS or Linux. The `windows` subfolder contains a batch script for windows users.

### Getting Started ###

TODO

### Publications ###

* See the iBench [project page](http://dblab.cs.toronto.edu/project/iBench/) at University of Toronto

### Contact ###

* Patricia Arocena - PostDoc at the University of Toronto Database Group
* Renée J. Miller - Professor at the University of Toronto Database Group
