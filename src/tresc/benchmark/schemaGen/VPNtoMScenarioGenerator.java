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
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;

// PRG Enhanced VP N-TO-M to handle Optional Source Keys based on ConfigOptions.PrimaryKeySize - Sep 18, 2012
// PRG FIXED Omission, must generate source relation with at least 2 elements (this was causing empty Skolem terms and PK FDs with empty RHS!)- Sep 19, 2012

// very similar to merging scenario generator, with source and target schemas swapped
//MN IMPLEMENTED chooseSourceRels and chooseTargeteRels (source and target reusability) - 2 May 2014
//MN ENHANCED genTargetRels to pass types of attributes of target relations as argument to addRelation - 4 May 2014

public class VPNtoMScenarioGenerator extends AbstractScenarioGenerator {

	public static final int MAX_NUM_TRIES = 10;
	
	private JoinKind jk;
	private int numOfSrcTblAttr;
	private int numOfTgtTables = 2;
	private int attsPerTargetRel;
	private int attrRemainder;
	private String skId1, skId2;
	private String[] sk1Args, sk2Args;
    
	// PRG ADDED to Support Optional Source Keys - Sep 18, 2012
	private int keySize;
	
    public VPNtoMScenarioGenerator()
    {
        ;
    }

    
    protected void initPartialMapping() {
    	super.initPartialMapping();
    	
        numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
            numOfElementsDeviation);

        // PRG ADD - Generate at least a source relation of 2 elements - Sep 19, 2012
        numOfSrcTblAttr = (numOfSrcTblAttr > 2 ? numOfSrcTblAttr : 2);
        
        attsPerTargetRel = numOfSrcTblAttr / numOfTgtTables;
        attrRemainder = numOfSrcTblAttr % numOfTgtTables; 
        
        
        jk = JoinKind.values()[joinKind];
        // join kind must be STAR for VP N-to-M
        // jk = JoinKind.STAR;
        
     
        // PRG ENHANCED VP N-TO-M according to Configuration Options - Sep 18, 2012
        // Reading ConfigOptions.PrimaryKeySize 
     	keySize = Utils.getRandomNumberAroundSomething(_generator, primaryKeySize, primaryKeySizeDeviation);
     	// adjust keySize as necessary with respect to number of source table attributes
     	// NOTE: we are not strictly enforcing a source key for VERTICAL PARTITION
     	keySize = (keySize >= numOfSrcTblAttr) ? numOfSrcTblAttr - 1 : keySize;
     	
    }

	@Override
	protected void genSourceRels() throws Exception {
		String sourceRelName = randomRelName(0);
		String[] attNames = new String[numOfSrcTblAttr];
		String hook = getRelHook(0);
		
		//for (int i = 0; i < numOfSrcTblAttr; i++)
		//	attNames[i] = randomAttrName(0, i);
		
		// PRG ADDED Generation of Source Key Elements when keySize > 0
		String[] keys = new String[keySize];
		for (int j = 0; j < keySize; j++)
			keys[j] = randomAttrName(0, 0) + "ke" + j;
		
		int keyCount = 0;
		for (int i = 0; i < numOfSrcTblAttr; i++) {
			String attrName = randomAttrName(0, i);

			if (keyCount < keySize)
				attrName = keys[keyCount];
			
			keyCount++;
			
			attNames[i] = attrName;
		}
		
		
		RelationType sRel = fac.addRelation(hook, sourceRelName, attNames, true);
		
		// PRG ADDED - DO NOT ENFORCE KEY UNLESS EXPLICITLY REQUESTED - Sep 18, 2012
		if (keySize > 0 )
			fac.addPrimaryKey(sourceRelName, keys, true);
	
		m.addSourceRel(sRel);
	}

	//MN implemented chooseSourceRels method to support source reusability - 2 May 2014
	@Override
	protected boolean chooseSourceRels() throws Exception {
		//MN I think that numOfTgtTables is always equals to 2 - 2 May 2014 
		int minAttrs = numOfTgtTables;
		//MN I am not sure if the following is needed - 2 May 2014
		if(keySize>numOfTgtTables)
			minAttrs = keySize;
			
		RelationType rel;
			
		//MN get a random relation - 27 April 2014
		rel = getRandomRel(true, minAttrs);
			
		if (rel == null) 
			return false;
			
		numOfSrcTblAttr = rel.sizeOfAttrArray();
		//MN reevaluate the following fields (tries to preserve initial value of numOfTgtTables not keySize) - 26 April 2014
		attsPerTargetRel = numOfSrcTblAttr / numOfTgtTables;
	    attrRemainder = numOfSrcTblAttr % numOfTgtTables; 
			
		//MN I think key elements are first elements of the rel attrs (am I right?) - 26 April 2014
		// create primary key if necessary
		if (!rel.isSetPrimaryKey() && keySize > 0) {
			fac.addPrimaryKey(rel.getName(), 
					CollectionUtils.createSequence(0, keySize), true);
		}
		// adapt keySize - MN I believe keySize is not really important for VP (Am I right?) - 26 April 2014
		else if (rel.isSetPrimaryKey()) {
			keySize = rel.getPrimaryKey().sizeOfAttrArray(); 
		}
			
		m.addSourceRel(rel);
		
		//MN what about jk? - 2 May 2014
		return true;
	}
		
	//MN ENHANCED to support source attr types - 3 May 2014
	@Override
	protected void genTargetRels() throws Exception {
        String[] attrs;
        //MN BEGIN - 3 May 2014
        String[] attrsType;
        //MN END
		String[] srcAttrs = m.getAttrIds(0, true);
		
		String joinAtt1 = randomAttrName(0, 0);
		String joinAtt1Ref = joinAtt1 + "Ref";
        String joinAtt2 = randomAttrName(1, 0);
        String joinAtt2Ref = joinAtt2 + "Ref";
		
        for (int i = 0; i < numOfTgtTables; i++)
        {
        	int offset = i * attsPerTargetRel;
        	String trgName = randomRelName(i);
        	String hook = getRelHook(i);
        	
        	// if its the last table then we tack the remainder onto it
        	int attrNum = (i < numOfTgtTables - 1) ? attsPerTargetRel: attsPerTargetRel + attrRemainder;
        	
        	int fkAttrs = ((jk == JoinKind.CHAIN && (i != 0 && i != numOfTgtTables - 1)) ? 2 : 1);
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

            if (i == 0){
            	attrs[attrs.length - 1] = joinAtt1;
            	//MN BEGIN - 8 May 2014
            	attrsType[attrs.length - 1] = "TEXT";
            	//MN END
            }
            else{
            	attrs[attrs.length - 1] = joinAtt2;
            	//MN BEGIN - 8 May 2014
            	attrsType[attrs.length - 1] = "TEXT";
            	//MN END
            }
            
            //MN changed - 4 May 2014
            String[] attrsCopy = new String[attrs.length];
            for(int h=0; h<attrs.length; h++)
            	attrsCopy[h] = attrsType[h];
            fac.addRelation(hook, trgName, attrs, attrsCopy, false);
            
            // add the primary key for each relation
            if (i == 0)
            	fac.addPrimaryKey(trgName, joinAtt1, false);
            else 
            	fac.addPrimaryKey(trgName, joinAtt2, false);
        }
        
        // create the third table which only has the join attributes
        String trgName = randomRelName(2);
    	String hook = getRelHook(2);
    	
    	//MN BEGIN - 4 May 2014
    	String[] attrsTypeLast = new String [2];
    	for(int i=0; i<2; i++)
    		attrsTypeLast[i] = m.getSourceRels().get(m.getSourceRels().size()-1).getAttrArray(i).getDataType();
    	fac.addRelation(hook, trgName, new String[] {joinAtt1Ref, joinAtt2Ref}, 
    			attrsTypeLast, false);
    	//MN END
    	
        addFKs();
	}

	//MN implemented chooseTargetRels method to support target reusability - 26 April 2014
	//MN assumptions - (1): attrRemainder =0 - 26 April 2014
	//MN note that key size is always 1 - 2 May 2014
	@Override
	protected boolean chooseTargetRels() throws Exception {
		List<RelationType> rels = new ArrayList<RelationType> ();
		int numTries = 0;
		int created = 0;
		boolean found = false;
		RelationType rel;
		//MN wanted to preserve the initial values of numOfTgtTables and attsPerTargetRel - 26 April 2014
		//MN here, we are dealing with target relations
		//MN that have attributes with correspondences to source relation attributes - 2 May 2014
		String[][] attrs = new String[numOfTgtTables+1][];
			
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
				}
				else
					found = false;
			}
				
			//MN VPNToM cares about primary key - 2 May 2014
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
		
		//MN the last target relation should be created - 2 May 2014
		//MN code for creating the last relation - 2 May 2014
		numTries=0;
		found = false;
		RelationType lastRel;
		while((numTries<MAX_NUM_TRIES) && (!found)){
			//For the last relation, 
			lastRel = getRandomRel(false, 2, 2);
			
			//MN check if this relation is the same as one of the target relations have been found so far - 2 May 2014
			if(lastRel != null){
				boolean found2 = true;
				for(int j=0; j<rels.size(); j++)
					if(rels.get(j).getName().equals(lastRel.getName()))
						found2 = false;
				if(found2)
					found=true;
			}
			
			if(found){
				rels.add(lastRel);
				m.addTargetRel(lastRel);

				attrs[created] = new String[lastRel.sizeOfAttrArray()];
				for(int i = 0; i < lastRel.sizeOfAttrArray(); i++)
					attrs[created][i] = lastRel.getAttrArray(i).getName();
				created++;
			}
			
			numTries++;
		}
		//MN if suitable relation for the last relation
		//MN has not found, create one ! - 2 May 2014
		if(!found){
			attrs[created] = new String[2];
			for(int j = 0; j < 2; j++)
				attrs[created][j] = randomAttrName(created, j);
				
			// create the relation
			String relName = randomRelName(created);
			rels.add(fac.addRelation(getRelHook(created), relName, attrs[created], false));
			created++;
		}
		
		//MN set attrRemainder and numOfSrcTables - 26 April 2014
		numOfSrcTblAttr= numOfTgtTables * attsPerTargetRel; 
		//MN considering only these two cases - 26 April 2014
		attrRemainder =0;
		//MN I need to discuss about the following line with Patricia - 2 May 2014
		keySize=1;
			
		//MN foreign key should be set - 26 April 2014
		addFKs();
		return true;
	}
		
	private void addFKs() 
	{
		// the first attribute of the "link" relation is a reference to the last attribute of the first relation
		int toA1 = m.getNumRelAttr(0, false) - 1;
		int fromA1 = 0;
		addFK(2, fromA1, 0, toA1, false);
		
		// the second attribute of the "link" relation is a reference to the last attribute of the second relation
		int toA2 = m.getNumRelAttr(0, false) - 1;
		int fromA2 = 1;
		addFK(2, fromA2, 1, toA2, false);
	}

	@Override
	protected void genMappings() throws Exception {
		MappingType m1 = fac.addMapping(m.getCorrs());
		String joinVar1 = "", joinVar2 = "";
		
		// source table gets fresh variables
		fac.addForeachAtom(m1, 0, fac.getFreshVars(0, numOfSrcTblAttr));

		switch (mapLang) 
		{
			case FOtgds:
				// take care of the first two relations
				for(int i = 0; i < numOfTgtTables; i++) 
				{
					int offset = i * attsPerTargetRel;
		        	int numAtts = (i < numOfTgtTables - 1) ? attsPerTargetRel : attsPerTargetRel + attrRemainder;
		        	
		        	if (i == 0)
		        		joinVar1 = fac.getFreshVars(offset + i + numAtts, 1)[0];
		        	else
		        		joinVar2 = fac.getFreshVars(offset + i + numAtts, 1)[0];
		        	
		        	// add one to the num of atts to account for the join attribute
		        	fac.addExistsAtom(m1, i, fac.getFreshVars(offset + i, numAtts + 1));
				}
				
				// for the last table we need to add the two existentials we made earlier
				fac.addExistsAtom(m1, 2, new String[] {joinVar1, joinVar2});
				
				break;
				
			case SOtgds:				
				for(int i = 0; i <= numOfTgtTables; i++) 
				{
					int offset = i * attsPerTargetRel;
		        	int numAtts = (i < numOfTgtTables - 1) ? attsPerTargetRel : attsPerTargetRel + attrRemainder;
		        	
		        	if (i == 0)
		        		joinVar1 = fac.getFreshVars(offset + i + numAtts, 1)[0];
		        	else
		        		joinVar2 = fac.getFreshVars(offset + i + numAtts, 1)[0];
		        	
		        	fac.addEmptyExistsAtom(m1, i);
		        	
		        	if (i < 2)
		        		fac.addVarsToExistsAtom(m1, i, fac.getFreshVars(offset, numAtts));
		        
		        	generateSKs(m1, i, offset, numAtts);
				}
				break;
		}
	}
	
	private void generateSKs(MappingType m1, int rel, int offset, int numAtts) 
	{	
		switch (rel)
		{
			case 0: 
				sk1Args = fac.getFreshVars(offset, numAtts);
				skId1 = fac.addSKToExistsAtom(m1, rel, sk1Args);
				break;
			case 1:
				sk2Args = fac.getFreshVars(offset, numAtts);
				skId2 = fac.addSKToExistsAtom(m1, rel, sk2Args);
				break;
			case 2:
				fac.addSKToExistsAtom(m1, rel, sk1Args, skId1);
				fac.addSKToExistsAtom(m1, rel, sk2Args, skId2);
				break;
		}
	}
	
	@Override
	protected void genTransformations() throws Exception {
		SPJQuery q;
		SPJQuery genQuery = genQuery(new SPJQuery());
		
		for(int i = 0; i <= numOfTgtTables; i++) {
			String creates = m.getTargetRels().get(i).getName();
			q = (SPJQuery) genQuery.getSelect().getTerm(i);
			
			fac.addTransformation(q.toTrampString(m.getMapIds()[0]), m.getMapIds(), creates);
		}
	}
	
	private SPJQuery genQuery(SPJQuery generatedQuery) throws Exception {
		String sourceRelName = m.getSourceRels().get(0).getName();
		SPJQuery[] queries = new SPJQuery[numOfTgtTables + 1];
		MappingType m1 = m.getMaps().get(0);
	
		String joinAtt1;
		String joinAtt2;
		
		joinAtt1 = m.getAttrId(0, m.getNumRelAttr(0, false) - 1, false);
        joinAtt2 = m.getAttrId(1, m.getNumRelAttr(1, false) - 1, false);

		// gen query
		for(int i = 0; i < numOfTgtTables; i++) 
		{
			String targetRelName = m.getTargetRels().get(i).getName();
			int numAttr = (i < numOfTgtTables - 1) ? attsPerTargetRel : attsPerTargetRel + attrRemainder;
			
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
		
		// generate the query for the "link" relation
		SPJQuery q = new SPJQuery();
		queries[2] = q;
		q.getFrom().add(new Variable("X"), new Projection(Path.ROOT, sourceRelName));
        generatedQuery.addTarget(m.getTargetRels().get(2).getName());
		
        if (mapLang.equals(MappingLanguageType.FOtgds)) {
        	skId1 = fac.getNextId("SK");
        	skId2 = fac.getNextId("SK");
        }
        
		// add skolem function for join
		for(int i = 0; i < numOfTgtTables; i++) 
		{
			SelectClauseList seli = queries[i].getSelect();
			SelectClauseList sel2 = q.getSelect();
			
			String name;
			int numVar;
			int numAttr = (i < numOfTgtTables - 1) ? attsPerTargetRel 
					: attsPerTargetRel + attrRemainder;

			// get sk function data
			if (mapLang.equals(MappingLanguageType.SOtgds)) 
			{
				SKFunction sk = m.getSkolemFromAtom(m1, false, i, numAttr);
				name = sk.getSkname();
				numVar = sk.getVarArray().length;
			}
			else 
			{
				if (i == 0)
					name = skId1;
				else
					name = skId2;
				
				if (i != 2)
					numVar = m.getNumRelAttr(i, false);
				else
					numVar = 2;
			}

			// create SK projection
			vtools.dataModel.expression.SKFunction stSK = new vtools.dataModel.expression.SKFunction(name);

			for(int k = 0; k < numVar; k++) 
			{			
				String sAttName = m.getAttrId(0, k, true);
				Projection att = new Projection(new Variable("X"), sAttName);
				stSK.addArg(att);
			}

			if(i == 0)
			{
				seli.add(joinAtt1, stSK);
				sel2.add(joinAtt1, stSK);
			}
			else
			{
				seli.add(joinAtt2, stSK);
				sel2.add(joinAtt2, stSK);
			}
		}
        
        // add the partial queries to the parent query
        // to form the whole transformation
        SelectClauseList pselect = pquery.getSelect();
        SelectClauseList gselect = generatedQuery.getSelect();
        for (int i = 0; i < numOfTgtTables+1; i++)
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
        	int numAtts = (i < numOfTgtTables - 1) ? attsPerTargetRel : attsPerTargetRel + attrRemainder;
        	
            for (int j = 0; j < numAtts; j++)
            	addCorr(0, offset + j, i, j);         
        }
	}
	
	@Override
	public ScenarioName getScenType() {
		return ScenarioName.VERTPARTITIONNTOM;
	}
}
