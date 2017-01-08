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
package tresc.benchmark.schemaGen;

import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;

import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.ConstantAtomicValue;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.values.StringValue;

public class ValueGenerationScenarioGenerator extends AbstractScenarioGenerator {

	public ValueGenerationScenarioGenerator() {
		;
	}

	@Override
	protected boolean chooseSourceRels() {
		return true;
	}
	
	@Override
	protected void genSourceRels() {
	}

	
	
	@Override
	protected boolean chooseTargetRels() throws Exception {
		return super.chooseTargetRels();
	}
	
	@Override
	protected void genTargetRels() {
		String relName = randomRelName(0);
		String[] attrs;
		String hook = getRelHook(0);
		
		int ranNumOfEl =Utils.getRandomNumberAroundSomething(_generator, 
				numOfElements, numOfElementsDeviation);
		attrs = new String[ranNumOfEl];
		
		for (int i = 0, imax = ranNumOfEl; i < imax; i++)
			attrs[i] = randomAttrName(0, i);	
		
		fac.addRelation(hook, relName, attrs, false);
	}

	@Override
	protected void genMappings() throws Exception {
		MappingType m1 = fac.addMapping(m.getCorrs());
		
		RelationType tRel = m.getTargetRels().get(0);
		fac.addExistsAtom(m1.getId(), tRel.getName(), 
				fac.getFreshVars(0, tRel.sizeOfAttrArray()));
	}

	@Override
	protected void genTransformations() throws Exception {
		String[] attrs = m.getAttrIds(0, false);
		String nameT = m.getTargetRels().get(0).getName();
		
		// gen query
		SPJQuery generatedQuery = new SPJQuery();
		SPJQuery query = new SPJQuery();
		SelectClauseList select = query.getSelect();

		for (String attName: attrs) {
			String randomName = Modules.nameFactory.getARandomName();
			// add the constants value to the select clause
			select.add(attName, new ConstantAtomicValue(
					new StringValue(randomName)));
		}

		// add everything to the final query
		query.setSelect(select);
//		SelectClauseList pselect = pquery.getSelect();
		SelectClauseList gselect = generatedQuery.getSelect();
//		pselect.add(nameT, query);
		gselect.add(nameT, query);
//		pquery.setSelect(pselect);
		generatedQuery.setSelect(gselect);
		
		// gen trans
		fac.addTransformation(query.toTrampString(m.getMapIds()), 
				m.getMapIds(), nameT);
	}

	@Override
	protected void genCorrespondences() {

	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.VALUEGEN;
	}
}
