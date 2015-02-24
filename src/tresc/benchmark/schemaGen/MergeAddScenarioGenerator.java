package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SKFunction;

import smark.support.PartialMapping;
import tresc.benchmark.Constants.JoinKind;
import tresc.benchmark.Constants.MappingLanguageType;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Constants.SkolemKind;
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
// PRG PRG FIXED Foreach Source Expression by correcting fromIndex - Sep 4, 2012 
// PRG FIXED BUG - ArrayCopy was always invoked with Null Destiny Array - Sep 5, 2012
// PRG Enhanced MERGE ADD to handle mandatory keys based on ConfigOptions.NumOfJoinAttributes - Sep 18, 2012
// PRG FIXED Infinite Loop Bug in method generateSKs(), case SkolemKind.RANDOM - Sep 18, 2012
// PRG Systematically using "Random Without Replacement" strategy/algorithm when dealing with SkolemKind.RANDOM mode everywhere! - Sep 21, 2012

// BORIS TO DO - Revise method genQueries() as it might be out of sync now - Sep 17, 2012

// MN IMPLEMENTED chooseSourceRels and chooseTargetRels (source and target reusability) - 5 May 2014
// MN FIXED genTargetRels - 8 May 2014
// MN ENHANCED genTargetRels to pass types of attributes of target relations as argument to addRelation - 8 May 2014
// MN MODIFIED chooseSourceTargetRels - 13 May 2014
// MN ENHANCED genSourceRels to pass types of attributes of source relations as argument to addRelation - 13 May 2014
// MN FIXED chooseSourceRels - 17 May 2014
// MN MODIFIED chooseTargetRels - 28 May 2014

public class MergeAddScenarioGenerator extends MergingScenarioGenerator {
	
	// PRG ADD August 30, 2012
	static Logger log = Logger.getLogger(MergeAddScenarioGenerator.class);
	
	public static final int MAX_NUM_TRIES = 10;
	
	private SkolemKind sk;
	
    public MergeAddScenarioGenerator()
    {		;		}
    
    @Override
    protected void initPartialMapping() {
    	m = new PartialMapping();
		fac.setPartialMapping(m);
    	
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
            tmpInt = (tmpInt <= getNumJoinAttrs(k)) ? getNumJoinAttrs(k) + 1 : tmpInt; 
            numOfAttributes[k] = tmpInt;
        }
        
        sk = SkolemKind.values()[typeOfSkolem];
        
        //MN BEGIN - 13 May 2014
        targetReuse = false;
        //MN END
    }
    
	@Override
	protected void genSourceRels() throws Exception {
		String[] sourceNames = new String[numOfTables];
		String[][] attrs  = new String[numOfTables][];
		joinAttrs = new String[numOfJoinAttributes];
		//MN BEGIN - considered an array to store types of attributes of source relations - 13 May 2014
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
		
			//MN BEGIN - 13 May 2014
			attrsType[i] = new String[numOfAttributes[i]];
			//MN END
			
			//MN BEGIN - 13 May 2014
			int offset =0;
			for(int k=0; k<i; k++)
				offset += (numOfAttributes[k] - getNumJoinAttrs(k));
			//MN END
			
			for(int j = 0; j < numOfNonJoinAttr; j++){
				attrs[i][j] = randomAttrName(i, j);
				
				//MN BEGIN - 13 May 2014
				if(targetReuse)
					attrsType[i][j] = m.getTargetRels().get(0).getAttrArray(offset + j).getDataType();
				//MN END
			}
		}
		
		if (jk == JoinKind.STAR)
			createStarJoinAttrs(attrs);
		if (jk == JoinKind.CHAIN)
			createChainJoinAttrs(attrs);
		
		//MN BEGIN - 13 May 2014
		for (int k=0; k<numOfTables; k++)
			for(int j=0; j<numOfAttributes[k]; j++)
				if(attrsType[k][j] == null)
					attrsType[k][j] = "TEXT";
		//MN END
		
		// create tables 
		//MN BEGIN - 13 May 2014
		for(int i = 0; i < numOfTables; i++){
			if(!targetReuse)
				fac.addRelation(getRelHook(i), sourceNames[i], attrs[i], true);
			else
				fac.addRelation(getRelHook(i), sourceNames[i], attrs[i], attrsType[i], true);
		}
		//MN END
		
		// create FK and key constraints
		// PRG ADD Creation of Primary Keys (method createPK() is implemented by the parent) - Sep 18, 2012
		createPKs(attrs);
		if (jk == JoinKind.STAR)
			createStarConstraints(attrs);
		if (jk == JoinKind.CHAIN)
			createChainConstraints(attrs);
		
		//MN BEGIN - 13 May 2014
		targetReuse = false;
		//MN END
	}

	
	
	private int getTotalNumNormalAttrs() {
		int totalNormalAttributes = 0;
    	for (int k = 0; k < numOfTables; k++)
    		totalNormalAttributes += getNumNormalAttrs(k);
    	
    	return totalNormalAttributes;
	}
		
	/**
	 * Find a table that has at least numOfTables * (numOfJoinAttributes + 1) + numNewAttr
	 * attributes and at most SUM(numOfAttributes) attributes. The table should 
	 * either have no key or the key should be on the last 
	 * numOfJoin Attributes * (numOfTables - 1) attributes.  
	 * @throws Exception 
	 */
	@Override
	protected boolean chooseTargetRels() throws Exception { //TODO more flexible to adapt numOfJoinAttributes
		RelationType r = null;
		int tries = 0;
		//MN it seems that we are going to preserve the values of numNewAttr, numOfJoinAttributes - 5 May 2014
		//MN Replaced the following minAttrs declaration with one that considers the exact needed number of attributes - 5 May 2014
		//MN Example: given R(a,b) and S(b,c) as source relations, we would need a target relation with a minimum of 3 attrs + any new
		//MN          Note that before, the minAttrs declaration was giving us a minimum of 4 attrs + any new
		//int minAttrs = (numOfTables * (numOfJoinAttributes + 1) + numNewAttr;
		int minAttrs = (numOfTables * numOfJoinAttributes) + 1 + numNewAttr;
		//MN Replaced the following maxAttrs declaration with a better one - 5 May 2014
		//int maxAttrs = CollectionUtils.sum(numOfAttributes) + numNewAttr;
		int maxAttrs = CollectionUtils.sum(numOfAttributes) - 1 + numNewAttr;
		int numTJoinAttrs = getTargetNumJoinAttrs();
		boolean ok = false;
		int numNormalAttr = 0;
		int[] joinAttPos = null;
		
		while(tries++ < MAX_NUM_TRIES && !ok) {
			r = getRandomRel(false, minAttrs, maxAttrs);
			
			if (r == null)
				break;
			
			//MN BEGIN - ME cannot handle source relations that do not have equal number of attributes - 13 May 2014
			//MN BEGIN - modified the criterion for reusing target relation - 28 May 2014
			if((r.sizeOfAttrArray() - numNewAttr + numTJoinAttrs) != (numOfTables * numOfAttributes[0])){
				ok= false;
				break;
			}
			//MN END
			
			//MN the following line has not been tested yet - 5 May 2014 - 13 May 2014 - 17 May 2014
			numNormalAttr = r.sizeOfAttrArray() - numTJoinAttrs - numNewAttr;
			
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
		// add keys and distribute the normal attributes of rel
		m.addTargetRel(r);
		//MN BEGIN - 13 May 2014
		targetReuse = true;
		//MN END 
		
		if (!r.isSetPrimaryKey())
			fac.addPrimaryKey(r.getName(), joinAttPos, false);

		// adapt number of normal attributes used (copied to target) per source rel
		//MN test: does it always produce the correct result? - 5 May 2014 - 13 May 2014
		int numPerSrcRel = (r.sizeOfAttrArray() - numNewAttr) / numOfTables;
		int usedAttrs = 0;
		//MN added the following line! - 5 May 2014
		numOfUseAttrs = new int [numOfTables];
		for(int i = 0; i < numOfTables; i++) {
			numOfUseAttrs[i] = (numPerSrcRel > (numOfAttributes[i] - getNumJoinAttrs(i))) 
					? (numOfAttributes[i] - getNumJoinAttrs(i)) : numPerSrcRel;
					//usedAttrs += numOfUseAttrs[i];
		}
		//MN I don't get it! I need to ask Patricia - 5 May 2014
		//numOfUseAttrs[numOfTables - 1] += numNormalAttr - usedAttrs;
		
		return true;
	}
	
	@Override
	protected void genTargetRels() throws Exception {
		String targetName = randomRelName(0);
		List<String> attrs = new ArrayList<String> ();
		//MN BEGIN
		List<String> attrsType = new ArrayList<String> ();
		//MN END
		int numNormalAttrs;
		
		// first copy normal attributes
		for(int i = 0; i < numOfTables; i++) {
			int numAtt = getNumNormalAttrs(i);
			for(int j = 0; j < numAtt; j++){
				attrs.add(m.getAttrId(i, j, true));
				//MN BEGIN - 8 May 2014
				attrsType.add(m.getSourceRels().get(i).getAttrArray(j).getDataType());
				//MN END
			}
		}
		
		numNormalAttrs = attrs.size();
		
		// then copy join attributes
		if (jk == JoinKind.STAR) {
			for(int i = 1; i < numOfTables; i++) {
				int offset = getNumNormalAttrs(i);
				for(int j = 0; j < numOfJoinAttributes; j++){
					attrs.add(m.getAttrId(i, j + offset, true));
					//MN BEGIN - 8 May 2014
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
					//MN BEGIN - 8 May 2014
					attrsType.add(m.getSourceRels().get(i).getAttrArray(j + offset).getDataType());
					//MN END
				}
			}
		}
		
		// calculate the total number of attributes (so we know the position of the new attributes)
		int numTotalAttrs = numNormalAttrs + numOfJoinAttributes;
//		for(int j = 0; j < numOfTables; j++)
//			numTotalAttrs += getNumNormalAttrs(j);
		
		// create random names for the added attrs
		for (int i = numTotalAttrs; i < numTotalAttrs + numNewAttr; i++){
			attrs.add(randomAttrName(0, i));
			//MN BEGIN - 8 May 2014
			attrsType.add("TEXT");
			//MN END
		}
		
		//MN modified the following line- 8 May 2014
		//MN begin
		fac.addRelation(getRelHook(0), targetName, attrs.toArray(new String[] {}), attrsType.toArray(new String[] {}), false);
		//MN end
		
		// add PK on join attributes
		fac.addPrimaryKey(targetName, CollectionUtils.createSequence(numNormalAttrs, getTargetNumJoinAttrs()), false);
	}
	
	protected int getTargetNumJoinAttrs () {
		return numOfJoinAttributes * (numOfTables - 1);
	}
	
	//MN added support for source reusability (this is the same as chooseSourceRels of Merge) - 5 May 2014
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
					//MN BEGIN has not tested yet - 11 May 2014
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
    
    

	@Override
	protected void genMappings() throws Exception {
		MappingType m1 = fac.addMapping(m.getCorrs());
		String[][] vars = new String[numOfTables][];
		String[] targetVars;
		int offset;
		
		// depending on whether they are first or second order tgds we need to allocate less space for the vars
		// (since in the actual mapping, SOtgds will use an SKFunction atom instead of a Var atom)
		targetVars = new String[m.getNumRelAttr(0, false) - numNewAttr];
		
		// add foreach atoms for the the source fragments
		offset = m.getNumRelAttr(0, true);
		vars[0] = fac.getFreshVars(0, offset);
		fac.addForeachAtom(m1, 0, vars[0]);
		
		// each table get fresh vars for its free and join attributes
		// the fk vars are takes from the join attributes they reference
		if (log.isDebugEnabled()) {log.debug("Relation 0 is of size " + numOfAttributes[0]);};
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
				if (log.isDebugEnabled()) {log.debug("Relation i is " + i  + " of size " + numOfAttributes[i] + ", " + numFreshVars + " , " + numOfJoinAttributes);};
				// PRG FIXED CHAIN JOIN PROBLEMS - August 30, 2012
				// fkVars = Arrays.copyOfRange(vars[i - 1], numFreshVars, 
				//		numFreshVars + numOfJoinAttributes);
				// PRG FIXED Foreach Source Expression by correcting fromIndex - Sep 4, 2012
				// int fromIndex = vars[i-1].length - numOfJoinAttributes;
				int fromIndex = vars[i-1].length - getNumJoinAttrs(i-1); 
				int toIndex = (numOfJoinAttributes == 1 ? fromIndex + 1 : fromIndex + numOfJoinAttributes);
				if (log.isDebugEnabled()) {log.debug("Copying Chain Join Variables from  " + fromIndex  + " to " + toIndex);};
				fkVars = Arrays.copyOfRange(vars[i - 1], fromIndex, toIndex);
			}
			vars[i] = CollectionUtils.concat(freeVars, fkVars);
			
			fac.addForeachAtom(m1, i, vars[i]);
			
			if (log.isDebugEnabled()) {log.debug("For Each Clause is: " + m1.getForeach().toString());};
		}
		
		// generate an array of vars for the target
		// first we add vars for the free attributes of all table
		// then we add the join attribute vars
		offset = 0;
		for(int i = 0; i < numOfTables; i++) {
			int numVars = vars[i].length;
			numVars = getNumNormalAttrs(i);
			
			System.arraycopy(vars[i], 0, targetVars, offset, numVars);
			offset += numVars;
		}
		
		int totalVars = offset;
		
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
		
		switch (mapLang) 
		{
			// target tables gets fresh vars for the new attrs
			case FOtgds:
				// add the new variables to the targetVar array
//				System.arraycopy(fac.getFreshVars(offset, numNewAttr), 0, targetVars, offset, numOfJoinAttributes);
				fac.addExistsAtom(m1, 0, CollectionUtils.concat(targetVars, fac.getFreshVars(offset, numNewAttr)));
				break;
			// target gets all the src variables + skolem terms for the new attrs
			case SOtgds:
				fac.addEmptyExistsAtom(m1, 0);
				fac.addVarsToExistsAtom(m1, 0, targetVars);
				SkolemKind sk1 = sk;
				if(sk == SkolemKind.VARIABLE)
					sk1 = SkolemKind.values()[_generator.nextInt(3)];
				generateSKs(m1, sk1, totalVars, vars, targetVars);
				break;
		}	
	}
	
	private void generateSKs(MappingType m1, SkolemKind sk, int totalVars, String[][] vars, String[] targetVars) 
	{	
		if (log.isDebugEnabled()) {log.debug("MERGE ADD - Method generateSKs() with totalVars = " + totalVars + " and Num of New Skolems = " + numNewAttr);};
		for (int i = 0; i < numNewAttr; i++)
		{
			// in KEY mode we use the join attributes as the skolem arguments
			if (sk == SkolemKind.KEY)
			{
				if (log.isDebugEnabled()) {log.debug("--- SKOLEM MODE = KEY ---");};

				// PRG FIXED BUG - ArrayCopy was always invoked with Null Destiny Array - Sep 5, 2012
				// Initially we thought about allocating a max number of elements, as determined by the length of targetVars
				// But then we decided to compute the exact number as follows: numOfJoinAttributes*(numOfTables - 1)
				// This formula seems to work for both STAR and CHAIN Joins
				// String[] argVars = null;
				// int offset = totalVars;
				String[] argVars = new String[numOfJoinAttributes*(numOfTables - 1)];
				int offset = 0;

				// star join, add join attribute vars from first table
				if (jk == JoinKind.STAR) {
					int start = getNumNormalAttrs(0);
					for(int j = 1; j < numOfTables; j++) {
						System.arraycopy(vars[0], start, argVars, offset, numOfJoinAttributes);
						offset += numOfJoinAttributes;
						start += numOfJoinAttributes;
					}
				}

				// chain join, take join attribute vars from each table
				if (jk == JoinKind.CHAIN) {
					for(int j = 0; j < numOfTables - 1; j++) {
						int start = getNumNormalAttrs(j);
						if (log.isDebugEnabled()) {log.debug("Relation j is " + j  + " of size " + numOfAttributes[j] + ", Num Join Attrs: " + numOfJoinAttributes);};
						if (log.isDebugEnabled()) {log.debug("Copying Chain Join Variables from  " + start  + " to " + numOfJoinAttributes);};
						System.arraycopy(vars[j], start, argVars, offset, numOfJoinAttributes);
						offset += numOfJoinAttributes;
					}
				}
				
				if (log.isDebugEnabled()) {log.debug("Key Argument Set: " + Arrays.toString(argVars));};
				fac.addSKToExistsAtom(m1, 0, argVars);
			}

			else if (sk == SkolemKind.RANDOM)
			{
				if (log.isDebugEnabled()) {log.debug("--- SKOLEM MODE = RANDOM ---");};
 
				// Generate a random number of args for this Skolem (Uniform distribution between 0 (inclusive) and totalVars (exclusive))
				int numArgsForSkolem = Utils.getRandomUniformNumber(_generator, totalVars);
				// Ensure we generate at least a random argument set of size > 0
				numArgsForSkolem = (numArgsForSkolem == 0 ? totalVars : numArgsForSkolem);

				if (log.isDebugEnabled()) {log.debug("Initial randomly picked number of arguments: " + numArgsForSkolem);};
				
				// Generate a random argument set		
				Vector<String> randomArgs = Utils.getRandomWithoutReplacementSequence(_generator, numArgsForSkolem, model.getAllVarsInMapping(m1, true));
				
				if (randomArgs.size() == targetVars.length) {
					if (log.isDebugEnabled()) {log.debug("Random Argument Set [using ALL instead]: " + randomArgs.toString());};
				}
				else {
					if (log.isDebugEnabled()) {log.debug("Random Argument Set: " + randomArgs.toString());};
				}
				fac.addSKToExistsAtom(m1, 0, Utils.convertVectorToStringArray(randomArgs));
				
				// PRG Replaced the following fragment of code as it does not guarantee convergence - Sep 21, 2012
				/*
				int numArgsForSkolem = Utils.getRandomNumberAroundSomething(_generator, totalVars/2, totalVars/2);
				// Adjust random position value just in case it falls outside limits
				numArgsForSkolem = (numArgsForSkolem >= totalVars) ? totalVars : numArgsForSkolem;
				
				if (log.isDebugEnabled()) {log.debug("Initial randomly picked number of arguments: " + numArgsForSkolem);};
				
				// generate the random vars to be arguments for the skolem
				Vector<String> randomVars = new Vector<String> ();
				
				int MaxRandomTries = 30;
				int attempts = 0;
				boolean ok = false;
				
				for (int k = 0; k < numArgsForSkolem; k++) {

					while (!ok & attempts++ < MaxRandomTries) {
						
						// Get random position 
						int pos = Utils.getRandomNumberAroundSomething(_generator, totalVars/2, totalVars/2);
						// Adjust random position value just in case it falls outside limits
						pos = (pos >= totalVars) ? totalVars-1 : pos;
						
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
					if (log.isDebugEnabled()) {log.debug("Random Argument Set: " + randomVars.toString());};
					fac.addSKToExistsAtom(m1, 0, Utils.convertVectorToStringArray(randomVars));
					
				} else  { // If not, just use all source attributes for the sake of completion
					
					if (log.isDebugEnabled()) {log.debug("Random Argument Set [using ALL instead]: " + Arrays.toString(targetVars));};
					fac.addSKToExistsAtom(m1, 0, targetVars);
					
				}
				*/
				            
			}
			else { // SkolemKind.ALL
			
				if (log.isDebugEnabled()) {log.debug("--- SKOLEM MODE = ALL ---");};
				if (log.isDebugEnabled()) {log.debug("ALL Argument Set: " + Arrays.toString(targetVars));};

				fac.addSKToExistsAtom(m1, 0, targetVars);
			}
		}
	}
	
	@Override
	protected void genTransformations() throws Exception {
		Query q;
		String creates = m.getRelName(0, false);
		String mapping = m.getMapIds()[0];
		
		q = genQueries();
		q.storeCode(q.toTrampStringOneMap(mapping));
		q = addQueryOrUnion(creates, q);
		
		fac.addTransformation(q.getStoredCode(), m.getMapIds(), creates);
		//MN BEGIN 16 August 2014
		fac.addTransformation("", m.getMapIds(), creates);
		//MN END
	}
	
	private SPJQuery genQueries() throws Exception {
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
        for (int tbli = 1, tblimax = numOfTables; ((tbli < tblimax) && (jk == JoinKind.STAR)); tbli++)
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
        for (int tbli = 1, tblimax = numOfTables; ((tbli < tblimax) && (jk == JoinKind.STAR)); tbli++)
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
        
        // retrieve skolems for the new attributes from what was generated in genMappings 
        // - this is basically just a way of cloning the existing skolem
        for(int i = 0 ; i < numNewAttr; i++) {
        	int attPos = i + getTotalNumNormalAttrs() +
        			(jk.equals(JoinKind.CHAIN) 
        				? numOfJoinAttributes * (numOfTables - 1) //TODO check
        				: numOfJoinAttributes * (numOfTables - 1)
        				);
        	String attName = m.getAttrIds(0, false)[attPos];
        	MappingType m1 = m.getMaps().get(0);
        	int numArgs = 0;
        	String skName;
        	
        	if (mapLang.equals(MappingLanguageType.SOtgds)) {
        		SKFunction sk = m.getSkolemFromAtom(m1, false, 0, attPos);
        		skName = sk.getSkname();
        		numArgs = sk.sizeOfVarArray();
        	}
        	else {
        		numArgs = numOfJoinAttributes; //TODO check other cases here
        		skName = fac.getNextId("SK");
        	}

        	vtools.dataModel.expression.SKFunction stSK = 
        			new vtools.dataModel.expression.SKFunction(skName);

        	// this works because the keys are always the first attributes 
        	for(int j = 0; j < numArgs; j++) {		// TODO need to get the right relation + attribute for each argument to skolem  
        		String sAttName = m.getAttrId(0, j, false);
        		Projection att = new Projection(new Variable("X0"), sAttName);
        		stSK.addArg(att);
        	}

        	sel.add(attName, stSK);
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
		return ScenarioName.MERGEADD;
	}
}
