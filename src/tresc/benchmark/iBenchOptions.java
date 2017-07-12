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
	
	//@Option(name="-map", usage="run with a fixed mapping XML file instead of using iBench")
	//public File mapFile = null;
}
