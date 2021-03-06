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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;

import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Constants.TrampXMLOutputSwitch;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.AND;
import vtools.dataModel.expression.ConstantAtomicValue;
import vtools.dataModel.expression.LE;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Query;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.values.IntegerValue;

//PRG FIXED Omission, must generate source relation with at least 2 elements (this was causing empty Skolem terms and PK FDs with empty RHS!)- Sep 19, 2012
//MN  FIXED tries++ in chooseSourceRels() - 28 April 2014
//MN  ENHANCED genTargetRels to pass types of attributes as argument to addRelation - 5 May 2014
//MN  ENHANCED genSourceRels to pass types of attributes as argument to addRelation - 11 May 2014
//MN  FIXED tries++ in chooseTargetRels() - 11 May 2014
//MN  FIXED chooseTargetRels() - 29 May 2014
//MN  CHANGED type of selector from int to text - 17 August 2014

public class HorizontalPartitionScenarioGenerator extends AbstractScenarioGenerator
{
	private static final int MAX_NUM_TRIES = 10;
	
	private int randomElements;
	private int randomFragments;

	private int fragmentWidth;
	//MN - added attribute to check whether we are reusing target relation -11 May 2014
	private boolean[] targetReuse;
    //MN
	
    public HorizontalPartitionScenarioGenerator()
    {		;		}


    @Override
    protected void initPartialMapping () {
    	super.initPartialMapping();
        randomElements = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
                numOfElementsDeviation);
       
        // PRG ADD - Generate at least a source relation of 2 elements - Sep 19, 2012
        // This enforcement is because HP's chosen selector attribute (always source attribute 0) is never copied into the target relation.
        // Thus generating a source relation of size 1, for example, would mean getting an empty target relation.
        randomElements = (randomElements > 2 ? randomElements : 2);
        
        randomFragments = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements, numOfSetElementsDeviation);
        randomFragments = (randomFragments > 1) ? randomFragments : 2;
        fragmentWidth = 10000 / randomFragments;
        
        //MN BEGIN - 11 May 2014
        targetReuse = new boolean [1000];
        for(int i=0; i<1000; i++)
        	targetReuse[i] = false;
        //MN END
    }

    /**
     * Following requirements for the relation:
     * 1) At least two attributes (one key, one free)
     * 2) if it has a key then it should be the first attr only (could be 
     * 		changed later)
     * 
     * @throws Exception 
     */
    @Override
    protected boolean chooseSourceRels() throws Exception {
    	RelationType r = null;
    	boolean ok = false;
    	int tries = 0;
    	
    	while(!ok && tries < MAX_NUM_TRIES) {
    		r = getRandomRel(true, 2);
    		if (r == null)
    			break;
    		if (r.isSetPrimaryKey()) {	
    			int[] keyPos = model.getPKPos(r.getName(), true);
    			if (keyPos.length == 1 && keyPos[0] == 0) {
    				ok = true;
    				break;
    			}
    		}
    		else {
    			ok = true;
    			break;
    		}
    		//MN added tries ++ - 28 April 2014
			tries++;
			//MN
    	}
    	
    	// did not find suitable relation
    	if (r == null)
    		return false;
    	// adapt fields
    	else {
    		m.addSourceRel(r);
    		// create PK if necessary
    		if (!r.isSetPrimaryKey())
    			fac.addPrimaryKey(r.getName(), 0, true);
    		randomElements = r.sizeOfAttrArray() - 1;
    		return true;
    	}
    }
    

	@Override
	protected void genSourceRels() {	
	    String srcName = randomRelName(0);
	    String[] attrs = new String[randomElements + 1];
	    String[] dTypes = new String[randomElements + 1];
	    String nameSelector = "selectorhp" + curRep;
	    
        // create the source attrs
        attrs[0] = nameSelector;
        //MN -changed type of selector - 17 August 2014
        //dTypes[0] = "INT8";
        dTypes[0] = "TEXT";
        
        // and now populate the src SMarkElement and the target fragments with
        // the rest of the attributes.
        for (int i = 0; i < randomElements; i++) {
            String attName = randomAttrName(0, i); 
            attrs[i + 1] = attName;
            
            //MN BEGIN - 11 May 2014
            boolean reused = false;
            for(int j=0; j<1000; j++)
            	if(targetReuse[j])
            		reused =true;
            if(!reused)
            	dTypes[i + 1] = "TEXT";
            else
            	dTypes[i + 1] = m.getTargetRels().get(0).getAttrArray(i).getDataType();
            //MN END
        }

        fac.addRelation(getRelHook(0), srcName, attrs, dTypes, true);
        
        //MN BEGIN - 11 May 2014
        for(int j=0; j<1000; j++)
        	targetReuse[j] = false;
        //MN END
	}

	/**
	 * We need "randomFragments" number of relations with the same number 
	 * of attributes and the first attribute as key (or no key).
	 * @throws Exception 
	 */
	@Override
	protected boolean chooseTargetRels() throws Exception {
		RelationType cand = null;
		int tries = 0;
		int numAttrs = 0;
		List<RelationType> rels = new ArrayList<RelationType> (randomFragments);
		//MN considered count to know which target relation are the ones that are being reused - 11 May 2014
		int count =0;
		//MN
		
		// first one
		while (tries < MAX_NUM_TRIES && rels.size() == 0) {
			cand = getRandomRel(false, 2);
			if (relOk(cand)) {
				rels.add(cand);
				//MN BEGIN - 11 May 2014
				targetReuse[count] = true;
				count++;
				//MN END
				break;
			}
			//MN BEGIN - 11 May 2014
			tries++;
			//MN END
		}
		
		// didn't find one? generate target relations
		if (rels.size() == 0)
			return false;
		
		numAttrs = cand.sizeOfAttrArray();

		// find additional relations with the same number of attributes 
		// and no key or the first attr as key
		while (tries < MAX_NUM_TRIES * randomFragments && cand != null 
				&& rels.size() < randomFragments) {
			cand = getRandomRelWithNumAttr(false, numAttrs);
			
			//MN BEGIN - 29 May 2014
			boolean ok = true;
			for(int i=0; i<rels.size(); i++)
				if(rels.get(i).getName().equals(cand.getName()))
					ok = false;
			//MN END
			//MN - 29 May 2014
			if (ok && relOk(cand)){
				rels.add(cand);
				//MN BEGIN - 11 May 2014
				targetReuse[count] = true;
				count++;
				//MN END
			}
			//MN BEGIN - 11 May 2014
			tries++;
			//MN END
		}
		
		// create additional target relations
		for (int i = 0; i < rels.size(); i++)
			m.addTargetRel(rels.get(i));
		for (int i = rels.size(); i < randomFragments; i++) {
			RelationType r = createFreeRandomRel(i, numAttrs);
			rels.add(r);
			fac.addRelation(getRelHook(i), r, false);
		}
		
		// create primary keys
		for (RelationType r: rels)
			if (!r.isSetPrimaryKey())
				fac.addPrimaryKey(r.getName(), 0, false);
		
		// adapt local parameters
		randomElements = numAttrs;
		
		return true;
	}
	
	private boolean relOk (RelationType r) throws Exception {
		if (r == null)
			return false;
		if (r.isSetPrimaryKey()) {
			int[] pkPos = model.getPKPos(r.getName(), false);
			if (pkPos.length == 1 & pkPos[0] == 0)
				return true;
		}
		// no PK? we are fine
		else
			return true;
		
		return false;
	}
	
	//MN modified genTargetRels - 5 May 2014
	@Override
	protected void genTargetRels() {
		String srcName = m.getSourceRels().get(0).getName();
        String[] attrs = m.getAttrIds(0, true);
        
        //MN BEGIN considered an array to store types of attributes of target relation - 5 May 2014
        List<String> attrsType = new ArrayList<String> ();
        //MN BEGIN
        for(int i=1; i< attrs.length; i++)
        	attrsType.add(m.getSourceRels().get(0).getAttrArray(i).getDataType());
		//MN END
        
        attrs = Arrays.copyOfRange(attrs, 1, attrs.length);
        
        
        
        for(int i = 0; i < randomFragments; i++) {
            int lowerLimit = i * fragmentWidth;
            int upperLimit = ((i + 1) * fragmentWidth) - 1;
            String hook =  getStamp() + curRep + "FR" + i + "_from_" + lowerLimit + "_to_"
                    + upperLimit;
            String name = srcName + "_" + hook;
            
            //MN modified the following line - 4 May 2014
        	fac.addRelation(hook, name, attrs, attrsType.toArray(new String[] {}), false);
        }
	}

	
	
	@Override
	protected void genMappings() throws Exception {
		String srcName = m.getSourceRels().get(0).getName();
		
		for(int i = 0; i < randomFragments; i++) {
			MappingType m1 = fac.addMapping(getCorrForFrag(i));
			String trgName = m.getTargetRels().get(i).getName();
			
			fac.addForeachAtom(m1.getId(), srcName, fac.getFreshVars(0, randomElements + 1));
			fac.addExistsAtom(m1.getId(), trgName, fac.getFreshVars(1, randomElements));
		}
	}
	
	private CorrespondenceType[] getCorrForFrag (int frag) {
		CorrespondenceType[] result = new CorrespondenceType[randomElements];
		
		if (!configuration.getTrampXMLOutputOption(
				TrampXMLOutputSwitch.Correspondences))
			return null;
		
		for(int i = 0; i < randomElements; i++)
			result[i] = m.getCorrs().get(frag * randomElements + i);
		
		return result;
	}
	
	@Override
	protected void genTransformations() throws Exception {
		Query q;
		
		for(int i = 0; i < randomFragments; i++) {
			String targetName = m.getTargetRels().get(i).getName();
			String map = m.getMapIds()[i];
			
			q = genQuery(i);
			q.storeCode(q.toTrampString(m.getMapIds()[i]));
			q = addQueryOrUnion(targetName, q);

			fac.addTransformation(q.getStoredCode(), new String[] {map}, targetName);
		}
	}
	
	private SPJQuery genQuery(int i) {
		SPJQuery q = new SPJQuery();
		String srcName = m.getSourceRels().get(0).getName();
		
        // create the selector attribute for the Where condition of the query
		String nameSelector = m.getSourceRels().get(0).getAttrArray()[0].getName();
        Variable var = new Variable("X");
        Projection attSelector = new Projection(var.clone(), nameSelector);
		
        int lowerLimit = i * fragmentWidth;
        int upperLimit = ((i + 1) * fragmentWidth) - 1;
        
		// create the From Clause for each subquery
		q.getFrom().add(var.clone(),
				new Projection(Path.ROOT, srcName));
		// create the Where Clause for each subquery
		AND andCond = new AND();
		andCond.add(new LE(new ConstantAtomicValue(new IntegerValue(
				lowerLimit)), attSelector));
		andCond.add(new LE(attSelector, new ConstantAtomicValue(
				new IntegerValue(upperLimit))));
		q.setWhere(andCond);
	
		String[] attrIds = m.getAttrIds(0, true);
		for(int j = 1; j <= randomElements; j++) {
			String elementName = attrIds[j];
			Projection sourceAtt = new Projection(var.clone(), elementName);
			SelectClauseList select = q.getSelect();
			select.add(elementName, sourceAtt.clone());
			q.setSelect(select);
		}
		
        return q;
	}

	@Override
	protected void genCorrespondences() {
		for(int i = 0; i < randomFragments; i++)
			for(int j = 1; j < randomElements + 1; j++)
				addCorr(0, j, i, j - 1);
	}
	
	
	@Override
	public ScenarioName getScenType() {
		return ScenarioName.HORIZPARTITION;
	}
}
