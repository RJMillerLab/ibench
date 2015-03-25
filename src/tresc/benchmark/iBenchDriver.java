package tresc.benchmark;

import java.io.File;
import java.io.IOException;


import tresc.benchmark.configGen.ConfigGenerator;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.varia.LevelRangeFilter;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import org.vagabond.util.LoggerUtil;

import tresc.benchmark.STBenchmark;
import tresc.benchmark.iBenchOptions;


public class iBenchDriver 
{
	static Logger log = Logger.getLogger(iBenchDriver.class);
	
	private static ConfigGenerator _configGen = new ConfigGenerator();
	private static iBenchOptions opt = new iBenchOptions();
	private static String path;
	private static File pathF;
	
	private static int _tallyNumSourceRels = 0;
	private static int _tallyNumSourceAttrs = 0;
	private static int _tallyNumTargetRels = 0;
	private static int _tallyNumTargetAttrs = 0;
	private static int _tallyNumRels = 0;
	private static int _tallyNumAttrs = 0;
	private static int _tallyNumTotalMapps = 0;
	private static double _tallyElapsedTime = 0;
	
	public static void main(String[] args) throws Exception 
	{	
		readConfig(args);

		_configGen.setConfigPath(path + File.separator);
		
		setUpExperimentalLogger(pathF);
		
		/*
		if (opt.mapFile != null)
			runWithFixedXML();
		else 
			if (opt.randomConfig)
				runWithRandomConfigs();
		else 
			if (opt.fixedConfig) {
				runWithFixedConfig();
			}
			else 
				runWithTemplateConfig();
		*/
		
		if (opt.fixedConfig) {
			runWithFixedConfig();
		}
		
	}

	
	private static void runWithFixedConfig() throws Exception {
		
		if (log.isDebugEnabled()) {log.debug("Run with Fixed Config File");};

		String configName = "propConfig";
		// String configPath = path + File.separator + configName;
		String configPath = opt.paramConfig.toString();
		
		setUpConfigLogger(pathF, configName);
		
		if (log.isDebugEnabled()) {log.debug("Set up logger");};
		if (log.isDebugEnabled()) {log.debug("Run config " + configPath);};
		
		
		// run the same config file several times
		for (int j = 0; j < opt.numIterations; j++)
		{
			if (log.isDebugEnabled()) {log.debug("RUN ITERATION " + j);};
			
			
			STBenchmark benchmark = runiBench(0, configPath);
			
			// get the names / paths of the files it created
			String schemaFile = STBenchmark.getConfiguration().getSchemaFile();
			
			// rename the schema xml file to include a counter so that each run of the scenario is different
			// note: 0 is the config index, "j" is the run/iteration index
			renameSchemaFile(0, j, schemaFile);
			
			printIterationStats(j, benchmark);
		}
		
		printExperimentStats();
		
		//UnSkolemize.printResultsPerConfig(configName, opt.numIterations);
		
		//numOfTotalConfigsPerExperiment++;
	}

	private static void printExperimentStats() {
		
		int avgNumSourceRels = _tallyNumSourceRels / opt.numIterations;
		int avgNumSourceAttrs = _tallyNumSourceAttrs / opt.numIterations;		
		int avgNumTargetRels = _tallyNumTargetRels / opt.numIterations;
		int avgNumTargetAttrs = _tallyNumTargetAttrs / opt.numIterations;		
		int avgNumRels = _tallyNumRels / opt.numIterations;
		int avgNumAttrs = _tallyNumAttrs / opt.numIterations;		
		int avgNumTotalMapps = _tallyNumTotalMapps / opt.numIterations;		
		double avgElapsedTime = _tallyElapsedTime / opt.numIterations;
		
		log.info("AVG OVER " + opt.numIterations + ", " + 
		         avgNumSourceRels  + ", " + avgNumSourceAttrs + ", " + 
		         avgNumTargetRels + ", " + avgNumTargetAttrs + ", " + 
		         avgNumRels + ", " + avgNumAttrs + ", " + 
		         avgNumTotalMapps + ", " + avgElapsedTime + "\n");	
	}
	
	private static void printIterationStats(int iteration, STBenchmark bench) {
		
		int numSourceRels = bench.getNumOfSourceRelations();
		int numSourceAttrs = bench.getNumOfSourceAttributes();
		int numTargetRels = bench.getNumOfTargetRelations();
		int numTargetAttrs = bench.getNumOfTargetAttributes();
		int numRels = numSourceRels + numTargetRels;
		int numAttrs = numSourceAttrs + numTargetAttrs;
		int numMapps = bench.getNumOfTotalMappings();
		double elapsedTime = bench.getMappingGenerationTime();
		
		log.info("Run " + iteration + ", " + 
		         numSourceRels + ", " + numSourceAttrs + ", " + 
		         numTargetRels + ", " + numTargetAttrs + ", " + 
		         numRels + ", " + numAttrs + ", " + 
		         numMapps + ", " + elapsedTime + "\n");
		
		_tallyNumSourceRels = _tallyNumSourceRels + numSourceRels;
		_tallyNumSourceAttrs = _tallyNumSourceAttrs + numSourceAttrs;
		
		_tallyNumTargetRels = _tallyNumTargetRels + numTargetRels;
		_tallyNumTargetAttrs = _tallyNumTargetAttrs + numTargetAttrs;
		
		_tallyNumRels = _tallyNumRels + numRels;
		_tallyNumAttrs = _tallyNumAttrs + numAttrs;
		
		_tallyNumTotalMapps = _tallyNumTotalMapps + numMapps;
		
		_tallyElapsedTime = _tallyElapsedTime + elapsedTime;
		
				
	}
	
	private static STBenchmark runiBench(int configIndex, String configPath) throws CmdLineException, Exception {
		
		STBenchmark benchmark = new STBenchmark();
		
		// PRG FIXED to segregate generation of schema mapping files per configIndex - Sep 10, 2012
		// String[] params = new String[] {"-p", configPath, "-schemaPrefix", path, "-instancePrefix", path};
		String outFolder = path + File.separator + "out" + configIndex;  
		String[] params = new String[] {"-p", configPath, "-schemaPrefix", outFolder, "-instancePrefix", outFolder};
		
		benchmark.parseArgs(params);
		benchmark.run(params);
		return benchmark;
		
	}

	private static void renameSchemaFile(int configIndex, int runIndex, String schemaFile) {
		//File file = new File(path + "/" + schemaFile);
		String outFolder = path + File.separator + "out" + configIndex + File.separator; 
		File file = new File(outFolder + schemaFile);
		String delims = "[.]";
		String[] tokens = schemaFile.split(delims);
		File file2 = new File(outFolder + tokens[0] + "_run" + runIndex + ".xml");
		file.renameTo(file2);
	}

	private static void readConfig(String[] args) throws CmdLineException {
		CmdLineParser parser;
		parser = new CmdLineParser(opt);
		
		if(args.length == 0)
		{
			System.out.println("Please specify command line arguments");
			parser.printUsage(System.err);
			System.exit(1);
		}
		
		System.out.println("Command line args are: <" + LoggerUtil.arrayToString(args) + ">");
		try {
			parser.parseArgument(args);
		}
		catch (CmdLineException e) {
			parser.printUsage(System.err);
			throw e;
		}
		
		path = opt.outPath.toString();
		pathF = opt.outPath;
	}

	
	private static void setUpExperimentalLogger(File pathF) throws IOException {
		
		Logger.getRootLogger().removeAllAppenders();
		log.removeAllAppenders();
		
		PropertyConfigurator.configure("resource/log4jproperties.txt");
		
		FileAppender resultAppender = new FileAppender(
				new PatternLayout("%m"),
				new File(pathF, "result.txt").toString(), false);
		
		LevelRangeFilter infoFilter = new LevelRangeFilter();
		infoFilter.setLevelMin(Level.INFO);
		infoFilter.setLevelMax(Level.FATAL);
		infoFilter.setAcceptOnMatch(true);
		resultAppender.addFilter(infoFilter);
		log.addAppender(resultAppender);
		Logger.getLogger(STBenchmark.class).addAppender(resultAppender);
		
	}

	
	private static void setUpConfigLogger(File pathF, String configName) throws IOException {
		Logger.getRootLogger().removeAllAppenders();
		
		String nameSuffix = configName.replace(".txt", "");
		
		PropertyConfigurator.configure("resource/log4jproperties.txt");	
		
		FileAppender logFileAppender = new FileAppender(
				new PatternLayout("%-4r [%t] %-5p %c %x - %m%n"),
				new File(pathF, "log_" + nameSuffix + ".txt").toString(), false);
		Logger.getRootLogger().addAppender(logFileAppender);
		
		if (opt.logToConsole)
			Logger.getRootLogger().addAppender(new ConsoleAppender(
					new PatternLayout("%-4r [%t] %-5p %c %x - %m%n")));
	}

	
	private static void exitWithUsage() {
		System.out.println("Error: Resource folder not specified.");
		System.out.println("Please provide an absolute path to the resource folder of your system.");
		System.out.println("Argument Format: \"-p /path/to/resource/folder/\" \n");
		System.exit(1);
	}
}
