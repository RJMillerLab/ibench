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
public class VPNtoMScenarioGenerator extends AbstractScenarioGenerator {

	private JoinKind jk;
	private int numOfSrcTblAttr;
	private int numOfTgtTables = 2;
	private int attsPerTargetRel;
	private int attrRemainder;
	private SkolemKind sk = SkolemKind.ALL;
	private String skId1, skId2;
	private String[] sk1Args, sk2Args;
    
    public VPNtoMScenarioGenerator()
    {
        ;
    }

    
    protected void initPartialMapping() {
    	super.initPartialMapping();
    	
        numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
            numOfElementsDeviation);

        attsPerTargetRel = numOfSrcTblAttr / numOfTgtTables;
        attrRemainder = numOfSrcTblAttr % numOfTgtTables; 
        
        
        jk = JoinKind.values()[joinKind];
        // join kind must be STAR for VP N-to-M
        // jk = JoinKind.STAR;
    }

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
        
        	// create normal attributes for table (copy from source)
            for (int j = 0; j < attrNum; j++)
            	attrs[j] = srcAttrs[offset + j];

            if (i == 0)
            	attrs[attrs.length - 1] = joinAtt1;
            else
            	attrs[attrs.length - 1] = joinAtt2;
            
            fac.addRelation(hook, trgName, attrs, false);
            
            // add the primary key for each relation
            if (i == 0)
            	fac.addPrimaryKey(trgName, joinAtt1, false);
            else 
            	fac.addPrimaryKey(trgName, joinAtt2, false);
        }
        
        // create the third table which only has the join attributes
        String trgName = randomRelName(2);
    	String hook = getRelHook(2);
    	fac.addRelation(hook, trgName, new String[] {joinAtt1Ref, joinAtt2Ref}, false);
    	
        addFKs();
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
		
		// add skolem function for join
		for(int i = 0; i < numOfTgtTables; i++) 
		{
			SelectClauseList seli = queries[i].getSelect();
			SelectClauseList sel2 = q.getSelect();
			
			String name;
			int numVar;
			int numAttr = (i < numOfTgtTables - 1) ? attsPerTargetRel : attsPerTargetRel + attrRemainder;

			if (mapLang.equals(MappingLanguageType.SOtgds)) 
			{
				SKFunction sk = m.getSkolemFromAtom(m1, false, i, numAttr);
				name = sk.getSkname();
				numVar = sk.getVarArray().length;
			}
			else 
			{
				name = fac.getNextId("SK");
				numVar = _generator.nextInt(numOfSrcTblAttr);
			}

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
