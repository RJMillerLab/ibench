package tresc.benchmark.schemaGen;

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

// very similar to merging scenario generator, with source and target schemas swapped
public class VerticalPartitionScenarioGenerator extends AbstractScenarioGenerator {

	private JoinKind jk;
	private int numOfSrcTblAttr;
	private int numOfTgtTables;
	private int attsPerTargetRel;
	private int attrRemainder;
	private SkolemKind sk = SkolemKind.ALL;
	private String skId;
    
    public VerticalPartitionScenarioGenerator()
    {
        ;
    }

    
    protected void initPartialMapping() {
    	super.initPartialMapping();
    	
        numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
            numOfElementsDeviation);

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
    }

	

    /**
     * This is the main function. It generates a table in the source, a number
     * of tables in the target and a respective number of queries.
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
	protected void genSourceRels() {
		String sourceRelName = randomRelName(0);
		String[] attNames = new String[numOfSrcTblAttr];
		String hook = getRelHook(0);
		
		for (int i = 0; i < numOfSrcTblAttr; i++)
			attNames[i] = randomAttrName(0, i);
		
		RelationType sRel = fac.addRelation(hook, sourceRelName, attNames, true);
		m.addSourceRel(sRel);
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
        	int fkAttrs = ((jk == JoinKind.CHAIN && 
        			(i != 0 && i != numOfTgtTables - 1)) 
        			? 2 : 1);
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
            // for chain join each one has a join and join ref to the previous
            // thus, the first does not have a ref and the last one does not have a join attr
            } else { // chain
            	if (i == 0)
            		attrs[attrs.length - 1] = joinAttName;
            	else if (i == numOfTgtTables - 1)
            		attrs[attrs.length - 1] = joinAttNameRef;
            	else {
            		attrs[attrs.length - 2] = joinAttName;
            		attrs[attrs.length - 1] = joinAttNameRef;
            	}
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
            	if (i == 0)
            		fac.addPrimaryKey(trgName, joinAttName, false);
            	else if (i == numOfTgtTables - 1)
            		fac.addPrimaryKey(trgName, joinAttNameRef, false);
            	else 
            		fac.addPrimaryKey(trgName, new String[] {joinAttName, joinAttNameRef}, false);
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
			int toA = m.getNumRelAttr(1, false) - 1;
			int fromA = m.getNumRelAttr(0, false) - 1;
			addFK(0, fromA, 1, toA, false);
			addFK(1, toA, 0, fromA, false);
			for(int i = 1; i < numOfTgtTables - 1; i++) {
				toA = m.getNumRelAttr(i + 1, false) - 1;
				fromA = m.getNumRelAttr(i, false) - 2;
				addFK(i, fromA, i+1, toA, false);
				addFK(i+1, toA, i, fromA, false);
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
				for(int i = 0; i < numOfTgtTables; i++) {
					int offset = i * attsPerTargetRel;
		        	int numAtts = (i < numOfTgtTables - 1) ? attsPerTargetRel :
		    				attsPerTargetRel + attrRemainder;
		        	
		        	fac.addExistsAtom(m1, i, fac.getFreshVars(offset, numAtts));
				}
				break;
				
			case SOtgds:
				for(int i = 0; i < numOfTgtTables; i++) {
					int offset = i * attsPerTargetRel;
		        	int numAtts = (i < numOfTgtTables - 1) ? attsPerTargetRel :
		    				attsPerTargetRel + attrRemainder;
		        	
		        	//RelAtomType atom = fac.addEmptyExistsAtom(m1, 0);
		        	fac.addEmptyExistsAtom(m1, i);
		        	fac.addVarsToExistsAtom(m1, i, fac.getFreshVars(offset, numAtts));
		        	SkolemKind sk1 = sk;
					if(sk == SkolemKind.VARIABLE)
						sk1 = SkolemKind.values()[_generator.nextInt(4)];
		        	generateSKs(m1, i, offset, numAtts, sk1);
				}
				break;
		}
	}
	
	private void generateSKs(MappingType m1, int rel, int offset, int numAtts, SkolemKind sk) {
		int numArgsForSkolem = numOfSrcTblAttr;

		// generate random number arguments for skolem function
		if (sk == SkolemKind.RANDOM)
			numArgsForSkolem = Utils.getRandomNumberAroundSomething(_generator, numOfSrcTblAttr / 2, numOfSrcTblAttr / 2);

		// ensure that we are still within the bounds of the number of source attributes
		numArgsForSkolem = (numArgsForSkolem > numOfSrcTblAttr) ? numOfSrcTblAttr : numArgsForSkolem;

		// check if we are only using the exchanged attributes in the skolem and change the starting point appropriately
		int start = 0;
		if(sk == SkolemKind.EXCHANGED)
		{
			start = offset;
			numArgsForSkolem = numAtts;
		}
		
		if (rel == 0)
			skId = fac.addSKToExistsAtom(m1, rel, fac.getFreshVars(start, numArgsForSkolem));
		else
			fac.addSKToExistsAtom(m1, rel, fac.getFreshVars(start, numArgsForSkolem), skId);
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
