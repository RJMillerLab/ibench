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
package tresc.benchmark.dataGen;

import java.io.File;

import org.apache.log4j.Logger;

import tresc.benchmark.Configuration;
import tresc.benchmark.dataGen.toxgenewrap.ToXGeneWrapper;
import vtools.dataModel.schema.Schema;

public class ToXDataGenerator extends ToXScriptOnlyDataGenerator {
	
	static Logger log = Logger.getLogger(ToXDataGenerator.class);

	// static final int STRING_LENGTH=25;
	// static final int INT_DOMAIN=10000;

	ToXGeneWrapper toxGen;

	public ToXDataGenerator(Configuration config) {
		super(config);
	}

	public ToXDataGenerator(Schema schema, Configuration config) {
		super(schema, config);
	}

	@Override
	protected void initFromConfig() {
		super.initFromConfig();
		toxGen = new ToXGeneWrapper("./lib");
	}

	@Override
	public void generateData() throws Exception {
		super.generateData();
		generateInstanceXML();
	}

	protected void generateInstanceXML() throws Exception {
		instanceXMLFile =
				toxGen.generate(new File(outputPath, template), outputPath, config.getSeed());
		if (log.isDebugEnabled()) {log.debug("created XML file " + instanceXMLFile + " in folder " + outputPath);};
	}

	public StringBuffer getDataBuffer() {
		return templateBuffer;
	}

}
