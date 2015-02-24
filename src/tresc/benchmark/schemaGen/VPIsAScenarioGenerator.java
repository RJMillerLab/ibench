package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.List;

import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;

import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;

//MN I've applied some modifications to correct correspondences of this mapping primitive - 16 April 2014
// very similar to merging scenario generator, with source and target schemas swapped

//MN IMPLEMENTED source and target reusability (chooseSourceRels and chooseTargetRels) - 3 May 2014
//MN ENHANCED genTargetRels to pass types of attributes of target relation as argument to addRelation - 8 May 2014
//MN ENHANCED genSourceRels to pass types of attributes of source relation as argument to addRelation - 13 May 2014
//MN FIXED the errors in foreign key generation - 24 June 2014

public class VPIsAScenarioGenerator extends AbstractScenarioGenerator
{
	public static final int MAX_NUM_TRIES = 10;
	
	private int numOfSrcTblAttr;
	private int numOfTgtTables;
	private int attsPerTargetRel;
	private int attrRemainder;
	private int keySize;
	
	//MN - considered an attribute to check whether we are reusing a target relation - 13 May 2014
	private boolean targetReuse;
	//MN

    public VPIsAScenarioGenerator()
    {
        ;
    }
    
    protected void initPartialMapping() 
    {
    	super.initPartialMapping();
    	
        numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
        //MN number of set elements refers to join size - 16 April 2014
        numOfTgtTables = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements, numOfSetElementsDeviation);
        keySize = Utils.getRandomNumberAroundSomething(_generator, primaryKeySize, primaryKeySizeDeviation);
        
        numOfTgtTables = (numOfTgtTables > 1) ? numOfTgtTables : 2;
        numOfSrcTblAttr = (numOfSrcTblAttr > 1) ? numOfSrcTblAttr : 2;
		keySize = (keySize >= numOfSrcTblAttr) ? numOfSrcTblAttr - 1 : keySize;
		keySize = (keySize > 0) ? keySize : 1;
    	
        attsPerTargetRel = (numOfSrcTblAttr-keySize) / numOfTgtTables;
        attrRemainder = (numOfSrcTblAttr-keySize) % numOfTgtTables; 
        
        //MN - 13 May 2014
        targetReuse = false;
        //MN
    }

    /*public void generateScenario(MappingScenario scenario, Configuration configuration)
    {
    	init(configuration, scenario);
       
        // whether we do star of chain joins
        for (int i = 0, imax = repetitions; i < imax; i++)
        {
            // decide how many attributes will the source table have
            int numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
                numOfElementsDeviation);

            // number of tables we will use in the target
            int numOfTgtTables = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements,
                numOfSetElementsDeviation);

            // decide the kind of join we will follow.
            JoinKind jk = JoinKind.values()[joinKind];
            if (jk == JoinKind.VARIABLE)
            {
                int tmp = Utils.getRandomNumberAroundSomething(_generator, 0, 1);
                if (tmp < 0)
                    jk = JoinKind.STAR;
                else jk = JoinKind.CHAIN;
            }
            createSubElements(source, target, numOfSrcTblAttr, numOfTgtTables, jk, i, pquery);
        }

    }*/

    /**
     * This is the main function. It generates a table in the source, a number
     * of tables in the target and a respective number of queries.
     */
    /*private void createSubElements(Schema source, Schema target, int numOfSrcTblAttr, int numOfTgtTables,
            JoinKind jk, int repetition, SPJQuery pquery)
    {   
        // since we add a key to the tables, we add one less free element to the source and target
        numOfSrcTblAttr--;
            
        // First create the source table
        String sourceRelName = Modules.nameFactory.getARandomName();
        String coding = getStamp() + repetition;
        sourceRelName = sourceRelName + "_" + coding;
        SMarkElement srcRel = new SMarkElement(sourceRelName, new Set(), null, 0, 0);
        srcRel.setHook(new String(coding));
        source.addSubElement(srcRel);
        
        // and populate that table with elements. The array attNames, keeps the
        // coding of these elements
        String[] attNames = new String[numOfSrcTblAttr];
        for (int i = 0; i < numOfSrcTblAttr; i++)
        {
            String namePrefix = Modules.nameFactory.getARandomName();
            coding = getStamp() + repetition + "A" + i;
            String srcAttName = namePrefix + "_" + coding;
            SMarkElement el = new SMarkElement(srcAttName, Atomic.STRING, null, 0, 0);
            el.setHook(new String(coding));
            srcRel.addSubElement(el);
            attNames[i] = srcAttName;
        }

        // create key for source table
        Key srcKey = new Key();
        srcKey.addLeftTerm(new Variable("X"), new Projection(Path.ROOT,srcRel.getLabel()));
        srcKey.setEqualElement(new Variable("X"));
        
        // create the actual key and add it to the source schema
        String randomName = Modules.nameFactory.getARandomName();
        String keyName = randomName + "_" + getStamp() + repetition + "KE0";
        SMarkElement es = new SMarkElement(keyName, Atomic.STRING, null, 0, 0);
        es.setHook(new String(getStamp() + repetition + "KE0"));
        srcRel.addSubElement(es);
        // add the key attribute to the source key
        srcKey.addKeyAttr(new Projection(new Variable("X"),keyName));
        
        // add constraint
        source.addConstraint(srcKey);

        // create the set of the partial (intermediate) queries
        // each query populates a target table. We also create the target tables
        SMarkElement[] trgTables = new SMarkElement[numOfTgtTables];
        SPJQuery[] queries = new SPJQuery[numOfTgtTables];
        for (int i = 0; i < numOfTgtTables; i++)
        {
            SPJQuery q = new SPJQuery();
            q.getFrom().add(new Variable("X"), new Projection(Path.ROOT, sourceRelName));
            queries[i] = q;

            String targetRelNamePrefix = Modules.nameFactory.getARandomName();
            coding = getStamp() + repetition + "TT" + i;
            String targetRelName = targetRelNamePrefix + "_" + coding;
            SMarkElement tgtRel = new SMarkElement(targetRelName, new Set(), null, 0, 0);
            tgtRel.setHook(new String(coding));
            target.addSubElement(tgtRel);
            trgTables[i] = tgtRel;
        }

        // we distribute the source atomic elements among the target relations
        // we add all the atomic elements in the partial queries
        // int attsPerTargetRel = (int) Math.ceil((float) numOfSrcTblAttr /
        // numOfTgtTables);
        int attsPerTargetRel = numOfSrcTblAttr / numOfTgtTables;
        int attrPos = 0;
        for (int ti = 0; ti < numOfTgtTables; ti++)
        {
            SelectClauseList sel = queries[ti].getSelect();
            SMarkElement tgtRel = trgTables[ti];
            for (int i = 0, imax = attsPerTargetRel; i < imax; i++)
            {
                String trgAttrName = attNames[attrPos];
                attrPos++;
                SMarkElement tgtAtomicElt = new SMarkElement(trgAttrName, Atomic.STRING, null, 0, 0);
                String hook = trgAttrName.substring(trgAttrName.indexOf("_"));
                tgtAtomicElt.setHook(hook);
                tgtRel.addSubElement(tgtAtomicElt);

                // since we added an attr in the target, we add an entry in the
                // respective select clause
                Projection att = new Projection(new Variable("X"), trgAttrName);
                sel.add(trgAttrName, att);
            }
        }

        // it may be the case that some elements are left over due to not
        // perfect division between integers. We add them all in the last
        // fragment
        for (int i = attrPos, imax = attNames.length; i < imax; i++)
        {
            String trgAttrName = attNames[i];
            SMarkElement tgtAtomicElt = new SMarkElement(trgAttrName, Atomic.STRING, null, 0, 0);
            String hook = trgAttrName.substring(trgAttrName.indexOf("_"));
            tgtAtomicElt.setHook(hook);
            trgTables[numOfTgtTables - 1].addSubElement(tgtAtomicElt);

            // since we added an attr in the target, we add an entry in the
            // respective select clause
            Projection att = new Projection(new Variable("X"), trgAttrName);
            queries[numOfTgtTables - 1].getSelect().add(trgAttrName, att);
        }

        // now we generate the join attributes in the target tables
        if (jk == JoinKind.STAR)
        {
            /~coding = getStamp() + repetition + "JoinAtt";
            String joinAttName = Modules.nameFactory.getARandomName() + "_" + coding;
            String joinAttNameRef = joinAttName + "Ref";

            SMarkElement joinAttElement = new SMarkElement(joinAttName, Atomic.STRING, null, 0, 0);
            joinAttElement.setHook(new String(coding));
            target.getSubElement(0).addSubElement(joinAttElement);
            // add to the first partial query a skolem function to generate
            // the join attribute in the first target table
            SelectClauseList sel0 = queries[0].getSelect();
            Function f0 = new Function("SK");
            for (int k = 0; k < numOfSrcTblAttr; k++)
            {
                Projection att = new Projection(new Variable("X"), attNames[k]);
                f0.addArg(att);
            }
            sel0.add(joinAttName, f0);
            queries[0].setSelect(sel0);~/
        	
        	// create key for target fragment
            Key tgtKey = new Key();
            tgtKey.addLeftTerm(new Variable("Y"), new Projection(Path.ROOT,target.getSubElement(0).getLabel()));
            tgtKey.setEqualElement(new Variable("Y"));
            
            // add the key to the target schema
            SMarkElement et = new SMarkElement(keyName, Atomic.STRING, null, 0, 0);
            et.setHook(new String(getStamp() + repetition + "KE0"));
            target.getSubElement(0).addSubElement(et);
            // add the key attribute to the target key
            tgtKey.addKeyAttr(new Projection(new Variable("Y"),keyName));
            
            // add constraint
            //target.addConstraint(tgtKey);
            
            SelectClauseList sel0 = queries[0].getSelect();
            Projection att0 = new Projection(new Variable("X"), keyName);
            sel0.add(keyName, att0);
            queries[0].setSelect(sel0);
            
            for (int i = 1; i < numOfTgtTables; i++)
            {
                SMarkElement keyElement = new SMarkElement(keyName, Atomic.STRING, null, 0, 0);
                keyElement.setHook(new String(coding + "Ref"));
                target.getSubElement(i).addSubElement(keyElement);
                // we create the target constraint(i.e. foreign key both ways )
                Variable varKey1 = new Variable("F");
                Variable varKey2 = new Variable("K");
                ForeignKey fKeySrc1 = new ForeignKey();
                fKeySrc1.addLeftTerm(varKey1.clone(),
                    new Projection(Path.ROOT, target.getSubElement(0).getLabel()));
                fKeySrc1.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,
                    target.getSubElement(i).getLabel()));
                fKeySrc1.addFKeyAttr(new Projection(varKey2.clone(), keyName), new Projection(
                    varKey1.clone(), keyName));
                target.addConstraint(fKeySrc1);
                ForeignKey fKeySrc2 = new ForeignKey();
                fKeySrc2.addLeftTerm(varKey1.clone(),
                    new Projection(Path.ROOT, target.getSubElement(i).getLabel()));
                fKeySrc2.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,
                    target.getSubElement(0).getLabel()));
                fKeySrc2.addFKeyAttr(new Projection(varKey2.clone(), keyName), new Projection(varKey1.clone(),
                		keyName));
                target.addConstraint(fKeySrc2);
                // add to the each partial query a skolem function to generate
                // the join
                // reference attribute in all the other target tables
                SelectClauseList seli = queries[i].getSelect();
                Projection att = new Projection(new Variable("X"), keyName);
                seli.add(keyName, att);
                queries[i].setSelect(seli);
            }
        }

        // add the partial queries to the parent query
        // to form the whole transformation
        SelectClauseList pselect = pquery.getSelect();
        for (int i = 0; i < numOfTgtTables; i++)
        {
            String tblTrgName = trgTables[i].getLabel();
            pselect.add(tblTrgName, queries[i]);
        }
        pquery.setSelect(pselect);
    }*/

	//MN implemented chooseSourceRels method to support source reusability - 3 May 2014
	@Override
	protected boolean chooseSourceRels() throws Exception {
		//MN I need to discuss it with Patricia - 3 May 2014
		int minAttrs = keySize;
		//MN - 13 May 2014
		if(keySize<numOfTgtTables)
			minAttrs = numOfTgtTables;
		//MN
		
		RelationType rel;
		
		//MN get a random relation - 26 April 2014
		rel = getRandomRel(true, minAttrs);
		
		if (rel == null) 
			return false;
		
		numOfSrcTblAttr = rel.sizeOfAttrArray();
		//MN reevaluate the following fields (tried to preserve initial value of numOfTgtTables not keySize) - 26 April 2014
        attsPerTargetRel = (numOfSrcTblAttr-keySize) / numOfTgtTables;
        attrRemainder = (numOfSrcTblAttr-keySize) % numOfTgtTables; 
		
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
		
		return true;
	}
	
	@Override
	protected void genSourceRels() throws Exception 
	{
		String sourceRelName = randomRelName(0);
		String[] attrs = new String[numOfSrcTblAttr];
		String hook = getRelHook(0);
		
		//MN - 13 May 2014
		String[] attrsType = new String[numOfSrcTblAttr];
		//MN
		
		// generate the appropriate number of keys
		String[] keys = new String[keySize];
		for (int j = 0; j < keySize; j++)
			keys[j] = randomAttrName(0, 0) + "ke" + j;

		// make the first keySize elements part of the primary key and insert them into the relation
		int keyCount = 0;
		for (int i = 0; i < numOfSrcTblAttr; i++) {
			String attrName = randomAttrName(0, i);

			if (keyCount < keySize)
				attrName = keys[keyCount];

			keyCount++;

			attrs[i] = attrName;
		}
		
		if (log.isDebugEnabled()) {log.debug("----------" + sourceRelName + "----------");};
		
		for (String a : attrs)
			if (log.isDebugEnabled()) {log.debug("attr: " + a);};
		
		//MN
		//MN - 13 May 2014
		if(targetReuse){
				int count =0;
				for(int i=0; i<keySize; i++)
					attrsType[i] = m.getTargetRels().get(0).getAttrArray(i).getDataType();
				count = keySize;
				for(int i=0; i<numOfTgtTables; i++){
					if(i != numOfTgtTables){
						for(int j=keySize; j<attsPerTargetRel+keySize; j++){
								attrsType[count] = m.getTargetRels().get(i).getAttrArray(j).getDataType();
								count++;
						}
					}
					else{
						for(int j=keySize; j<attsPerTargetRel + attrRemainder + keySize; j++){
							attrsType[count] = m.getTargetRels().get(i).getAttrArray(j).getDataType();
							count++;
					}
					}
				}
		}
		//MN
		
		
		RelationType sRel = null;
		
		if(!targetReuse)
			sRel = fac.addRelation(hook, sourceRelName, attrs, true);
		else
			sRel = fac.addRelation(hook, sourceRelName, attrs, attrsType, true);
		
		m.addSourceRel(sRel);
		
		fac.addPrimaryKey(sourceRelName, keys, true);
		
		//MN - 13 May 2014
		targetReuse = false;
		//MN
	}

	//MN implemented chooseTargetRels method to support target reusability - 3 May 2014
	//MN assumptions - (1): attrRemainder =0 - 3 May 2014
	@Override
	protected boolean chooseTargetRels() throws Exception {
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
				if(keySize < attsPerTargetRel)
					rel = getRandomRel(false, attsPerTargetRel);
				else
					//MN we use minimum to increase reusability - 3 May 2014
					rel = getRandomRel(false, keySize);
			}
			else{
				rel = getRandomRel(false, attsPerTargetRel+keySize, attsPerTargetRel+keySize);
					
				if(rel != null){
					for(int j=0; j<rels.size(); j++)
						if(rels.get(j).getName().equals(rel.getName()))
							found = false;
				}else{
					found = false;
				}
			}
				
			//MN VPISA cares about primary key - 26 April 2014
			if(found && !rel.isSetPrimaryKey()) {
				//MN set to false because this is target relation (Am I right?) - 3 May 2014
				int [] primaryKeyPos = new int [keySize];
				for(int i=0; i<keySize; i++)
					primaryKeyPos[i] = i;
				fac.addPrimaryKey(rel.getName(), primaryKeyPos, false);
			}
				
			if(found && rel.isSetPrimaryKey()){
				//MN keySize should be keySize and key attrs should be the first attrs- 3 May 2014
				int[] pkPos = model.getPKPos(rel.getName(), false);
					
				if(pkPos[0] != 0)
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
				if(created == 0){
					keySize = model.getPKPos(rel.getName(), false).length; 
					attsPerTargetRel = rel.getAttrArray().length- keySize;
				}
				
				created++;
				numTries = 0;
			}
			// not found, have exhausted number of tries? then create new one - path tested 1 May 2014
			else {
				numTries++;
				if (numTries >= MAX_NUM_TRIES)
				{
					numTries = 0;
					attrs[created] = new String[attsPerTargetRel+keySize];
					for(int j = 0; j < attsPerTargetRel+keySize; j++)
						attrs[created][j] = randomAttrName(created, j);
						
					// create the relation
					String relName = randomRelName(created);
					rels.add(fac.addRelation(getRelHook(created), relName, attrs[created], false));
						
					// primary key should be set and its size should be 1- 26 April 2014
					int [] primaryKeyPos = new int [keySize];
					for(int i=0; i<keySize; i++)
						primaryKeyPos[i] = i;
					fac.addPrimaryKey(relName, primaryKeyPos, false);
						
					//MN should I add it to TargetRel? - 26 April 2014
					//m.addTargetRel(rels.get(rels.size()-1));
						
					created++;
					numTries = 0;
				}
			}
		}
			
		//MN set attrRemainder and numOfSrcTables - 26 April 2014
		numOfSrcTblAttr= (numOfTgtTables * attsPerTargetRel) + keySize; 
		//MN considering only this case - 26 April 2014
		attrRemainder =0;
			
		//MN foreign key should be set - 26 April 2014
		//MN should be fixed - 24 June 2014
		addFKsNoReuse();
		
		//MN - 13 May 2014
		targetReuse = true;
		//MN
		return true;
	}
	
	//MN FIXED addFKs - 24 June 2014
	private void addFKsNoReuse() 
	{
			for(int i = 1; i < numOfTgtTables; i++) 
			{
				// add a variable number of foreign keys per table (always the first keySize attributes)
				//MN fixed the errors of incorrectly generating foreign key - 24 June 2014
				//for (int j = 0; j < keySize; j++)
					//addFK(i, j, 0, j, false);
				String[] fAttr = new String[keySize];
				for(int j=0; j<keySize; j++)
					fAttr[j] = m.getTargetRels().get(i).getAttrArray(j).getName();
				
				String[] tAttr = new String[keySize];
				for (int j=0; j<keySize; j++)
					tAttr[j] = m.getTargetRels().get(0).getAttrArray(j).getName();
				
				addFK(i, fAttr, 0, tAttr, false);
			}
	}
	
	@Override
	protected void genTargetRels() throws Exception 
	{
		String[] attrs;
		String[] attrsType;
		String[] attrsTypeKey;
		String[] srcAttrs = m.getAttrIds(0, true);
		
		// create key names with JoinAttr/Ref tacked on
		String[] keyAttNames = new String[keySize];
		String[] keyAttNameRef = new String[keySize];
		
		attrsTypeKey = new String [keySize];
		
		for(int i = 0; i < keySize; i++)
		{
			keyAttNames[i] = srcAttrs[i] + "JoinAttr";
			keyAttNameRef[i] = srcAttrs[i] + "Ref";
			//MN BEGIN - 8 May 2014
			attrsTypeKey[i] = m.getSourceRels().get(0).getAttrArray(i).getDataType();
			//MN END
		}
		
        // the offset should be (current offset) + keySize
        // if its the first table add in the Join Attrs, otherwise add in the Refs
        // then add in the normal attributes (copied directly from the source table)
        // then set the primary key 
        
        for (int i = 0; i < numOfTgtTables; i++)
        {
        	String trgName = randomRelName(i);
        	String hook = getRelHook(i);
        	
        	if (log.isDebugEnabled()) {log.debug("----------" + trgName + "----------");};
        	
        	// offset determines which source attributes go to which table (so there is no overlap)
        	int offset = i * attsPerTargetRel;
        	
        	int attrNum = (i < numOfTgtTables - 1) ? (attsPerTargetRel + keySize) : (attsPerTargetRel + attrRemainder + keySize);
        	attrs = new String[attrNum];
        	//MN BEGIN
        	attrsType = new String[attrNum];
        	//MN END
        	
        	if (log.isDebugEnabled()) {log.debug("attrNum: " + attrNum);};
        	
        	int j = 0;
        	//MN BEGIN
        	int index = 0;
        	//MN END
        	// if its the first relation then we add a join attribute, otherwise it is a foreign key so we use the ref attribute
            if (i==0)
	            for (String key : keyAttNames){
	            	attrs[j] = key;
	            	//MN BEGIN - 8 May 2014
	            	attrsType[j++] = attrsTypeKey[index];
	            	index++;
	            	//MN END
	            }
            else
            	for (String key : keyAttNameRef){
	            	attrs[j] = key;
	            	//MN BEGIN - 8 May 2014
	            	attrsType[j++] = attrsTypeKey[index];
	            	index++;
	            	//MN END
            	}
            
        	// create normal attributes for table (copy from source)
            for (; j < attrNum; j++){
            	attrs[j] = srcAttrs[offset + j];
            	//MN BEGIN
            	attrsType[j] = m.getSourceRels().get(0).getAttrArray(j + offset).getDataType();
            	//MN END
            }
            
            for (String s : attrs)
        	   if (log.isDebugEnabled()) {log.debug("attr: " + s);};
            
        	//MN BEGIN enhancement - 3 May 2014 and 8 May 2014
            String[] attrsCopy = new String[attrs.length];
            for(int h=0; h<attrs.length; h++)
               	attrsCopy[h] = attrsType[h];
            fac.addRelation(hook, trgName, attrs, attrsCopy, false);
            //MN END
            
            if (i==0)
            	fac.addPrimaryKey(trgName, keyAttNames, false);
            else
            	addFKs(attrs, i);
        }
        
        //addFKs();
	}
	
	//MN FIXED addFKs - 24 June 2014
	private void addFKs(String attrs[], int relNum) 
	{
		//for(int i = 1; i < numOfTgtTables; i++) 
		//{
			// add a variable number of foreign keys per table (always the first keySize attributes)
			//MN fixed the errors of incorrectly generating foreign key - 24 June 2014
			//for (int j = 0; j < keySize; j++)
				//addFK(i, j, 0, j, false);
			String[] fAttr = new String[keySize];
			for(int i=0; i<keySize; i++)
				fAttr[i] = attrs[i];
			
			String[] tAttr = new String[keySize];
			for (int i=0; i<keySize; i++)
				tAttr [i] = m.getTargetRels().get(0).getAttrArray(i).getName();
			
			addFK(relNum, fAttr, 0, tAttr, false);
		//}
	}
	
	@Override
	protected void genMappings() throws Exception 
	{
		MappingType m1 = fac.addMapping(m.getCorrs());
		String[] srcVars = fac.getFreshVars(0, numOfSrcTblAttr);
		
		// get the variables to be used for the keys
		String[] keyVars = fac.getFreshVars(0, keySize);
		
		fac.addForeachAtom(m1, 0, srcVars);
		
		for(int i = 0; i < numOfTgtTables; i++) 
		{
			int offset = i * attsPerTargetRel;
        	int numAtts = (i < numOfTgtTables - 1) ? (attsPerTargetRel + keySize) : (attsPerTargetRel + attrRemainder + keySize);
        	
        	String[] tgtVars = new String[numAtts];
        	
        	// add in the key vars first
        	int k = 0;
        	for (String key : keyVars)
        		tgtVars[k++] = key;
            
        	// now split the remaining vars appropriately
        	for (; k < numAtts; k++)
        		tgtVars[k] = srcVars[k+offset];
        		
        	fac.addExistsAtom(m1, i, tgtVars);
		}
	}
	
	@Override
	protected void genTransformations() throws Exception 
	{
		SPJQuery q;
		SPJQuery genQuery = genQuery(new SPJQuery());
		
		for(int i = 0; i < numOfTgtTables; i++) {
			String creates = m.getTargetRels().get(i).getName();
			q = (SPJQuery) genQuery.getSelect().getTerm(i);
			//MN BEGIN 16 August 2014
			fac.addTransformation(q.toTrampString(m.getMapIds()[0]), m.getMapIds(), creates);
			//MN changed the line above to the following line - 16 August 2014
//			fac.addTransformation(" ", m.getMapIds(), creates);
			//MN END
		}
	}
	
	private SPJQuery genQuery(SPJQuery generatedQuery) {
		String sourceRelName = m.getSourceRels().get(0).getName();
		SPJQuery[] queries = new SPJQuery[numOfTgtTables];
		
		// gen query
		for(int i = 0; i < numOfTgtTables; i++) {
			String targetRelName = m.getTargetRels().get(i).getName();
			int numAttr = ((i < numOfTgtTables - 1) ? 
						(attsPerTargetRel + keySize) : 
						(attsPerTargetRel + attrRemainder + keySize));
			
			// gen query for the target table
			SPJQuery q = new SPJQuery();
			queries[i] = q;
	        q.getFrom().add(new Variable("X"), new Projection(Path.ROOT, sourceRelName));
	        generatedQuery.addTarget(targetRelName);
	        SelectClauseList sel = q.getSelect();
	        
	        for (int j = 0; j < numAttr; j++) {
	        	String trgAttrName = m.getAttrId(i, j, false);
	        	
	        	Projection att;
	        	
	        	if(j < keySize)
	        		att = new Projection(new Variable("X"), m.getAttrId(0, j, true));
	        	else
	        		att = new Projection(new Variable("X"), trgAttrName);
	        	
				sel.add(trgAttrName, att);
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
	//MN I've applied some modifications to correct value correspondences in this mapping scenarios - 16 April 2014
	protected void genCorrespondences() 
	{
		for (int i = 0; i < numOfTgtTables; i++)
        {
        	int offset = i * attsPerTargetRel;
        	int numAtts = (i < numOfTgtTables - 1) ? (attsPerTargetRel + keySize) :
    				(attsPerTargetRel + attrRemainder + keySize);
        	
        	int keyIndex =0;
            for (int j = 0; j < numAtts; j++)
            	if((j>=keySize) || ((j<keySize) && (i==0))){
            		addCorr(0, offset + j, i, j);}
            	else{
            		addCorr(0, 0 + keyIndex, i, j);
            		keyIndex++;
            	}
        }
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.VERTPARTITIONISA;
	}
}
