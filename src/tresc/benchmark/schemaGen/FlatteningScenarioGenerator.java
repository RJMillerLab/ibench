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

import smark.support.SMarkElement;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;

public class FlatteningScenarioGenerator extends AbstractScenarioGenerator
{

    private final String _stamp = "FL";

    private int _atElCounter;

    private int _setElCounter;

    public FlatteningScenarioGenerator()
    {
        ;
    }

    /*
     * Creates a Flattening scenario. The flattening scenario consists of a
     * nested schema and of a set of a target schema that has all the attributes
     * that appear in the source schema.
     */
//    public void generateScenario(MappingScenario scenario, Configuration configuration)
//    {
//    	init(configuration, scenario);
//    	
//        for (int i = 0, imax = repetitions; i < imax; i++)
//        {
//            _atElCounter = 0;
//            _setElCounter = 0;
//            createRootElement(source, target, i, numOfElements, numOfElementsDeviation, numOfSetElements,
//                numOfSetElementsDeviation, nesting, nestingDeviation, pquery);
//        }
//    }

    /*
     * Creates one root element that will be the nested element.
     */
    void createRootElement(Schema src, Schema trg, int repetition, int numOfElements, int numOfElementsDeviation,
            int numOfSetElements, int numOfSetElementsDeviation, int nesting, int nestingDeviation,
            SPJQuery pquery)
    {
        String randomName = Modules.nameFactory.getARandomName();
        String nameS = randomName + "_" + _stamp + repetition + "S";
        String nameT = randomName + "_" + _stamp + repetition + "T";

        // Create the source and target root elements
        Element srcRootSubElement = new SMarkElement(nameS, new Set(), null, 0, 0);
        srcRootSubElement.setHook(new String(_stamp+repetition + "S"));
        Element trgRootSubElement = new SMarkElement(nameT, new Set(), null, 0, 0);
        trgRootSubElement.setHook(new String(_stamp+repetition + "T"));
        src.addSubElement(srcRootSubElement);
        trg.addSubElement(trgRootSubElement);

        int setElemCounter = 0;
        int maxNesting = Utils.getRandomNumberAroundSomething(_generator, nesting, nestingDeviation) - 1;

        // create the local query.
        SPJQuery query = new SPJQuery();
        Variable varParent = new Variable("X"+ setElemCounter);
        // add the relation to the from clause
        query.getFrom().add(new Variable("X"+ setElemCounter), 
                            new Projection(Path.ROOT, nameS));
        
        createSubElements(srcRootSubElement, trgRootSubElement, 1, maxNesting, repetition, numOfElements,
            numOfElementsDeviation, numOfSetElements, numOfSetElementsDeviation, nesting, nestingDeviation, query, varParent);
        
        // add the local query to the final query 
        pquery.getSelect().add(nameT, query);
    }

    /*
     * returns next values to be used for the next atomic element and set
     * element counters; the pathvarParent keeps the variable path (the relation)
     * which the atomic element that are created in the source belong to
     */
    void createSubElements(Element sourceParent, Element targetParent, int currentNestingLevel, int maxNesting,
            int repetition, int numOfElements, int numOfElementsDeviation, int numOfSetElements,
            int numOfSetElementsDeviation, int nesting, int nestingDeviation, SPJQuery query, Variable varParent)
    {
        // Decide how many subelements we will have
        int nElts = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
        // You need to have at least 1 subelement
        nElts = (nElts < 1) ? 1 : nElts;
        // Decide how many of these subelements will be set subelements.
        // The default is 0
        int nSetElts = 0;
        // but if we need to go deeper we need to have set subelements
        if ((nesting != 0) && currentNestingLevel <= maxNesting)
        {
            nSetElts = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements,
                numOfSetElementsDeviation);
            // if there are 0 set elements, because we need to go deeper, make
            // it 1
            nSetElts = (nSetElts == 0) ? 1 : nSetElts;
            // if the set elements are more than the available elements, then
            // make them exactly as the available elements
            nSetElts = (nSetElts > nElts) ? nElts : nSetElts;
        }
        // and finally decide the atomic elements. note that it may be 0
        int nAtomicElts = nElts - nSetElts;
        
        // make the atomic elements
        SelectClauseList select = query.getSelect();
        for (int i = 0, imax = nAtomicElts; i < imax; i++)
        {
            String randomName = Modules.nameFactory.getARandomName();
            String name = randomName + "_" + _stamp + repetition + "AE" + _atElCounter++;
            Element es = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            es.setHook(new String(_stamp+repetition + "AE" + _atElCounter++));
            sourceParent.addSubElement(es);
            Element targetAtomicElt = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            targetAtomicElt.setHook(new String(_stamp+repetition + "AE" + _atElCounter++));
            targetParent.addSubElement(targetAtomicElt);
            
            // add attributes to the select of the query
            Projection attr = new Projection(varParent.clone(), name);
            select.add(name, attr); 
        }
        query.setSelect(select);

        // now we generate the set SMarkElements
        for (int i = 0; i < nSetElts; i++)
        {
            // To make things random, every time we create a set we create a new
            // maxNesting value and we call subsequent generations with that new
            // one.
            maxNesting = Utils.getRandomNumberAroundSomething(_generator, nesting, nestingDeviation) - 1;
            String randomName = Modules.nameFactory.getARandomName();
            String name = randomName + "_" + _stamp + repetition + "SE" + _setElCounter++;
            Element newSetEl = new SMarkElement(name, new Set(), null, 0, 0);
            newSetEl.setHook(new String(_stamp+repetition + "SE" + _setElCounter++));
            sourceParent.addSubElement(newSetEl);
            
            // add the relation in the from clause; 
            // also create the new path variable that will be used by the child-relations
            // of the newSetElement created above
            Variable newVarParent = new Variable("X" + _setElCounter);   
            query.getFrom().add(newVarParent, new Projection(varParent.clone(), name));
            
            createSubElements(newSetEl, targetParent, currentNestingLevel + 1, maxNesting, repetition,
                numOfElements, numOfElementsDeviation, numOfSetElements, numOfSetElementsDeviation, nesting,
                nestingDeviation, query, newVarParent);
        }
    }

	

	@Override
	protected void genSourceRels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void genTargetRels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.FLATTENING;
	}

	@Override
	protected void genCorrespondences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void genMappings() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void genTransformations() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
