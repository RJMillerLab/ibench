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
package tresc.benchmark;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.LoggerUtil;
import org.vagabond.xmlmodel.MappingScenarioDocument;

import smark.support.MappingScenario;
import tresc.benchmark.Constants.DataGenType;
import tresc.benchmark.Constants.ParameterName;
import tresc.benchmark.dataGen.DataGenerator;
import tresc.benchmark.schemaGen.AbstractScenarioGenerator;
import tresc.benchmark.schemaGen.AddAttributeScenarioGenerator;
import tresc.benchmark.schemaGen.AddDeleteScenarioGenerator;
import tresc.benchmark.schemaGen.CopyScenarioGenerator;
import tresc.benchmark.schemaGen.DeleteAttributeScenarioGenerator;
import tresc.benchmark.schemaGen.FlatteningScenarioGenerator;
import tresc.benchmark.schemaGen.FusionScenarioGenerator;
import tresc.benchmark.schemaGen.GLAVScenarioGenerator;
import tresc.benchmark.schemaGen.HorizontalPartitionScenarioGenerator;
import tresc.benchmark.schemaGen.LoadExistingScenarioGenerator;
import tresc.benchmark.schemaGen.MergeAddScenarioGenerator;
import tresc.benchmark.schemaGen.MergingScenarioGenerator;
import tresc.benchmark.schemaGen.NestingScenarioGenerator;
import tresc.benchmark.schemaGen.RandomSourceSkolemToMappingGenerator;
import tresc.benchmark.schemaGen.ScenarioGenerator;
import tresc.benchmark.schemaGen.SelfJoinScenarioGenerator;
import tresc.benchmark.schemaGen.SourceFDGenerator;
import tresc.benchmark.schemaGen.SourceInclusionDependencyGenerator;
import tresc.benchmark.schemaGen.SurrogateKeysScenarioGenerator;
import tresc.benchmark.schemaGen.TargetInclusionDependencyGenerator;
import tresc.benchmark.schemaGen.VPHasAScenarioGenerator;
import tresc.benchmark.schemaGen.VPIsAScenarioGenerator;
import tresc.benchmark.schemaGen.VPNtoMScenarioGenerator;
import tresc.benchmark.schemaGen.ValueGenerationScenarioGenerator;
import tresc.benchmark.schemaGen.ValueManagementScenarioGenerator;
import tresc.benchmark.schemaGen.VerticalPartitionScenarioGenerator;
import tresc.benchmark.schemaGen.VPIsAAuthorityScenarioGenerator;

//MN added new VP - 23 June 2014
//PRG RENAMED CLASS - Before was newVP, Now is VPIsAAuthorityScenarioGenerator - 16 Oct 2014

public class Generator {
	static Logger log = Logger.getLogger(Generator.class);
	
	private AbstractScenarioGenerator[] scenarioGenerators;
	private AbstractScenarioGenerator[] loadScenarioGenerators;
	private ScenarioGenerator fdGen;
	private DataGenerator dataGenerator;
	private RandomSourceSkolemToMappingGenerator skGen;
	
	//MN two attributes for random source and target 
	private ArrayList<String> randomSourceInclusionDependencies;
	private ArrayList<String> randomTargetInclusionDependencies;
	
	public Generator(Configuration config) throws XmlException, IOException {
		int numOfScenarios = Constants.ScenarioName.values().length - 1;
		
		//TODO if you creates new primitives from existing mapping files then add these here
		
		scenarioGenerators = new AbstractScenarioGenerator[numOfScenarios];
		
		for (int i = 0; i < numOfScenarios; i++)
			scenarioGenerators[i] = null;

		scenarioGenerators[Constants.ScenarioName.COPY.ordinal()] =
				new CopyScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.VALUEGEN.ordinal()] =
				new ValueGenerationScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.HORIZPARTITION.ordinal()] =
				new HorizontalPartitionScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.SURROGATEKEY.ordinal()] =
				new SurrogateKeysScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.MERGING.ordinal()] =
				new MergingScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.VALUEMANAGEMENT.ordinal()] =
				new ValueManagementScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.FUSION.ordinal()] =
				new FusionScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.FLATTENING.ordinal()] =
				new FlatteningScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.NESTING.ordinal()] =
				new NestingScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.SELFJOINS.ordinal()] =
				new SelfJoinScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.VERTPARTITION.ordinal()] =
				new VerticalPartitionScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.GLAV.ordinal()] =
				new GLAVScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.ADDATTRIBUTE.ordinal()] =
				new AddAttributeScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.DELATTRIBUTE.ordinal()] =
				new DeleteAttributeScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.VERTPARTITIONISA.ordinal()] =
				new VPIsAScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.VERTPARTITIONHASA.ordinal()] =
				new VPHasAScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.VERTPARTITIONNTOM.ordinal()] =
				new VPNtoMScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.ADDDELATTRIBUTE.ordinal()] =
				new AddDeleteScenarioGenerator();
		scenarioGenerators[Constants.ScenarioName.MERGEADD.ordinal()] =
				new MergeAddScenarioGenerator();
		//MN added new vertical partitioning - 23 June 2014
		scenarioGenerators[Constants.ScenarioName.VERTPARTITIONISAAUTHORITY.ordinal()] =
				new VPIsAAuthorityScenarioGenerator();
		
		// add new scenario generators for load scenarios
		int numLoad = config.getNumLoadScenarios();
		loadScenarioGenerators = new AbstractScenarioGenerator[numLoad];
		for(int i = 0; i < numLoad; i++) {
			String name = config.getLoadScenarioNames().get(i);
			MapScenarioHolder h = new MapScenarioHolder(); 
			h.setDocument(MappingScenarioDocument.Factory.parse(config.getExistingScenarios().get(i)));
			loadScenarioGenerators[i] = new LoadExistingScenarioGenerator(h, name);
		}
		
		// create an FD generator
		fdGen = new SourceFDGenerator();
	
		// create an generator for source attribute skolem functions
		skGen = new RandomSourceSkolemToMappingGenerator();
		
		// create a data generator
		dataGenerator =  instDataGen(config);
	};

	private DataGenerator instDataGen(Configuration conf) {
		DataGenType type = conf.getDataGen();
		Class<? extends DataGenerator> clazz =
				(Class<? extends DataGenerator>) Constants.dataGens.get(type);
		DataGenerator result;
		Constructor<? extends DataGenerator> c;
		try {
			c = clazz.getConstructor(Configuration.class);
			result = c.newInstance(conf);
			return result;
		}
		catch (Exception e) {
			LoggerUtil.logException(e, log);
			System.exit(1);
		}
		return null; // keep compiler quiet
	}
	
	//MN this method returns random source inclusion dependencies (only regular ones) - 14 April 2014
	public ArrayList<String> getRandomSourceInlcusionDependencies (){
		return randomSourceInclusionDependencies;
	}
	
	//MN this method returns random target inclusion dependencies (only regular ones) - 14 April 2014
	public ArrayList<String> getRandomTargetInclusionDependencies(){
		return randomTargetInclusionDependencies;
	}

	public MappingScenario generateScenario(Configuration configuration) throws Exception {
		/*
		 * First we create an empty dummy schema
		 */
		MappingScenario scenario = new MappingScenario(configuration);

		// no reuse of source and target schemas?
		if (configuration.getParam(ParameterName.NoReuseScenPerc) == 100) {
			for (int i = 0, imax = scenarioGenerators.length; i < imax; i++)
				scenarioGenerators[i].generateScenario(scenario, configuration);
			for (int i = 0, imax = loadScenarioGenerators.length; i < imax; i++)
				loadScenarioGenerators[i].generateScenario(scenario, configuration);
		}
		// partial reuse of generated source and target schema elements
		else {
			// init generators
			for (int i = 0, imax = scenarioGenerators.length; i < imax; i++)
				scenarioGenerators[i].init(configuration, scenario);
			for (int i = 0, imax = loadScenarioGenerators.length; i < imax; i++)
				loadScenarioGenerators[i].init(configuration, scenario);
			
			// do one scenario of each until we are done
			while(scenario.getNumBasicScen() < configuration.getTotalNumScen()) {
				for (int i = 0, imax = scenarioGenerators.length; i < imax; i++)
					scenarioGenerators[i].generateNextScenario(scenario, configuration);
				for (int i = 0, imax = loadScenarioGenerators.length; i < imax; i++)
					loadScenarioGenerators[i].generateNextScenario(scenario, configuration);
			}
		}
		
		
		//MN generates Random Source Inclusion Dependencies
		SourceInclusionDependencyGenerator srcIDGen = new SourceInclusionDependencyGenerator();
		srcIDGen.generateScenario(scenario, configuration);
		//MN to inject random source inclusion dependencies into mappings - 14 April 2014
		randomSourceInclusionDependencies = srcIDGen.getRandomSourceIDs();
				
		//MN generates Random Target Inclusion Dependencies
		TargetInclusionDependencyGenerator trgIDGen = new TargetInclusionDependencyGenerator();
		trgIDGen.generateScenario(scenario, configuration);
		//MN to inject random target inclusion dependencies into mappings - 14 April 2014
		randomTargetInclusionDependencies = trgIDGen.getRandomTargetIDs();

		// create FKs
		scenario.getDocFac().copyFKsToRealDoc();
		
		// create FDs?
		if (configuration.getTrampXMLOutputOption(Constants.TrampXMLOutputSwitch.FDs))
			fdGen.generateScenario(scenario, configuration);

		// do the source attr with skolem trick
		if (configuration.getParam(Constants.ParameterName.SourceSkolemPerc) != 0)
			skGen.generateScenario(scenario, configuration);
		
		return scenario;
	}

	public void generateSourceData(MappingScenario scenario) throws Exception {
		dataGenerator.setSchema(scenario.getSource());
		dataGenerator.setConstraints(scenario.getSrcConstraints());
		dataGenerator.setScenario(scenario);
		dataGenerator.generateData();
	}
	
	public void generateTargetData(MappingScenario scenario) throws Exception {
		// new method not sure if anything works at all
		dataGenerator.setSchema(scenario.getTarget());          
		// to be tested \/
		dataGenerator.setConstraints(scenario.getTgtConstraints());
		dataGenerator.setScenario(scenario);
		dataGenerator.generateData();
	}


	public DataGenerator getDataGenerator() {
		return dataGenerator;
	}

}
