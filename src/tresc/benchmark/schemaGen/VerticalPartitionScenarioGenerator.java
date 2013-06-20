package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SKFunction;

import tresc.benchmark.Constants.JoinKind;
import tresc.benchmark.Constants.MappingLanguageType;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Constants.SkolemKind;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;

// PRG Enhanced VERTICAL PARTITION to handle Optional Source Keys based on ConfigOptions.PrimaryKeySize - Sep 18, 2012
// PRG REMOVED HardCoded Skolemization Mode (SkolemKind.ALL) and ADDED dynamic Skolemization Modes (i.e. KEY, ALL and RANDOM) - Sep 18, 2012
// PRG FIXED Infinite Loop Bug in method generateSKs(), case SkolemKind.RANDOM  - Sep 18, 2012
// PRG FIXED Omission, must generate source relation with at least 2 elements (this was causing empty Skolem terms and PK FDs with empty RHS!)- Sep 19, 2012
// PRG Systematically using "Random Without Replacement" strategy/algorithm when dealing with SkolemKind.RANDOM mode everywhere! - Sep 21, 2012

// BORIS TO DO - Revise method genQueries() as it might be out of sync now - Sep 21, 2012


// very similar to merging scenario generator, with source and target schemas swapped
/**
 * 
 * @author lord_pretzel
 * 
 * DESCRIPTION OF SCENARIO:
 *******************************
 *
 * Vertically partition a single source relation into multiple target relations. 
 * Each target relation gets a subset of the attributes of the source. The 
 * attributes are split evenly between the target relations (the last target 
 * relation gets the remainder). Target relations are connected through their 
 * primary key which is added as an additional last attribute. 
 * 
 * ASSUMPTIONS:
 *******************************
 *
 *	1) Each target tables primary key is the last attribute
 *  2) Source attributes are split evenly amongst the target tables. The last
 *     target table gets the remaining attribute that cannot be split evenly.
 * 
 * DESCRIPTION OF PARAMETERS:
 *******************************
 * 
 * numOfSrcTblAttr: Number of total attributes in the single source relation for this scenario. 
 * 					At least 2 (to be able to create two target relations)
 * numOfTgtTables: Number of target tables in the target schema for this scenario. At least 2.
 * attsPerTargetRel: Number of attributes in each target table. The last one could be larger than
 * 					number since it contains the remainders.
 * attrRemainder: Number of remainders of the division of numOfSrcTblAttr and numOfTgtTables. They
 *  				will be added into the last table.
 * 
 * EXAMPLE SCENARIO:
 *******************************
 * numOfSrcTblAttr = 3, keySize = 0, numOfTgtTables = 2, attsPerTargetRel = 1, attrRemainder = 1
 * 
 * Source Schema: R(A,B,C) no primary key
 * Target Schema: S(A,D) and T(B,C,E) with primary keys (D) and (E)
 * Correspondences: C1: R.A -> S.A, C2: R.B -> T.B, C3: R.C -> T.C
 * Mapping: R(a,b,c) -> S(a,d) and T(b,c,d)
 * Transformation:  
 * 
 */
public class VerticalPartitionScenarioGenerator extends AbstractScenarioGenerator {
	private static final int MAX_NUM_TRIES = 10;
	private JoinKind jk;
	private int numOfSrcTblAttr;
	private int numOfTgtTables;
	private int attsPerTargetRel;
	private int attrRemainder;
	// PRG REMOVED Hard coded Skolemization Mode - Sep 18, 2012
    // private SkolemKind sk = SkolemKind.ALL;
    private SkolemKind sk;
    // PRG ADDED instance variable skIdRandomArgs and comments below - Sep 18, 2012
	// VP only needs to generate 1 Skolem Function; skId keeps track of its Skolem Id
    private String skId;
    // skIdRandomArgs keeps track of the randomly generated argument set (only used for SkolemKind.RANDOM mode)
    private Vector<String> skIdRandomArgs;
	    
	// PRG ADDED to Support Optional Source Keys - Sep 18, 2012
	private int keySize;
	
    public VerticalPartitionScenarioGenerator()
    {
        ;
    }
    
    /**
     * 
     */
    protected void initPartialMapping() {
    	super.initPartialMapping();
    	
        numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
            numOfElementsDeviation);
        
        // PRG ADD - Generate at least a source relation of 3 elements - Sep 19, 2012
        numOfSrcTblAttr = (numOfSrcTblAttr > 2 ? numOfSrcTblAttr : 2);
        
        jk = JoinKind.values()[joinKind];
        if (jk == JoinKind.VARIABLE)
        {
            int tmp = Utils.getRandomNumberAroundSomething(_generator, 0, 1);
            if (tmp < 0)
                jk = JoinKind.STAR;
            else jk = JoinKind.CHAIN;
        }//decide which join kind to implement
        
		// PRG ENHANCED VERTICAL PARTITION according to Configuration Options - Sep 18, 2012
		// Reading ConfigOptions.PrimaryKeySize and ConfigOptions.SkolemKind
		keySize = Utils.getRandomNumberAroundSomething(_generator, primaryKeySize, primaryKeySizeDeviation);
		sk = SkolemKind.values()[typeOfSkolem];
		// adjust keySize as necessary with respect to number of source table attributes
		// NOTE: we are not strictly enforcing a source key for VERTICAL PARTITION, unless SkolemKind.KEY explicitly requested
		keySize = (keySize > numOfSrcTblAttr - 2) ? numOfSrcTblAttr - 2 : keySize;
		if (sk == SkolemKind.KEY)
			keySize = (keySize > 0) ? keySize : 1;
		
        numOfTgtTables = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements,
                numOfSetElementsDeviation);    	
        numOfTgtTables = (numOfTgtTables <= numOfSrcTblAttr) ? numOfTgtTables : numOfSrcTblAttr;
        
        attsPerTargetRel = numOfSrcTblAttr / numOfTgtTables;
        attrRemainder = numOfSrcTblAttr % numOfTgtTables;        
    }

	
    
    @Override
	protected boolean chooseSourceRels() throws Exception {
    	RelationType r = null;
    	boolean ok = false;
    	int tries = 0;
    	
    	while(!ok && tries++ < MAX_NUM_TRIES) {
    		r = getRandomRel(true, 2);
    		if (r == null)
    			break;
    		ok = true;
    		if (r.isSetPrimaryKey()) {	
    			int[] keyPos = model.getPKPos(r.getName(), true);
    			keySize = keyPos.length;
    		}
    		else {
    			keySize = 0;
    		}
    	}
    	
    	// did not find suitable relation
    	if (r == null)
    		return false;
    	// adapt fields
    	else {
    		m.addSourceRel(r);
    		
//    		// create PK if necessary
//    		if (!r.isSetPrimaryKey())
//    			fac.addPrimaryKey(r.getName(), 0, true);
    		
    		numOfSrcTblAttr = r.sizeOfAttrArray();
    		
    		attsPerTargetRel = numOfSrcTblAttr/numOfTgtTables;    	
    		attrRemainder = numOfSrcTblAttr % numOfTgtTables; 		
   		
    		return true;
    	}
	}
    
    
    
	@Override
	protected void genSourceRels() throws Exception {
		String sourceRelName = randomRelName(0);
		String[] attNames = new String[numOfSrcTblAttr];
		String hook = getRelHook(0);
		
		// for (int i = 0; i < numOfSrcTblAttr; i++)
		// 	attNames[i] = randomAttrName(0, i);
		
		// PRG ADDED Generation of Source Key Elements when keySize > 0
		String[] keys = new String[keySize];

		for (int i = 0; i < numOfSrcTblAttr; i++) {
			String attrName = randomAttrName(0,i);
			if (i < keySize) {
				attrName = attrName + "ke" + i;
				keys[i] = attrName;
			}
			attNames[i] = attrName;
		}
		
		RelationType sRel = fac.addRelation(hook, sourceRelName, attNames, true);
		
		// PRG ADDED - DO NOT ENFORCE KEY UNLESS EXPLICITLY REQUESTED - Sep 18, 2012
		if (keySize > 0 )
			fac.addPrimaryKey(sourceRelName, keys, true);
		
		m.addSourceRel(sRel);
		
	}

	@Override
	protected boolean chooseTargetRels() throws Exception {
		RelationType cand = null;
		int tries = 0;
		int numAttrs = 0;
		keySize = 0;
		List<RelationType> rels = new ArrayList<RelationType> (numOfTgtTables);
		
		// first one
		while (tries++ < MAX_NUM_TRIES && rels.size() == 0) {
			cand = getRandomRel(false, 2);
			if (relOk(cand)) {
				rels.add(cand);
				break;
			}
		}
		
		// didn't find one? generate target relations
		if (rels.size() == 0)
			return false;
		
		numAttrs = cand.sizeOfAttrArray();

		// find additional relations with the same number of attributes 
		// and no key or the first attr as key
		while (tries++ < MAX_NUM_TRIES * numOfTgtTables && cand != null 
				&& rels.size() < numOfTgtTables - 1) {
			cand = getRandomRelWithNumAttr(false, numAttrs);//Why we don't use getRandomRel again here? What's the difference of them.
			//Because we require exact number of attr.
			if (relOk(cand))
				rels.add(cand);
		}
		
		//TODO code that finds the last one
		if (rels.size() == numOfTgtTables - 1) {
			while (tries++ < MAX_NUM_TRIES * numOfTgtTables && cand != null 
					&& rels.size() < numOfTgtTables) {
				cand = getRandomRelWithNumAttr(false, numAttrs);//Why we don't use getRandomRel again here? What's the difference of them.
				//Because we require exact number of attr.
				if (relOk(cand))
					rels.add(cand);
			}
		}
			
		// check that we have numOfTgdTables - 1
		
		// create additional target relations
		for (int i = 0; i < rels.size(); i++)
			m.addTargetRel(rels.get(i));
		for (int i = rels.size(); i < numOfTgtTables; i++) {
			RelationType r = createFreeRandomRel(i, numAttrs);
			rels.add(r);
			fac.addRelation(getRelHook(i), r, false);
		}
		
		// create primary keys
		for (RelationType r: rels)
			if (!r.isSetPrimaryKey())
				fac.addPrimaryKey(r.getName(), r.sizeOfAttrArray() - 1, false);
		
		// adapt local parameters
		attsPerTargetRel = numAttrs - 1;//Why minus one?
		attrRemainder = 0; //TODO

		// adapt numOfSrcTblAttrs
		numOfSrcTblAttr = attsPerTargetRel * numOfTgtTables + attrRemainder;

		return true;
	}
	
	private boolean relOk (RelationType r) throws Exception {
		if (r == null)
			return false;
		if (r.isSetPrimaryKey()) {
			int[] pkPos = model.getPKPos(r.getName(), false);
			if (pkPos.length == 1 & pkPos[0] == r.sizeOfAttrArray() - 1)
				return true;
			return false;
		}
		// no PK? we are fine
		else
			return true;
	}
	
	@Override
	protected void genTargetRels() throws Exception {
        String[] attrs;
		String[] srcAttrs = m.getAttrIds(0, true);
		
		String joinAttName = randomAttrName(0, 0) + "JoinAttr";
        String joinAttNameRef = joinAttName + "Ref";
		
        for (int i = 0; i < numOfTgtTables; i++)
        {
        	int offset = i * attsPerTargetRel;
        	String trgName = randomRelName(i);
        	String hook = getRelHook(i);
        	int attrNum = (i < numOfTgtTables - 1) ? attsPerTargetRel:
        		attsPerTargetRel + attrRemainder;
  //TODO according to patricia, the chain and star joins should be the same here, because single
  // skolem on all input attributes
        	
//        	int fkAttrs = ((jk == JoinKind.CHAIN && 
//        			(i != 0 && i != numOfTgtTables - 1)) 
//        			? 2 : 1);
        	int fkAttrs = 1;
        	int attWithFK = attrNum + fkAttrs;
        	attrs = new String[attWithFK];
        
        	// create normal attributes for table (copy from source)
            for (int j = 0; j < attrNum; j++)
            	attrs[j] = srcAttrs[offset + j];
            
            // create the join attributes
            // for star join the first one has the join attribute and the following ones 
            // have the join reference (FK)
            if (jk == JoinKind.STAR) {
            	if (i == 0)//TODO check
            		attrs[attrs.length - 1] = joinAttName;
            	else
            		attrs[attrs.length - 1] = joinAttNameRef;
            // for chain join each one has one join attribute             	
//            OLD	has a join and join ref to the previous
//             thus, the first does not have a ref and the last one does not have a join attr
            } else { // chain
//            	if (i == 0)
            		attrs[attrs.length - 1] = joinAttName;
//            	else if (i == numOfTgtTables - 1)
//            		attrs[attrs.length - 1] = joinAttNameRef;
//            	else {
//            		attrs[attrs.length - 2] = joinAttName;
//            		attrs[attrs.length - 1] = joinAttNameRef;
//            	}
            }
            
            fac.addRelation(hook, trgName, attrs, false);
            
            if (jk == JoinKind.STAR) 
            {
            	if (i == 0)//TODO check
            		fac.addPrimaryKey(trgName, joinAttName, false);
            	else 
            		fac.addPrimaryKey(trgName, joinAttNameRef, false);
            // for chain join each one has a join and join ref to the previous
            // thus, the first does not have a ref and the last one does not have a join attr
            } 
            else 
            { // chain
//            	if (i == 0)
            		fac.addPrimaryKey(trgName, joinAttName, false);
//            	else if (i == numOfTgtTables - 1)
//            		fac.addPrimaryKey(trgName, joinAttNameRef, false);
//            	else 
//            		fac.addPrimaryKey(trgName, new String[] {joinAttName, joinAttNameRef}, false);
            }
        }
        
        addFKs();
	}

	private void addFKs() {
		if (jk == JoinKind.STAR) {
			for(int i = 1; i < numOfTgtTables; i++) {
				int toA = m.getNumRelAttr(0, false) - 1;
				int fromA = m.getNumRelAttr(i, false) - 1;
				addFK(i, fromA, 0, toA, false);
				addFK(0, toA, i, fromA, false);
			}
		} else { // chain
//			int toA = m.getNumRelAttr(1, false) - 1;
//			int fromA = m.getNumRelAttr(0, false) - 1;
//			addFK(0, fromA, 1, toA, false);
//			addFK(1, toA, 0, fromA, false);
			for(int i = 0; i < numOfTgtTables - 1; i++) {
				int toA = m.getNumRelAttr(i + 1, false) - 1;
				int fromA = m.getNumRelAttr(i, false) - 1;
				addFK(i, fromA, i+1, toA, false);
				addFK(i+1, toA, i, fromA, false);
			}
		}
	}

	@Override
	protected void genMappings() throws Exception {
		MappingType m1 = fac.addMapping(m.getCorrs());
		String[] keyVars;
		
		// source table gets fresh variables
		fac.addForeachAtom(m1, 0, fac.getFreshVars(0, numOfSrcTblAttr));
		keyVars = fac.getFreshVars(numOfSrcTblAttr, 1);
		
		switch (mapLang) 
		{
			case FOtgds:
				for(int i = 0; i < numOfTgtTables; i++) {
					int offset = i * attsPerTargetRel;
		        	int numAtts = (i < numOfTgtTables - 1) ? attsPerTargetRel :
		    				attsPerTargetRel + attrRemainder;
		        	
		        	fac.addExistsAtom(m1, i, CollectionUtils.concat(fac.getFreshVars(offset, numAtts), keyVars));
				}
				break;
				
			case SOtgds:
				
				SkolemKind sk1 = sk;
				if(sk == SkolemKind.VARIABLE)
					sk1 = SkolemKind.values()[_generator.nextInt(4)];
				
				for(int i = 0; i < numOfTgtTables; i++) {
					int offset = i * attsPerTargetRel;
		        	int numAtts = (i < numOfTgtTables - 1) ? attsPerTargetRel :
		    				attsPerTargetRel + attrRemainder;
		        	
		        	//RelAtomType atom = fac.addEmptyExistsAtom(m1, 0);
		        	fac.addEmptyExistsAtom(m1, i);
		        	fac.addVarsToExistsAtom(m1, i, fac.getFreshVars(offset, numAtts));
		        	// PRG FIX BUG - The selection can't be done here or else it affects record keeping (skId and skIdRandomArgs) - Sep 18, 2012
		        	// SkolemKind sk1 = sk;
					// if(sk == SkolemKind.VARIABLE)
					//  	sk1 = SkolemKind.values()[_generator.nextInt(4)];
		        	generateSKs(m1, i, offset, numAtts, sk1);
				}
				break;
		}
	}
	
	// PRG Rewrote method generateSKs() to permit dynamic Skolemization Modes - Sep 18, 2012
	// PRG Systematically using "Random Without Replacement" strategy/algorithm when dealing with SkolemKind.RANDOM mode everywhere! - Sep 21, 2012
	
	private void generateSKs(MappingType m1, int rel, int offset, int numAtts, SkolemKind sk) {
		int numArgsForSkolem = numOfSrcTblAttr;

		if (log.isDebugEnabled()) {log.debug("VP - Method generateSKs() with totalVars = " + numOfSrcTblAttr + " and Num of New Skolems = 1");};
		
		// if we are using a key in the original relation then we base the skolem on just that key	
		if (sk == SkolemKind.KEY) {
			
			// We always generate the same Skolem function (i.e. same id, as recorded by instance variable "skId"),
			// using the source key as argument set
			if (rel == 0) {
				if (log.isDebugEnabled()) {log.debug("--- SKOLEM MODE = KEY ---");};
			    if (log.isDebugEnabled()) {log.debug("Key Argument Set: " + Arrays.toString(fac.getFreshVars(0, keySize)));};
				skId = fac.addSKToExistsAtom(m1, rel, fac.getFreshVars(0, keySize));
			}
			else
				fac.addSKToExistsAtom(m1, rel, fac.getFreshVars(0, keySize), skId);		
		}
				
		else if (sk == SkolemKind.RANDOM) {
			
			if (rel == 0) {
				
				if (log.isDebugEnabled()) {log.debug("--- SKOLEM MODE = RANDOM ---");};
				
				// PRG NOTE: We must save the only generated Skolem function (skId and skIdRandomArgs values)for following method invocations
				
				// Generate a random number of args for this Skolem (Uniform distribution between 0 (inclusive) and totalVars (exclusive))
				numArgsForSkolem = Utils.getRandomUniformNumber(_generator, numOfSrcTblAttr);
				// Ensure we generate at least a random argument set of size > 0
				numArgsForSkolem = (numArgsForSkolem == 0 ? numOfSrcTblAttr : numArgsForSkolem);

				if (log.isDebugEnabled()) {log.debug("Initial randomly picked number of arguments: " + numArgsForSkolem);};
				
				// Generate a random argument set
				skIdRandomArgs = Utils.getRandomWithoutReplacementSequence(_generator, numArgsForSkolem, model.getAllVarsInMapping(m1, true));
				
				if (skIdRandomArgs.size() == numOfSrcTblAttr) {
					if (log.isDebugEnabled()) {log.debug("Random Argument Set [using ALL instead]: " + skIdRandomArgs.toString());};
				}
				else {
					if (log.isDebugEnabled()) {log.debug("Random Argument Set: " + skIdRandomArgs.toString());};
				}
				skId = fac.addSKToExistsAtom(m1, 0, Utils.convertVectorToStringArray(skIdRandomArgs));
				
				// PRG Replaced the following fragment of code as it does not guarantee convergence - Sep 21, 2012
				/*
				numArgsForSkolem = Utils.getRandomNumberAroundSomething(_generator, numOfSrcTblAttr / 2, numOfSrcTblAttr / 2);
				numArgsForSkolem = (numArgsForSkolem >= numOfSrcTblAttr) ? numOfSrcTblAttr : numArgsForSkolem;
				
				if (log.isDebugEnabled()) {log.debug("Initial randomly picked number of arguments: " + numArgsForSkolem);};
				
				skIdRandomArgs = new Vector<String>();
				
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
						if (skIdRandomArgs.indexOf(fac.getFreshVars(pos, 1)[0]) == -1) {
							skIdRandomArgs.add(fac.getFreshVars(pos, 1)[0]);
							ok = true;
						    break;
						}
						
					}
					// Plainly give up after 30 tries. If so, we may end up with an argument set with fewer variables.
					
				}
				// Make sure we were able to generate at least 1 variable from randomArgs. If not, we use all source attributes
				if (skIdRandomArgs.size() > 0) {
				
					Collections.sort(skIdRandomArgs);
					if (log.isDebugEnabled()) {log.debug("Random Argument Set: " + skIdRandomArgs.toString());};
					skId = fac.addSKToExistsAtom(m1, 0, Utils.convertVectorToStringArray(skIdRandomArgs));
					
				} else  { // If not, just use all source attributes for the sake of completion
					if (log.isDebugEnabled()) {log.debug("Random Argument Set [using ALL instead] : " + Arrays.toString(fac.getFreshVars(0, numOfSrcTblAttr)));};
					skId = fac.addSKToExistsAtom(m1, 0, fac.getFreshVars(0, numOfSrcTblAttr));
				}
				*/
			
			}
			else { 
				
				// Simply add the previously generated Skolem Function to any other relation except number 0
				fac.addSKToExistsAtom(m1, rel, Utils.convertVectorToStringArray(skIdRandomArgs), skId);	
			}
				
		}
		else { // SkolemKind.ALL
			if (rel == 0) {
				if (log.isDebugEnabled()) {log.debug("--- SKOLEM MODE = ALL ---");};
				if (log.isDebugEnabled()) {log.debug("ALL Argument Set: " + Arrays.toString(fac.getFreshVars(0, numArgsForSkolem)));};
				skId = fac.addSKToExistsAtom(m1, rel, fac.getFreshVars(0, numArgsForSkolem));
			}
				
			else
				fac.addSKToExistsAtom(m1, rel, fac.getFreshVars(0, numArgsForSkolem), skId);
			
		}
		
	}
	
	@Override
	protected void genTransformations() throws Exception {
		SPJQuery q;
		SPJQuery genQuery = genQuery(new SPJQuery());
		
		for(int i = 0; i < numOfTgtTables; i++) {
			String creates = m.getTargetRels().get(i).getName();
			q = (SPJQuery) genQuery.getSelect().getTerm(i);
			
			fac.addTransformation(q.toTrampString(m.getMapIds()[0]), m.getMapIds(), creates);
		}
	}
	
	private SPJQuery genQuery(SPJQuery generatedQuery) throws Exception {
		String sourceRelName = m.getSourceRels().get(0).getName();
		SPJQuery[] queries = new SPJQuery[numOfTgtTables];
		MappingType m1 = m.getMaps().get(0);
		
		String joinAttName;
		String joinAttNameRef;
		
		// join attrs different for star and chain join.
		if (jk == JoinKind.STAR) {
			joinAttName = m.getAttrId(0, m.getNumRelAttr(0, false) - 1, false);
            joinAttNameRef = m.getAttrId(1, m.getNumRelAttr(1, false) - 1, false);
            
            //TODO check what they do there really
		}
		else {
			int numAttr = m.getNumRelAttr(0, false);
			joinAttName = m.getAttrId(0, numAttr - 1, false);
            joinAttNameRef = m.getAttrId(0, numAttr - 2, false);
		}

		// gen query
		for(int i = 0; i < numOfTgtTables; i++) {
			String targetRelName = m.getTargetRels().get(i).getName();
			int numAttr = (i < numOfTgtTables - 1) ? attsPerTargetRel :
					attsPerTargetRel + attrRemainder;
			
			// gen query for the target table
			SPJQuery q = new SPJQuery();
			queries[i] = q;
	        q.getFrom().add(new Variable("X"), new Projection(Path.ROOT, sourceRelName));
	        generatedQuery.addTarget(targetRelName);
	        SelectClauseList sel = q.getSelect();
	        
	        for (int j = 0; j < numAttr; j++) {
	        	String trgAttrName = m.getAttrId(i, j, false);
				Projection att = new Projection(new Variable("X"), trgAttrName);
				sel.add(trgAttrName, att);
	        }
		}
		
		// add skolem function for join
		if (jk == JoinKind.STAR) {

			for(int i = 0; i < numOfTgtTables; i++) {
				SelectClauseList seli = queries[i].getSelect();
				String name;
				int numVar;
				int numAttr = (i < numOfTgtTables - 1) ? attsPerTargetRel : attsPerTargetRel + attrRemainder;

				if (mapLang.equals(MappingLanguageType.SOtgds)) {
					SKFunction sk = m.getSkolemFromAtom(m1, false, i, numAttr);
			 		name = sk.getSkname();
			 		numVar = sk.getVarArray().length;
            	}
            	else {
            		name = fac.getNextId("SK");
            		numVar = Utils.getRandomNumberAroundSomething(_generator, numOfSrcTblAttr / 2, numOfSrcTblAttr / 4);
            	}
		 		
		 		vtools.dataModel.expression.SKFunction stSK = new vtools.dataModel.expression.SKFunction(name);
		 			
		 		// this works because the key is always the first attribute 
		 		for(int k = 0; k < numVar; k++) {			
		 			String sAttName = m.getAttrId(0, k, true);
		 			Projection att = new Projection(new Variable("X"), sAttName);
		 			stSK.addArg(att);
		 		}
				
		 		if(i == 0)
		 			seli.add(joinAttName, stSK);
		 		else
		 			seli.add(joinAttNameRef, stSK);
			}
		}
		
        if (jk == JoinKind.CHAIN)
        {
            for (int i = 0; i < numOfTgtTables - 1; i++)
            {
            	String name;
            	int numAttr = (i < numOfTgtTables - 1) ? attsPerTargetRel : attsPerTargetRel + attrRemainder;
            	int numVar;
            	
            	if (mapLang.equals(MappingLanguageType.SOtgds)) {
			 		SKFunction sk = m.getSkolemFromAtom(m1, false, i, numAttr);
			 		name = sk.getSkname();
			 		numVar = sk.getVarArray().length;
            	}
            	else {
            		name = fac.getNextId("SK");
            		numVar = Utils.getRandomNumberAroundSomething(_generator, numOfSrcTblAttr / 2, numOfSrcTblAttr / 4);
            	}
            	
		 		vtools.dataModel.expression.SKFunction stSK = new vtools.dataModel.expression.SKFunction(name);
		 			
		 		for(int k = 0; k < numVar; k++) {			
		 			String sAttName = m.getAttrId(0, k, true);
		 			Projection att = new Projection(new Variable("X"), sAttName);
		 			stSK.addArg(att);
		 		}
            	
            	SelectClauseList sel1 = queries[i].getSelect();
                sel1.add(joinAttName, stSK);
                queries[i].setSelect(sel1);
                
                SelectClauseList sel2 = queries[i + 1].getSelect();
                sel2.add(joinAttNameRef, stSK);
                queries[i + 1].setSelect(sel2);
            }
        }
        
        // add the partial queries to the parent query
        // to form the whole transformation
        SelectClauseList pselect = pquery.getSelect();
        SelectClauseList gselect = generatedQuery.getSelect();
        for (int i = 0; i < numOfTgtTables; i++)
        {
            String tblTrgName = m.getRelName(i, false);
            pselect.add(tblTrgName, queries[i]);
            gselect.add(tblTrgName, queries[i]);
        }
        pquery.setSelect(pselect);
        generatedQuery.setSelect(gselect);
		return generatedQuery;
	} 

	@Override
	protected void genCorrespondences() {
        for (int i = 0; i < numOfTgtTables; i++)
        {
        	int offset = i * attsPerTargetRel;
        	int numAtts = (i < numOfTgtTables - 1) ? attsPerTargetRel :
    				attsPerTargetRel + attrRemainder;
        	
            for (int j = 0; j < numAtts; j++)
            	addCorr(0, offset + j, i, j);         
        }
	}
	
	@Override
	public ScenarioName getScenType() {
		return ScenarioName.VERTPARTITION;
	}
}
