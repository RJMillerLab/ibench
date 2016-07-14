package tresc.benchmark;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import org.kohsuke.args4j.Option;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.PropertyWrapper;

import tresc.benchmark.Constants.DBOption;
import tresc.benchmark.Constants.DESErrorType;
import tresc.benchmark.Constants.DataGenType;
import tresc.benchmark.Constants.MappingLanguageType;
import tresc.benchmark.Constants.OutputOption;
import tresc.benchmark.Constants.ParameterName;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Constants.TrampXMLOutputSwitch;
import tresc.benchmark.data.NamingPolicy;
import vtools.dataModel.types.DataType;
import vtools.dataModel.types.DataTypeHandler;


public class Configuration {
	private int[] _repetitions;
	private int[] _numExplsForType;
	
	// private long[] _seeds;
	private long _seed;
	private Random randomGenerator;

	private int[][] _configurations;
	private boolean[] _outputOpts;
	private int[] _groundTruthValues;
	private int[] _groundTruthDev;
	private boolean[] _trampXMLoutputOpts;
	private String[] _dbOptions;
	
	@Option(name = "-sourceSchemaFile",
			usage = "name for the source schema file")
	String sourceSchemaFile;
	@Option(name = "-targetSchemaFile",
			usage = "name for the target schema file")
	String targetSchemaFile;
	@Option(name = "-sourceInstanceFile",
			usage = "name for the source instance file")
	String sourceInstanceFile;
	@Option(name = "-sourceDocumentName",
			usage = "name for the source document")
	String sourceDocumentName;
	@Option(name = "-mappingFile",
			usage = "name for file to store the mappings")
	String mappingFile;
	@Option(name = "-schemaFile",
			usage = "name for Vagabond mapping scenarion XML file to generate")
	String schemaFile;

	@Option(name = "-schemaPrefix", usage = "schema path prefix")
	static String schemaPathPrefix = "./out";
	@Option(name = "-instancePrefix", usage = "instance path prefix")
	static String instancePathPrefix = "./out";

	@Option(name = "-p", usage = "Read configuration from this property file")
	String propertyFileName = null;
	@Option(name = "-c", usage = "Read configuarations from this file")
	String configurationFile = null;

	@Option(name = "-repElemCount", usage = "???")
	int repElemCountValue;
	@Option(name = "-maxStringLength", usage = "maximal length of strings used")
	int maxStringLength;
	@Option(name = "-maxNumericValue", usage = "maximal numeric value used")
	int maxNumValue;

	int namingPolicy;

	List<File> loadScenarios;
	private List<String> loadScenarioNames;
	private int numLoadScenarios = 0;
	private int[] numLoadScenarioInsts;
	
	DataGenType dataGen = DataGenType.TrampCSV;
	MappingLanguageType mapType = MappingLanguageType.FOtgds;
	

	public Configuration() {
		initArrays();
		setDefaultConfiguration();
		loadScenarios = new ArrayList<File> ();
		loadScenarioNames = new ArrayList<String> ();
	}

	public Configuration(PropertyWrapper prop) throws Exception {
		initArrays();
		readFromProperties(prop);
	}

	public void readFromProperties(PropertyWrapper prop) throws Exception {
		String fileNameSuffix = "";

		// read the output file prefixes
		setSchemaPathPrefix(prop.getProperty("SchemaPathPrefix", "out"));
		setInstancePathPrefix(prop.getProperty("InstancePathPrefix", "out"));

		// read the number of instances to create for each scenario
		prop.setPrefix("Scenarios");
		for (ScenarioName name : Constants.ScenarioName.values()) {
			int numScen = prop.getInt(name.toString(), 0);
			_repetitions[name.ordinal()] = numScen;
			if (numScen != 0)
				fileNameSuffix += Constants.nameForScenarios.get(name);
		}
		prop.resetPrefix();

		// read the scenario configuration parameters
		prop.setPrefix("ConfigOptions");
		for (ParameterName p : Constants.ParameterName.values()) {
			int value = prop.getInt(p.toString(), 
					Constants.defaultParameterValues.get(p));
			_configurations[p.ordinal()][0] = value;
			fileNameSuffix += "_" + value;
		}
		prop.resetPrefix();

		// read the standard derivations for the configuration parameters
		prop.setPrefix("ConfigOptionsDeviation");
		for (ParameterName p : Constants.ParameterName.values()) {
			int value = prop.getInt(p.toString(), 
					Constants.defaultParameterDeviation.get(p));
			_configurations[p.ordinal()][1] = value;
		}
		prop.resetPrefix();

		// read the output activation/deactivation options
		prop.setPrefix("OutputOption");
		for (OutputOption o : Constants.OutputOption.values()) {
			boolean b = prop.getBool(o.toString(), _outputOpts[o.ordinal()]);
			_outputOpts[o.ordinal()] = b;
		}
		prop.resetPrefix();
		
		prop.setPrefix("GroundTruthValues");
		for (DESErrorType v : Constants.DESErrorType.values()) {
			int b = prop.getInt(v.toString(), _groundTruthValues[v.ordinal()]);
			_groundTruthValues[v.ordinal()] = b;
		}
		prop.resetPrefix();
		
		prop.setPrefix("GroundTruthDev");
		for (DESErrorType v : Constants.DESErrorType.values()) {
			int b = prop.getInt(v.toString(), _groundTruthDev[v.ordinal()]);
			_groundTruthDev[v.ordinal()] = b;
		}
		prop.resetPrefix();
		
		// read switches for omitting parts of the Tramp XML document
		prop.setPrefix("TrampXMLOutput");
		for (TrampXMLOutputSwitch s : Constants.TrampXMLOutputSwitch.values()) {
			boolean b =
					prop.getBool(s.toString(), _trampXMLoutputOpts[s.ordinal()]);
			_trampXMLoutputOpts[s.ordinal()] = b;
		}
		prop.resetPrefix();

		// read how many explanations to generate
		prop.setPrefix("ExplGenNum");
		for(DESErrorType e: Constants.DESErrorType.values()) {
			int val = prop.getInt(e.toString(), 0);
			_numExplsForType[e.ordinal()] = val;
		}
		prop.resetPrefix();
		
		// database connection options
		prop.setPrefix("DB");
		for (DBOption o : Constants.DBOption.values()) {
			String val = prop.getProperty(o.toString(), _dbOptions[o.ordinal()]);
			_dbOptions[o.ordinal()] = val;
		}
		prop.resetPrefix();
		
		// explgen options like percentage of errors to add
		
		// options for loading existing scenarios as new primitives
		prop.setPrefix("LoadScenarios");
		setNumLoadScenarios(prop.getInt("NumScenarios", 0));
		numLoadScenarioInsts = new int[numLoadScenarios];
		
		for (int i = 0; i < getNumLoadScenarios(); i++) {
			String fileName = prop.getProperty(i + ".File", "");
			String name = prop.getProperty(i + ".Name", "");
			int numInst = prop.getInt(i + ".Inst", 0);
			File scenFile = new File(fileName);
			if (!scenFile.exists())
				throw new Exception("scenario file <" + scenFile + "> from <" + fileName + "> does not exist");
			numLoadScenarioInsts[i] = numInst;
			loadScenarios.add(scenFile);
			loadScenarioNames.add(name);
		}
		prop.resetPrefix();
		
		// Reading user's data type and distribution
		prop.setPrefix("DataType");
		DataTypeHandler.getInst().setNumDTs(prop.getInt("NumDataType"));
		DataType data;
		
		List<DataType> types = new ArrayList<>();
		float[] percentages = new float[DataTypeHandler.getInst().getNumDTs()];
		Map<String, DataType> typesMap = new HashMap<>();
		
		for (int i = 0; i < DataTypeHandler.getInst().getNumDTs(); i++) {
			data = new DataType();
			data.setName(prop.getProperty(i + ".Name")); 
			data.setClassPath(prop.getProperty(i + ".ClassPath"));
			data.setPercentage(prop.getFloat(i + ".Percentage"));
			data.setDbType(prop.getProperty(i + ".DBType"));
			
			percentages[i] = data.getPercentage();
			typesMap.put(data.getName(), data);
			
			//DataTypeHandler.getInst().getNameToDTMap().put(data.getDbType(), data);
			types.add(data);
			
		}
		DataTypeHandler.getInst().setNameToDTMap(typesMap);
		DataTypeHandler.getInst().setTypes(types);
		DataTypeHandler.getInst().setPercentages(percentages);
		prop.resetPrefix();
		
		// read remaining and optional parameters
		_seed = prop.getLong("RandomSeed", 0L);
		if (_seed == 0L)
			randomGenerator = new Random();
		else
			randomGenerator.setSeed(_seed);

		repElemCountValue = prop.getInt("RepElementCount", 1);
		maxStringLength = prop.getInt("MaxStringLength", 10);
		maxNumValue = prop.getInt("MaxNumValue", 100);

		// data generator and type of mapping language
		dataGen =
				(DataGenType) prop.getEnumProperty("DataGenerator",
						DataGenType.class, dataGen);
		mapType =
				(MappingLanguageType) prop.getEnumProperty("MappingLanguage",
						MappingLanguageType.class, mapType);
		// read optional parameters
		genFileNames(fileNameSuffix);

		prop.setPrefix("FileNames");
		sourceSchemaFile = prop.getProperty("SourceSchema", sourceSchemaFile);
		targetSchemaFile = prop.getProperty("TargetSchema", targetSchemaFile);
		sourceInstanceFile =
				prop.getProperty("SourceInstance", sourceInstanceFile);
		sourceDocumentName =
				prop.getProperty("SourceDocumentName", sourceDocumentName);
		schemaFile = prop.getProperty("Schemas", schemaFile);
		prop.resetPrefix();
	}

	// deprecate this one because it does not read all the options anymore
	public Configuration(String configLine) {
		// other than that, we need to read a configuration file for which by
		// the name, we get some info.
		initArrays();

		String fileNameSuffix = "";

		// COPY VALUGEN HORIPARITION SURROGATEKEY VERTPARTITION FLATTENING
		// NESTING SELF_JOINS MERGING FUSION VALUEMAANGEMENT GLAV
		// NUMBER_OF_SUB_ELEMENTS NESTING_DEPTH JOIN_SIZE JOIN_KIND
		// NUM_OF_JOIN_ATTRS NUM_OF_PARAM_IN_FUNC
		// DERIVATION: NUMBER_OF_SUB_ELEMENTS NESTING_DEPTH JOIN_SIZE JOIN_KIND
		// NUM_OF_JOIN_ATTRS NUM_OF_PARAM_IN_FUNC
		// optional parameters SOURCE_SCHEMA_NAME TARGET_SCHEMA_NAME
		// INSTANCE_FILE SOURCE_DOC_NAME MAPPING_FILE_NAME

		StringTokenizer st = new StringTokenizer(configLine, " \t");
		
		// read the number of repetitions for each scenario
		for (int i = 0; i < Constants.ScenarioName.values().length; i++) {
			ScenarioName scen = Constants.ScenarioName.values()[i]; 
			int numScen = Integer.valueOf(st.nextToken());

			_repetitions[i] = numScen;
			if (numScen != 0)
				fileNameSuffix += Constants.nameForScenarios.get(scen);
		}
		
		// read the parameters
		for(int i = 0; i < Constants.ParameterName.values().length; i++) {
			int val= Integer.valueOf(st.nextToken());
			_configurations[i][0] = val;
			fileNameSuffix += "_" + val;
		}

		// read the deviations
		for(int i = 0; i < Constants.ParameterName.values().length; i++) {
			int val= Integer.valueOf(st.nextToken());
			_configurations[i][1] = val;
			fileNameSuffix += "_" + val;
		}
		
		// generate the output file names
		genFileNames(fileNameSuffix);

		// now the seed RANDOM_SEED REP_ELEM_COUNT MAX_STRING_LEN MAX_NUM_VALUE
		_seed = Integer.valueOf(st.nextToken());
		randomGenerator.setSeed(_seed);

		repElemCountValue = Integer.valueOf(st.nextToken());
		maxStringLength = Integer.valueOf(st.nextToken());
		maxNumValue = Integer.valueOf(st.nextToken());

		// optional parameters SOURCE_SCHEMA_NAME TARGET_SCHEMA_NAME
		// INSTANCE_FILE SOURCE_DOC_NAME MAPPING_FILE_NAME

		if (st.hasMoreTokens())
			sourceSchemaFile = st.nextToken();

		if (st.hasMoreTokens())
			targetSchemaFile = st.nextToken();

		if (st.hasMoreTokens())
			sourceInstanceFile = st.nextToken();

		if (st.hasMoreTokens())
			sourceDocumentName = st.nextToken();

		if (st.hasMoreTokens())
			mappingFile = st.nextToken();

	}

	private void genFileNames(String fileNameSuffix) {
		sourceSchemaFile = "S" + fileNameSuffix + "_Src.xsd";
		targetSchemaFile = "S" + fileNameSuffix + "_Tgt.xsd";
		sourceInstanceFile = "T" + fileNameSuffix + ".tsl";
		sourceDocumentName = "I" + fileNameSuffix;
		schemaFile = "Schemas" + fileNameSuffix + ".xml";

		mappingFile = "M" + fileNameSuffix;
	}

	private void initArrays() {
		_repetitions = new int[Constants.ScenarioName.values().length];
		_configurations = new int[Constants.ParameterName.values().length][2];
		_outputOpts = new boolean[Constants.OutputOption.values().length];
		_groundTruthValues = new int[Constants.DESErrorType.values().length];
		_groundTruthDev = new int[Constants.DESErrorType.values().length];
		_trampXMLoutputOpts =
				new boolean[Constants.TrampXMLOutputSwitch.values().length];
		_numExplsForType = new int[Constants.DESErrorType.values().length];
		_dbOptions = new String[Constants.DBOption.values().length];
		
		randomGenerator = new Random();
		initOptionsDefaults();
	}

	private void initOptionsDefaults() {
		for (OutputOption o : Constants.OutputOption.values())
			_outputOpts[o.ordinal()] =
					Constants.defaultOutputOptionValues.get(o);
		
		for (DESErrorType v : Constants.DESErrorType.values())
			_groundTruthValues[v.ordinal()] = 
					Constants.defaultGroundTruthValues.get(v);
		
		for (DESErrorType v : Constants.DESErrorType.values())
			_groundTruthDev[v.ordinal()] = 
					Constants.defaultGroundTruthDeviation.get(v);
		
		for (TrampXMLOutputSwitch s : Constants.TrampXMLOutputSwitch.values())
			_trampXMLoutputOpts[s.ordinal()] =
					Constants.trampXmlOutDefaults.get(s);
		
		for (DBOption o : Constants.DBOption.values())
			_dbOptions[o.ordinal()] =
					Constants.dbOptionDefaults.get(o);
	}

	private void setDefaultConfiguration() {
		for (int i = 0, imax = _repetitions.length; i < imax; i++) {
			_repetitions[i] = 0;
		}
		// set the repetitions
		_repetitions[Constants.ScenarioName.COPY.ordinal()] = 1;
		_repetitions[Constants.ScenarioName.VALUEGEN.ordinal()] = 0;
		_repetitions[Constants.ScenarioName.HORIZPARTITION.ordinal()] = 0;
		_repetitions[Constants.ScenarioName.SURROGATEKEY.ordinal()] = 0;
		_repetitions[Constants.ScenarioName.MERGING.ordinal()] = 0;
		_repetitions[Constants.ScenarioName.VALUEMANAGEMENT.ordinal()] = 0;
		_repetitions[Constants.ScenarioName.FUSION.ordinal()] = 0;
		_repetitions[Constants.ScenarioName.FLATTENING.ordinal()] = 0;
		_repetitions[Constants.ScenarioName.NESTING.ordinal()] = 0;
		_repetitions[Constants.ScenarioName.SELFJOINS.ordinal()] = 0;
		_repetitions[Constants.ScenarioName.VERTPARTITION.ordinal()] = 0;
		_repetitions[Constants.ScenarioName.GLAV.ordinal()] = 0;

		// set the seeds for the randomness
		/*
		 * _seeds[Constants.ScenarioName.COPY.ordinal()] = 1973;
		 * _seeds[Constants.ScenarioName.VALUEGEN.ordinal()] = 1978;
		 * _seeds[Constants.ScenarioName.HORIZPARTITION.ordinal()] = 1974;
		 * _seeds[Constants.ScenarioName.SURROGATEKEY.ordinal()] = 1980;
		 * _seeds[Constants.ScenarioName.MERGING.ordinal()] = 1952;
		 * _seeds[Constants.ScenarioName.VALUEMANAGEMENT.ordinal()] = 1947;
		 * _seeds[Constants.ScenarioName.FUSION.ordinal()] = 1821;
		 * _seeds[Constants.ScenarioName.FLATTENING.ordinal()] = 1940;
		 * _seeds[Constants.ScenarioName.NESTING.ordinal()] = 1453;
		 * _seeds[Constants.ScenarioName.SELFJOINS.ordinal()] = 2000;
		 * _seeds[Constants.ScenarioName.VERTPARTITION.ordinal()] = 2007;
		 * _seeds[Constants.ScenarioName.GLAV.ordinal()] = 0;
		 */

		_seed = 1981;
		randomGenerator.setSeed(_seed);

		// Set the parameters
		_configurations[Constants.ParameterName.NumOfSubElements.ordinal()][0] =
				3;
		_configurations[Constants.ParameterName.NestingDepth.ordinal()][0] = 5;
		_configurations[Constants.ParameterName.JoinSize.ordinal()][0] = 3;
		_configurations[Constants.ParameterName.JoinKind.ordinal()][0] =
				Constants.JoinKind.CHAIN.ordinal();
		_configurations[Constants.ParameterName.NumOfJoinAttributes.ordinal()][0] =
				2;
		_configurations[Constants.ParameterName.NumOfParamsInFunctions
				.ordinal()][0] = 0;

		// Set the default deviation to be the half of the corresponding param
		for (int i = 0, imax = _configurations.length; i < imax; i++) {
			// _configurations[i][1] = (int) (_configurations[i][0] * 0.5);
			_configurations[i][1] = 0;
		}

		sourceSchemaFile = "S";
		// targetSchemaFile = "ToXgene.template";
		targetSchemaFile = "T";
		sourceInstanceFile = "ToXgene.template";
		// sourceDocumentName = null;
		sourceDocumentName = "I.xml";
		schemaPathPrefix = ".";
		instancePathPrefix = ".";
		namingPolicy = NamingPolicy.LowerUpper;

		mappingFile = "M.html";
		schemaFile = "Schemas.xml";

	}

	public int getNamingPolicy() {
		return namingPolicy;
	}

	public void setParam(Constants.ParameterName param, int value) {
		_configurations[param.ordinal()][0] = value;
	}

	public int getParam(Constants.ParameterName param) {
		return _configurations[param.ordinal()][0];
	}

	public void setDeviation(Constants.ParameterName param, int value) {
		_configurations[param.ordinal()][1] = value;
	}

	public int getDeviation(Constants.ParameterName param) {
		return _configurations[param.ordinal()][1];
	}

	public int getScenarioRepetitions(int scenario) {
		return _repetitions[scenario];
	}
	
	public void setScenarioRepetitions(ScenarioName n, int value) {
		setScenarioRepetitions(n.ordinal(), value);
	}
	
	public void setScenarioRepetitions(int scenario, int value) {
		_repetitions[scenario] = value;
	}

	public boolean getOutputOption(OutputOption o) {
		return _outputOpts[o.ordinal()];
	}
	
	public int getGroundTruthValues(DESErrorType v) {
		return _groundTruthValues[v.ordinal()];
	}
	
	public int getGroundTruthDev(DESErrorType v) {
		return _groundTruthDev[v.ordinal()];
	}

	public boolean getTrampXMLOutputOption (TrampXMLOutputSwitch s) {
		return _trampXMLoutputOpts[s.ordinal()];
	}
	
	/*
	 * public long getScenarioSeeds(int scenario) { return _seeds[scenario]; }
	 */

	public long getSeed() {
		return _seed;
	}

	public Random getRandomGenerator() {
		return randomGenerator;
	}
	
	public void resetRandomGenerator() {
		randomGenerator = new Random(_seed);
	}

	public String getSourceSchemaFile() {
		return sourceSchemaFile;
	}

	public String getTargetSchemaFile() {
		return targetSchemaFile;
	}

	public String getSchemaFile() {
		return schemaFile;
	}

	public String getSourceInstanceFile() {
		return sourceInstanceFile;
	}

	public String getSourceDocumentName() {
		return sourceDocumentName;
	}

	public String getMappingFileName() {
		return mappingFile;
	}

	public int getRepElemCount() {
		return repElemCountValue;
	}

	public int getMaxStringLength() {
		return maxStringLength;
	}

	public int getMaxNumValue() {
		return maxNumValue;
	}

	public void setSchemaPathPrefix(String __schemaPathPrefix) {
		schemaPathPrefix = __schemaPathPrefix;
	}

	public void setInstancePathPrefix(String __instancePathPrefix) {
		instancePathPrefix = __instancePathPrefix;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		result.append("---- PATHS ----\n");
		result.append("schemaPathPrefix: <" + schemaPathPrefix + ">\n");
		result.append("instancePathPrefix: <" + instancePathPrefix + ">\n");

		result.append("\n\n---- SCENARIOS ----\n");
		for (ScenarioName name : Constants.ScenarioName.values()) {
			int val = _repetitions[name.ordinal()];
			result.append(name.toString() + ": <" + val + ">\n");
		}

		result.append("\n\n---- OPTIONS ----\n");
		for (ParameterName p : Constants.ParameterName.values()) {
			int val = _configurations[p.ordinal()][0];
			result.append(p.toString() + ": <" + val + ">\n");
		}

		result.append("\n\n---- OPTIONS DERIVATIONS ----\n");
		for (ParameterName p : Constants.ParameterName.values()) {
			int val = _configurations[p.ordinal()][1];
			result.append(p.toString() + ": <" + val + ">\n");
		}

		result.append("\n\n---- FILES ----\n");
		result.append("sourceSchemaFile: <" + sourceSchemaFile + ">\n");
		result.append("targetSchemaFile: <" + targetSchemaFile + ">\n");
		result.append("sourceInstanceFile: <" + sourceInstanceFile + ">\n");
		result.append("sourceDocumentName: <" + sourceDocumentName + ">\n");
		result.append("mappingFile: <" + mappingFile + ">\n");

		result.append("\n\n---- OTHER ----\n");
		result.append("repElemCountValue : <" + repElemCountValue + ">\n");
		result.append("maxStringLength : <" + maxStringLength + ">\n");
		result.append("maxNumValue : <" + maxNumValue + ">\n");
		result.append("_namingPolicy : <" + namingPolicy + ">\n");
		result.append("_seed: <" + _seed + ">\n");
		result.append("mapType: <" + mapType + ">\n");
		result.append("dataGen: <" + dataGen + ">\n");
		
		result.append("\n\n---- OUTPUT OPTIONS ----\n");
		for (OutputOption p : Constants.OutputOption.values()) {
			boolean val = _outputOpts[p.ordinal()];
			result.append(p.toString() + ": <" + val + ">\n");
		}
		
		result.append("\n\n---- GROUND TRUTH VALUES ----\n");
		for (DESErrorType v : Constants.DESErrorType.values()) {
			int val = _groundTruthValues[v.ordinal()];
			result.append(v.toString() + ": <" + val + ">\n");
		}
		
		result.append("\n\n---- TRAMP XML OUTPUT OPTIONS ----\n");
		for (TrampXMLOutputSwitch p : Constants.TrampXMLOutputSwitch.values()) {
			boolean val = _trampXMLoutputOpts[p.ordinal()];
			result.append(p.toString() + ": <" + val + ">\n");
		}
		
		return result.toString();
	}

	public String getSchemaPathPrefix() {
		return schemaPathPrefix;
	}

	public static String getInstancePathPrefix() {
		return instancePathPrefix;
	}
	
	public static String getAbsoluteInstancePath () {
		return new File(instancePathPrefix).getAbsolutePath();
	}

	public DataGenType getDataGen() {
		return dataGen;
	}

	public void setDataGen(DataGenType dataGen) {
		this.dataGen = dataGen;
	}

	public MappingLanguageType getMapType() {
		return mapType;
	}

	public void setMapType(MappingLanguageType mapType) {
		this.mapType = mapType;
	}
	
	public String getDBOption (DBOption o) {
		return _dbOptions[o.ordinal()];
	}
	
	public int getReuseThreshold () {
		int totalNumScen = getTotalNumScen();
		int thresh =  (int) Math.floor(totalNumScen * 0.01 * 
				_configurations[Constants.ParameterName.
				                NoReuseScenPerc.ordinal()][0]);
		
		return thresh;
	}

	public int getTotalNumScen() {
		return CollectionUtils.sum(_repetitions) + CollectionUtils.sum(numLoadScenarioInsts);
	}

	public void sanityCheck() {
		/* check that variations are valid: 
		 * 		1) parameter value < 0
		 * 				set param = 0
		 * 		2) parameter value - deviation >= 0
		 * 				set deviation= parameter value  
		 */
		for(ParameterName p: Constants.ParameterName.values())
			checkParameterRange(p, 0, 1.0);
		
		/* check specific parameter derivations */
		checkParameterRange(ParameterName.PrimaryKeySize, 0, 0.5);
		checkParameterRange(ParameterName.JoinSize, 1, 0.5);
		checkParameterRange(ParameterName.NumOfAttributesToDelete, 1, 0.5);
		checkParameterRange(ParameterName.NumOfJoinAttributes, 1, 0.5);
		checkParameterRange(ParameterName.NumOfNewAttributes, 1, 0.5);
		checkParameterRange(ParameterName.NumOfParamsInFunctions, 1, 0.5);
		checkParameterRange(ParameterName.PrimaryKeyFDs, 0, 1);
		
		/* check that number of elements including variation is large enough. If number of elements is
		 * not sufficiently large we increase the number of elements 
		 * 2 x primary key size
		 * 2 x NumOfJoinAttributes
		 * 3 x NumOfParamsInFunctions
		 * 2 x NumOfNewAttributes
		 * 3 x NumOfAttributesToDelete
		 * 2 x PrimaryKeySize
		 */
		int minNumElem = _configurations[ParameterName.NumOfSubElements.ordinal()][0];
		
		minNumElem = CollectionUtils.max(minNumElem,
				2 * paramMax(ParameterName.PrimaryKeySize),
				2 * paramMax(ParameterName.NumOfJoinAttributes),
				2 * paramMax(ParameterName.NumOfNewAttributes),
				3 * paramMax(ParameterName.NumOfParamsInFunctions),
				3 * paramMax(ParameterName.NumOfAttributesToDelete));
		// add deviation of NumOfSubElements
		minNumElem += getDeviation(ParameterName.NumOfSubElements);
		setParam(ParameterName.NumOfSubElements, minNumElem);
		
		
	}

	private void checkParameterRange(ParameterName p, int min, int max) {//TODO log warnings?
		int pValue = _configurations[p.ordinal()][0];
		int pDev = _configurations[p.ordinal()][1];
		
		if (pValue < min)
			pValue = min;
		if (pValue > max)
			pValue = max;
		
		if (pValue - pDev < min)
			pDev = (pValue - min);
		if (pValue + pDev < max)
			pDev = (max - pValue);
		
		_configurations[p.ordinal()][0] = pValue;
		_configurations[p.ordinal()][1] = pDev;
	}
	
//	private void checkParameterRange(ParameterName p, int min) {
//		checkParameterRange(p, min, 1.0);
//	}
	
	private void checkParameterRange(ParameterName p, int min, double dev) {
		int pValue = _configurations[p.ordinal()][0];
		int pDev = _configurations[p.ordinal()][1];
		
		if (pValue < min)
			pValue = min;
		if (pDev > pValue * dev)
			pDev = (int) (pValue * dev);
		if (pValue - pDev < min)
			pDev = (pValue - min);
		_configurations[p.ordinal()][0] = pValue;
		_configurations[p.ordinal()][1] = pDev;
	}
	
	public int paramMin(ParameterName p) {
		return _configurations[p.ordinal()][0] - _configurations[p.ordinal()][1]; 
	}
	
	public int paramMax(ParameterName p) {
		return _configurations[p.ordinal()][0] + _configurations[p.ordinal()][1]; 
	}

	public void setSchemaFile(String schemaFile) {
		this.schemaFile = schemaFile;
	}

	public List<File> getExistingScenarios() {
		return loadScenarios;
	}

	public void setExistingScenarios(List<File> existingScenarios) {
		this.loadScenarios = existingScenarios;
	}

	public List<String> getLoadScenarioNames() {
		return loadScenarioNames;
	}

	public int getNumLoadScenarios() {
		return numLoadScenarios;
	}

	public void setNumLoadScenarios(int numLoadScenarios) {
		this.numLoadScenarios = numLoadScenarios;
	}

	public int[] getNumLoadScenarioInsts() {
		return numLoadScenarioInsts;
	}


}
