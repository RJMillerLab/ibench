package tresc.benchmark.schemaGen;

import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;

import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;

// very similar to merging scenario generator, with source and target schemas swapped
public class VPIsAScenarioGenerator extends AbstractScenarioGenerator
{
	private int numOfSrcTblAttr;
	private int numOfTgtTables;
	private int attsPerTargetRel;
	private int attrRemainder;
	private int keySize;

    public VPIsAScenarioGenerator()
    {
        ;
    }
    
    protected void initPartialMapping() 
    {
    	super.initPartialMapping();
    	
        numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
        numOfTgtTables = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements, numOfSetElementsDeviation);
        keySize = Utils.getRandomNumberAroundSomething(_generator, primaryKeySize, primaryKeySizeDeviation);
        
        numOfTgtTables = (numOfTgtTables > 1) ? numOfTgtTables : 2;
        numOfSrcTblAttr = (numOfSrcTblAttr > 1) ? numOfSrcTblAttr : 2;
		keySize = (keySize >= numOfSrcTblAttr) ? numOfSrcTblAttr - 1 : keySize;
		keySize = (keySize > 0) ? keySize : 1;
    	
        attsPerTargetRel = (numOfSrcTblAttr-keySize) / numOfTgtTables;
        attrRemainder = (numOfSrcTblAttr-keySize) % numOfTgtTables; 
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


	@Override
	protected void genSourceRels() throws Exception 
	{
		String sourceRelName = randomRelName(0);
		String[] attrs = new String[numOfSrcTblAttr];
		String hook = getRelHook(0);
		
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
		
		RelationType sRel = fac.addRelation(hook, sourceRelName, attrs, true);
		m.addSourceRel(sRel);
		
		fac.addPrimaryKey(sourceRelName, keys, true);
	}

	@Override
	protected void genTargetRels() throws Exception 
	{
		String[] attrs;
		String[] srcAttrs = m.getAttrIds(0, true);
		
		// create key names with JoinAttr/Ref tacked on
		String[] keyAttNames = new String[keySize];
		String[] keyAttNameRef = new String[keySize];
		
		for(int i = 0; i < keySize; i++)
		{
			keyAttNames[i] = srcAttrs[i] + "JoinAttr";
			keyAttNameRef[i] = srcAttrs[i] + "Ref";
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
        	
        	if (log.isDebugEnabled()) {log.debug("attrNum: " + attrNum);};
        	
        	int j = 0;
        	// if its the first relation then we add a join attribute, otherwise it is a foreign key so we use the ref attribute
            if (i==0)
	            for (String key : keyAttNames)
	            	attrs[j++] = key;
            else
            	for (String key : keyAttNameRef)
	            	attrs[j++] = key;
            
        	// create normal attributes for table (copy from source)
            for (; j < attrNum; j++)
            	attrs[j] = srcAttrs[offset + j];
            
            for (String s : attrs)
        	   if (log.isDebugEnabled()) {log.debug("attr: " + s);};
            
            fac.addRelation(hook, trgName, attrs, false);
            
            if (i==0)
            	fac.addPrimaryKey(trgName, keyAttNames, false);
        }
        
        addFKs();
	}
	
	private void addFKs() 
	{
		for(int i = 1; i < numOfTgtTables; i++) 
		{
			// add a variable number of foreign keys per table (always the first keySize attributes)
			for (int j = 0; j < keySize; j++)
				addFK(i, j, 0, j, false);
		}
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
			
			fac.addTransformation(q.toTrampString(m.getMapIds()[0]), m.getMapIds(), creates);
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
	protected void genCorrespondences() 
	{
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
		return ScenarioName.VERTPARTITIONISA;
	}
}
