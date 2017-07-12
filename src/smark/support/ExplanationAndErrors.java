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
package smark.support;

import java.util.HashMap;
import java.util.Map;

import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;

import tresc.benchmark.Constants.DESErrorType;

public class ExplanationAndErrors {

	private Map<DESErrorType, IExplanationSet> expls;
	private IExplanationSet allExpls;
	
	public ExplanationAndErrors () {
		expls = new HashMap<DESErrorType, IExplanationSet> ();
		allExpls = ExplanationFactory.newExplanationSet();
	}
	
	public void addSet (DESErrorType type, IExplanationSet e) {
		expls.put(type, e);
		allExpls.addAll(e);
	}
	
	public IExplanationSet getAll () {
		return allExpls;
	}
	
	public IExplanationSet getSetForErrorType (DESErrorType type) {
		return expls.get(type);
	}
	
}
