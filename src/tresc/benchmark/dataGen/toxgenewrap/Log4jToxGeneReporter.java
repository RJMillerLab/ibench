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
/**
 * 
 */
package tresc.benchmark.dataGen.toxgenewrap;

import java.util.Vector;

import org.apache.log4j.Logger;

import toxgene.interfaces.ToXgeneReporter;

/**
 * @author lord_pretzel
 *
 */
public class Log4jToxGeneReporter implements ToXgeneReporter {
	
	private Logger log;
	private Vector<String> warnings;
	
	public Log4jToxGeneReporter (Logger log) {
		this.log = log;
		warnings = new Vector<String>();
	}
	
	
	/* (non-Javadoc)
	 * @see toxgene.interfaces.ToXgeneReporter#explain(java.lang.String)
	 */
	@Override
	public void explain(String arg0) {
		log.debug(arg0);
	}

	/* (non-Javadoc)
	 * @see toxgene.interfaces.ToXgeneReporter#progress(java.lang.String)
	 */
	@Override
	public void progress(String arg0) {
		log.info(arg0);
	}

	/* (non-Javadoc)
	 * @see toxgene.interfaces.ToXgeneReporter#warning(java.lang.String)
	 */
	@Override
	public void warning(String arg0) {
		log.warn(arg0);
		warnings.add(arg0);
	}

	public int warnings(){
		return warnings.size();
	}

	public void printAllWarnings(){
		for (int i=0; i<warnings.size(); i++){
			log.warn(warnings.get(i));
		}
		warnings.clear();
	}
}
