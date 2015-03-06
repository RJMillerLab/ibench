package tresc.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.XmlOptions;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.PropertyWrapper;

import tresc.benchmark.configGen.ConfigGenerator;

import smark.support.MappingScenario;
import tresc.benchmark.Constants.OutputOption;
import tresc.benchmark.schemaGen.SourceInclusionDependencyGenerator;
import tresc.benchmark.schemaGen.TargetInclusionDependencyGenerator;
import vtools.dataModel.expression.Expression;
import vtools.dataModel.expression.HTMLPresenter;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.schema.Schema;
import vtools.xml.XSDWriter;
import vtools.xml.XSMLWriter;


// PRG ADD July 5, 2011
// PRG ADD Instance Variable to hold the schema mapping currently being generated
// PRG ADD STBenchmark's to-be generated schema mapping is called "Mapping Scenario"
// PRG Removed redundant output and also replaced code to avoid generating string if it is not going to be output - Oct 5, 2012
// MN  ADD two methods to generate random source and target inclusion dependencies, print results as mapjob, xsml and xsd files - 3 April 2014
// PRG RENAMED CLASS - Before was newVP, Now is VPIsAAuthorityScenarioGenerator - 16 Oct 2014

public class STBenchmark {
	static Logger log = Logger.getLogger(STBenchmark.class);

	private static Configuration _configuration;
	// PRG ADD Instance Variable to hold the schema mapping currently being generated
	private MappingScenario _scenario;

	public STBenchmark() {
		_configuration = new Configuration();
		// PRG ADD Initialization of instance variable
		_scenario = new MappingScenario();
	}
	
	// PRG ADD Instance Method	
	public static Configuration getConfiguration()
	{ 
		return _configuration;
	}
	
	// PRG ADD Instance Method
	public MappingScenario getMappingScenario()
    {
        return _scenario;
    }
	
	public void parseArgs (String[] args) throws CmdLineException {
		CmdLineParser parser;
		parser = new CmdLineParser(_configuration);
		
		if(args.length == 0)
		{
			if (log.isDebugEnabled()) {log.debug("Please specify command line arguments");};
			parser.printUsage(System.err);
			System.exit(1);
		}
		
		if (log.isDebugEnabled()) {log.debug("Command line args are: <" + LoggerUtil.arrayToString(args) + ">");};
		try {
			parser.parseArgument(args);
		}
		catch (CmdLineException e) {
			LoggerUtil.logException(e, log);
			parser.printUsage(System.err);
			throw e;
		}
	}

	public void parseConfigFile(String scenarioListFile) throws Exception {
		if (!new File(scenarioListFile).exists())
			throw new Exception(scenarioListFile + " does not exit");

		try {
			RandomAccessFile raf = new RandomAccessFile(scenarioListFile, "r");

			String line = "";
			while (line != null) {
				line = raf.readLine();

				if (line == null)
					break;
				if (line.startsWith("#"))
					continue;

				String trimmedLine = line.trim();
				if (trimmedLine.length() == 0)
					continue;

				run(trimmedLine);
			}

			raf.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("resource/log4jproperties.txt");

		STBenchmark benchmark = new STBenchmark();
		//MN added lines to make connection with random config file generator - 21 April 2014
		//MN the following lines should be enabled - 26 April 2014
		//ConfigGenerator cg = new ConfigGenerator();
		//cg.setConfigPath("config/");
		//String configFileName = cg.generateConfigFile(false);
		//configFileName = "config/" + configFileName;
		//_configuration.configurationFile = configFileName;
		//MN end of adding some lines - 21 April 2014
		benchmark.parseArgs(args);
		benchmark.run(args);
	}

	//MN prints results as mapjob, xsml and xsd files
	//MN it also injects random source and target regular inclusion dependencies into mappings - 14 April 2014
	private void printResultsMapjobAndXSMLAndXSD(MappingScenario scenario, String S, String T,
			String M, String S1, ArrayList<String> randomSourceInclusionDependencies, ArrayList<String> randomTargetInclusionDependencies) throws Exception {
		if (log.isDebugEnabled()) {log.debug("Printing results in Mapjob, XSML and XSD formats !");};
		
		File xsmlDir = new File("./out0");
		if (!xsmlDir.exists())
			xsmlDir.mkdirs();
		
		if (log.isDebugEnabled()) {log.debug("mapjob, xsml and xsd schema path: " + xsmlDir.toString());};
		
		String mapjob = S.substring(0, S.length()-8);
		//prints as map job file
		try{
		    
		    StringBuffer bufMapjob = new StringBuffer ();
		    bufMapjob.append("<?xml version=\"1.0\" encoding=\"ASCII\"?>\n");
		    bufMapjob.append("<job:Job xmlns:job=\"http://com.ibm.clio.model/job/1.0\">\n");
		    bufMapjob.append("<mapping>"+mapjob+".xsml#/</mapping>\n");
		    bufMapjob.append("</job:Job>");
		    
			BufferedWriter bufWriterMapjob =
					new BufferedWriter(new FileWriter(new File(
							"./out0", mapjob + ".mapjob")));
			bufWriterMapjob.write(bufMapjob.toString());
			bufWriterMapjob.close();
		}
		catch (Exception e) {
			LoggerUtil.logException(e, log);
			throw e;
		}
		
		//prints as XSML file
		XSMLWriter xsmlPrinter = new XSMLWriter();
		
		StringBuffer bufXSML = new StringBuffer();
		//////print schemas
		xsmlPrinter.print(bufXSML, mapjob);
		/////print correspondences
		xsmlPrinter.print(bufXSML, mapjob, scenario);
		////print logical mappings (for first experiment)
		xsmlPrinter.print(bufXSML, scenario, mapjob, randomSourceInclusionDependencies, randomTargetInclusionDependencies);
		try {
			BufferedWriter bufWriterXSML =
					new BufferedWriter(new FileWriter(new File(
							"./out0", mapjob + ".xsml")));
			bufWriterXSML.write(bufXSML.toString());
			bufWriterXSML.close();
			System.out.print(".xsml file done!\n");
		}
		catch (Exception e) {
			LoggerUtil.logException(e, log);
			throw e;
		}
		
		//prints as XSD file
		XSDWriter xsdPrinter = new XSDWriter();
		
		StringBuffer bufSourceXSD = new StringBuffer();
		///print source schema (schema.getLabel())
		xsdPrinter.printSource(bufSourceXSD, scenario, mapjob + "_Src", 0);
		
		StringBuffer bufTargetXSD = new StringBuffer();
		///print target schema (schema.getLabel())
		xsdPrinter.printTarget(bufTargetXSD, scenario, mapjob + "_Trg", 0);
		try {
			BufferedWriter bufWriterXSD =
					new BufferedWriter(new FileWriter(new File(
							"./out0", S)));
			bufWriterXSD.write(bufSourceXSD.toString());
			bufWriterXSD.close();
			System.out.print("source .xsd file done!\n");

			bufWriterXSD =
					new BufferedWriter(new FileWriter(new File(
							"./out0", T.substring(0, T.length()-7) + "Trg.xsd")));
			bufWriterXSD.write(bufTargetXSD.toString());
			bufWriterXSD.close();
			System.out.print("target .xsd file done!\n");
		}
		catch (Exception e) {
			LoggerUtil.logException(e, log);
			throw e;
		}
	}
	
	private void printResults(MappingScenario scenario, String S, String T,
			String M, String S1) throws Exception {
		if (log.isDebugEnabled()) {log.debug("Printing results !");};
		File instDir = new File (Configuration.instancePathPrefix);
		if (!instDir.exists())
			instDir.mkdirs();
		File schemDir = new File(Configuration.schemaPathPrefix);
		if (!schemDir.exists())
			schemDir.mkdirs();
		
		if (log.isDebugEnabled()) {log.debug("instance path: " + instDir.toString());};
		if (log.isDebugEnabled()) {log.debug("schema path: " + schemDir.toString());};
		
		XSDWriter schemaPrinter = new XSDWriter();
//		XMLWriter schemaWriter = new XMLWriter();

		// print scenario on the screen if required
		StringBuffer buf = new StringBuffer();
		// PRG - Replaced to avoid generating string if it is not going to be output - Oct 5, 2012
		// scenario.prettyPrint(buf, 0);
		// if (log.isDebugEnabled()) {log.debug(buf);};
		if (log.isDebugEnabled()) {log.debug(scenario.toString());};

		// print scenario on file
		if (_configuration.getOutputOption(OutputOption.XMLSchemas)) {
			StringBuffer sourceSchemaBuffer = new StringBuffer();
			schemaPrinter.print(sourceSchemaBuffer, scenario.getSource(), 0);
			
			StringBuffer targetSchemaBuffer = new StringBuffer();
			schemaPrinter.print(targetSchemaBuffer, scenario.getTarget(), 0);
			
			try {
				BufferedWriter bufWriter =
						new BufferedWriter(new FileWriter(new File(
								Configuration.schemaPathPrefix, S)));
				bufWriter.write(sourceSchemaBuffer.toString());
				bufWriter.close();

				bufWriter =
						new BufferedWriter(new FileWriter(new File(
								Configuration.schemaPathPrefix, T)));
				bufWriter.write(targetSchemaBuffer.toString());
				bufWriter.close();
			}
			catch (Exception e) {
				LoggerUtil.logException(e, log);
				throw e;
			}
		}
		
		if (_configuration.getOutputOption(OutputOption.TrampXML)) {
//			StringBuffer mappingScenarioXMLBuffer = new StringBuffer();
			
//			schemaWriter.print(mappingScenarioXMLBuffer, scenario, 0,
//					instDir.getAbsolutePath(), _configuration);

			try {
				File trampFile = new File(schemDir, S1);
//				if (trampFile.exists()) {
//					trampFile.delete();
//					trampFile.createNewFile();
//				}
				BufferedWriter bufWriter =
						new BufferedWriter(new FileWriter(trampFile));
				XmlOptions options = new XmlOptions();
				options.setSavePrettyPrint();
				scenario.getDoc().getDocument().save(bufWriter, options);
//				bufWriter.write(mappingScenarioXMLBuffer.toString());
				bufWriter.close();
			}
			catch (IOException e) {
				LoggerUtil.logException(e, log);
				throw e;
			}
		}

		if (_configuration.getOutputOption(OutputOption.HTMLSchemas)) {
			// print the src schema in an HTML file
			buf = new StringBuffer();
			Object[] o = new Object[10];
			o[0] = buf;
			o[1] = new Integer(0);
			Schema s = scenario.getSource();
			s.accept(vtools.dataModel.schema.HTMLPresenter.HTMLPresenter, o);
			// vtools.dataModel.schema.HTMLPresenter.HTMLPresenter.printInHtmlFile(buf,
			// "web/" + S + ".html");
			vtools.dataModel.schema.HTMLPresenter.HTMLPresenter
					.printInHtmlFile(buf, Configuration.schemaPathPrefix, S
							+ ".html");

			// print the trg schema in an HTML file
			buf = new StringBuffer();
			o = new Object[10];
			o[0] = buf;
			o[1] = new Integer(0);
			s = scenario.getTarget();
			s.accept(vtools.dataModel.schema.HTMLPresenter.HTMLPresenter, o);
			// vtools.dataModel.schema.HTMLPresenter.HTMLPresenter.printInHtmlFile(buf,
			// "web/" + T + ".html");
			vtools.dataModel.schema.HTMLPresenter.HTMLPresenter
					.printInHtmlFile(buf, Configuration.schemaPathPrefix, T
							+ ".html");
		}

		// and finally the transformation
		if (_configuration.getOutputOption(OutputOption.HTMLMapping)) {
			buf = new StringBuffer();
			Object[] o = new Object[10];
			o[0] = buf;
			o[1] = new Integer(0);
			SPJQuery spjq = scenario.getTransformation();
			SelectClauseList sel = spjq.getSelect();
			for (int i = 0, imax = sel.size(); i < imax; i++) {
				String name = sel.getTermName(i);
				Expression exp = sel.getTerm(i);
				buf.append("<p/><hr>insert into <b>" + name + "</b><br/>");
				exp.accept(vtools.dataModel.expression.HTMLPresenter.HTMLPresenter,
						o);
			}

			HTMLPresenter.HTMLPresenter.printInHtmlFile(buf,
					Configuration.schemaPathPrefix, M + ".html");
		}
	}

	public void run(String[] args) throws Exception {
		
		if (_configuration.configurationFile != null) {
			//parseConfigFile(_configuration.configurationFile);
			//MN changed the code - 21 April 2014
//			System.out.print(_configuration.configurationFile);
			PropertyWrapper props = new PropertyWrapper(_configuration.configurationFile);
			if (log.isDebugEnabled()) {log.debug(props.toString());};
			_configuration.readFromProperties(props);
			parseArgs(args);
			if (log.isDebugEnabled()) {log.debug(_configuration.toString());};
			runConfig();
		}
		else if (_configuration.propertyFileName != null) {
//			System.out.print(_configuration.propertyFileName);
			PropertyWrapper props = new PropertyWrapper(_configuration.propertyFileName);
			if (log.isDebugEnabled()) {log.debug(props.toString());};
			_configuration.readFromProperties(props);
			parseArgs(args);
			if (log.isDebugEnabled()) {log.debug(_configuration.toString());};
			runConfig();
		}
		else {
			runConfig();
		}
	}
	
	public void runConfig (Configuration conf) throws Exception {
		STBenchmark._configuration = conf;
		_configuration.sanityCheck();
		runConfig();
	}
	
	public void runConfig() throws Exception {
		
		Modules.scenarioGenerator = new Generator(_configuration);
		// PRG MODIFY next lines to use instance variable instead of local variable
		// PRG REPLACED "scenario" with "_scenario" in runConfig() source code
		// MappingScenario scenario =
		_scenario = 
				Modules.scenarioGenerator.generateScenario(_configuration);
		
		//MN returns random source and target inclusion dependencies - 14 April 2014
		ArrayList<String> randomSourceInclusionDependencies = Modules.scenarioGenerator.getRandomSourceInlcusionDependencies();
		ArrayList<String> randomTargetInclusionDependencies = Modules.scenarioGenerator.getRandomTargetInclusionDependencies();
		
		// log.debug("---- GENERATED SCENARIO -----\n\n\n" + _scenario.toString());

		// At last, STBenchmark must output and/or write to disk the generated output in TrampXML format
		printResults(_scenario, 
				     _configuration.getSourceSchemaFile(), 
				     _configuration.getTargetSchemaFile(),
				     _configuration.getMappingFileName(),
				     _configuration.getSchemaFile());
		
		if (_configuration.getOutputOption(OutputOption.Clio)) {
		//MN prints results in Mapjob, XSML and XSD formats for the purpose of evaluating MapMerge
		printResultsMapjobAndXSMLAndXSD(_scenario, 
			     _configuration.getSourceSchemaFile(), 
			     _configuration.getTargetSchemaFile(),
			     _configuration.getMappingFileName(),
			     _configuration.getSchemaFile(), randomSourceInclusionDependencies, randomTargetInclusionDependencies);
		}
		
		
		if (_configuration.getOutputOption(OutputOption.Data))
			Modules.scenarioGenerator.generateSourceData(_scenario);
		if (_configuration.getOutputOption(OutputOption.ErrorsAndExplanations))
			Modules.explGen.genearteExpls(_scenario, _configuration);
	}

	public void run(String configLine) throws Exception {
		_configuration = new Configuration(configLine);
		if (log.isDebugEnabled()) {log.debug("Configuration is: " + _configuration.toString());};
		runConfig();
	}

	// public MappingScenario generateSingleMappingScenario(String[] args) throws Exception {
	
	//	PropertyConfigurator.configure("resource/log4jproperties.txt");

	//	STBenchmark benchmark = new STBenchmark();
	//	benchmark.parseArgs(args);
	//	benchmark.run(args);
		
	//}
}
