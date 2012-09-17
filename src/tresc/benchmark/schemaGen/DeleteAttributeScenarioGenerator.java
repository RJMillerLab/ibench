package tresc.benchmark.schemaGen;

import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;

import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Constants.SkolemKind;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Query;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;

/**
 * Copies the source relation and deletes an attribute from the target relation.
 * 
 * @author mdangelo
 */
public class DeleteAttributeScenarioGenerator extends AbstractScenarioGenerator
{
	private int numOfSrcTblAttr;
	private int numDelAttr;
	private int keySize;
	
    public DeleteAttributeScenarioGenerator()
    {
        ;
    }

	@Override
	protected void initPartialMapping() {
		super.initPartialMapping();
		numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements,numOfElementsDeviation);
		numDelAttr = Utils.getRandomNumberAroundSomething(_generator, numRemovedAttr, numRemovedAttrDeviation);
		
		// PRG BUG FIX - Generate source relation with at least 2 elements - Sep 16, 2012
		numOfSrcTblAttr = (numOfSrcTblAttr > 1 ? numOfSrcTblAttr : 2);
		
		// make sure we never delete all attributes, and that we never delete no attributes
		numDelAttr = (numDelAttr > 0) ? numDelAttr : 1;
		numDelAttr = (numDelAttr < numOfSrcTblAttr) ? numDelAttr : (numOfSrcTblAttr-1);
		
		// PRG FIX - DO NOT ENFORCE KEY UNLESS EXPLICITLY REQUESTED - Sep 16, 2012
		keySize = Utils.getRandomNumberAroundSomething(_generator, primaryKeySize, primaryKeySizeDeviation);
		// PRG Adjust keySize w.r.t number of source table attributes and number of to be deleted attributes
		// e.g. numOfSrcTblAttr = 3, numDelAttr = 2 and ConfigOptions.PrimaryKeySize = 2. Then keySize should be 1
		// e.g. numOfSrcTblAttr = 3, numDelAttr = 1 and ConfigOptions.PrimaryKeySize = 2. Then keySize should be 2
		keySize = (keySize > numOfSrcTblAttr - numDelAttr) ? numOfSrcTblAttr - numDelAttr : keySize;
		
	}
	
   /* public void generateScenario(MappingScenario scenario, Configuration configuration)
    {
    	init(configuration, scenario);
        SPJQuery pquery = scenario.getTransformation();

        for (int i = 0, imax = repetitions; i < imax; i++)
        {
            // decide how many attributes will the source table have
            int numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
                numOfElementsDeviation);
            
            // if trying to delete more attributes than those that exist, then just delete one element
            if (numDelAttr > numOfSrcTblAttr)
            	numDelAttr = 1;
            
            // if going to delete all elements that were in the source table then make sure that there is at least one extra element in the source table
            if (numDelAttr == numOfSrcTblAttr)
            	numOfSrcTblAttr++;
            
            createSubElements(source, target, numOfSrcTblAttr, numDelAttr, i, pquery);
        }
    }

    private void createSubElements(Schema source, Schema target, int numOfSrcTblAttr,
            int numDelAttr, int repetition, SPJQuery pquery)
    {
    	String coding = getStamp() + repetition;
    	int curTbl = repetition;
    	
        // First create the source table
        String sourceRelName = Modules.nameFactory.getARandomName();
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

        // create the target table
        String targetRelName = Modules.nameFactory.getARandomName();
        targetRelName = targetRelName + "_" + coding;
        SMarkElement tgtRel = new SMarkElement(targetRelName, new Set(), null, 0, 0);
        tgtRel.setHook(new String(coding));
        target.addSubElement(tgtRel);
        
        // create the query for the target table
        SPJQuery q = new SPJQuery();
        q.getFrom().add(new Variable("X"), new Projection(Path.ROOT, sourceRelName));
        
        // populate this table with the same element created above for the source
        SelectClauseList sel = q.getSelect();
        
        // go through all the attributes put in the source table and pop them into the target
        for (int i = 0, imax = attNames.length - numDelAttr; i < imax; i++)
        {
        	String tgtAttrName = attNames[i];
        	SMarkElement tgtAtomicElt = new SMarkElement(tgtAttrName, Atomic.STRING, null, 0, 0);
        	String hook = tgtAttrName.substring(tgtAttrName.indexOf("_"));
        	tgtAtomicElt.setHook(hook);
        	tgtRel.addSubElement(tgtAtomicElt);
        	
        	// since we added an attr in the target, we add an entry in the
            // respective select clause
            Projection att = new Projection(new Variable("X"), tgtAttrName);
            sel.add(tgtAttrName, att);
        }
        
        // add the partial queries to the parent query
        // to form the whole transformation
        SelectClauseList pselect = pquery.getSelect();
        String tblTrgName = tgtRel.getLabel();
        pselect.add(tblTrgName, q);
            
        pquery.setSelect(pselect);
    }*/

	@Override
	// PRG ADD Source Code to Support Key Generation - Sep 17, 2012
	protected void genSourceRels() throws Exception {
		String srcName = randomRelName(0);
		String[] attrs = new String[numOfSrcTblAttr];
		
		// First, generate the appropriate number of key elements
		// Note: keySize should be > 0 to generate any key elements
		String[] keys = new String[keySize];
		for (int j = 0; j < keySize; j++)
			keys[j] = randomAttrName(0, 0) + "ke" + j;

		// Second, generate remaining source attributes
		int keyCount = 0;
		for (int i = 0; i < numOfSrcTblAttr; i++) {
			
			String attrName = randomAttrName(0, i);

			// Note: the body of this IF construct would not be executed when keySize = 0
			if (keyCount < keySize)
				attrName = keys[keyCount];
			
			keyCount++;
			
			attrs[i] = attrName;
		}
		
		fac.addRelation(getRelHook(0), srcName, attrs, true);
		// Add primary key if explicitly requested 
		if (keySize > 0)
			fac.addPrimaryKey(srcName, keys, true);
	
	}

	@Override
	// PRG ADD Source Code to Support Key Generation - Sep 17, 2012
	protected void genTargetRels() throws Exception {
		String trgName = randomRelName(0);
		String[] attrs = new String[numOfSrcTblAttr-numDelAttr];
		String[] srcAttrs = m.getAttrIds(0, true);

		// copy all the source attributes except the last few (to account for the ones we want to delete)
		System.arraycopy(srcAttrs, 0, attrs, 0, numOfSrcTblAttr-numDelAttr);

		fac.addRelation(getRelHook(0), trgName, attrs, false);
		
		// PRG ADD primary key to target relation if necessary
		String[] keys = new String[keySize];
		for (int j = 0; j < keySize; j++)
			keys[j] = srcAttrs[j];
		
		if (keySize > 0)
			fac.addPrimaryKey(trgName, keys, false);
		
	}

	@Override
	protected void genCorrespondences() {
		for (int i = 0; i < numOfSrcTblAttr-numDelAttr; i++)
			addCorr(0, i, 0, i);
	}

	@Override
	protected void genMappings() throws Exception {
		MappingType m1 = fac.addMapping(m.getCorrs());
		
		// source and target tables get fresh variables
		fac.addForeachAtom(m1, 0, fac.getFreshVars(0, numOfSrcTblAttr));
		fac.addExistsAtom(m1, 0, fac.getFreshVars(0, numOfSrcTblAttr - numDelAttr));
	}

	@Override
	protected void genTransformations() throws Exception {
		String creates = m.getRelName(0, false);
		Query q;
		
		q = genQueries();
		q.storeCode(q.toTrampString(m.getMapIds()));
		q = addQueryOrUnion(creates, q);
		fac.addTransformation(q.getStoredCode(), m.getMapIds(), creates);
	}
	
	private Query genQueries() throws Exception {
		String sourceRelName = m.getRelName(0, true);
		String[] tAttrs = m.getAttrIds(0, false);
		
		// create the query for the source table and add the from clause
		SPJQuery q = new SPJQuery();
		q.getFrom().add(new Variable("X"),
				new Projection(Path.ROOT, sourceRelName));

		SelectClauseList sel = q.getSelect();

		// add entries to the select clause
		for (String a: tAttrs) {
			Projection att = new Projection(new Variable("X"), a);
			sel.add(a, att);
		}

		return q;
	}
	
	@Override
	public ScenarioName getScenType() {
		return ScenarioName.DELATTRIBUTE;
	}


}
