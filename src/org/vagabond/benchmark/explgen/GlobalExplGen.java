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
package org.vagabond.benchmark.explgen;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.xmlbeans.ExplanationAndErrorXMLLoader;
import org.vagabond.xmlmodel.explanderror.ExplanationAndErrorsDocument;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;

public class GlobalExplGen {

	static Logger log = Logger.getLogger(GlobalExplGen.class);
	
	// private ArrayList<ExplanationGenerator> explGens;

	public GlobalExplGen() {
		// explGens = new ArrayList<ExplanationGenerator> ();
		// // stores references to individual gens
		// CorrespondenceGen cg = new CorrespondenceGen();
		// SourceCopyGen scg = new SourceCopyGen();
		// SourceJoinGen sjg = new SourceJoinGen();
		// SourceSkeletonGen skg = new SourceSkeletonGen();
		// SuperfluousMappingGen smg = new SuperfluousMappingGen();
		// TargetSkeletonGen tsg = new TargetSkeletonGen();
		// explGens.add(cg);
		// explGens.add(scg);
		// explGens.add(sjg);
		// explGens.add(skg);
		// explGens.add(smg);
		// explGens.add(tsg);
	}

	public ExplanationAndErrorsDocument genearteExpls(MappingScenario scenario,
			Configuration configuration) throws Exception {

		// load to DB and creates Expl XML docu
		Connection dbCon = ConnectionManager.getInstance().getConnection(
				scenario.getDoc());
		ExplanationAndErrorsDocument eDoc = ExplanationAndErrorsDocument.Factory
				.newInstance();
		DatabaseScenarioLoader.getInstance().loadScenario(dbCon,
				scenario.getDoc());

		// call each gen
		for (ExplanationGenerator gen : Constants.errorGenerators.values())
			gen.generateExpl(scenario, dbCon, eDoc, configuration);
		// output

		eDoc = ExplanationAndErrorXMLLoader.getInstance().translateToXML(
				scenario.getErr().getAll());

		return eDoc;
	}

}
