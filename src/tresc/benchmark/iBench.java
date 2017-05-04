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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.XmlOptions;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionHandlerRegistry;
import org.vagabond.benchmark.model.TrampXMLModel;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.PropertyWrapper;
import org.vagabond.xmlmodel.SchemaType;

import smark.support.MappingScenario;
import tresc.benchmark.Constants.OutputOption;
import vtools.dataModel.expression.Expression;
import vtools.dataModel.expression.HTMLPresenter;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.schema.Schema;
import vtools.xml.XSDWriter;
import vtools.xml.XSMLWriter;


// PRG ADD July 5, 2011
// PRG ADD Instance Variable to hold the schema mapping currently being generated
// PRG ADD iBench's to-be generated schema mapping is called "Mapping Scenario"
// PRG Removed redundant output and also replaced code to avoid generating string if it is not going to be output - Oct 5, 2012
// MN  ADD two methods to generate random source and target inclusion dependencies, print results as mapjob, xsml and xsd files - 3 April 2014
// PRG RENAMED CLASS - Before was newVP, Now is VPIsAAuthorityScenarioGenerator - 16 Oct 2014
// PRG ADD Benchmarking elapsed mapping generation time - 25 FEB 2015
// PRG ADD Computing Metadata Stats - 25 FEB 2015
// PRG ADD Instance Variables and Public Methods to hold/get Metadata Stats - 15 MAR 2015

public class iBench {
	/**
	 * 
	 */
	public static final String LOG4JPROPERIES_DEFAULT_LOCATION = "resource/log4jproperties.txt";

	static Logger log = Logger.getLogger(iBench.class);
	static Logger progressAndResultLog = Logger.getLogger("iBenchProgressAndResult");
	
	
	private static Configuration _configuration;
	// PRG ADD Instance Variable to hold the schema mapping currently being generated
	private MappingScenario _scenario;
	
	// PRG ADD Benchmarking elapsed mapping generation time - 25 FEB 2015
	private double _elapsedTime = 0;
	// PRG ADD Instance Variables to hold Metadata Stats - 15 MAR 2015
	private int numOfSourceRelations = 0;
	private int numOfSourceAttributes = 0;
	private int numOfTargetRelations = 0;
	private int numOfTargetAttributes = 0;
	private int numOfTotalMappings = 0;
	// PRG END ADD Instance Variables to hold Metadata Stats - 15 MAR 2015

	public iBench() {
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
	
	// PRG ADD Benchmarking elapsed mapping generation time - 25 FEB 2015
	public double getMappingGenerationTime()
    {
        return _elapsedTime;
    }	
	
	public int getNumOfSourceRelations() {
		return numOfSourceRelations;
	}
	
	public int getNumOfTargetRelations() {
		return numOfTargetRelations;
	}
	
	public int getNumOfSourceAttributes() {
		return numOfSourceAttributes;
	}
	
	public int getNumOfTargetAttributes() {
		return numOfTargetAttributes;
	}
	
	public int getNumOfTotalMappings() {
		return numOfTotalMappings;
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
		iBench benchmark = new iBench();
		benchmark.defaultLogConfig();
		//MN added lines to make connection with random config file generator - 21 April 2014
		//MN the following lines should be enabled - 26 April 2014
		//ConfigGenerator cg = new ConfigGenerator();
		//cg.setConfigPath("config/");
		//String configFileName = cg.generateConfigFile(false);
		//configFileName = "config/" + configFileName;
		//_configuration.configurationFile = configFileName;
		//MN end of adding some lines - 21 April 2014
		benchmark.parseArgs(args);
		benchmark.reconfigLog();
		benchmark.run(args);
	}

	/**
	 *  read log4j properties from user defined location or default location
	 * @throws FileNotFoundException 
	 */
	public void reconfigLog() throws FileNotFoundException {
		File location = _configuration.getLogConfig();
		Level llevel = _configuration.loglevel;
		Logger.getRootLogger().removeAllAppenders();
		progressAndResultLog.setAdditivity(false);
		
		// user provided log location?
		if (location != null) {
			if (!location.exists())
			{
				System.err.printf("User provided log location does not exist: %s", location);
				System.exit(1);
			}
			PropertyConfigurator.configure(new FileInputStream(location));
			log.info("user has provided log level location: " +  location);
		}
		// user has given a global log level
		else if (llevel != null) {
			ConsoleAppender c = new ConsoleAppender();
			c.setLayout(new PatternLayout("%-4r [%t] %-5p %l - %m%n"));
			c.setThreshold(llevel);
			c.activateOptions();
			Logger.getRootLogger().addAppender(c);			
		
			log.info("user set log level to " + llevel.toString());
		}
		// do we have a log file at the default location
		else if (new File(LOG4JPROPERIES_DEFAULT_LOCATION).exists())
		{
			defaultLogPropertyFileConfig();
			log.info("use default log properties location " + LOG4JPROPERIES_DEFAULT_LOCATION);
		}
		// just set everything to error log level
		else {
			defaultLogConfig();
		}
		
		outputLog(llevel == null ? Level.INFO : llevel);	
	}
	
	public void outputLog(Level l) {
		if (!progressAndResultLog.getAllAppenders().hasMoreElements()) {
			progressAndResultLog.removeAllAppenders();
			ConsoleAppender console = new ConsoleAppender(); 
			String PATTERN = "%m%n";
			console.setLayout(new PatternLayout(PATTERN)); 
			console.setThreshold(l);
			console.activateOptions();
			progressAndResultLog.addAppender(console);
		}
	}
	
	public void defaultLogConfig() {
		// standard appender is console
		ConsoleAppender console = new ConsoleAppender(); 
		String PATTERN = "%d [%p] %l %m%n";
		console.setLayout(new PatternLayout(PATTERN)); 
		console.setThreshold(Level.ERROR);
		console.activateOptions();
		Logger.getRootLogger().addAppender(console);
	}
	
	public void defaultLogPropertyFileConfig() {
		PropertyConfigurator.configure(LOG4JPROPERIES_DEFAULT_LOCATION);
		log.debug("reading from default log configuration file");
	}
	
	//MN prints results as mapjob, xsml and xsd files
	//MN it also injects random source and target regular inclusion dependencies into mappings - 14 April 2014
//	private void printResultsMapjobAndXSMLAndXSD(MappingScenario scenario, String S, String T,
//			String M, String S1, ArrayList<String> randomSourceInclusionDependencies, ArrayList<String> randomTargetInclusionDependencies) throws Exception {
//		if (log.isDebugEnabled()) {log.debug("Printing results in Mapjob, XSML and XSD formats !");};
//		
//		File xsmlDir = new File("./out0");
//		if (!xsmlDir.exists())
//			xsmlDir.mkdirs();
//		
//		if (log.isDebugEnabled()) {log.debug("mapjob, xsml and xsd schema path: " + xsmlDir.toString());};
//		
//		String mapjob = S.substring(0, S.length()-8);
//		//prints as map job file
//		try{
//		    
//		    StringBuffer bufMapjob = new StringBuffer ();
//		    bufMapjob.append("<?xml version=\"1.0\" encoding=\"ASCII\"?>\n");
//		    bufMapjob.append("<job:Job xmlns:job=\"http://com.ibm.clio.model/job/1.0\">\n");
//		    bufMapjob.append("<mapping>"+mapjob+".xsml#/</mapping>\n");
//		    bufMapjob.append("</job:Job>");
//		    
//			BufferedWriter bufWriterMapjob =
//					new BufferedWriter(new FileWriter(new File(
//							"./out0", mapjob + ".mapjob")));
//			bufWriterMapjob.write(bufMapjob.toString());
//			bufWriterMapjob.close();
//		}
//		catch (Exception e) {
//			LoggerUtil.logException(e, log);
//			throw e;
//		}
//		
//		//prints as XSML file
//		XSMLWriter xsmlPrinter = new XSMLWriter();
//		
//		StringBuffer bufXSML = new StringBuffer();
//		//////print schemas
//		xsmlPrinter.print(bufXSML, mapjob);
//		/////print correspondences
//		xsmlPrinter.print(bufXSML, mapjob, scenario);
//		////print logical mappings (for first experiment)
//		xsmlPrinter.print(bufXSML, scenario, mapjob, randomSourceInclusionDependencies, randomTargetInclusionDependencies);
//		try {
//			BufferedWriter bufWriterXSML =
//					new BufferedWriter(new FileWriter(new File(
//							"./out0", mapjob + ".xsml")));
//			bufWriterXSML.write(bufXSML.toString());
//			bufWriterXSML.close();
//			System.out.print(".xsml file done!\n");
//		}
//		catch (Exception e) {
//			LoggerUtil.logException(e, log);
//			throw e;
//		}
//		
//		//prints as XSD file
//		XSDWriter xsdPrinter = new XSDWriter();
//		
//		StringBuffer bufSourceXSD = new StringBuffer();
//		///print source schema (schema.getLabel())
//		xsdPrinter.printSource(bufSourceXSD, scenario, mapjob + "_Src", 0);
//		
//		StringBuffer bufTargetXSD = new StringBuffer();
//		///print target schema (schema.getLabel())
//		xsdPrinter.printTarget(bufTargetXSD, scenario, mapjob + "_Trg", 0);
//		try {
//			BufferedWriter bufWriterXSD =
//					new BufferedWriter(new FileWriter(new File(
//							"./out0", S)));
//			bufWriterXSD.write(bufSourceXSD.toString());
//			bufWriterXSD.close();
//			System.out.print("source .xsd file done!\n");
//
//			bufWriterXSD =
//					new BufferedWriter(new FileWriter(new File(
//							"./out0", T.substring(0, T.length()-7) + "Trg.xsd")));
//			bufWriterXSD.write(bufTargetXSD.toString());
//			bufWriterXSD.close();
//			System.out.print("target .xsd file done!\n");
//		}
//		catch (Exception e) {
//			LoggerUtil.logException(e, log);
//			throw e;
//		}
//	}
	
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
		
//		XSDWriter schemaPrinter = new XSDWriter();
//		XMLWriter schemaWriter = new XMLWriter();

		// print scenario on the screen if required
		StringBuffer buf = new StringBuffer();
		// PRG - Replaced to avoid generating string if it is not going to be output - Oct 5, 2012
		// scenario.prettyPrint(buf, 0);
		// if (log.isDebugEnabled()) {log.debug(buf);};
//		if (log.isDebugEnabled()) {log.debug(scenario.toString());};

		//********************************************************************************
		// PRINT CLIO INPUTS IF REQUESTED
		if (_configuration.getOutputOption(OutputOption.Clio)) {
			if (log.isDebugEnabled()) {log.debug("Printing results in Mapjob, XSML and XSD formats !");};

			//MN returns random source and target inclusion dependencies - 14 April 2014
			ArrayList<String> randomSourceInclusionDependencies = Modules.scenarioGenerator.getRandomSourceInlcusionDependencies();
			ArrayList<String> randomTargetInclusionDependencies = Modules.scenarioGenerator.getRandomTargetInclusionDependencies();

			if (log.isDebugEnabled()) {log.debug("mapjob, xsml and xsd schema path: " + schemDir.toString());};

			String mapjob = S.substring(0, S.length()-8);
			//prints as map job file
			try{

				StringBuffer bufMapjob = new StringBuffer ();
				bufMapjob.append("<?xml version=\"1.0\" encoding=\"ASCII\"?>\n");
				bufMapjob.append("<job:Job xmlns:job=\"http://com.ibm.clio.model/job/1.0\">\n");
				bufMapjob.append("<mapping>"+mapjob+".xsml#/</mapping>\n");
				bufMapjob.append("</job:Job>");

				BufferedWriter bufWriterMapjob =
						new BufferedWriter(new FileWriter(new File(schemDir, mapjob + ".mapjob")));
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
			xsmlPrinter.printAll(bufXSML, scenario, mapjob, randomSourceInclusionDependencies, randomTargetInclusionDependencies);
			try {
				BufferedWriter bufWriterXSML =
						new BufferedWriter(new FileWriter(new File(
								schemDir, mapjob + ".xsml")));
				bufWriterXSML.write(bufXSML.toString());
				bufWriterXSML.close();
				progressAndResultLog.info(".xsml file done!\n");
			}
			catch (Exception e) {
				LoggerUtil.logException(e, log);
				throw e;
			}
		}
		
		//********************************************************************************
		// print XML schemas for source and target if requested
		if (_configuration.getOutputOption(OutputOption.XMLSchemas)) {
			XSDWriter xsdPrinter = new XSDWriter();
			String mapjob = S.substring(0, S.length()-8);
			
			StringBuffer bufSourceXSD = new StringBuffer();
			///print source schema (schema.getLabel())
			xsdPrinter.printSource(bufSourceXSD, scenario, mapjob + "_Src", 0);
			
			StringBuffer bufTargetXSD = new StringBuffer();
			///print target schema (schema.getLabel())
			xsdPrinter.printTarget(bufTargetXSD, scenario, mapjob + "_Trg", 0);
			try {
				BufferedWriter bufWriterXSD =
						new BufferedWriter(new FileWriter(new File(
								schemDir, S)));
				bufWriterXSD.write(bufSourceXSD.toString());
				bufWriterXSD.close();
				progressAndResultLog.info("source .xsd file done!");

				bufWriterXSD =
						new BufferedWriter(new FileWriter(new File(
								schemDir, T.substring(0, T.length()-7) + "Trg.xsd")));
				bufWriterXSD.write(bufTargetXSD.toString());
				bufWriterXSD.close();
				progressAndResultLog.info("target .xsd file done!\n");
			}
			catch (Exception e) {
				LoggerUtil.logException(e, log);
				throw e;
			}
//			StringBuffer sourceSchemaBuffer = new StringBuffer();
//			schemaPrinter.print(sourceSchemaBuffer, scenario.getSource(), 0);
//			
//			StringBuffer targetSchemaBuffer = new StringBuffer();
//			schemaPrinter.print(targetSchemaBuffer, scenario.getTarget(), 0);
//			
//			try {
//				BufferedWriter bufWriter =
//						new BufferedWriter(new FileWriter(new File(
//								Configuration.schemaPathPrefix, S)));
//				bufWriter.write(sourceSchemaBuffer.toString());
//				bufWriter.close();
//
//				bufWriter =
//						new BufferedWriter(new FileWriter(new File(
//								Configuration.schemaPathPrefix, T)));
//				bufWriter.write(targetSchemaBuffer.toString());
//				bufWriter.close();
//			}
//			catch (Exception e) {
//				LoggerUtil.logException(e, log);
//				throw e;
//			}
		}
		
		//********************************************************************************
		// PRINT TRAMP XML FORMAT OUTPUT
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
		
		//********************************************************************************
		// PRINT HTML SCHEMAS
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

		//********************************************************************************
		// PRINT HTML MAPPINGS 
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
		iBench._configuration = conf;
		_configuration.sanityCheck();
		runConfig();
	}
	
	public void runConfig() throws Exception {
		
		// PRG ADD Benchmarking elapsed mapping generation time - 25 FEB 2015
		long startTime = System.currentTimeMillis();
		
		Modules.scenarioGenerator = new Generator(_configuration);
		// PRG MODIFY next lines to use instance variable instead of local variable
		// PRG REPLACED "scenario" with "_scenario" in runConfig() source code
		// MappingScenario scenario =
		_scenario = 
				Modules.scenarioGenerator.generateScenario(_configuration);
		
		// PRG ADD Benchmarking elapsed mapping generation time - 25 FEB 2015
		_elapsedTime = 1. * (System.currentTimeMillis() - startTime) / 1000;
				
		// log.debug("---- GENERATED SCENARIO -----\n\n\n" + _scenario.toString());

		// At last, iBench must output and/or write to disk the generated output in TrampXML format
		printResults(_scenario, 
				     _configuration.getSourceSchemaFile(), 
				     _configuration.getTargetSchemaFile(),
				     _configuration.getMappingFileName(),
				     _configuration.getSchemaFile());
		
//		if (_configuration.getOutputOption(OutputOption.Clio)) {
//		//MN prints results in Mapjob, XSML and XSD formats for the purpose of evaluating MapMerge
//		printResultsMapjobAndXSMLAndXSD(_scenario, 
//			     _configuration.getSourceSchemaFile(), 
//			     _configuration.getTargetSchemaFile(),
//			     _configuration.getMappingFileName(),
//			     _configuration.getSchemaFile(), randomSourceInclusionDependencies, randomTargetInclusionDependencies);
//		}
		
		
		if (_configuration.getOutputOption(OutputOption.Data))
			Modules.scenarioGenerator.generateSourceData(_scenario);
		if (_configuration.getOutputOption(OutputOption.ErrorsAndExplanations))
			Modules.explGen.genearteExpls(_scenario, _configuration);
		
		// PRG ADD Benchmarking elapsed mapping generation time - 25 FEB 2015
		progressAndResultLog.info("\niBench - Mapping Generation Time: " + _elapsedTime + " seconds");
		
		// PRG ADD Computing Metadata Stats - 25 FEB 2015
		computeMetadataStats();
		
	}

	public void run(String configLine) throws Exception {
		_configuration = new Configuration(configLine);
		if (log.isDebugEnabled()) {log.debug("Configuration is: " + _configuration.toString());};
		runConfig();
	}

	// public MappingScenario generateSingleMappingScenario(String[] args) throws Exception {
	
	//	PropertyConfigurator.configure("resource/log4jproperties.txt");

	//	benchmark.parseArgs(args);
	//	benchmark.run(args);
		
	//}
	
	// PRG ADD Computing Metadata Stats - 25 FEB 2015
	private void computeMetadataStats() throws Exception {
		
		long startTime = System.currentTimeMillis();
		
		TrampXMLModel txModel = _scenario.getDoc();
		
		SchemaType source = txModel.getSchema(true);
		SchemaType target = txModel.getSchema(false);
		
		numOfSourceRelations = source.sizeOfRelationArray();
		
		for (int relIndex = 0; relIndex < numOfSourceRelations; relIndex++) {
			numOfSourceAttributes = numOfSourceAttributes + source.getRelationArray(relIndex).sizeOfAttrArray();
		}
		
		numOfTargetRelations = target.sizeOfRelationArray();
		
		for (int relIndex = 0; relIndex < numOfTargetRelations; relIndex++) {
			numOfTargetAttributes = numOfTargetAttributes + target.getRelationArray(relIndex).sizeOfAttrArray();
		}
		
		numOfTotalMappings = txModel.getMappings().length;
		
		double elapsedTime = 1. * (System.currentTimeMillis() - startTime) / 1000;
		
		progressAndResultLog.info("\niBench - Stats Computation Time: " + elapsedTime + " seconds");
		
		progressAndResultLog.info("\nSource Schema Stats: " + numOfSourceRelations + " (relations) " + numOfSourceAttributes + " (attributes)");
		
		progressAndResultLog.info("\nTarget Schema Stats: " + numOfTargetRelations + " (relations) " + numOfTargetAttributes + " (attributes)");
		
		progressAndResultLog.info("\nTotal Schema Stats: " + (numOfSourceRelations+numOfTargetRelations) + " (relations) " 
		                    + (numOfSourceAttributes+numOfTargetAttributes) + " (attributes)");
		
		progressAndResultLog.info("\nTotal Mappings Stats: " + numOfTotalMappings + " (mappings)"); 		
		
	}
}
