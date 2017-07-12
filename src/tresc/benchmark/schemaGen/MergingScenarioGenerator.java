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

import org.apache.log4j.Logger;
import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;

import tresc.benchmark.Constants.JoinKind;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.AND;
import vtools.dataModel.expression.EQ;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Query;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;

// PRG FIXED Chain Join Problems - August 30, 2012
// PRG FIXED Foreach Source Expression by correcting fromIndex - Sep 4, 2012 
// MN  FIXED NumOfAttributes in chooseSourceRels - 3 May 2014
// MN  FIXED joinAttrs in chooseSourceRels  - 3 May 2014
// MN  FIXED chooseTargetRels to correctly set numOfUseAttrs which is needed to generate source rels - 9 May 2014
// MN  ENHANCED genTargetRels to pass types of attributes of target relation as argument to addRelation - 11 May 2014
// MN  MODIFIED criteria for choosing target relation in chooseTargetRels - 11 May 2014
// MN  ENHANCED genSourcRels to pass types of attributes of source relations as argument to addRelation - 11 May 2014
// MN  FIXED primaryKeys in chooseSourceRels - 11 May 2014
// MN  FIXED numOfUseAttrs in chooseSourceRels - 12 May 2014
// MN  FIXED chooseSourceRels - 17 May 2014
// MN  MODIFIED chooseTargetRels - 28 May 2014

public class MergingScenarioGenerator extends AbstractScenarioGenerator {
	
	// PRG ADD August 30, 2012
	static Logger log = Logger.getLogger(MergingScenarioGenerator.class);
	
	public static final int MAX_NUM_TRIES = 10;
	
	protected int numOfTables;
	protected int numOfJoinAttributes;
	protected JoinKind jk;
	protected int[] numOfAttributes;
	protected int[] numOfUseAttrs;
	protected String[] joinAttrs;
	
	//MN ADDED attribute to check whether we are reusing a target relation - 11 May 2014
	protected boolean targetReuse;
	//MN
    
    public MergingScenarioGenerator()
    {		;		}
    
    @Override
    protected void initPartialMapping() {
    	super.initPartialMapping();
    	
        numOfTables = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements,
            numOfSetElementsDeviation);
        
        numOfTables = (numOfTables > 1) ? numOfTables : 2;
        
        numOfJoinAttributes = Utils.getRandomNumberAroundSomething(_generator, keyWidth,
            keyWidthDeviation);
        jk = JoinKind.values()[joinKind];
        if (jk == JoinKind.VARIABLE)
        {
            int tmp = Utils.getRandomNumberAroundSomething(_generator, 0, 1);
            if (tmp < 0)
                jk = JoinKind.STAR;
            else jk = JoinKind.CHAIN;
        }
        numOfAttributes = new int[numOfTables];
        numOfUseAttrs = new int[numOfTables];
        for (int k = 0, kmax = numOfAttributes.length; k < kmax; k++)
        {
            int tmpInt = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
                numOfElementsDeviation);
            // make sure that we have enough attribute for the join + at least on free one
            tmpInt = (tmpInt < getNumJoinAttrs(k) + 1) ? getNumJoinAttrs(k) + 1 : tmpInt; 
            numOfAttributes[k] = tmpInt;
            numOfUseAttrs[k] = tmpInt - getNumJoinAttrs(k);
        }
        
        //MN BEGIN - 11 May 2014
        targetReuse = false;
        //MN END
    }
	
    
    /**
     * Find source rels that have enough attributes and either have no key or
     * have a key on the last numOfJoinAttributes attrs (except for the first
     * one in a STAR join)
     * @throws Exception 
     */
    @Override
	protected boolean chooseSourceRels() throws Exception {
		List<RelationType> rels = new ArrayList<RelationType> ();
		int numTries = 0;
		int created = 0;
		boolean found = false;
		RelationType rel;
		String[][] attrs = new String[numOfTables][];
		
		
		// first choose one that has no key or key at the right place
		while(created < numOfTables) {
			found = true;
			//MN I'm not sure whether the following is correct (Am I right?)- 3 May 2014
			rel = getRandomRel(true, getNumJoinAttrs(rels.size()) + 1);
			if (rel != null) {
				//MN BEGIN - 3 May 2014
				for(int g=0; g<rels.size(); g++)
					if(rels.get(g).getName().equals(rel.getName()))
						found = false;
				//MN END
				// if PK, then has to be num of join attributes
				if (rel.isSetPrimaryKey()) {
					int[] pkPos = model.getPKPos(rel.getName(), true);
					if (pkPos.length != numOfJoinAttributes)
						found = false;
					//MN BEGIN tested - 17 May 2014
					if(found){
						if(created ==0 )
							for(int i = 0; i < numOfJoinAttributes; i++) {
								if (pkPos[i] != rel.sizeOfAttrArray() - numOfJoinAttributes + i)
									found = false;
							}
						if(created>0 && created<numOfTables-1)
							for(int i = 0; i < numOfJoinAttributes; i++) {
								if (pkPos[i] != rel.sizeOfAttrArray() - numOfJoinAttributes + i - numOfJoinAttributes)
									found = false;
							}
					}
					//MN END
				}
			}
			else
				found = false;
			
			// found a fitting relation
			if (found) {
				rels.add(rel);
				m.addSourceRel(rel);
				attrs[created] = new String[rel.sizeOfAttrArray()];
				for(int i = 0; i < rel.sizeOfAttrArray(); i++)
					attrs[created][i] = rel.getAttrArray(i).getName();
				numOfAttributes[created] = rel.sizeOfAttrArray();
				//MN BEGIN - 3 May 2014 - 12 May 2014
				numOfUseAttrs[created] = rel.sizeOfAttrArray() - getNumJoinAttrs(created);
				//MN END
				//MN BEGIN - 11 May 2014
				//if (!rel.isSetPrimaryKey()) {
					////int[] templateKey = new int[getNumJoinAttrs(created)];
					////for(int i = 0; i < templateKey.length; i++)
						////templateKey[i] = rel.sizeOfAttrArray() - numOfJoinAttributes;
					////fac.addPrimaryKey(rel.getName(), templateKey, true);
					//MN BEGIN - 11 May 2014
					//createPKsRel(attrs, created, rel);
					//MN END
				//}
				//MN END
				created++;
				numTries = 0;
			}
			// not found, have exhausted number of tries? then create new one
			else {
				numTries++;
				if (numTries >= MAX_NUM_TRIES)
				{
					numTries = 0;
					attrs[created] = new String[numOfAttributes[created]];
					int numOfNonJoinAttr = numOfAttributes[created] - getNumJoinAttrs(created);
					for(int j = 0; j < numOfNonJoinAttr; j++)
						attrs[created][j] = randomAttrName(created, j);
					// create join and join ref attributes
					if (jk == JoinKind.STAR) {
						//MN BEGIN - 3 May 2014
						joinAttrs = new String[numOfJoinAttributes];
						// create join attr names
						for(int i = 0; i < numOfJoinAttributes; i++)
							joinAttrs[i] = randomAttrName(0, i);
						//MN END
						// first relation (center of the star) add fk attributes
						if (created == 0) {
							int offset = numOfAttributes[0] - ((numOfTables - 1) * numOfJoinAttributes);
							for(int i = 1; i < numOfTables; i++) {
								for(int j = 0; j < numOfJoinAttributes; j++)
									attrs[0][offset + j] = getJoinRef(i, j);
								offset += numOfJoinAttributes;
							}
						}
						// other relation add join attributes
						else {
							for(int i=1; i<numOfTables; i++){
								int offset = numOfAttributes[created] - (numOfJoinAttributes);
								for(int j = 0; j < numOfJoinAttributes; j++)
									attrs[created][offset + j] = getJoinAttr(created, j);
							}
						}
					}
					// same for chain joins
					if (jk == JoinKind.CHAIN) {
						//MN BEGIN - 3 May 2014
						joinAttrs = new String[numOfJoinAttributes];
						// create join attr names
						for(int i = 0; i < numOfJoinAttributes; i++)
							joinAttrs[i] = randomAttrName(0, i);
						//MN END
						// if not last table, create join attributes
						if (created != numOfTables -1) {
							int fac = created == 0 ? 1 : 2;
							int offset = numOfAttributes[created] - (numOfJoinAttributes * fac);
							for(int j = 0; j < numOfJoinAttributes; j++)
								attrs[created][offset + j] = getJoinAttr(created, j);
						}
						// create fk attributes if not first table
						if (created != 0) {
							int offset = numOfAttributes[created] - numOfJoinAttributes;
							for(int j = 0; j < numOfJoinAttributes; j++)
								attrs[created][offset + j] = getJoinRef(created, j);
						}
						
					}
					
					//createPK
					//MN BEGIN - 11 May 2014
					//createPKsRel(attrs, created, rel);
					//MN END
					
					// create the relation
					rels.add(fac.addRelation(getRelHook(created), randomRelName(created), attrs[created], true));
					
					created++;
					numTries = 0;
				}
			}
		}
		
		createConstraints(attrs);
		
		return true;
	}


	// The way the algorithm works is by generating the following situation
    // where the left column describes the case of a star join
    // and the right one the case of a chain join.
    // 
    // XXXXComp0JoinAttr0Ref1______________XXXXComp0JoinAttr0
    // XXXXComp0JoinAttr1Ref1______________XXXXComp0JoinAttr1
    // XXXXComp0JoinAttr0Ref2________________________________
    // XXXXComp0JoinAttr0Ref2________________________________
    //
    //
    // XXXXComp1JoinAttr0__________________XXXXComp1JoinAttr0Ref0
    // XXXXComp1JoinAttr1__________________XXXXComp1JoinAttr1Ref0
    // ____________________________________XXXXComp1JoinAttr0
    // ____________________________________XXXXComp1JoinAttr1
    //                              
    //
    // XXXXComp2JoinAttr0__________________XXXXComp2JoinAttr0Ref1
    // XXXXComp2JoinAttr1__________________XXXXComp2JoinAttr1Ref1
    // ____________________________________XXXXComp1JoinAttr0
    // ____________________________________XXXXComp1JoinAttr1
    // .....
    //
	@Override
	protected void genSourceRels() throws Exception {
		String[] sourceNames = new String[numOfTables];
		String[][] attrs  = new String[numOfTables][];
		joinAttrs = new String[numOfJoinAttributes];
		//MN BEGIN - considered an array to store types of attributes of source relations - 11 May 2014
		String[] [] attrsType = new String[numOfTables][];
		//MN END
		
		// create join attr names
		for(int i = 0; i < numOfJoinAttributes; i++)
			joinAttrs[i] = randomAttrName(0, i);
		// create numOfTables in the source to be denormalized
		for(int i = 0; i < numOfTables; i++) {
			sourceNames[i] = randomRelName(i);
			int numOfNonJoinAttr = numOfAttributes[i] - getNumJoinAttrs(i);
			attrs[i] = new String[numOfAttributes[i]];
			//MN BEGIN - 11 May 2014
			attrsType[i] = new String[numOfAttributes[i]];
			//MN END
			
			//MN BEGIN - 11 May 2014
			int offset =0;
			for(int k=0; k<i; k++)
				offset += (numOfAttributes[k] - getNumJoinAttrs(k));
			//MN END
			for(int j = 0; j < numOfNonJoinAttr; j++){
				attrs[i][j] = randomAttrName(i, j);
				//MN BEGIN - 11 May 2014
				if(targetReuse)
					attrsType[i][j] = m.getTargetRels().get(0).getAttrArray(offset + j).getDataType();
				//MN END
			}
		}
		
		createJoinAttrs(attrs);
		
		//MN BEGIN - 11 May 2014
		for (int k=0; k<numOfTables; k++)
			for(int j=0; j<numOfAttributes[k]; j++)
				if(attrsType[k][j] == null)
					attrsType[k][j] = "TEXT";
		//MN END
		// create tables 
		//MN BEGIN - 11 May 2014
		for(int i = 0; i < numOfTables; i++){
			if(!targetReuse)
				fac.addRelation(getRelHook(i), sourceNames[i], attrs[i], true);
			else
				fac.addRelation(getRelHook(i), sourceNames[i], attrs[i], attrsType[i], true);
		}
		//MN END
		
		createConstraints(attrs);
		//MN BEGIN - 11 May 2014
		targetReuse = false;
		//MN END
	}



	protected int getNumJoinAttrs(int i) {
		if (jk == JoinKind.STAR) {
			if (i == 0)
				return numOfJoinAttributes * (numOfTables - 1);
			return numOfJoinAttributes;
		}
		if (jk == JoinKind.CHAIN) {
			if (i == 0 || i == numOfTables - 1)
				return numOfJoinAttributes;
			return 2 * numOfJoinAttributes;
		}
		return -1;
	}
	
	protected int getNumNormalAttrs(int i) {
		return numOfAttributes[i] - getNumJoinAttrs(i);
	}

	protected void createConstraints(String[][] attrs) throws Exception {
		createPKs(attrs);
		if (jk == JoinKind.STAR)
			createStarConstraints(attrs);
		if (jk == JoinKind.CHAIN)
			createChainConstraints(attrs);
	}
	
	
	//MN added method to create PK for reused source relations - 11 May 2014
	protected void createPKsRel(String [][] attrs, int relNum, RelationType rel) throws Exception
	{
		int i = relNum;
		if ((jk == JoinKind.STAR && i != 0) // center of star has different PK 
				|| (jk == JoinKind.CHAIN && i != numOfTables - 1)) // last table for chain join has no PK 
		{ 
			if (!rel.isSetPrimaryKey())				
				fac.addPrimaryKey(rel.getName(), getJoinAttrs(i, attrs), true);
		}
		// center of the star
		else if (jk == JoinKind.STAR && i == 0) {
			if (!rel.isSetPrimaryKey()) {
				int numAttr = rel.getAttrArray().length;
				int numJoinAttr = numOfJoinAttributes * (numOfTables - 1);
				fac.addPrimaryKey(rel.getName(), 
						CollectionUtils.createSequence(numAttr - numJoinAttr, 
								numJoinAttr), 
						true);
			}
		}
	}
	//MN END
	
	protected void createPKs(String[][] attrs) throws Exception {
		for(int i = 0; i < numOfTables; i++) {
			if ((jk == JoinKind.STAR && i != 0) // center of star has different PK 
					|| (jk == JoinKind.CHAIN && i != numOfTables - 1)) // last table for chain join has no PK 
			{ 
				String relName = m.getRelName(i, true);
				if (!model.hasPK(relName, true))				
					fac.addPrimaryKey(relName, getJoinAttrs(i, attrs), true);
			}
			// center of the star
			else if (jk == JoinKind.STAR && i == 0) {
				String relName = m.getRelName(i, true);
				if (!model.hasPK(relName, true)) {
					int numAttr = m.getNumRelAttr(i, true);
					int numJoinAttr = numOfJoinAttributes * (numOfTables - 1);
					fac.addPrimaryKey(relName, 
							CollectionUtils.createSequence(numAttr - numJoinAttr, 
									numJoinAttr), 
							true);
				}
			}
		}
	}
	
	protected void createChainConstraints(String[][] attrs) throws Exception {
		// join every table with the previous one
		for(int i = 1; i < numOfTables; i++) {
			String[] fAttr, tAttr;
			fAttr = getJoinRefs(i, attrs);
			tAttr = getJoinAttrs(i - 1, attrs);
			addFK(i, fAttr, i - 1, tAttr, true);
		}
	}

	protected void createStarConstraints(String[][] attrs) throws Exception {
		// create fks from every table to the first table
		for(int i = 1; i < numOfTables; i++) {
			String[] fAttr, tAttr;
			tAttr = getJoinAttrs(i, attrs);
			fAttr = getJoinRefs(i, attrs);
			addFK(0, fAttr, i, tAttr, true);
		}
	}
	
	protected String[] getJoinRefs(int i, String[][] attrs) {
		String[] result = new String[numOfJoinAttributes];
		
		if (jk == JoinKind.CHAIN) {
			int offset = numOfAttributes[i] - numOfJoinAttributes;
			for(int j = 0; j < numOfJoinAttributes; j++)
				result[j] = attrs[i][offset + j];
		}
		if (jk == JoinKind.STAR) {
			int offset = numOfAttributes[0] - getNumJoinAttrs(0) + ((i - 1) * numOfJoinAttributes);
			for(int j = 0; j < numOfJoinAttributes; j++)
				result[j] = attrs[0][offset + j];
		}
		return result;
	}

	protected String[] getJoinAttrs (int i, String[][] attrs) {
		String[] result = new String[numOfJoinAttributes];
		int fac = (i != 0 && jk == JoinKind.CHAIN) ? 2 : 1;
		int offset = numOfAttributes[i] - (numOfJoinAttributes * fac);
		
		for(int j = 0; j < numOfJoinAttributes; j++)
			result[j] = attrs[i][offset + j];
		
		return result;
	}

	protected void createJoinAttrs(String[][] attrs) {
		if (jk == JoinKind.STAR)
			createStarJoinAttrs(attrs);
		if (jk == JoinKind.CHAIN)
			createChainJoinAttrs(attrs);
	}
	
	protected void createStarJoinAttrs(String[][] attrs) {
		// create join attrs in all except the first table (center of star)
		for(int i = 1; i < numOfTables; i++) {
			int offset = numOfAttributes[i] - (numOfJoinAttributes);
			for(int j = 0; j < numOfJoinAttributes; j++)
				attrs[i][offset + j] = getJoinAttr(i, j);
		}
		
		// create fk attributes in first table
		int offset = numOfAttributes[0] - ((numOfTables - 1) * numOfJoinAttributes);
		for(int i = 1; i < numOfTables; i++) {
			for(int j = 0; j < numOfJoinAttributes; j++)
				attrs[0][offset + j] = getJoinRef(i, j);
			offset += numOfJoinAttributes;
		}
	}
 

	protected void createChainJoinAttrs(String[][] attrs) {
		// create join attributes in all tables except the last one
		for(int i = 0; i < numOfTables - 1; i++) {
			int fac = i == 0 ? 1 : 2;
			int offset = numOfAttributes[i] - (numOfJoinAttributes * fac);
			for(int j = 0; j < numOfJoinAttributes; j++)
				attrs[i][offset + j] = getJoinAttr(i, j);
		}
		
		// create fk attributes in all tables except first
		for (int i = 1; i < numOfTables; i++) {
			int offset = numOfAttributes[i] - numOfJoinAttributes;
			for(int j = 0; j < numOfJoinAttributes; j++)
				attrs[i][offset + j] = getJoinRef( i, j);
		}
	}
	
	protected String getJoinRef(int i, int j) {
		return joinAttrs[j] + "comp" + i + "_joinref_" + j;
	}

	protected String getJoinAttr(int i, int j) {
		return joinAttrs[j] + "comp" + i +  "_joinattr_" + j;
	}
	
	/**
	 * Find a table that has at least numOfTables * (numOfJoinAttributes + 1)
	 * attributes and at most SUM(numOfAttributes) attirbutes. The table should 
	 * either have no key or the key should be on the last 
	 * numOfJoin Attributes * (numOfTables - 1) attributes.  
	 * @throws Exception 
	 */
	@Override
	protected boolean chooseTargetRels() throws Exception { //TODO more flexible to adapt numOfJoinAttributes
		RelationType r = null;
		int tries = 0;
		int minAttrs = numOfTables * (numOfJoinAttributes + 1);
		int maxAttrs = CollectionUtils.sum(numOfAttributes);
		int numTJoinAttrs = getTargetNumJoinAttrs();
		boolean ok = false;
		int numNormalAttr = 0;
		int[] joinAttPos = null;
		
		while(tries++ < MAX_NUM_TRIES && !ok) {
			r = getRandomRel(false, minAttrs, maxAttrs);
			
			if (r == null)
				break;
			
			//MN BEGIN - ME cannot handle source relations that do not have equal number of attributes - 11 May 2014
			//MN modified the criterion for reusing target relation - 28 May 2014
			if((r.sizeOfAttrArray() + numTJoinAttrs) != (numOfTables * numOfAttributes[0])){
				ok= false;
				break;
			}
			//MN END
			
			numNormalAttr = r.sizeOfAttrArray() - numTJoinAttrs;
			joinAttPos = CollectionUtils.createSequence(numNormalAttr, 
					numTJoinAttrs);
			
			if (r.isSetPrimaryKey()) {
				int[] pkPos = model.getPKPos(r.getName(), false);
				
				// PK has to be on the numTJoinAttrs last attributes
				if (pkPos.length ==  numTJoinAttrs 
						&& Arrays.equals(pkPos, joinAttPos))
					ok = true;
			}
			else
				ok = true;
		}
		
		// didn't find suiting rel? Create it
		if (!ok)
			return false;
		
		//MN BEGIN - 11 May 2014
		targetReuse = true;
		//MN END
		// add keys and distribute the normal attributes of rel
		m.addTargetRel(r);

		if (!r.isSetPrimaryKey())
			fac.addPrimaryKey(r.getName(), joinAttPos, false);

		// adapt number of normal attributes used (copied to target) per source rel
		//MN I don't understand this part of the code! - 9 May 2014
		int numPerSrcRel = r.sizeOfAttrArray() / numOfTables;
		int usedAttrs = 0;
		//MN BEGIN (numOfAttributes has been set in initialPartialMapping !) - 9 May 2014 - 13 May 2014
		for(int i = 0; i < numOfTables; i++) {
			numOfUseAttrs[i] = (numPerSrcRel > getNumNormalAttrs(i)) 
					? getNumNormalAttrs(i) : numPerSrcRel;
			//usedAttrs += numOfUseAttrs[i];
			//numOfAttributes[i] = numOfUseAttrs[i];
		}
		//numOfUseAttrs[numOfTables - 1] += (r.sizeOfAttrArray() - usedAttrs);
		//numOfAttributes[numOfTables - 1] = numOfUseAttrs[numOfTables - 1];
		//MN END
		return true;
	}
	
	
	@Override
	protected void genTargetRels() throws Exception {
		String targetName = randomRelName(0);
		List<String> attrs = new ArrayList<String> ();
		int numNormalAttrs;
		//MN considered an array to store types of attributes of target relation - 29 April 2014
		List<String> attrsType = new ArrayList<String> ();

		// first copy normal attributes
		for(int i = 0; i < numOfTables; i++) {
			int numAtt = getNumNormalAttrs(i);
			for(int j = 0; j < numAtt; j++){
				attrs.add(m.getAttrId(i, j, true));
				//MN BEGIN - 29 April 2014
				attrsType.add(m.getSourceRels().get(i).getAttrArray(j).getDataType());
				//MN END
			}
		}
		
		if (log.isDebugEnabled())
			log.debug("normal attrs are: " + attrs.toString());
		
		numNormalAttrs = attrs.size();
		
		// then copy join attributes
		if (jk == JoinKind.STAR) {
			for(int i = 1; i < numOfTables; i++) {
				int offset = getNumNormalAttrs(i);
				for(int j = 0; j < numOfJoinAttributes; j++){
					attrs.add(m.getAttrId(i, j + offset, true));
					//MN BEGIN - 29 April 2014
					attrsType.add(m.getSourceRels().get(i).getAttrArray(j + offset).getDataType());
					//MN END
				}
			}
		}
		if (jk == JoinKind.CHAIN) {
			for(int i = 0; i < numOfTables - 1; i++) {
				int offset = getNumNormalAttrs(i);
				for(int j = 0; j < numOfJoinAttributes; j++){
					attrs.add(m.getAttrId(i, j + offset, true));	
					//MN BEGIN - 29 April 2014
					attrsType.add(m.getSourceRels().get(i).getAttrArray(j + offset).getDataType());
					//MN END
				}
			}
		}
		
		//MN - 29 April 2014
		fac.addRelation(getRelHook(0), targetName, 
				attrs.toArray(new String[] {}), attrsType.toArray(new String[] {}), false);
		//MN
		
		// add PK on join attributes
		fac.addPrimaryKey(targetName, CollectionUtils.
				createSequence(numNormalAttrs, getTargetNumJoinAttrs()), 
				false);
	}
	
	protected int getTargetNumJoinAttrs () {
		return numOfJoinAttributes * (numOfTables - 1);
	}
	
	@Override
	protected void genMappings() throws Exception {
		MappingType m1 = fac.addMapping(m.getCorrs());
		String[][] vars = new String[numOfTables][];
		String[] targetVars = new String[m.getNumRelAttr(0, false)];
		int offset;
		
		// add foreach atoms for the the source fragments
		offset = m.getNumRelAttr(0, true);
		vars[0] = fac.getFreshVars(0, offset);
		fac.addForeachAtom(m1, 0, vars[0]);
		
		// each table get fresh vars for its free and join attributes
		// the fk vars are takes from the join attributes they reference
		for(int i = 1; i < numOfTables; i++) {
			int numFreshVars = numOfAttributes[i] - numOfJoinAttributes;
			String[] freeVars = fac.getFreshVars(offset, numFreshVars);
			offset += numFreshVars;
			String[] fkVars = null;
			
			// get vars for the referenced attributes from the first table 
			if (jk == JoinKind.STAR) {
				int from = numOfAttributes[0] - ((numOfTables - i) * numOfJoinAttributes);
				fkVars = Arrays.copyOfRange(vars[0], from, from + numOfJoinAttributes);
			}
			// get vars for the referenced attributes from the previous table
			if (jk == JoinKind.CHAIN) {
				if (log.isDebugEnabled()) {log.debug("i is " + i  + " , " + numFreshVars + " , " + numOfJoinAttributes);};
				// PRG FIXED CHAIN JOIN PROBLEMS - August 30, 2012
				// Replaced the following 2 lines to avoid ArrayIndexOutOfBoundsException
				//fkVars = Arrays.copyOfRange(vars[i - 1], numFreshVars, 
				//		numFreshVars + numOfJoinAttributes);
				// PRG FIXED Foreach Source Expression by correcting fromIndex - Sep 4, 2012
				// int fromIndex = vars[i-1].length - numOfJoinAttributes; 
				int fromIndex = vars[i-1].length - getNumJoinAttrs(i-1); 
				int toIndex = (numOfJoinAttributes == 1 ? fromIndex + 1 : fromIndex + numOfJoinAttributes);
				if (log.isDebugEnabled()) {log.debug("Copying Chain Join Variables from  " + fromIndex  + " to " + toIndex);};
				fkVars = Arrays.copyOfRange(vars[i - 1], fromIndex, toIndex);
				if (log.isDebugEnabled()) {log.debug("FK Chain Join Vars are " + Arrays.toString(fkVars));};
			}
			vars[i] = CollectionUtils.concat(freeVars, fkVars);
			
			fac.addForeachAtom(m1, i, vars[i]);
		}
		
		// generate an array of vars for the target
		// first we add vars for the free attributes of all tables
		// then we add the join attribute vars
		offset = 0;
		for(int i = 0; i < numOfTables; i++) {
			int numVars = vars[i].length;
			
			numVars = numOfUseAttrs[i];
			
			System.arraycopy(vars[i], 0, targetVars, offset, numVars);
			offset += numVars;
		}
		
		// star join, add join attribute vars from first table
		if (jk == JoinKind.STAR) {
			int start = getNumNormalAttrs(0);
			for(int i = 1; i < numOfTables; i++) {
				System.arraycopy(vars[0], start, targetVars, offset, numOfJoinAttributes);
				offset += numOfJoinAttributes;
				start += numOfJoinAttributes;
			}
		}
		// chain join, take join attribute vars from each table
		if (jk == JoinKind.CHAIN) {
			for(int i = 0; i < numOfTables - 1; i++) {
				int start = getNumNormalAttrs(i);
				System.arraycopy(vars[i], start, targetVars, offset, numOfJoinAttributes);
				offset += numOfJoinAttributes;
			}
		}
		
		fac.addExistsAtom(m1, 0, targetVars);
	}
	
	@Override
	protected void genTransformations() throws Exception {
		Query q;
		String creates = m.getRelName(0, false);
		
		q = genQueries();
		q.storeCode(q.toTrampString(m.getMapIds()));
		q = addQueryOrUnion(creates, q);
		fac.addTransformation(q.getStoredCode(), m.getMapIds(), creates);		
	}
	
	private SPJQuery genQueries() {
		SPJQuery query = new SPJQuery();
    	SelectClauseList sel = query.getSelect();
    	FromClauseList from = query.getFrom();
    	
       // first we create the source tables and their attributes 
        // that do not participate in the joins.
        for (int i = 0, imax = numOfTables; i < imax; i++) {
            // add the table to the from clause
            from.add(new Variable("X"+i), new Projection(Path.ROOT,  m.getRelName(i, true)));

            // create the non join attributes of the specific table/fragment/component
            int numOfNonJoinAttr = numOfAttributes[i] - getNumJoinAttrs(i);
            
            for (int k = 0, kmax = numOfNonJoinAttr; k < kmax; k++) {
            	String attrName = m.getAttrId(i, k, true);
                // add the non-join attribute to the select clause
                Projection att = new Projection(new Variable("X"+i), attrName);
                sel.add(attrName, att);
            }
        }

        // first create the join SMarkElements names
        String joinAttrNames[] = new String[numOfJoinAttributes];
        for (int i = 0, imax = numOfJoinAttributes; i < imax; i++)
            joinAttrNames[i] = Modules.nameFactory.getARandomName();
        //*******************************************************************
        // for the case of a STAR join, create the join attributes for all the
        // tables (apart from the first one) 
        // also, all the join attributes will be added to the target table
        // and to the select clause of the local query
        for (int tbli = 1, tblimax = numOfAttributes.length; ((tbli < tblimax) && (jk == JoinKind.STAR)); tbli++)
        {
            for (int i = 0, imax = numOfJoinAttributes; i < imax; i++)
            {
            	String attrName = getJoinAttr(tbli, i);
                // add the join attribute to the select clause
                Projection att = new Projection(new Variable("X"+tbli), attrName);
                sel.add(attrName, att);
            }
        }
        
        // and now we add the reference (Foreign key if you like the term)
        // attributes in the first table. the foreign keys will point to 
        // all the join attributes from the other tables.
        // create the join conditions in the where clause and the fkeys.
        AND andCond = new AND();
        andCond.toString();
        for (int tbli = 1, tblimax = numOfAttributes.length; ((tbli < tblimax) && (jk == JoinKind.STAR)); tbli++)
        {    	
        	for (int i = 0, imax = numOfJoinAttributes; i < imax; i++)
        	{
        		String attrName = getJoinRef(tbli, i);
        		String referencedAttrName = getJoinAttr(tbli, i);
        		
                // create the where clause; the join attributes and the reference attributes that
                // make the join condition
                Projection att1 = new Projection(new Variable("X"+0), attrName);
                Projection att2 = new Projection(new Variable("X"+tbli), referencedAttrName);
                andCond.add(new EQ(att1,att2));
        	}
        }	

        // ********************************************************************
        // for the case of a CHAIN join, create the join attributes for all the
        // tables (apart from the last one of course since none is referencing
        // that).also, all the join attributes will be added to the target table.
        for (int tbli = 0, tblimax = numOfAttributes.length; ((tbli < (tblimax - 1)) && (jk == JoinKind.CHAIN)); tbli++)
        {
            for (int i = 0, imax = numOfJoinAttributes; i < imax; i++)
            {
            	String attrName = getJoinAttr(tbli, i);
            	
                // add the join attribute to the select clause
                Projection att = new Projection(new Variable("X"+tbli), attrName);
                sel.add(attrName, att);
            }
        }

        // and now we add the reference (Foreign key if you like the term)
        // attributes. The only difference between the CHAIN and the STAR join
        // is that in the former case they reference the previous table, while
        // in the STAR they all reference the first table
        // in the case of CHAIN join, the foreign keys will not be added to the target table
        for (int tbli = 1, tblimax = numOfAttributes.length; ((tbli < tblimax) && (jk == JoinKind.CHAIN)); tbli++)
        {     
            for (int i = 0, imax = numOfJoinAttributes; i < imax; i++)
            {
            	String attrName = getJoinRef(tbli, i);
        		String referencedAttrName = getJoinAttr(tbli - 1, i);
        		int referencedTable = tbli - 1;
                // create the where clause; the join attributes and the reference attributes that
                // make the join condition
                Projection att1 = new Projection(new Variable("X"+tbli), attrName);
                Projection att2 = new Projection(new Variable("X"+referencedTable), referencedAttrName);
                andCond.add(new EQ(att1,att2));
            }
       }

        query.setSelect(sel);
        query.setFrom(from);
        if(andCond.size() > 0)
        	query.setWhere(andCond);
        
        return query;
	}

	@Override
	protected void genCorrespondences() {
		int tOffset = 0;
		
		// create correspondences for free attributes
		for(int i = 0; i < numOfTables; i++) {
			int numAtt = getNumNormalAttrs(i);
			for(int j = 0; j < numAtt; j++)
				addCorr(i, j, 0, tOffset++);
		}
		
		// create correspondences for join attributes
		if (jk == JoinKind.STAR) {
			for(int i = 1; i < numOfTables; i++) {
				int offset = getNumNormalAttrs(i);
				for(int j = 0; j < numOfJoinAttributes; j++)
					addCorr(i, j + offset, 0, tOffset++);
			}
		}
		if (jk == JoinKind.CHAIN) {
			for(int i = 0; i < numOfTables - 1; i++) {
				int offset = getNumNormalAttrs(i);
				for(int j = 0; j < numOfJoinAttributes; j++)
					addCorr(i, j + offset, 0, tOffset++);
			}
		}
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.MERGING;
	}
}
