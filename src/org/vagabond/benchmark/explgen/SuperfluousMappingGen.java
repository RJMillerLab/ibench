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
import java.util.Collections;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.SuperfluousMappingExplanationGenerator;
import org.vagabond.explanation.marker.MarkerSetFlattenedView;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.explanderror.ExplanationAndErrorsDocument;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.DESErrorType;

public class SuperfluousMappingGen extends AbstractExplanationGenerator {

	static Logger log = LogProviderHolder.getInstance().getLogger(
			SuperfluousMappingExplanationGenerator.class);

	private SuperfluousMappingExplanationGenerator gen = new SuperfluousMappingExplanationGenerator();

	@Override
	public void generateExpl(MappingScenario scenario, Connection dbCon,
			ExplanationAndErrorsDocument eDoc, Configuration conf)
			throws Exception {
		super.generateExpl(scenario, dbCon, eDoc, conf);

		Vector<String> mappings = scenario.getDoc().getMapIds();
//		SuperflousMappingError err;

		Collections.shuffle(mappings);
		mappings.setSize(numExpl);
		
		for (String mapping : mappings) {

			MappingType mapType = scenario.getDoc().getMapping(mapping);
//			Set<String> mappingSet = CollectionUtils.makeSet(mapping);
//			TransformationType[] transForMap = scenario.getDoc().getTransForMap(
//					mapType);
			
//			for (TransformationType t : transForMap) {
//				String targetName = t.getCreates();
//				String cmd = "SELECT tid FROM (SELECT MAPPROV * FROM "
//						+ targetName + ") x " + "WHERE trans_prov LIKE '%"
//						+ mapping + "%' limit 1";
//				try {
//					ResultSet rs = ConnectionManager.getInstance().execQuery(
//							cmd);
//					String tid = null;
//					if (rs.next()) {
//						tid = rs.getString("tid");
//					}
//					if (tid != null) {
//						ISingleMarker errorMarker = MarkerFactory
//								.newAttrMarker(targetName, tid, 0);
//						err = new SuperflousMappingError(
//								(IAttributeValueMarker) errorMarker);
//						gen.setExpl(err);
//						gen.getExpl().addMapSE(mapType);
//						gen.getExpl().setTransSE(
//								CollectionUtils.makeList(transForMap));
//						break;
//					}
//				} catch (Exception e) {
//					LoggerUtil.logException(e, log);
//				}
//			}
//			for (TransformationType t: transForMap)
//				gen.computeSideEffects(t.getCreates(), mappingSet);
//			
//			e.addExplanation(gen.getExpl());
		}
	}

	@Override
	protected DESErrorType getType() {
		return DESErrorType.SuperfluousMapping;
	}

}
