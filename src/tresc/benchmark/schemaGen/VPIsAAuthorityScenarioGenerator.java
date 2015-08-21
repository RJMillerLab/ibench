package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.CorrespondenceType;
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
import vtools.dataModel.expression.Query;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;

//MN implemented new Vertical Partitioning - 23 June 2014
//MN limitation: keySize =1 - 23 June 2014
//MN only investigated genSource and genTarget - 23 June 2014
//MN the only difference between this scenario and vertical partitioning is in foreign keys - 23 June 2014
//MN ToDo: enhance chooseTargetRels to check existing foreign keys on reused target relations - 23 June 2014
//PRG RENAMED CLASS - Before was newVP, Now is VPIsAAuthorityScenarioGenerator - 16 Oct 2014
//MN Changes to VP Authority Scenario - 6 Feb 2015 (submitted to prg@cs via email on 10 Feb 2015, not committed as changes were done to an old java class)
//PRG - COMPLETE RE-IMPLEMENTATION as the previous code did not work at all - 17 Feb 2015
//PRG - Comment out creation of primary key for slave target relations as per MapMerge's VPGenerator.java code - 19 FEB 2015
//PRG ADD Parameter to control the complexity of the VP Authority Scenario - 24 FEB 2015
//PRG - RECODE genMappings(), Mapping Output = SOtgds, to generate n^2 mappings instead of one single mapping per VP Authority Scenario - MAR 5 2015

public class VPIsAAuthorityScenarioGenerator extends AbstractScenarioGenerator{
	public static final int MAX_NUM_TRIES = 10;
	
	private JoinKind jk;
	private int numOfSrcTblAttr;
	private int numOfTgtTables;
	private int attsPerTargetRel;
	private int attrRemainder;
	
    private SkolemKind sk;
    
    private String skId;
    // skIdRandomArgs keeps track of the randomly generated argument set (only used for SkolemKind.RANDOM mode)
    private Vector<String> skIdRandomArgs;
	    
	private int keySize;
	
	// MN considered an attribute to check whether we are reusing target relation - 13 May 2014
	private boolean targetReuse;
	// MN
	
   	// VP Authority Scenario, assume complexityScen denotes "n" (here n can be equal or greater than 2)
	private int complexityScen = 2;

	
    public VPIsAAuthorityScenarioGenerator ()
    {
        ;
    }

    
    protected void initPartialMapping() {
    	
    	super.initPartialMapping();
    	
    	// VP Authority Scenario, assume complexityScen denotes "n"
    	
    	complexityScen = Utils.getRandomNumberAroundSomething(_generator, VPAuthorityComplexity, VPAuthorityComplexityDeviation);
    	// Sanity Check to enforce lower and upper limits for this scenario
    	complexityScen = (complexityScen < 2) ? 2 : complexityScen;
    	complexityScen = (complexityScen > 16) ? 16 : complexityScen;
    	
    	numOfSrcTblAttr = complexityScen * (complexityScen + 1);   	
    	numOfTgtTables = complexityScen * (complexityScen + 1);
      
    	attsPerTargetRel = 2;      
    	attrRemainder = 0; 
        
        jk = JoinKind.CHAIN;
      
        // We generate one denormalized source relation, therefore we do not generate any keys for the source
        keySize = 0;
        
        // We use a FIXED Skolemization Strategy, i.e., SkolemKind.ALL
		sk = SkolemKind.values()[typeOfSkolem];
		
		// We do not use any source/target reuse for this mapping scenario
	  	targetReuse = false;
	  	
    }

    
    // PRG RE-CODE VP AUTHORITY - genSourceRels() Method - FEB 12 2015
    // Assume complexityScen denotes "n"
    // Generate one denormalized source relation of n*(n+1) attributes
	@Override
	protected void genSourceRels() throws Exception {
		String sourceRelName = randomRelName(0);
		String[] attNames = new String[numOfSrcTblAttr];
		String hook = getRelHook(0);		
		String[] attrsType = new String[numOfSrcTblAttr];
		
		// Generate numOfSrcTblAtt attributes for this source relation
		for (int i = 0; i < numOfSrcTblAttr; i++) {			
			attNames[i] = randomAttrName(0, i);			
		}
				
		// ATTENTION - FEB 12 2013
		// PRG - NOT HANDLING SOURCE/TARGET REUSE FOR VP AUTHORITY - FEB 12 2013
		/*
		//MN To Do: Target Reusability - 6 Feb 2015
		if(targetReuse){
			//MN assumption: skolem is the last attribute - 23 June 2014
			int count =0;
			for(int i=0; i<numOfTgtTables; i++){
				if(i != numOfTgtTables -1){
					for(int j=0; j<attsPerTargetRel -1; j++){
						attrsType[count] = m.getTargetRels().get(i).getAttrArray(j).getDataType();
						count++;
					}
				}
				else{
					for(int j=0; j<attsPerTargetRel + attrRemainder -1; j++){
						attrsType[count] = m.getTargetRels().get(i).getAttrArray(j).getDataType();
						count++;
					}
				}
			}
			
			for(int i=0; i<numOfSrcTblAttr; i++)
				if(attrsType[i] == null)
					attrsType[i] = "TEXT";
		}
		*/
		
		RelationType sRel = null;
    
		if(!targetReuse)
			 sRel = fac.addRelation(hook, sourceRelName, attNames, true);
		else
			 sRel = fac.addRelation(hook, sourceRelName, attNames, attrsType, true);
		
		// PRG - Do not generate and/or add any keys to our denormalized source relation - FEB 12 2015
        /*
		if (keySize > 0 )
			fac.addPrimaryKey(sourceRelName, keys, true);
		
		m.addSourceRel(sRel);
        */
		
		targetReuse = false;
		
	}

	// ATTENTION - FEB 12 2015
	// PRG - NOT HANDLING SOURCE/TARGET REUSE FOR VP AUTHORITY - FEB 12 2015
	@Override
	protected boolean chooseSourceRels() throws Exception {
		int minAttrs = numOfTgtTables;

		// ATTENTION - FEB 12 2015    
		// PRG - NOT HANDLING SOURCE/TARGET REUSE FOR VP AUTHORITY - FEB 12 2015  
		/*
		 * 
		//MN I am not sure if the following is necessary - 26 April 2014
		if(keySize>numOfTgtTables)
			minAttrs = keySize;
		//MN
		
		//MN -13 May 2014
		boolean ok = true;
		//MN
		
		RelationType rel = null;
		
		//MN - 13 May 2014
		int numTries =-1;
		
		while(numTries++<MAX_NUM_TRIES){
			//MN get a random relation - 26 April 2014
			rel = getRandomRel(true, minAttrs);
		
			if (rel == null) 
				return false;
		
			numOfSrcTblAttr = rel.sizeOfAttrArray();
			//MN reevaluate the following fields (tried to preserve initial value of numOfTgtTables not keySize) - 26 April 2014
			attsPerTargetRel = numOfSrcTblAttr / numOfTgtTables;
			attrRemainder = numOfSrcTblAttr % numOfTgtTables; 
		
			//MN I think key elements are first elements of the rel attrs (am I right?) YES - 26 April 2014
			// create primary key if necessary
			if (!rel.isSetPrimaryKey() && keySize > 0) {
				fac.addPrimaryKey(rel.getName(), 
					CollectionUtils.createSequence(0, keySize), true);
				ok = true;
			}
			// adapt keySize - MN I believe keySize is not really important for VP (Am I right?) - 26 April 2014
			else if (rel.isSetPrimaryKey()) {
				//MN BEGIN - 13 May 2014
				int[] pkPos = model.getPKPos(rel.getName(), true);
				for(int i=0; i<pkPos.length; i++)
					if(pkPos[i] != i)
						ok = false;
				if(ok)
					keySize = rel.getPrimaryKey().sizeOfAttrArray(); 
				//MN END
			}
			
			if(ok)
				break;
		}
		
		m.addSourceRel(rel);
		
		return true;
		*
		*/
    
		return false;
	
	}
	
  
	//MN implemented chooseTargetRels method to support target reusability - 26 April 2014
	//MN assumptions - (1): attrRemainder =0 (2): keySize =1 - 26 April 2014
	// ATTENTION - FEB 12 2015   
	// PRG - NOT HANDLING SOURCE/TARGET REUSE FOR VP AUTHORITY - FEB 12 2015
	@Override
	protected boolean chooseTargetRels() throws Exception {

    
		// ATTENTION - FEB 12 2015   
		// PRG - NOT HANDLING SOURCE/TARGET REUSE FOR VP AUTHORITY - FEB 12 2015
    
		/*
		 * 
		List<RelationType> rels = new ArrayList<RelationType> ();
		int numTries = 0;
		int created = 0;
		boolean found = false;
		RelationType rel;
		//MN wanted to preserve the initial values of numOfTgtTables and attsPerTargetRel - 26 April 2014
		String[][] attrs = new String[numOfTgtTables][];
		
		// first choose one that has attsPerTargetRel
		while(created < numOfTgtTables) {
			found = true;
			
			//MN check the following again (it is really tricky) - 26 April 2014
			if(created == 0){
				rel = getRandomRel(false, attsPerTargetRel+1);
			}
			else{
				rel = getRandomRel(false, attsPerTargetRel+1, attsPerTargetRel+1);
				
				if(rel != null){
					for(int j=0; j<rels.size(); j++)
						if(rels.get(j).getName().equals(rel.getName()))
							found = false;
				}else{
					found = false;
				}
			}
			
			//MN VP cares about primary key - 26 April 2014
			if(found && !rel.isSetPrimaryKey()) {
				//MN set to false because this is target relation (Am I right?) - 26 April 2014
				//MN primary key size should be 1 - 26 April 2014
				int [] primaryKeyPos = new int [1];
				primaryKeyPos[0] = rel.sizeOfAttrArray()-1;
				fac.addPrimaryKey(rel.getName(), primaryKeyPos[0], false);
			}
			
			if(found && rel.isSetPrimaryKey()){
				//MN keySize should be 1 and key attr should be the last attr- 26 April 2014
				int[] pkPos = model.getPKPos(rel.getName(), false);
				if(pkPos.length != 1)
					found = false;
				if(found)
					if((pkPos[0] != (rel.getAttrArray().length-1)))
						found = false;
			}
			
			// found a fitting relation
			if (found) {
				rels.add(rel);
				m.addTargetRel(rel);

				attrs[created] = new String[rel.sizeOfAttrArray()];
				for(int i = 0; i < rel.sizeOfAttrArray(); i++)
					attrs[created][i] = rel.getAttrArray(i).getName();
				
				//MN attsPerTargetRel should be set (check that) (it is really tricky) - 26 April 2014
				if(created == 0)
					attsPerTargetRel = rel.getAttrArray().length-1;
				
				created++;
				numTries = 0;
			}
			// not found, have exhausted number of tries? then create new one - path tested 1 May 2014
			else {
				numTries++;
				if (numTries >= MAX_NUM_TRIES)
				{
					numTries = 0;
					attrs[created] = new String[attsPerTargetRel+1];
					for(int j = 0; j < attsPerTargetRel+1; j++)
						attrs[created][j] = randomAttrName(created, j);
					
					// create the relation
					String relName = randomRelName(created);
					rels.add(fac.addRelation(getRelHook(created), relName, attrs[created], false));
					
					// primary key should be set and its size should be 1- 26 April 2014
					int [] primaryKeyPos = new int [1];
					primaryKeyPos [0] = attsPerTargetRel;
					fac.addPrimaryKey(relName, primaryKeyPos[0], false);
					
					//MN should I add it to TargetRel? - 26 April 2014
					//m.addTargetRel(rels.get(rels.size()-1));
					
					created++;
					numTries = 0;
				}
			}
		}
		
		//MN set attrRemainder and numOfSrcTables - 26 April 2014
		numOfSrcTblAttr= numOfTgtTables * attsPerTargetRel; 
		//MN considering only these two cases - 26 April 2014
		attrRemainder =0;
		keySize=1;
		
		//MN foreign key should be set - 26 April 2014
		addFKs();
		
		targetReuse = true;
		
		return true;
		
		*/
		
		return false;

	}
	
	// PRG RE-CODE VP AUTHORITY - genTargetRels() Method - FEB 12 2015
    // Assume complexityScen denotes "n"
    // Generate "n" hierarchies of Authority Relations, where each hierarchy has "n" slaves
	// Assumption: as per MapMerge's Paper, each target relation have exactly 2 attributes
	@Override
	protected void genTargetRels() throws Exception {
		String[] attrs;
		String[] attrsType;
		String[] srcAttrs = m.getAttrIds(0, true);
		
		int index = 0;
		while (index < numOfSrcTblAttr) {
			
			// Generate one single Authority Relation Hierarchy, i.e., one Authority Relation + n slaves 
			// Step 1: create one Top Authority Relation of two attributes (one source attribute and one Skolem)
			// Step 2: create n Target Relations, each with two attributes (one distinct source attribute and one Skolem)
			// Step 3: add foreign keys and link the target relations accordingly
			// Step 4: make sure value correspondences are done properly
			
			// Step 1 - Create one Top Authority Relation
			String topAuthName = randomRelName(index);
			String hookAuth = getRelHook(index);
			// By default, the top Authority Relation has only 2 attributes (one copied from the source, and one Surrogate Key Skolem)
       	    int attrNum = 1;           
        	int fkAttrs = 1;
        	attrs = new String[attrNum + fkAttrs];
        	attrsType = new String[attrNum + fkAttrs];      
        	// Create normal attribute for the top Authority Relation (copy from source)
            attrs[0] = srcAttrs[index];
            attrsType[0] = m.getSourceRels().get(0).getAttrArray(index).getDataType();
            // Create the join attribute name that will hold a Surrogate Key Skolem, and the ref to be used in the slave relations
    		String joinAttName = randomAttrName(0, 0) + "JoinAttr";
    		String joinAttNameRef = joinAttName + "Ref";
    		// Create the join attribute
    		attrs[1] = joinAttName;
    		attrsType[1] = "TEXT";
    		// Add Top Authority Relation
    		fac.addRelation(hookAuth, topAuthName, attrs, false);
    		fac.addPrimaryKey(topAuthName, joinAttName, false);
    		
    		// Step 2 - Create n Slave Target Relations 
    		for (int slave = 1; slave < complexityScen + 1; slave++) {
    			//System.out.println("Num of Src Tbl Attr is " + numOfSrcTblAttr);
    			//System.out.println("Slave Index is " + slave + ", Auth Rel Index is " + index + "\n");
    			String trgName = randomRelName(index+slave);
    			String hookTarget = getRelHook(index+slave);
    			// By default, each Target Slave Relation has only 2 attributes
    			String[] attrsTarget = new String[2];
    			String[] attrsTypeTarget = new String[2];
    			// Create normal attribute for the top Authority Relation (copy from source)
                attrsTarget[0] = srcAttrs[index+slave];
                attrsTypeTarget[0] = m.getSourceRels().get(0).getAttrArray(index+slave).getDataType();
        		// Create the join attribute
        		attrsTarget[1] = joinAttNameRef;
        		attrsTypeTarget[1] = "TEXT";
        		// Add Slave Relation
        		fac.addRelation(hookTarget, trgName, attrsTarget, false);
        		// PRG - Comment out creation of primary key for slave target relation as per MapMerge's VPGenerator.java code - FEB 19 2015
        		// fac.addPrimaryKey(trgName, joinAttNameRef, false);   
        		// Step 3 - Add FKs - This is fixed, irrelevant of join kind 
        		// fromRel is determined by "index+slave", fromAttr is 1, toRel is determined by index, toAttr is 1, false means is a target FK
        		addFK(index+slave,1,index,1, false);
    		}
    					
			index = index + complexityScen + 1;
			
		}
		
	}


	// ATTENTION - FEB 12 2015
    // PRG - OBSOLETE CODE AFTER RE-IMPLEMENTING VP AUTHORITY - FEB 12 2015
	/*
	 * 	
	//MN Changed the method to implement authority relation pattern - 23 June 2014
	private void addFKs() {
		if (jk == JoinKind.STAR) {
			for(int i = 1; i < numOfTgtTables; i++) {
				int toA = m.getNumRelAttr(0, false) - 1;
				int fromA = m.getNumRelAttr(i, false) - 1;
				addFK(i, fromA, 0, toA, false);
			}
		} else { // chain
			for(int i = 1; i < numOfTgtTables; i++) {
				//MN I'm not sure about correctness of this part of the code - 23 June 2014
				int toA = m.getNumRelAttr(i + 1, false) - 1;
				int fromA = m.getNumRelAttr(i, false) - 1;
				addFK(i, fromA, i+1, toA, false);
			}
		}
	}
	*/
	
	private List<CorrespondenceType> getCorrsForThisMapping(int targetTopAuth, int targetSlave) {
		
		String topAuthName = m.getRelName(targetTopAuth, false);
		String slaveName = m.getRelName(targetSlave, false);
		
		List<CorrespondenceType> result = new ArrayList<CorrespondenceType> ();
		
		for(CorrespondenceType c : m.getCorrs()) {
			if (c.getTo().getTableref().equals(topAuthName) || c.getTo().getTableref().equals(slaveName)) {
				result.add(c);
			}
		}
		return result;
	}

	// PRG RE-CODE VP AUTHORITY - genMappings() Method - FEB 16 2015
    // Assume complexityScen denotes "n"
    // Generate "n" hierarchies of Authority Relations, where each hierarchy has "n" slaves
	// ASSUMPTIONS: as per MapMerge's Paper, (a) each target relation has exactly 2 attributes. 
	// (b) Each hierarchy shares the same Skolem function. We use a single argument per Skolem to save some computation resources ;)
	// (c) In the case Mapping Output = SOtgds, we generate exactly n^2 mappings (cuadratic in the scenario complexity) where n is between 2 and 16 max.
	// Note that we keep generating a single schema mapping if Mapping Output = FOtgds.
	@Override
	protected void genMappings() throws Exception {
		
		int previousMarker = 0;
		
		String[] sourceVars = fac.getFreshVars(0, numOfSrcTblAttr);
		//System.out.println("All vars are " + Arrays.toString(sourceVars));
		
		switch (mapLang) {
		
		case FOtgds:
			MappingType m1 = fac.addMapping(m.getCorrs());
			String[] newExistVars = new String[1];
			
			fac.addForeachAtom(m1, 0, sourceVars);
			
			int hierarchyIndex = 0;
			for(int i = 0; i < numOfTgtTables; i++) {
				
				int offset = i * attsPerTargetRel;
	        	int numAtts = attsPerTargetRel;
	        	
	        	//RelAtomType atom = fac.addEmptyExistsAtom(m1, 0);
	        	fac.addEmptyExistsAtom(m1, i);
	        	fac.addVarsToExistsAtom(m1, i, fac.getFreshVars(i, 1));
	        	if (i == 0 || i == (previousMarker + complexityScen + 1)) {
	        		previousMarker = i;
	        		newExistVars = fac.getFreshVars(numOfSrcTblAttr + hierarchyIndex++, 1);
	        		//System.out.println("New Exist Vars are " + Arrays.toString(newExistVars));
	        	}
	        	fac.addVarsToExistsAtom(m1, i, newExistVars);	        	
			}					
			break;
		
		case SOtgds:
					
			boolean createSK = false;
			
			for(int i = 0; i < numOfTgtTables; i++) {
	        	
	        	if (i == 0 || i == (previousMarker + complexityScen + 1)) {
	        		// Processing a Target Relation that represents a Top Authority; simply save its index
	        		previousMarker = i; 
	        		createSK = true;
	        	}
	        	else {
	        		// Generating a single mapping per each <top authority, slave> pair	  
	        		MappingType thisMapping = fac.addMapping(getCorrsForThisMapping(previousMarker,i));
	        		// PRG NOTE - We may want to add attaching the correct set of correspondences just to save computation resources
	        		// If so, use the following line or code instead of the previous one - MAR 5 2015
	        		// MappingType thisMapping = fac.addMapping(new ArrayList<CorrespondenceType> ());
	        		fac.addForeachAtom(thisMapping, 0, sourceVars);
	        		
	        		// (1) Add top authority exists atom
	        		fac.addEmptyExistsAtom(thisMapping, previousMarker);
	        		// 0 in the next instruction means the first exists atom in the array
	        		fac.addVarsToExistsAtom(thisMapping, 0, fac.getFreshVars(previousMarker, 1));
	        		if (createSK) {
	        			skId = fac.addSKToExistsAtom(thisMapping, 0, fac.getFreshVars(previousMarker, 1));
	        			createSK = false;
	        		}
	        		else {
	        			fac.addSKToExistsAtom(thisMapping, 0, fac.getFreshVars(previousMarker, 1), skId);
	        		}
	        		
	        		// (2) Add slave exists atom	        		
	        		fac.addEmptyExistsAtom(thisMapping, i);
	        		String[] slaveExistVars = fac.getFreshVars(i, 1);
	        		//System.out.println("Slave Exist vars are " + Arrays.toString(slaveExistVars));
		        	fac.addVarsToExistsAtom(thisMapping, 1, fac.getFreshVars(i, 1));	        		
	        		// Adding a previously generated Skolem function "skId" using SkolemKind.ALL
	        		fac.addSKToExistsAtom(thisMapping, 1, fac.getFreshVars(previousMarker, 1), skId);
	        	}	
			}		
			break;
		
		}
		
		/* 
		 * NOTE PRG Mar 5 2015 
		 * For reference purposes, below is the previous implementation of genMappings() which supported the generation 
		 * of *one* single schema mapping per VP Authority Scenario. We replaced this code with a new implementation that
		 * generates n^2 mappings to produce the same kind of mappings used in the MapMerge's paper.
		 * 
		 *
		MappingType m1 = fac.addMapping(m.getCorrs());
		String[] newExistVars = new String[1];
		int previousMarker = 0;
				
		// source table gets fresh variables
		String[] sourceVars = fac.getFreshVars(0, numOfSrcTblAttr);
		fac.addForeachAtom(m1, 0, sourceVars);
		
		switch (mapLang) {
		
		case FOtgds:
			int hierarchyIndex = 0;
			for(int i = 0; i < numOfTgtTables; i++) {
				
				int offset = i * attsPerTargetRel;
	        	int numAtts = attsPerTargetRel;
	        			
	        	//RelAtomType atom = fac.addEmptyExistsAtom(m1, 0);
	        	fac.addEmptyExistsAtom(m1, i);
	        	fac.addVarsToExistsAtom(m1, i, fac.getFreshVars(i, 1));
	        	if (i == 0 || i == (previousMarker + complexityScen + 1)) {
	        		previousMarker = i;
	        		newExistVars = fac.getFreshVars(numOfSrcTblAttr + hierarchyIndex++, 1);
	        	}
	        	fac.addVarsToExistsAtom(m1, i, newExistVars);	        	
			}					
			break;
		
		case SOtgds:
			
			for(int i = 0; i < numOfTgtTables; i++) {
				
				int offset = i * attsPerTargetRel;
	        	int numAtts = attsPerTargetRel;
	        			
	        	fac.addEmptyExistsAtom(m1, i);
	        	fac.addVarsToExistsAtom(m1, i, fac.getFreshVars(i, 1));
	        	if (i == 0 || i == (previousMarker + complexityScen + 1)) {
	        		previousMarker = i;
	        		// Generating one fresh Skolem function using SkolemKind.ALL using a single argument (the top authority main attribute)
	        		//skId = fac.addSKToExistsAtom(m1, i, fac.getFreshVars(0, numOfSrcTblAttr));
	        		// PRG FEB 25 2015 - See comment below
	        		// Let's try to use a single argument per Skolem function to save some computation resources ;)
	        		skId = fac.addSKToExistsAtom(m1, i, fac.getFreshVars(i, 1));
	        		
	        	}
	        	else {
	        		// Adding a previously generated Skolem function "skId" using SkolemKind.ALL
	        		//fac.addSKToExistsAtom(m1, i, fac.getFreshVars(0, numOfSrcTblAttr), skId);
	        		// PRG FEB 25 2015 - See comment below
	        		// Let's try to use a single argument per Skolem function to save some computation resources ;)
	        		fac.addSKToExistsAtom(m1, i, fac.getFreshVars(previousMarker, 1), skId);
	        	}	
			}		
			break;
		
		}
		*/
			
	}
	
	
	@Override
	protected void genTransformations() throws Exception {
		Query q;
		SPJQuery genQuery = genQuery(new SPJQuery());
		
		for(int i = 0; i < numOfTgtTables; i++) {
			String creates = m.getTargetRels().get(i).getName();
			q = (SPJQuery) genQuery.getSelect().getTerm(i);
			q.storeCode(q.toTrampString(m.getMapIds()));
			q = addQueryOrUnion(creates, q);
			fac.addTransformation(q.getStoredCode(), m.getMapIds(), creates);
//
//			fac.addTransformation(q.toTrampString(m.getMapIds()[0]), m.getMapIds(), creates);
			//MN BEGIN 16 August 2014
//			fac.addTransformation("", m.getMapIds(), creates);
			//MN END
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
			 		SKFunction sk = m.getSkolemFromAtom(m1, false, i, numAttr - 1); //TODO -1 is ok?
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
                sel1.add(joinAttName + i, stSK);
                queries[i].setSelect(sel1);
                
                SelectClauseList sel2 = queries[i + 1].getSelect();
                sel2.add(joinAttNameRef + (i + 1), stSK);
                queries[i + 1].setSelect(sel2);
            }
        }
        
        // add the partial queries to the parent query
        // to form the whole transformation
//        SelectClauseList pselect = pquery.getSelect();
        SelectClauseList gselect = generatedQuery.getSelect();
        for (int i = 0; i < numOfTgtTables; i++)
        {
            String tblTrgName = m.getRelName(i, false);
//            pselect.add(tblTrgName, queries[i]);
            gselect.add(tblTrgName, queries[i]);
        }
//        pquery.setSelect(pselect);
        generatedQuery.setSelect(gselect);
		return generatedQuery;
	} 

	// PRG ADD VP AUTHORITY - genCorrespondences() Method - FEB 12 2015
    // Assume complexityScen denotes "n", we have one denormalized source relation, and n*(n+1) target relations
	@Override
	protected void genCorrespondences() {
		for (int i = 0; i < numOfTgtTables; i++) {       	
			// Step 4 - Add Correspondence
			// sRel is 0, sAttr is determined by i, tRel is determined by i, tAttr is 0
			addCorr(0, i, i, 0);
        }
	}
	
	@Override
	public ScenarioName getScenType() {
		return ScenarioName.VERTPARTITIONISAAUTHORITY;
	}
}
