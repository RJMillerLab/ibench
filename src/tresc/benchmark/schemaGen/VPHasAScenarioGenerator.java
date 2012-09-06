package tresc.benchmark.schemaGen;

import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SKFunction;

import tresc.benchmark.Constants.JoinKind;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Constants.SkolemKind;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;

public class VPHasAScenarioGenerator extends AbstractScenarioGenerator {

	private JoinKind jk;
	private int numOfSrcTblAttr;
	private int numOfTgtTables;
	private int attsPerTargetRel;
	private int attrRemainder;
	private SkolemKind sk = SkolemKind.ALL;
	private String skId;
    
    public VPHasAScenarioGenerator()
    {
        ;
    }

    
    protected void initPartialMapping() {
    	log.debug("---INIT---");
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

	@Override
	protected void genSourceRels() {
		log.debug("---GENERATING SOURCE---");
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
		log.debug("---GENERATING TARGET---");
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
            	if (i == 0) // foreign key only goes in one direction so the primary key is set only for the first relation
            		fac.addPrimaryKey(trgName, joinAttName, false);
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

	private void addFKs() {
		if (jk == JoinKind.STAR) {
			for(int i = 1; i < numOfTgtTables; i++) {
				int toA = m.getNumRelAttr(0, false) - 1;
				int fromA = m.getNumRelAttr(i, false) - 1;
				addFK(i, fromA, 0, toA, false);
			}
		} else { // chain
			int toA = m.getNumRelAttr(1, false) - 1;
			int fromA = m.getNumRelAttr(0, false) - 1;
			addFK(0, fromA, 1, toA, false);
			for(int i = 1; i < numOfTgtTables - 1; i++) {
				toA = m.getNumRelAttr(i + 1, false) - 1;
				fromA = m.getNumRelAttr(i, false) - 2;
				addFK(i, fromA, i+1, toA, false);
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
		        	generateSKs(m1, i, offset, numAtts);
				}
				break;
		}
	}
	
	private void generateSKs(MappingType m1, int rel, int offset, int numAtts) {
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
				
				int numAttr = (i < numOfTgtTables - 1) ? attsPerTargetRel : attsPerTargetRel + attrRemainder;

		 		SKFunction sk = m.getSkolemFromAtom(m1, false, i, numAttr);
		 			
		 		vtools.dataModel.expression.SKFunction stSK = new vtools.dataModel.expression.SKFunction(sk.getSkname());
		 			
		 		// this works because the key is always the first attribute 
		 		for(int k = 0; k < sk.getVarArray().length; k++) {			
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
            	int numAttr = (i < numOfTgtTables - 1) ? attsPerTargetRel : attsPerTargetRel + attrRemainder;

		 		SKFunction sk = m.getSkolemFromAtom(m1, false, i, numAttr);
		 			
		 		vtools.dataModel.expression.SKFunction stSK = new vtools.dataModel.expression.SKFunction(sk.getSkname());
		 			
		 		for(int k = 0; k < sk.getVarArray().length; k++) {			
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
