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
import vtools.dataModel.expression.Query;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;

//MN ENHANCED genTargetRels to pass types of attributes as argument to addRelation - 11 May 2014
//MN ENHANCED genSourceRels to pass types of attributes as argument to addRelation - 11 May 2014
//MN FIXED keySize in chooseTargetRels - 11 May 2014
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
	
	//MN added new attribute to check whether we are reusing target relation or not - 11 May 2014
	private boolean targetReuse;
	//MN
	
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
		
		//MN BEGIN - 11 May 2014
		targetReuse = false;
		//MN END
	}

	@Override
	protected boolean chooseSourceRels() throws Exception {
		int minAttrs = keySize + numDelAttr;
		RelationType rel;
		
		// get a random relation
		rel = getRandomRel(true, minAttrs);
		
		if (rel == null) 
			return false;
		
		numOfSrcTblAttr = rel.sizeOfAttrArray();
		
		// create primary key if necessary
		if (!rel.isSetPrimaryKey() && keySize > 0) {
			//MN BEGIN - 11 May 2014 (discuss it with Patricia)
			keySize = (keySize<numOfSrcTblAttr)?keySize:numOfSrcTblAttr-numDelAttr;
			//MN END
			fac.addPrimaryKey(rel.getName(), 
					CollectionUtils.createSequence(0, keySize), true);
		}
		// adapt keySize
		else if (rel.isSetPrimaryKey()) {
			keySize = rel.getPrimaryKey().sizeOfAttrArray();
			if (rel.sizeOfAttrArray() - keySize < numDelAttr)
				numDelAttr = rel.sizeOfAttrArray() - keySize; 
		}
		
		m.addSourceRel(rel);
		
		return true;
	}
	
	@Override
	// PRG ADD Source Code to Support Key Generation - Sep 17, 2012
	protected void genSourceRels() throws Exception {
		String srcName = randomRelName(0);
		String[] attrs = new String[numOfSrcTblAttr];
		//MN considered an array to store types of attributes of sourc relation - 11 May 2014
		String[] attrsType = new String[numOfSrcTblAttr];
		//MN
		
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
			//MN BEGIN - 11 May 2014
			if(targetReuse)
				if(i<m.getTargetRels().get(0).getAttrArray().length)
					attrsType[i] = m.getTargetRels().get(0).getAttrArray(i).getDataType();
				else
					attrsType[i] = "TEXT";
			//MN END
		}
		
		//MN BEGIN - 11 May 2014
		if(!targetReuse)
			fac.addRelation(getRelHook(0), srcName, attrs, true);
		else
			fac.addRelation(getRelHook(0), srcName, attrs, attrsType, true);
		//MN END
		
		// Add primary key if explicitly requested 
		if (keySize > 0)
			fac.addPrimaryKey(srcName, keys, true);
	
		//MN BEGIN - 11 May 2014
		targetReuse = false;
		//MN END
	}

	@Override
	protected boolean chooseTargetRels() throws Exception {
		RelationType rel;
		int minAttr = keySize;
		
		rel = getRandomRel(false, minAttr);
		//MN we can consider Max_Num_Tries to add more flexibility - 11 May 2014
		if (rel == null)
			return false;
			
		if (keySize > 0 && !rel.isSetPrimaryKey()) {
			//MN BEGIN - 11 May 2014 (discuss it with Patricia)
			keySize = (keySize<rel.sizeOfAttrArray())? keySize: (rel.sizeOfAttrArray());
			//MN END
			fac.addPrimaryKey(rel.getName(), 
					CollectionUtils.createSequence(0, keySize), false);
		}
		else if (rel.isSetPrimaryKey()) {
			keySize = rel.getPrimaryKey().sizeOfAttrArray();
		}
		numOfSrcTblAttr = rel.sizeOfAttrArray() + numDelAttr;
		
		m.addTargetRel(rel);
		//MN BEGIN - 11 May 2014
		targetReuse = true;
		//MN END
		return true;
	}
	
	@Override
	// PRG ADD Source Code to Support Key Generation - Sep 17, 2012
	protected void genTargetRels() throws Exception {
		String trgName = randomRelName(0);
		String[] attrs = new String[numOfSrcTblAttr-numDelAttr];
		String[] srcAttrs = m.getAttrIds(0, true);
		//MN considered an array to store types of attributes of target relation - 4 May 2014
		List<String> attrsType = new ArrayList<String> ();
		//MN
		
		// copy all the source attributes except the last few (to account for the ones we want to delete)
		System.arraycopy(srcAttrs, 0, attrs, 0, numOfSrcTblAttr-numDelAttr);
		
		//MN BEGIN - 4 May 2014
		for(int i=0; i<numOfSrcTblAttr-numDelAttr; i++)
			attrsType.add(m.getSourceRels().get(0).getAttrArray(i).getDataType());
		//MN END

		//MN changed - 4 May 2014
		fac.addRelation(getRelHook(0), trgName, attrs, attrsType.toArray(new String[] {}), false);
		
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
