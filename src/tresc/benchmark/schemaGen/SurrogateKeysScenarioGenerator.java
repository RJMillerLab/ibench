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
import java.util.Vector;

import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SKFunction;

import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Constants.SkolemKind;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Query;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;

// PRG Enhanced SURROGATE KEY Scenario to handle Source Keys based on ConfigOptions.PrimaryKeySize
// and both dynamic and custom Skolemization Modes for Skolem 1 ("IDIndep" attr) and Skolem 2 ("IDOnFirst" attr), respectively.
// By default (when ConfigOptions.PrimaryKeySize = 0), we enforce a key of size 1 - Sep 17, 2012
// PRG FIXED Infinite Loop Bug in method generateSKs(), case SkolemKind.RANDOM  - Sep 18, 2012
// PRG FIXED Omission, must generate source relation with at least 2 elements (this was causing empty Skolem terms and PK FDs with empty RHS!)- Sep 19, 2012
// PRG Systematically using "Random Without Replacement" strategy/algorithm when dealing with SkolemKind.RANDOM mode everywhere! - Sep 21, 2012

// BORIS TO DO - Revise method genQueries() as it might be out of sync now - Sep 17, 2012

// MN IMPLEMENTED chooseSourceRels and chooseTargetRels - 5 May 2014
// MN ENHANCED genTargetRels to pass types of attributes of target relation as argument to addRelation - 5 May 2014
// MN uniform (ToDo) - 6 May 2014
// MN ENHANCED genSourceRels to pass types of attributes of source relation as argument to addRelation - 13 May 2014


public class SurrogateKeysScenarioGenerator extends AbstractScenarioGenerator
{

	private int params;
	private int numOfSrcTblAttr;
	// PRG REMOVED Hard coded Skolemization Mode - Sep 17, 2012
	// private SkolemKind sk = SkolemKind.ALL;
	private SkolemKind sk;
	private int keySize;
	
	//MN - added attribute to check whether we are reusing a target relation - 13 May 2014
	private boolean targetReuse;
	//MN
    
    public SurrogateKeysScenarioGenerator()
    {
        ;
    }

    @Override
	protected void initPartialMapping() {
		super.initPartialMapping();
		numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
		
		// PRG ADD - Generate at least a source relation of 2 elements - Sep 19, 2012
        numOfSrcTblAttr = (numOfSrcTblAttr > 2 ? numOfSrcTblAttr : 2);
        
		// this reading corresponds to ConfigOptions.NumOfParamsInFunctions and shall be used to determine the number of arguments of Skolem 2
		params = Utils.getRandomNumberAroundSomething(_generator, numOfParams, numOfParamsDeviation);
		// make sure params are at least 2
		params = (params < 2) ? 2 : params;
		// and the elements are at least as many as the the params
		if (params > numOfSrcTblAttr)
			numOfSrcTblAttr = params;
		
		// PRG ENHANCED Key Generation and Skolemization Modes for SURROGATE KEY Scenario according to Configuration Options - Sep 17, 2012
		// Reading ConfigOptions.PrimaryKeySize and ConfigOptions.SkolemKind
		sk = SkolemKind.values()[typeOfSkolem];
		keySize = Utils.getRandomNumberAroundSomething(_generator, primaryKeySize, primaryKeySizeDeviation);
		// adjust keySize as necessary with respect to number of source table attributes
		keySize = (keySize >= numOfSrcTblAttr) ? numOfSrcTblAttr - 1 : keySize;
		// PRG ENFORCE MANDATORY KEY - Sep 17, 2012
		keySize = (keySize > 0) ? keySize : 1;
		
		//MN - 13 May 2014
		targetReuse = false;
		//MN
		
	}

	@Override
	protected void genSourceRels() throws Exception {
		String relName = randomRelName(0);
		String[] attrs = new String[numOfSrcTblAttr];
		
		//MN - considered an array to store types of attributes of source relation - 13 May 2014
		String[] attrsType = new String[numOfSrcTblAttr];
		//MN
		
		//for(int i =0; i < numOfSrcTblAttr; i++) {
		//	attrs[i] = randomAttrName(0, i);
		//}
		
		// generate the appropriate number of keys
		String[] keys = new String[keySize];
		for (int j = 0; j < keySize; j++)
			keys[j] = randomAttrName(0, 0) + "ke" + j;
				
				
		int keyCount = 0;				
		for (int i = 0; i < numOfSrcTblAttr; i++) {
					
			String attrName = randomAttrName(0, i);		
			
			if (keyCount < keySize){						
				attrName = keys[keyCount];
			}
					
			keyCount++;		
			
			attrs[i] = attrName;
			//MN BEGIN - 13 May 2014
			if(targetReuse)
				attrsType [i] = m.getTargetRels().get(0).getAttrArray(i).getDataType();
			//MN END
					
		}		
		
		//MN BEGIN - 13 May 2014
		if(!targetReuse)
			fac.addRelation(getRelHook(0), relName, attrs, true);
		else
			fac.addRelation(getRelHook(0), relName, attrs, attrsType, true);
		//MN END
		
		// PRG FIX BUG - The generated key may have more than 1 element as indicated by keySize - Sep 17, 2012
		//fac.addPrimaryKey(relName, 0, true);
		fac.addPrimaryKey(relName, keys, true);
		
		//MN - 13 May 2014
		targetReuse = false;
		//MN
	}

	// override to adapt the local fields
	//MN Question: Do we want to preserve the value of keySize? - 5 May 2014
	//MN Question: what about source reusability of other primitives? - 5 May 2014
	/**
	 * Also set the number of source attributes
	 */
	@Override
	protected boolean chooseSourceRels() throws Exception {
		RelationType rel;
		
		//MN there is no constraint - 5 May 2014
		rel = getRandomRel(true, 1);
		
		if (rel == null)
			return false;
		
		// set number of src tbl attributes
		numOfSrcTblAttr = rel.sizeOfAttrArray();
		
		if (keySize > 0 && !rel.isSetPrimaryKey()) {
			//MN keys should be the first elements of the relation - 5 May 2014
			if(keySize <= rel.sizeOfAttrArray()){
				fac.addPrimaryKey(rel.getName(), 
					CollectionUtils.createSequence(0, keySize), true);}
			else{
				keySize = 1;
				fac.addPrimaryKey(rel.getName(), 
						CollectionUtils.createSequence(0, keySize), true);
			}
				
		}
		else if (rel.isSetPrimaryKey()){
			//MN we need to check that if primary key is in the right position - 5 May 2014
			int[] pkPos = model.getPKPos(rel.getName(), true);
			boolean ok = true;
			for(int i=0; i<pkPos.length; i++)
				if(pkPos[i] != i)
					ok = false;
			if(!ok)
				return false;
			keySize = rel.getPrimaryKey().sizeOfAttrArray();
		}
		
		//the relation should be added to "m" - 5 May 2014
		m.addSourceRel(rel);
		return true;
	}
	
	//MN Question: what about keySize? - 5 May 2014
	@Override
	protected void genTargetRels() {
		String tRelName = randomRelName(0);
		int numTargetEl = numOfSrcTblAttr + 2;
		String[] attrs = new String[numTargetEl];
		//MN BEGIN - considered an array to store types of attributes of target relation - 5 May 2014
		List<String> attrsType = new ArrayList<String> ();
		//MN END
		
		for(int i = 0; i < numOfSrcTblAttr; i++){
			attrs[i] = m.getAttrId(0, i, true);
			//MN BEGIN - 5 May 2014
			attrsType.add(m.getSourceRels().get(0).getAttrArray(i).getDataType());
			//MN END
		}
		// PRG NOTE - We're explicitly creating two new target attributes for which we need to generate appropriate Skolems 
		attrs[numOfSrcTblAttr] = randomAttrName(0, numOfSrcTblAttr) + "IDindep";
		//MN BEGIN - 5 May 2014
		attrsType.add("TEXT");
		//MN END
		
		attrs[numOfSrcTblAttr + 1] = randomAttrName(0, numOfSrcTblAttr + 1) + "IDOnFirst";
		//MN BEGIN - 5 May 2014
		attrsType.add("TEXT");
		//MN END
		
		// PRG NOTE - The original StBench does not generate a target key at all for SURROGATE KEY Scenarios.
		// In lieu of this, we do likewise (that is, no target key is being generated so far) - Sep 17, 2012
		
		//MN  - 5 May 2014
		fac.addRelation(getRelHook(0), tRelName, attrs, attrsType.toArray(new String[] {}), false);
	}
	
	
	// override to adapt the local fields
	//MN added support for target reusability - 5 May 2014
	//MN Question: Do we want to preserve the value of keySize? we set keySize to "1" - 5 May 2014
	//MN 
	/**
	 * Also set the number of source attributes
	 */
	@Override
	protected boolean chooseTargetRels() throws Exception {
			RelationType rel;
			
			//MN minRequiredNumOfAttrs =  2 for skolems + keySize in source - 5 May 2014
			//MN Question: Is that ok to not to consider keySize? - 5 May 2014
			rel = getRandomRel(false, 2 + 1);
			
			if (rel == null)
				return false;
			
			// set number of src tbl attributes
			//MN 2 skolems in target relation - 5 May 2014
			numOfSrcTblAttr = rel.sizeOfAttrArray() - 2;
			
			//MN set keySize for genSourceRels - 5 May 2014
			//MN the reason for setting value of keySize to 1 is increase in reusability - 13 May 2014
			keySize = 1;
			
			//the relation should be added to "m" - 5 May 2014
			m.addTargetRel(rel);
			
			//MN - 13 May 2014
			targetReuse = true;
			//MN 
			
			return true;
	}
		
	@Override
	protected void genMappings() throws Exception {
		MappingType m1 = fac.addMapping(m.getCorrs());
		
		fac.addForeachAtom(m1, 0, fac.getFreshVars(0, numOfSrcTblAttr));

		switch (mapLang) 
		{
			case FOtgds:
				fac.addExistsAtom(m1, 0, fac.getFreshVars(0, numOfSrcTblAttr + 2));
				break;
				
			case SOtgds:
				fac.addEmptyExistsAtom(m1, 0);
				fac.addVarsToExistsAtom(m1, 0, fac.getFreshVars(0, numOfSrcTblAttr));
				SkolemKind sk1 = sk;
				if(sk == SkolemKind.VARIABLE)
					sk1 = SkolemKind.values()[_generator.nextInt(4)];
				generateSKs(m1, sk1);
				break;
		}
	}
	
	// PRG Rewrote method generateSKs() to permit different types of Skolemizations over Skolem 1 "IDIndep" attribute - Sep 17, 2012
	// PRG Systematically using "Random Without Replacement" strategy/algorithm when dealing with SkolemKind.RANDOM mode everywhere! - Sep 21, 2012
	
	private void generateSKs(MappingType m1, SkolemKind sk) {
		
		// Generate two Skolems, one for target attribute "IDindep" and one for target attribute "IDOnFirst"
		
		if (log.isDebugEnabled()) {log.debug("SURROGATE KEY - Method generateSKs() with totalVars = " + numOfSrcTblAttr + " and Num of New Skolems = 2");};
		
		// worst case, get ready for SkolemKind.ALL and assume the number of source table attributes 
		int numArgsForSkolem = numOfSrcTblAttr;

		// if we are using a key in the original relation then we base the skolem on just that key
		if (sk == SkolemKind.KEY) {
			
			if (log.isDebugEnabled()) {log.debug("--- SKOLEM MODE = KEY ---");};
			if (log.isDebugEnabled()) {log.debug("1st Skolem, Key Argument Set: " + Arrays.toString(fac.getFreshVars(0, keySize)));};
			
			// Skolem 1 "IDIndep": define argument set based on the source relation key 
			fac.addSKToExistsAtom(m1, 0, fac.getFreshVars(0, keySize));
		    
		}
		else if (sk == SkolemKind.RANDOM)
		{
			if (log.isDebugEnabled()) {log.debug("--- SKOLEM MODE = RANDOM ---");};
			
			// Generate a random number of args for this Skolem (Uniform distribution between 0 (inclusive) and numOfSrcTblAttr (exclusive))
			numArgsForSkolem = Utils.getRandomUniformNumber(_generator, numOfSrcTblAttr);
			// Ensure we generate at least a random argument set of size > 0
			numArgsForSkolem = (numArgsForSkolem == 0 ? numOfSrcTblAttr : numArgsForSkolem);

			if (log.isDebugEnabled()) {log.debug("Initial randomly picked number of arguments: " + numArgsForSkolem);};
			
			// Generate a random argument set	
			Vector<String> randomArgs = Utils.getRandomWithoutReplacementSequence(_generator, numArgsForSkolem, model.getAllVarsInMapping(m1, true));
			
			if (randomArgs.size() == numOfSrcTblAttr) {
				if (log.isDebugEnabled()) {log.debug("1st Skolem, Random Argument Set [using ALL instead]: " + randomArgs.toString());};
			}
			else {
				if (log.isDebugEnabled()) {log.debug("1st Skolem, Random Argument Set: " + randomArgs.toString());};
			}
			
			// Skolem 1 "IDIndep": define argument set based on randomly picked source attributes
			fac.addSKToExistsAtom(m1, 0, Utils.convertVectorToStringArray(randomArgs));
			
			// PRG Replaced the following fragment of code as it does not guarantee convergence - Sep 21, 2012
			/*
			numArgsForSkolem = Utils.getRandomNumberAroundSomething(_generator, numOfSrcTblAttr/2, numOfSrcTblAttr/2);
			// ensure that we are still within bounds
			numArgsForSkolem = (numArgsForSkolem >= numOfSrcTblAttr) ? numOfSrcTblAttr : numArgsForSkolem;
			
			if (log.isDebugEnabled()) {log.debug("Initial randomly picked number of arguments: " + numArgsForSkolem);};
			
			// generate the random vars to be arguments for the skolem
			Vector<String> randomVars = new Vector<String> ();
			
			int MaxRandomTries = 30;
			int attempts = 0;
			boolean ok = false;
			
			for (int i = 0; i < numArgsForSkolem; i++) {

				while (!ok & attempts++ < MaxRandomTries) {
					
					// Get random position 
					int pos = Utils.getRandomNumberAroundSomething(_generator, numOfSrcTblAttr / 2, numOfSrcTblAttr / 2);
					// Adjust random position value just in case it falls outside limits
					pos = (pos >= numOfSrcTblAttr) ? numOfSrcTblAttr - 1 : pos;
					
					// Make sure we have not already added this variable before
					// If so, attempt to get another random position up to a max of 30 tries
					if (randomVars.indexOf(fac.getFreshVars(pos, 1)[0]) == -1) {
						randomVars.add(fac.getFreshVars(pos, 1)[0]);
						ok = true;
					    break;
					}
					
				}
				// Plainly give up after 30 tries. If so, we may end up with an argument set with fewer variables.
			
			}
			// Make sure we were able to generate at least 1 variable from randomArgs. If not, we use all source attributes
			if (randomVars.size() > 0) {
			
				Collections.sort(randomVars);
				if (log.isDebugEnabled()) {log.debug("1st Skolem, Random Argument Set: " + randomVars.toString());};
				// Skolem 1 "IDIndep": define argument set based on randomly picked source attributes
				fac.addSKToExistsAtom(m1, 0, Utils.convertVectorToStringArray(randomVars));
				
			} else  // If not, just use all source attributes for the sake of completion
				if (log.isDebugEnabled()) {log.debug("1st Skolem, Random Argument Set [using ALL instead]: " + Arrays.toString(fac.getFreshVars(0, numOfSrcTblAttr)));};
				// Skolem 1 "IDIndep": define argument set based on all source attributes
				fac.addSKToExistsAtom(m1, 0, fac.getFreshVars(0, numOfSrcTblAttr));
			*/
		}
		else // SkolemKind.ALL
		{
			if (log.isDebugEnabled()) {log.debug("--- SKOLEM MODE = ALL ---");};
			if (log.isDebugEnabled()) {log.debug("1st Skolem, ALL Argument Set: " + Arrays.toString(fac.getFreshVars(0, numArgsForSkolem)));};
			// Skolem 1 "IDIndep": define argument set based on all source attributes 
			fac.addSKToExistsAtom(m1, 0, fac.getFreshVars(0, numArgsForSkolem));
			
		}
		
		if (log.isDebugEnabled()) {log.debug("2nd Skolem, Custom Argument Set: " + Arrays.toString(fac.getFreshVars(0, numOfParams)));};
		// Skolem 2 IDOnFirst": use only amount of source table attributes specified in config file 		
		fac.addSKToExistsAtom(m1, 0, fac.getFreshVars(0, numOfParams));
		
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
	
	private SPJQuery genQueries() throws Exception {
		String sourceName = m.getRelName(0, true);
		String targetName = m.getRelName(0, false);
		String[] attrNames = m.getAttrIds(0, false);
		String sAttName;
		Projection att;
		
		MappingType m1 = m.getMaps().get(0);
		
		// create the intermediate query
		SPJQuery query = new SPJQuery();

		// create the From Clause of the query
		query.getFrom().add(new Variable("X"),
				new Projection(Path.ROOT, sourceName));

		SelectClauseList select = query.getSelect();
		
		for (int i = 0; i < numOfSrcTblAttr; i++) {
			// create the atomic element in the source and the target
			// add the subelements as attributes to the Select clause of the
			// query
			att = new Projection(new Variable("X"), attrNames[i]);
			select.add(attrNames[i], att);
		}

		// create the Function corresponding to the key
		// and add it to the select clause of query
		vtools.dataModel.expression.SKFunction stSK1;
		vtools.dataModel.expression.SKFunction stSK2;
		switch (mapLang) {
		case FOtgds:
			stSK1 = new vtools.dataModel.expression.SKFunction(
					fac.getNextId("SK"));
			sAttName = m.getAttrId(0, 0, true);
 			att= new Projection(new Variable("X"), sAttName);
 			stSK1.addArg(att);
			
			stSK2 = new vtools.dataModel.expression.SKFunction(
					fac.getNextId("SK"));
			for(int k = 0; k < numOfParams; k++) {			
	 			sAttName = m.getAttrId(0, k, true);
	 			att = new Projection(new Variable("X"), sAttName);
	 			stSK2.addArg(att);
	 		}
			break;
		case SOtgds:
		default:
			SKFunction sk = m.getSkolemFromAtom(m1, false, 0, numOfSrcTblAttr);
	 		stSK1 = new vtools.dataModel.expression.SKFunction(sk.getSkname());
	 			
	 		for(int k = 0; k < sk.getVarArray().length; k++) {			
	 			sAttName = m.getAttrId(0, k, true);
	 			att = new Projection(new Variable("X"), sAttName);
	 			stSK1.addArg(att);
	 		}

	 		// second skolem function
	 		SKFunction sk2 = m.getSkolemFromAtom(m1, false, 0, numOfSrcTblAttr + 1);
	 		stSK2 = new vtools.dataModel.expression.SKFunction(sk2.getSkname());
	 			
	 		for(int k = 0; k < sk2.getVarArray().length; k++) {			
	 			sAttName = m.getAttrId(0, k, true);
	 			att = new Projection(new Variable("X"), sAttName);
	 			stSK2.addArg(att);
	 		}

			break;
		}

		// add
 		select.add(attrNames[numOfSrcTblAttr], stSK1);
 		select.add(attrNames[numOfSrcTblAttr + 1], stSK2);
		
		// add the subquery to the final transformation query
		query.setSelect(select);
//		SelectClauseList pselect = pquery.getSelect();
//		pselect.add(targetName, query);
//		pquery.setSelect(pselect);
		
		return query;
	}

	@Override
	protected void genCorrespondences() {
		for(int i = 0; i < numOfSrcTblAttr; i++)
			addCorr(0, i, 0, i);
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.SURROGATEKEY;
	}
}
