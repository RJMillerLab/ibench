package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.List;

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

//PRG FIXED Omission, must generate source relation with at least 2 elements (this was causing empty Skolem terms and PK FDs with empty RHS!)- Sep 19, 2012
//MN  IMPLEMENTED chooseSourceRels and chooseTargetRels - 1 May 2014
//MN  ENHANCED genTargetRels to pass types of attributes of target relations as argument to addRelation - 3 May 2014
//MN  ENHANCED genSourceRels to pass types of attributes of source relation as argument to addRelation - 13 May 2014

public class VPHasAScenarioGenerator extends AbstractScenarioGenerator {

	public static final int MAX_NUM_TRIES = 10;
	
	private JoinKind jk;
	private int numOfSrcTblAttr;
	private int numOfTgtTables;
	private int attsPerTargetRel;
	private int attrRemainder;
	private SkolemKind sk = SkolemKind.ALL;
//	private String skId;
	
	//MN - considered an attribute to check whether we are reusing target relations - 13 May 2014
	private boolean targetReuse;
	//MN
    
    public VPHasAScenarioGenerator()
    {
        ;
    }

    
    protected void initPartialMapping() {
    	if (log.isDebugEnabled()) {log.debug("---INIT---");};
    	super.initPartialMapping();
    	
        numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
            numOfElementsDeviation);
        
        // PRG ADD - Generate at least a source relation of 2 elements - Sep 19, 2012
        numOfSrcTblAttr = (numOfSrcTblAttr > 2 ? numOfSrcTblAttr : 2);

        numOfTgtTables = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements,
            numOfSetElementsDeviation);
    	
        numOfTgtTables = (numOfTgtTables > 1) ? numOfTgtTables : 2;

        attsPerTargetRel = numOfSrcTblAttr / numOfTgtTables;
        attrRemainder = numOfSrcTblAttr % numOfTgtTables; 
        
        
        jk = JoinKind.values()[joinKind];
        if (jk == JoinKind.VARIABLE)
        {
            int tmp = Utils.getRandomNumberAroundSomething(_generator, 0, 1);
            if (tmp < 0)
                jk = JoinKind.STAR;
            else jk = JoinKind.CHAIN;
        }
        
        //MN - 13 May 2014
        targetReuse = false;
        //MN
    }

	@Override
	protected void genSourceRels() {
		if (log.isDebugEnabled()) {log.debug("---GENERATING SOURCE---");};
		String sourceRelName = randomRelName(0);
		String[] attNames = new String[numOfSrcTblAttr];
		String hook = getRelHook(0);
		
		//MN - 13 May 2014
		String[] attrsType = new String[numOfSrcTblAttr];
		//MN
		
		for (int i = 0; i < numOfSrcTblAttr; i++)
			attNames[i] = randomAttrName(0, i);
		
		//MN - 13 May 2014
		if(targetReuse){
			int count =0;
			for(int i=0; i<numOfTgtTables; i++){
				if(i == 0){
					for(int j=0; j<attsPerTargetRel -1; j++){
							attrsType[count] = m.getTargetRels().get(i).getAttrArray(j).getDataType();
							count++;
							}
				}
				else if (i == numOfTgtTables -1){
					for(int j=0; j<attsPerTargetRel + attrRemainder -2; j++){
							attrsType[count] = m.getTargetRels().get(i).getAttrArray(j).getDataType();
							count++;
					}
				}
				else{
					for(int j=0; j<attsPerTargetRel -2; j++){
						attrsType[count] = m.getTargetRels().get(i).getAttrArray(j).getDataType();
						count++;
					}
				}
					
			 for(i=0; i<numOfSrcTblAttr; i++)
				if(attrsType[i] == null)
					attrsType[i] = "TEXT";
			 }
		}
		//MN
		
		
		RelationType sRel = null;
		
		//MN - 13 May 2014
		if(!targetReuse)
			sRel = fac.addRelation(hook, sourceRelName, attNames, true);
		else
			sRel = fac.addRelation(hook, sourceRelName, attNames, attrsType, true);
		//MN
		
		//MN - 13 May 2014
		targetReuse = false;
		//MN
		
		m.addSourceRel(sRel);
	}

	//MN implemented chooseSourceRels method to support source reusability - 1 May 2014
	@Override
	protected boolean chooseSourceRels() throws Exception {
		int minAttrs = numOfTgtTables;

			
		RelationType rel;
			
		//MN get a random relation - 26 April 2014
		rel = getRandomRel(true, minAttrs);
			
		if (rel == null) 
			return false;
			
		numOfSrcTblAttr = rel.sizeOfAttrArray();
		//MN reevaluate the following fields (tried to preserve initial value of numOfTgtTables) - 1 May 2014
		attsPerTargetRel = numOfSrcTblAttr / numOfTgtTables;
	    attrRemainder = numOfSrcTblAttr % numOfTgtTables; 
			
		m.addSourceRel(rel);
			
		return true;
	}
		
	//MN ENHANCED to support types of target attributes - 3 May 2014
	@Override
	protected void genTargetRels() throws Exception {
		if (log.isDebugEnabled()) {log.debug("---GENERATING TARGET---");};
        String[] attrs;
        //MN BEGIN
        String[] attrsType;
        //MN END
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
        	int fkAttrs = (
        			(jk == JoinKind.CHAIN && i != 0) ||
        			(jk == JoinKind.STAR && i != 0)
        			? 2 : 1);
        	int attWithFK = attrNum + fkAttrs;
        	attrs = new String[attWithFK];
        	//MN BEGIN
        	attrsType = new String[attWithFK];
        	//MN END
        
        	// create normal attributes for table (copy from source)
            for (int j = 0; j < attrNum; j++){
            	attrs[j] = srcAttrs[offset + j];
            	//MN BEGIN - 8 May 2014
            	attrsType[j] = m.getSourceRels().get(0).getAttrArray(j + offset).getDataType();
            	//MN END
            }
            
            // create the join attributes
            // for star join the first one has the join attribute and the following ones 
            // have the join reference (FK)
            if (jk == JoinKind.STAR) {
            	if (i == 0){//TODO check
            		attrs[attrs.length - 1] = joinAttName;
            		//MN BEGIN
            		attrsType[attrs.length - 1] = "TEXT";
            		//MN END
            	}
            	else {
            		attrs[attrs.length - 2] = joinAttName;
            		attrs[attrs.length - 1] = joinAttNameRef;
            		//MN BEGIN
            		attrsType[attrs.length - 2] = "TEXT";
            		attrsType[attrs.length - 1] = "TEXT";
            		//MN END
            	}
            // for chain join each one has a join and join ref to the previous
            // thus, the first does not have a ref and the last one does not have a join attr
            } else { // chain
            	if (i == 0){
            		attrs[attrs.length - 1] = joinAttName;
            		//MN BEGIN
            		attrsType[attrs.length - 1] = "TEXT";
            		//MN END
            	}
            	else {
            		attrs[attrs.length - 2] = joinAttName;
            		attrs[attrs.length - 1] = joinAttNameRef;
            		//MN BEGIN
            		attrsType[attrs.length - 2] = "TEXT";
            		attrsType[attrs.length - 1] = "TEXT";
            		//MN END
            	}
            }
            
            //MN modified the following line- 4 May 2014
            String[] attrsCopy = new String[attrs.length];
            for(int h=0; h<attrs.length; h++)
            	attrsCopy[h] = attrsType[h];
            fac.addRelation(hook, trgName, attrs, attrsCopy, false);
            
            if (jk == JoinKind.STAR) 
            {
            	if (i == 0) // foreign key only goes in one direction so the primary key is set only for the first relation
            		fac.addPrimaryKey(trgName, joinAttName, false);
            	else
            		fac.addPrimaryKey(trgName, attrs[attrs.length - 2], false);
            } 
            
            else 
            {
            	if (i == 0)
            		fac.addPrimaryKey(trgName, joinAttName, false);
            	else
            		fac.addPrimaryKey(trgName, joinAttName, false);
            }
        }
        
        addFKs();
	}

	//MN implemented chooseTargetRels method to support target reusability - 1 May 2014
	//MN assumptions - (1): attrRemainder =0 - 1 May 2014
	@Override
	protected boolean chooseTargetRels() throws Exception {
		List<RelationType> rels = new ArrayList<RelationType> ();
		int numTries = 0;
		int created = 0;
		boolean found = false;
		RelationType rel;
		//MN wanted to preserve the initial values of numOfTgtTables and attsPerTargetRel - 1 May 2014
		String[][] attrs = new String[numOfTgtTables][];
			
		// first choose one that has attsPerTargetRel
		while(created < numOfTgtTables) {
			found = true;
				
			//MN check the following again (it is really tricky) - 1 May 2014
			if(created == 0){
				rel = getRandomRel(false, attsPerTargetRel+1);
			}
			else{
				//MN second, third, ... target relations should have attsPerTargetRel+2 attributes - 1 May 2014
				rel = getRandomRel(false, attsPerTargetRel+2, attsPerTargetRel+2);
				
				if(rel != null){
					for(int j=0; j<rels.size(); j++)
						if(rels.get(j).getName().equals(rel.getName()))
							found = false;
				}else{
					found = false;
				}
			}
				
			//MN VP cares about primary key - 1 May 2014
			if(found && !rel.isSetPrimaryKey()) {
				//MN set to false because this is target relation (Am I right?) - 26 April 2014
				//MN primary key size should be 1 - 1 May 2014
				int [] primaryKeyPos = new int [1];
				if(created == 0){
					primaryKeyPos[0] = rel.sizeOfAttrArray()-1;
					fac.addPrimaryKey(rel.getName(), primaryKeyPos[0], false);
				}
				else{
					primaryKeyPos[0] = rel.sizeOfAttrArray()-2;
					fac.addPrimaryKey(rel.getName(), primaryKeyPos[0], false);
				}
			}
				
			if(found && rel.isSetPrimaryKey()){
				//MN keySize should be 1 and key attr should be the last attr or last attr -1 May 2014
				int[] pkPos = model.getPKPos(rel.getName(), false);
				if(pkPos.length != 1)
					found = false;
				if(found)
					if(created == 0){
						if(pkPos[0] != (rel.getAttrArray().length-1))
							found = false;}
					else
						if(pkPos[0] != (rel.getAttrArray().length-2))
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
			// not found, have exhausted number of tries? then create new one
			else {
				numTries++;
				if (numTries >= MAX_NUM_TRIES)
				{
					numTries = 0;
					//MN created plays an important role here - 1 May 2014
					if(created == 0){
						attrs[created] = new String[attsPerTargetRel+1];
						for(int j = 0; j < attsPerTargetRel+1; j++)
							attrs[created][j] = randomAttrName(created, j);
					}
					else{
						attrs[created] = new String[attsPerTargetRel+2];
						for(int j = 0; j < attsPerTargetRel+2; j++)
							attrs[created][j] = randomAttrName(created, j);
					}
					
					// create the relation
					String relName = randomRelName(created);
					rels.add(fac.addRelation(getRelHook(created), relName, attrs[created], false));
						
					// primary key should be set and its size should be 1- 1 May 2014
					int [] primaryKeyPos = new int [1];
					if(created == 0)
						primaryKeyPos [0] = attsPerTargetRel;
					else
						primaryKeyPos [0] = attsPerTargetRel+1;
 					
					fac.addPrimaryKey(relName, primaryKeyPos[0], false);
						
					//MN should I add it to TargetRel? - 26 April 2014 - tested 1 May 2014
					//m.addTargetRel(rels.get(rels.size()-1));
						
					created++;
					numTries = 0;
				}
			}
		}
			
		//MN set attrRemainder and numOfSrcTables - 1 May 2014
		numOfSrcTblAttr= numOfTgtTables * attsPerTargetRel; 
		//MN considering only these two cases - 1 May 2014
		attrRemainder =0;
			
		//MN foreign key should be set - 26 April 2014
		addFKs();
		return true;
	}
		
	private void addFKs() {
		if (jk == JoinKind.STAR) {
			for(int i = 1; i < numOfTgtTables; i++) {
				int toA = m.getNumRelAttr(0, false) - 1;
				int fromA = m.getNumRelAttr(i, false) - 1;
				addFK(i, fromA, 0, toA, false);
			}
		} else { // chain
			int toA = m.getNumRelAttr(0, false) - 1;
			int fromA = m.getNumRelAttr(1, false) - 1;
			addFK(1, fromA, 0, toA, false);
			for(int i = 2; i < numOfTgtTables; i++) {
				int toRel = i - 1;
				int fromRel = i;
				toA = m.getNumRelAttr(toRel, false) - 2;
				fromA = m.getNumRelAttr(fromRel, false) - 1;
				addFK(fromRel, fromA, toRel, toA, false);
			}
		}
	}

	@Override
	protected void genMappings() throws Exception {
		MappingType m1 = fac.addMapping(m.getCorrs());
		
		// source table gets fresh variables
		fac.addForeachAtom(m1, 0, fac.getFreshVars(0, numOfSrcTblAttr));

		switch (mapLang) 
		{
			case FOtgds:
				// vars for the primary keys
				String[] keyVars = fac.getFreshVars(numOfSrcTblAttr, numOfTgtTables);
				
				for(int i = 0; i < numOfTgtTables; i++) {
					int offset = i * attsPerTargetRel;
		        	int numAtts = (i < numOfTgtTables - 1) ? attsPerTargetRel :
		    				attsPerTargetRel + attrRemainder;
		        	String[] fkVar = new String[] {};
		        	String[] vars;
		        	
		        	if (i != 0) {
		        		switch(jk) {
		        		case STAR:
		        			fkVar = new String[] {keyVars[0]};
		        			break;
		        		case CHAIN:
		        			fkVar = new String[] {keyVars[i - 1]};
		        			break;
						default:
							throw new Exception("should never see VARIABLE join kind here");
		        		}
		        	}
		        	
		        	vars = CollectionUtils.concatArrays(
        					fac.getFreshVars(offset, numAtts), 
        					new String[] {keyVars[i]},
        					fkVar);
		        	
		        	fac.addExistsAtom(m1, i, vars);
				}
				break;
				
			case SOtgds:
				String[] skIds = new String[numOfTgtTables];
				for(int i = 0; i < numOfTgtTables; i++)
					skIds[i] = fac.getNextId("SK");
				
				for(int i = 0; i < numOfTgtTables; i++) {
					int offset = i * attsPerTargetRel;
		        	int numAtts = (i < numOfTgtTables - 1) ? attsPerTargetRel :
		    				attsPerTargetRel + attrRemainder;
		        	
		        	//RelAtomType atom = fac.addEmptyExistsAtom(m1, 0);
		        	fac.addEmptyExistsAtom(m1, i);
		        	fac.addVarsToExistsAtom(m1, i, fac.getFreshVars(offset, numAtts));
		        	generateSKs(m1, i, offset, numAtts, skIds);
				}
				break;
		}
	}
	
	//TODO does not seem to make a lot of sense, using the same skolem function, but with different number of arguments is wrong
	private void generateSKs(MappingType m1, int rel, int offset, int numAtts, String[] skIds) {
//		int numArgsForSkolem;

//		// generate random number arguments for skolem function
//		if (sk == SkolemKind.RANDOM)
//			numArgsForSkolem = Utils.getRandomNumberAroundSomething(_generator, numOfSrcTblAttr / 2, numOfSrcTblAttr / 2);
//
//		// ensure that we are still within the bounds of the number of source attributes
//		numArgsForSkolem = (numArgsForSkolem > numOfSrcTblAttr) ? numOfSrcTblAttr : numArgsForSkolem;
//
//		// check if we are only using the exchanged attributes in the skolem and change the starting point appropriately
//		int start = 0;
//		if(sk == SkolemKind.EXCHANGED)
//		{
//			start = offset;
//			numArgsForSkolem = numAtts;
//		}
		
		if (jk.equals(JoinKind.STAR)) {
			if (rel == 0)
				fac.addSKToExistsAtom(m1, rel, fac.getFreshVars(0, attsPerTargetRel), 
						skIds[rel]);
			else {
				// pk skolem = attrs of center + attrs of fragment
				fac.addSKToExistsAtom(m1, rel, 
						CollectionUtils.concatArrays(
								fac.getFreshVars(0, attsPerTargetRel),
								fac.getFreshVars(rel * attsPerTargetRel, 
										attsPerTargetRel)), 
						skIds[rel]);
				// fk to center of star
				fac.addSKToExistsAtom(m1, rel, 
						fac.getFreshVars(0, attsPerTargetRel), 
						skIds[rel - 1]);			
				}
		}
		// CHAIN join
		else {
			// first rel add pk skolem
			if (rel == 0)
				fac.addSKToExistsAtom(m1, rel, 
						fac.getFreshVars(0, attsPerTargetRel), skIds[rel]);
			// intermediate rel add pk skolem + fk skolem to previous
			else {
				fac.addSKToExistsAtom(m1, rel, 
						fac.getFreshVars(0, (rel + 1) * attsPerTargetRel), 
						skIds[rel]);
				fac.addSKToExistsAtom(m1, rel, 
						fac.getFreshVars(0, rel * attsPerTargetRel), 
						skIds[rel - 1]);
			}
		}
	}
	
	@Override
	protected void genTransformations() throws Exception {
		//SPJQuery q;
		//SPJQuery genQuery = genQuery(new SPJQuery());
		
		for(int i = 0; i < numOfTgtTables; i++) {
			String creates = m.getTargetRels().get(i).getName();
			//q = (SPJQuery) genQuery.getSelect().getTerm(i);
			
			//fac.addTransformation(q.toTrampString(m.getMapIds()[0]), m.getMapIds(), creates);
			//MN BEGIN 16 August 2014
			fac.addTransformation("", m.getMapIds(), creates);
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
			String skName = "";
			
			for(int i = 0; i < numOfTgtTables; i++) {
				SelectClauseList seli = queries[i].getSelect();
				int numAttr = (i < numOfTgtTables - 1) ? attsPerTargetRel : attsPerTargetRel + attrRemainder;

				int numArgs;
				
				if (mapLang.equals(MappingLanguageType.SOtgds)) {
					SKFunction sk = m.getSkolemFromAtom(m1, false, i, numAttr);
					skName = sk.getSkname();
					numArgs = sk.sizeOfVarArray();
				}
				else {
//					int offset = i * attsPerTargetRel;
		        	int numAtts = (i < numOfTgtTables - 1) ? attsPerTargetRel :
		    				attsPerTargetRel + attrRemainder;
		        	
		        	numArgs = numOfSrcTblAttr;

		    		// generate random number arguments for skolem function
		    		if (sk == SkolemKind.RANDOM)
		    			numArgs = Utils.getRandomNumberAroundSomething(_generator, numOfSrcTblAttr / 2, numOfSrcTblAttr / 2);

		    		// ensure that we are still within the bounds of the number of source attributes
		    		numArgs = (numArgs > numOfSrcTblAttr) ? numOfSrcTblAttr : numArgs;

		    		// check if we are only using the exchanged attributes in the skolem and change the starting point appropriately
//		    		int start = 0;
		    		if(sk == SkolemKind.EXCHANGED)
		    		{
//		    			start = offset;
		    			numArgs = numAtts;
		    		}
		        	
					if (i == 0)
						skName = fac.getNextId("SK");
					numArgs = 1;//TODO
				}
		 			
				vtools.dataModel.expression.SKFunction stSK = 
						new vtools.dataModel.expression.SKFunction(skName);
		 			
		 		// this works because the key is always the first attribute 
		 		for(int k = 0; k < numArgs; k++) {	//TODO differs from what SKs do		
		 			String sAttName = m.getAttrId(0, k, true);
		 			Projection att = new Projection(new Variable("X"), sAttName);
		 			stSK.addArg(att);
		 		}
				
		 		if(i == 0)
		 			seli.add(joinAttName, stSK);
		 		
	            seli.add(joinAttNameRef, stSK);
			}
		}
		
        if (jk == JoinKind.CHAIN)
        {
        	String skName = "";
            for (int i = 0; i < numOfTgtTables - 1; i++)
            {
            	int numArgs;
            	int numAttr = (i < numOfTgtTables - 1) ? attsPerTargetRel : attsPerTargetRel + attrRemainder;

				if (mapLang.equals(MappingLanguageType.SOtgds)) {
					SKFunction sk = m.getSkolemFromAtom(m1, false, i, numAttr);
					skName = sk.getSkname();
					numArgs = sk.sizeOfVarArray();
				}
				else {
					if (i == 0)
						skName = fac.getNextId("SK");
					numArgs = 1;//TODO
				}		 			
		 		vtools.dataModel.expression.SKFunction stSK = 
		 				new vtools.dataModel.expression.SKFunction(skName);
		 			
		 		for(int k = 0; k < numArgs; k++) {			
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
		return ScenarioName.VERTPARTITIONHASA;
	}
}
