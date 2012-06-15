package tresc.benchmark;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;
import org.vagabond.util.LoggerUtil;

import smark.support.MappingScenario;
import tresc.benchmark.Constants.DataGenType;
import tresc.benchmark.Constants.ParameterName;
import tresc.benchmark.schemaGen.AddAttributeScenarioGenerator;
import tresc.benchmark.schemaGen.CopyScenarioGenerator;
import tresc.benchmark.schemaGen.DeleteAttributeScenarioGenerator;
import tresc.benchmark.schemaGen.FlatteningScenarioGenerator;
import tresc.benchmark.schemaGen.FusionScenarioGenerator;
import tresc.benchmark.schemaGen.GLAVScenarioGenerator;
import tresc.benchmark.schemaGen.HorizontalPartitionScenarioGenerator;
import tresc.benchmark.schemaGen.MergingScenarioGenerator;
import tresc.benchmark.schemaGen.NestingScenarioGenerator;
import tresc.benchmark.schemaGen.AbstractScenarioGenerator;
import tresc.benchmark.schemaGen.RandomSourceSkolemToMappingGenerator;
import tresc.benchmark.schemaGen.ScenarioGenerator;
import tresc.benchmark.schemaGen.SelfJoinScenarioGenerator;
import tresc.benchmark.schemaGen.SourceFDGenerator;
import tresc.benchmark.schemaGen.SurrogateKeysScenarioGenerator;
import tresc.benchmark.schemaGen.VPIsAScenarioGenerator;
import tresc.benchmark.schemaGen.ValueGenerationScenarioGenerator;
import tresc.benchmark.schemaGen.ValueManagementScenarioGenerator;
import tresc.benchmark.schemaGen.VerticalPartitionScenarioGenerator;
import tresc.benchmark.dataGen.DataGenerator;
import tresc.benchmark.dataGen.ToXDataGenerator;
import tresc.benchmark.dataGen.TrampCSVGen;
import vtools.dataModel.schema.Schema;

public class Generator {
	static Logger log = Logger.getLogger(Generator.class);
	
	private AbstractScenarioGenerator[] scenarioGenerators;
	private ScenarioGenerator fdGen;
	private DataGenerator dataGenerator;
	private RandomSourceSkolemToMappingGenerator skGen;
	
	public Generator(Configuration config) {
		int numOfScenarios = Constants.ScenarioName.values().length;
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

	public MappingScenario generateScenario(Configuration configuration) throws Exception {
		/*
		 * First we create an empty dummy schema
		 */
		MappingScenario scenario = new MappingScenario(configuration);

		// no reuse of source and target schemas?
		if (configuration.getParam(ParameterName.NoReuseScenPerc) == 100)
			for (int i = 0, imax = scenarioGenerators.length; i < imax; i++)
				scenarioGenerators[i].generateScenario(scenario, configuration);
		// partial reuse of generated source and target schema elements
		else {
			// init generators
			for (int i = 0, imax = scenarioGenerators.length; i < imax; i++)
				scenarioGenerators[i].init(configuration, scenario);
			
			// do one scenario of each until we are done
			while(scenario.getNumBasicScen() < configuration.getTotalNumScen()) {
				for (int i = 0, imax = scenarioGenerators.length; i < imax; i++)
					scenarioGenerators[i].generateNextScenario(scenario, configuration);				
			}
		}
		
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

	public DataGenerator getDataGenerator() {
		return dataGenerator;
	}

}
