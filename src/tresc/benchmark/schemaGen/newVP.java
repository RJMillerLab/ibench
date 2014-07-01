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

//MN implemented new Vertical Partitioning - 23 June 2014
//MN limitation: keySize =1 - 23 June 2014
//MN only investigated genSource and genTarget - 23 June 2014
//MN the only difference between this scenario and vertical partitioning is in foreign keys - 23 June 2014
//MN ToDo: enhance chooseTargetRels to check existing foreign keys on reused target relations - 23 June 2014
public class newVP extends AbstractScenarioGenerator{
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
	
    public newVP()
    {
        ;
    }

    
    protected void initPartialMapping() {
    	super.initPartialMapping();
    	
        numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
            numOfElementsDeviation);
        
        
        numOfSrcTblAttr = (numOfSrcTblAttr > 2 ? numOfSrcTblAttr : 2);
        //MN numOfSetElement is JoinSize
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
        
		
		//keySize = Utils.getRandomNumberAroundSomething(_generator, primaryKeySize, primaryKeySizeDeviation);
        keySize = 1;
        
		sk = SkolemKind.values()[typeOfSkolem];
		// adjust keySize as necessary with respect to number of source table attributes
		// NOTE: we are not strictly enforcing a source key for VERTICAL PARTITION, unless SkolemKind.KEY explicitly requested
		//keySize = (keySize >= numOfSrcTblAttr) ? numOfSrcTblAttr - 1 : keySize;
		//if (sk == SkolemKind.KEY)
			//keySize = (keySize > 0) ? keySize : 1;
		
		targetReuse = false;
    }

	

    /**
     * This is the main function. It generates a table in the source, a number
     * of tables in the target and a respective number of queries.
     * @throws Exception 
     */
    /*private SMarkElement createSubElements(Schema source, Schema target, int numOfSrcTblAttr, int numOfTgtTables,
            JoinKind jk, int repetition, SPJQuery pquery, SPJQuery generatedQuery)
    {
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
            generatedQuery.addTarget(tgtRel.getLabel());
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
            coding = getStamp() + repetition + "JoinAtt";
            String joinAttName = Modules.nameFactory.getARandomName() + "_" + coding;
            String joinAttNameRef = joinAttName + "Ref";

            SMarkElement joinAttElement = new SMarkElement(joinAttName, Atomic.STRING, null, 0, 0);
            joinAttElement.setHook(new String(coding));
            target.getSubElement(0).addSubElement(joinAttElement);
            
            // add to the first partial query a skolem function to generate
            // the join attribute in the first target table
            SelectClauseList sel0 = queries[0].getSelect();
            SKFunction f0 = new SKFunction("SK");
            for (int k = 0; k < numOfSrcTblAttr; k++)
            {
                Projection att = new Projection(new Variable("X"), attNames[k]);
                f0.addArg(att);
            }
            sel0.add(joinAttName, f0);
            queries[0].setSelect(sel0);

            for (int i = 1; i < numOfTgtTables; i++)
            {
                SMarkElement joinAttRefElement = new SMarkElement(joinAttNameRef, Atomic.STRING, null, 0, 0);
                joinAttRefElement.setHook(new String(coding + "Ref"));
                target.getSubElement(i).addSubElement(joinAttRefElement);
                // we create the target constraint(i.e. foreign key both ways )
                Variable varKey1 = new Variable("F");
                Variable varKey2 = new Variable("K");
                ForeignKey fKeySrc1 = new ForeignKey();
                fKeySrc1.addLeftTerm(varKey1.clone(),
                    new Projection(Path.ROOT, target.getSubElement(0).getLabel()));
                fKeySrc1.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,
                    target.getSubElement(i).getLabel()));
                fKeySrc1.addFKeyAttr(new Projection(varKey2.clone(), joinAttNameRef), new Projection(
                    varKey1.clone(), joinAttName));
                target.addConstraint(fKeySrc1);
                ForeignKey fKeySrc2 = new ForeignKey();
                fKeySrc2.addLeftTerm(varKey1.clone(),
                    new Projection(Path.ROOT, target.getSubElement(i).getLabel()));
                fKeySrc2.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,
                    target.getSubElement(0).getLabel()));
                fKeySrc2.addFKeyAttr(new Projection(varKey2.clone(), joinAttName), new Projection(varKey1.clone(),
                    joinAttNameRef));
                target.addConstraint(fKeySrc2);
                // add to the each partial query a skolem function to generate
                // the join
                // reference attribute in all the other target tables
                SelectClauseList seli = queries[i].getSelect();
                Function fi = (Function) f0.clone();
                seli.add(joinAttNameRef, fi);
                queries[i].setSelect(seli);
            }
        }

        if (jk == JoinKind.CHAIN)
        {
            // create a skolem function which has all the
            // source attributes as arguments
            Function f = new Function("SK");
            for (int k = 0; k < numOfSrcTblAttr; k++)
            {
                Projection att = new Projection(new Variable("X"), attNames[k]);
                f.addArg(att);
            }

            for (int i = 0; i < numOfTgtTables - 1; i++)
            {
            	int tgtPos = repetition * numOfTgtTables + i;
                coding = getStamp() + repetition + "JoinAtt";
                String joinAttName = Modules.nameFactory.getARandomName() + "_" + coding;
                String joinAttNameRef = joinAttName + "Ref";

                SMarkElement joinAttElement = new SMarkElement(joinAttName, Atomic.STRING, null, 0, 0);
                joinAttElement.setHook(new String(coding));
                SMarkElement joinAttRefElement = new SMarkElement(joinAttNameRef, Atomic.STRING, null, 0, 0);
                joinAttRefElement.setHook(new String(coding + "Ref"));

                target.getSubElement(tgtPos).addSubElement(joinAttElement);
                target.getSubElement(tgtPos + 1).addSubElement(joinAttRefElement);
                // we create the target constraint(i.e. foreign key both ways )
                Variable varKey1 = new Variable("F");
                Variable varKey2 = new Variable("K");
                
                ForeignKey fKeySrc1 = new ForeignKey();
                fKeySrc1.addLeftTerm(varKey1.clone(),
                    new Projection(Path.ROOT, target.getSubElement(tgtPos).getLabel()));
                fKeySrc1.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,
                    target.getSubElement(tgtPos + 1).getLabel()));
                fKeySrc1.addFKeyAttr(new Projection(varKey2.clone(), joinAttNameRef), new Projection(
                    varKey1.clone(), joinAttName));
                target.addConstraint(fKeySrc1);
                
                ForeignKey fKeySrc2 = new ForeignKey();
                fKeySrc2.addLeftTerm(varKey1.clone(), new Projection(Path.ROOT,
                    target.getSubElement(tgtPos + 1).getLabel()));
                fKeySrc2.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,
                    target.getSubElement(tgtPos).getLabel()));
                fKeySrc2.addFKeyAttr(new Projection(varKey2.clone(), joinAttName), new Projection(varKey1.clone(),
                    joinAttNameRef));
                target.addConstraint(fKeySrc2);
                // add to each partial query the skolem function that generates
                // the join attribute
                // and the join reference attribute in each target table
                SelectClauseList sel1 = queries[i].getSelect();
                Function f1 = (Function) f.clone();
                sel1.add(joinAttName, f1);
                queries[i].setSelect(sel1);
                SelectClauseList sel2 = queries[i + 1].getSelect();
                Function f2 = (Function) f.clone();
                sel2.add(joinAttNameRef, f2);
                queries[i + 1].setSelect(sel2);
            }
        }

        // add the partial queries to the parent query
        // to form the whole transformation
        SelectClauseList pselect = pquery.getSelect();
        SelectClauseList gselect = generatedQuery.getSelect();
        for (int i = 0; i < numOfTgtTables; i++)
        {
            String tblTrgName = trgTables[i].getLabel();
            pselect.add(tblTrgName, queries[i]);
            gselect.add(tblTrgName, queries[i]);
        }
        pquery.setSelect(pselect);
        generatedQuery.setSelect(gselect);
        return srcRel;
    }*/

	@Override
	protected void genSourceRels() throws Exception {
		String sourceRelName = randomRelName(0);
		String[] attNames = new String[numOfSrcTblAttr];
		String hook = getRelHook(0);
		
		String[] attrsType = new String[numOfSrcTblAttr];
		
		// for (int i = 0; i < numOfSrcTblAttr; i++)
		// 	attNames[i] = randomAttrName(0, i);
		
		
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
		
		RelationType sRel = null;
		if(!targetReuse)
			 sRel = fac.addRelation(hook, sourceRelName, attNames, true);
		else
			 sRel = fac.addRelation(hook, sourceRelName, attNames, attrsType, true);
		
		
		if (keySize > 0 )
			fac.addPrimaryKey(sourceRelName, keys, true);
		
		m.addSourceRel(sRel);
		
		targetReuse = false;
		
	}

	
	@Override
	protected boolean chooseSourceRels() throws Exception {
		int minAttrs = numOfTgtTables;
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
	}
	
	//MN implemented chooseTargetRels method to support target reusability - 26 April 2014
	//MN assumptions - (1): attrRemainder =0 (2): keySize =1 - 26 April 2014
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
	}
	
	
	@Override
	protected void genTargetRels() throws Exception {
        String[] attrs;
        String[] attrsType;
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
            
        	int fkAttrs = 1;
        	int attWithFK = attrNum + fkAttrs;
        	attrs = new String[attWithFK];
        	attrsType = new String[attWithFK];
        	
        
        	// create normal attributes for table (copy from source)
            for (int j = 0; j < attrNum; j++){
            	attrs[j] = srcAttrs[offset + j];
            	attrsType[j] = m.getSourceRels().get(0).getAttrArray(offset + j).getDataType();
            }
            
            // create the join attributes
            // for star join the first one has the join attribute and the following ones 
            // have the join reference (FK)
            if (jk == JoinKind.STAR) {
            	if (i == 0)//TODO check
            	{
            		attrs[attrs.length - 1] = joinAttName;
            		attrsType[attrs.length - 1] = "TEXT";
            	}
            	else{
            		attrs[attrs.length - 1] = joinAttNameRef;
            		attrsType[attrs.length - 1] = "TEXT";
            	}
            // for chain join each one has one join attribute             	
//            OLD	has a join and join ref to the previous
//             thus, the first does not have a ref and the last one does not have a join attr
            } else { // chain
//            	if (i == 0)
            		attrs[attrs.length - 1] = joinAttName;
            		attrsType[attrs.length - 1] = "TEXT";
//            	else if (i == numOfTgtTables - 1)
//            		attrs[attrs.length - 1] = joinAttNameRef;
//            	else {
//            		attrs[attrs.length - 2] = joinAttName;
//            		attrs[attrs.length - 1] = joinAttNameRef;
//            	}
            }
            
            //MN changed the following line - 4 May 2014 and 8 May 2014
            String[] attrsCopy = new String [attrs.length];
            for(int h=0; h<attrs.length; h++)
            	attrsCopy[h] = attrsType[h];
            fac.addRelation(hook, trgName, attrs, attrsCopy, false);
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

		if (log.isDebugEnabled()) {log.debug("newVP - Method generateSKs() with totalVars = " + numOfSrcTblAttr + " and Num of New Skolems = 1");};
		
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
		return ScenarioName.VERTPARTITIONIASAAUTHORITY;
	}
}
