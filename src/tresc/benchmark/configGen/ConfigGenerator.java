/*
 *
 * Copyright 2016 Big Data Curation Lab, University of Toronto,
 * 		   	  	  	   				 Patricia Arocena,
 *   								 Boris Glavic,
 *  								 Renee J. Miller
 *
 * This software also contains code derived from STBenchmark as described in
 * with the permission of the authors:
 *
 * Bogdan Alexe, Wang-Chiew Tan, Yannis Velegrakis
 *
 * This code was originally described in:
 *
 * STBenchmark: Towards a Benchmark for Mapping Systems
 * Alexe, Bogdan and Tan, Wang-Chiew and Velegrakis, Yannis
 * PVLDB: Proceedings of the VLDB Endowment archive
 * 2008, vol. 1, no. 1, pp. 230-244
 *
 * The copyright of the ToxGene (included as a jar file: toxgene.jar) belongs to
 * Denilson Barbosa. The iBench distribution contains this jar file with the
 * permission of the author of ToxGene
 * (http://www.cs.toronto.edu/tox/toxgene/index.html)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package tresc.benchmark.configGen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import tresc.benchmark.configGen.Constants.DisabledScenarios;
import tresc.benchmark.configGen.Constants.OutputOption;
import tresc.benchmark.configGen.Constants.ParameterName;
import tresc.benchmark.configGen.Constants.ScenarioName;
import tresc.benchmark.configGen.Constants.TrampXMLOutputSwitch;


/**
 * Generates config files for mapmerge evaluation based on various parameters.
 * 
 * @author mdangelo
 */


// MN there are no parameters with respect to .mapjob, .xsml and.xsd files - 21 April 2014
// PRG FIX Removed generation of random seed config line in method generateRandomsAndMappingLang() - Sep 12, 2012
// The random seed is what forces iBench to generate the same schema schema mappings (minus variations in 
// the name of elements in the source/target schemas) during any iteration of a given configuration. We now can
// generate random config files without imposing any particular seed. iBench supplies a random seem on demand.

// PRG ADDED + 1 to ensure that max repetition value is also taken into consideration in the random draw - Sep 22, 2012

// PRG FEB 20 2014 - Replaced "ParameterName" with "ConfigOptions" prefix when generating Reuse Configuration Lines
// tresc.benchmark.Configuration.java expects "ConfigOptions" as prefix in method readFromProperties!

public class ConfigGenerator {
	protected int _counter = 0;
	protected Random _generator = new Random();
//
	private String _outDir = "out";
	//MN changed the path - 21 April 2014
	private String _configPath = "resource/";
	
	private int max_repetitions = 10;
	
	public void setConfigPath (String configPath) {
		_configPath = configPath;
	}
	
	public String generateConfigFile(boolean noOutput) throws IOException
	{
		String _configName = "config" + _counter + ".txt";
		_outDir = "out" + _counter;
		_counter++;
		
		BufferedWriter configWriter = new BufferedWriter(new FileWriter(_configPath + _configName));

		generatePreambleAndOutputPaths(configWriter);
		generateScenarioRepetitions(configWriter);
		generateParameters(configWriter);
		generateDeviations(configWriter);
		generateRandomsAndMappingLang(configWriter);
		generateOutputActivation(configWriter, noOutput);
		generateTrampOutputActivation(configWriter);

		configWriter.close();
		
		return _configName;
	}

	private void generatePreambleAndOutputPaths(BufferedWriter configWriter) throws IOException 
	{
		configWriter.write("# Configuration File for iBench\n");
		configWriter.write("\n");
		
		configWriter.write("# Output Path Prefixes\n");
		configWriter.write("SchemaPathPrefix=" + _outDir + "\n");
		configWriter.write("InstancePathPrefix=" + _outDir + "\n");
		
		configWriter.write("\n");
	}

	public void generateScenarioRepetitions(BufferedWriter configWriter) throws IOException
	{
		configWriter.write("# Number of Instances for each Basic Scenario Type\n");
		
		// go through each scenario and generate a random numbers of repetitions for it
		for (ScenarioName name : Constants.ScenarioName.values()) {
			// PRG ADDED + 1 to ensure that max repetition value is also taken into consideration in the random draw - Sep 22, 2012
			// configWriter.write("Scenarios." + name + " = " + _generator.nextInt(max_repetitions) + "\n");
			configWriter.write("Scenarios." + name + " = " + _generator.nextInt(max_repetitions+1) + "\n");
		}

		configWriter.write("\n# These should always be zero\n");
		
		// certain scenarios cannot be run in iBench so ensure that they are disabled
		//MN How do we disable these two scenarios i.e., fusion and self joins? - 21 April 2014
		for (DisabledScenarios name : Constants.DisabledScenarios.values())
			configWriter.write("Scenarios." + name + " = " + 0 + "\n");
		
		configWriter.write("\n");
	}
	
	public void generateParameters(BufferedWriter configWriter) throws IOException
	{
		configWriter.write("# Parameters that define the shape of the schema and basic scenarios\n");
		
		for (ParameterName p : Constants.ParameterName.values()) 
			configWriter.write("ConfigOptions." + p + " = " + (_generator.nextInt(Constants.maxParameterValues.get(p)) + Constants.minParameterValues.get(p)) + "\n");

		configWriter.write("ConfigOptions.NestingDepth = " + 0 + "\n");
		
		//MN removed the following line - 21 April 2014
		//int srcReusePerc = _generator.nextInt(100);
		// PRG FEB 20 2014 - Replaced "ParameterName" with "ConfigOptions" prefix when generating Reuse Configuration Lines
		// tresc.benchmark.Configuration.java expects "ConfigOptions" as prefix in method readFromProperties!
		// configWriter.write("ParameterName.ReuseSourcePerc = " + srcReusePerc + "\n");
		// configWriter.write("ParameterName.NoReuseScenPerc = " + (100 - srcReusePerc) + "\n");
		//MN removed the following two lines - 21 April 2014
		//configWriter.write("ConfigOptions.ReuseSourcePerc = " + srcReusePerc + "\n");
		//configWriter.write("ConfigOptions.NoReuseScenPerc = " + (100 - srcReusePerc) + "\n");
		configWriter.write("\n");
	}
	
	public void generateDeviations(BufferedWriter configWriter) throws IOException
	{
		configWriter.write("# Deviations for each of the parameters\n");
		
		for (ParameterName p : Constants.ParameterName.values())
			configWriter.write("ConfigOptionsDeviation." + p + " = " + Constants.parameterDeviations.get(p) + "\n");
		
		configWriter.write("\n");
	}
	
	private void generateRandomsAndMappingLang(BufferedWriter configWriter) throws IOException 
	{
		configWriter.write("# Random number generator and max values, DataGenerator and MappingLang\n");
		// PRG DELETED Next Line to Avoid Generating Always the Same Schema Mappings per Iteration - Sep 12, 2012
		//configWriter.write("RandomSeed = " + _generator.nextInt() + "\n");
		configWriter.write("RepElementCount = 5\n");
		configWriter.write("MaxStringLength = 5\n");
		configWriter.write("MaxNumValue = 1000\n");
		configWriter.write("DataGenerator = TrampCSV\n");
		configWriter.write("MappingLanguage = SOtgds\n");
		
		configWriter.write("\n");
	}
	
	private void generateOutputActivation(BufferedWriter configWriter, 
			boolean noOutput) throws IOException 
	{
		configWriter.write("# Optional activation/deactivation of output options\n");
		
		for (OutputOption o : Constants.OutputOption.values()) {
			// do not output schema if nooutput option is set
			if (o.equals(OutputOption.TrampXML) && noOutput)
				configWriter.write("OutputOption." + o + " = false \n");
			else
				configWriter.write("OutputOption." + o + " = " 
						+ Constants.defaultOutputOptions.get(o) + "\n");
		}
		
		configWriter.write("\n");
	}
	
	private void generateTrampOutputActivation(BufferedWriter configWriter) throws IOException 
	{
		configWriter.write("# Optional activation/deactivation of output parts of the Tramp XML document\n");
		
		for (TrampXMLOutputSwitch t : Constants.TrampXMLOutputSwitch.values())
			configWriter.write("TrampXMLOutput." + t + " = " + Constants.defaultTrampXMLOutput.get(t) + "\n");
		
		configWriter.write("\n");
	}

}
