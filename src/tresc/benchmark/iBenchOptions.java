package tresc.benchmark;

import java.io.File;

import org.kohsuke.args4j.Option;


public class iBenchOptions {
	
	//@Option(name="-r", usage="set to true to use generate random configurations")
	//public boolean randomConfig = false;
	
	@Option(name="-f", usage="set to true to use a fixed configuration file")
	public boolean fixedConfig = false;
	
	@Option(name="-i", usage="the number of times iBench is run with each config file")
	public int numIterations = 1;
	
	//@Option(name="-nConfig", usage="the number of randomly generated configuration files")
	//public int numRandomConfigs = 1;
	
	@Option(name="-p", usage="all configurations, logs, and results will be output into this folder")
	public File outPath = new File("test" + File.separator);
	
	@Option(name="-c", usage="A configuration file template with substitution parameters")
	public File paramConfig;
		
	@Option(name="-log", usage="log to console too")
	public boolean logToConsole = false;
	
	@Option(name="-noout", usage="do not write the generated XML mapping scenarios to disk")
	public boolean noOutput = false;
	
	//@Option(name="-map", usage="run with a fixed mapping XML file instead of using STBenchmark")
	//public File mapFile = null;
}
