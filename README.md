# iBench #

iBench is a metadata generator for creating arbitrarily large and complex mappings, schemas and schema constraints. iBench can be used with a data generator to efficiently generate realistic data integration scenarios with varying degrees of size and complexity. iBench can be used to create benchmarks for different integration tasks including (virtual) data integration, data exchange, schema evolution, mapping operators like composition and inversion, and schema matching.

The user provides a configuration file specifying what types of meta-data and data to generate (e.g., schemas, data, constraints, mappings, ...) and properties of the generated scenario. iBench produces an XML file storing the meta-data generated based on the user configuration. If requested, iBench also generates instance data for generated schemas.

### Setup Guide ###

iBench is written in Java. To build the system you need [ant](http://ant.apache.org/). Simply run 

```
ant
```

in the main directory. This will build a jar file and create a `build` folder. This folder contains two "fat" jar files `iBench.jar` and `confFileGenerator.jar`. `iBench.jar` is the actual iBench system while `confFileGenerator.jar` is a batch tool for creating configuration files for iBench from a template. Furthermore, this folder contains scripts for running iBench on Mac OS or Linux as well as windows.

### Getting Started ###

The input to iBench is a configuration file (a text file with key value pairs, i.e., a Java Properties file) that determines the structure and characteristics of the scenario to be created. Some of the parameters control the structure of the generated schemas, mappings, and metadata, some parameters determine which mapping primitives the integration scenario should be composed of, and finally there are parameters that control what metadata is producted and in which format.

* The tech report mentioned below explains the available primitives and parameters
* The [Wiki - Configuration File](https://bitbucket.org/ibencher/ibench/wiki/ConfigurationFile) also has a page describing the configuration file format and parameters 

#### Usage Example


```sh
cd build
./iBench.sh -c 
```

### Example Configuration Files and UDPs ###

We maintain a [public repository](https://bitbucket.org/ibencher/ibenchconfigurationsandscenarios) with example configuration files and integration scenarios (which can be used as UDPs). Additions to this repository from the community are highly encouraged.

### Wiki ###

More detailed explanations of the configuration file format and how to use user-defined primitives (UDPs) will be added to the [Wiki](https://bitbucket.org/ibencher/ibench/wiki/Home) in the future.

### Publications ###

* Our recent [technical report](http://dblab.cs.toronto.edu/project/iBench/docs/iBench-TR-2015.pdf) gives a good overview of the project and current status
* See the iBench [project page](http://dblab.cs.toronto.edu/project/iBench/) at University of Toronto for a full list of publications

### Contact ###

* Patricia Arocena - Lead Data Architect at the University of Toronto Database Group
* Renée J. Miller - Professor at the University of Toronto Database Group
