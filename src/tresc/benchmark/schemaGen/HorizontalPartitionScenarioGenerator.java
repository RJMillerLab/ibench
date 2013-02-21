package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;

import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Constants.TrampXMLOutputSwitch;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.AND;
import vtools.dataModel.expression.ConstantAtomicValue;
import vtools.dataModel.expression.LE;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Query;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.values.IntegerValue;

//PRG FIXED Omission, must generate source relation with at least 2 elements (this was causing empty Skolem terms and PK FDs with empty RHS!)- Sep 19, 2012

public class HorizontalPartitionScenarioGenerator extends AbstractScenarioGenerator
{
	private static final int MAX_NUM_TRIES = 10;
	
	private int randomElements;
	private int randomFragments;

	private int fragmentWidth;
    
    public HorizontalPartitionScenarioGenerator()
    {		;		}


    @Override
    protected void initPartialMapping () {
    	super.initPartialMapping();
        randomElements = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
                numOfElementsDeviation);
       
        // PRG ADD - Generate at least a source relation of 2 elements - Sep 19, 2012
        // This enforcement is because HP's chosen selector attribute (always source attribute 0) is never copied into the target relation.
        // Thus generating a source relation of size 1, for example, would mean getting an empty target relation.
        randomElements = (randomElements > 2 ? randomElements : 2);
        
        randomFragments = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements, numOfSetElementsDeviation);
        randomFragments = (randomFragments > 1) ? randomFragments : 2;
        fragmentWidth = 10000 / randomFragments;
    }

    //
    // Algorithm: The schema generated is the following
    // In the source one element is created with randomElements elements
    // in the target we generate randomFragments tables each one being a copy of
    // what we had in the source.
    // In addition, the source has an attribute called selector that is used to
    // do the selection of the tuples
    //
    /*private void createHorizPartitionCase(Schema source, Schema target, int numOfElements, int numOfFragments,
            int repetition, SPJQuery pquery, SPJQuery generatedQuery)
    {
        String srcName = Modules.nameFactory.getARandomName();
        String srcNameinFull = srcName + "_" + getStamp() + repetition;
        // This is used to decide how we will split the tuples. It has nothing
        // to do with the schema
        int fragmentWidth = 10000 / numOfFragments;

        // create the source table
        SMarkElement srcElement = new SMarkElement(srcNameinFull, new Set(), null, 0, 0);
        srcElement.setHook(new String(getStamp() + repetition));
        source.addSubElement(srcElement);

        // generate the selector attribute
        String nameSelector = "selector" + getStamp() + repetition;
        SMarkElement e = new SMarkElement(nameSelector, Atomic.INTEGER, null, 0, 0);
        e.setHook(new String(getStamp() + repetition));
        srcElement.addSubElement(e);
        
        // create the selector attribute for the Where condition of the query
        Variable var = new Variable("X");
        Projection attSelector = new Projection(var.clone(),nameSelector);
        
        // create the target tables and part of the subqueries involved in the final query
        SMarkElement[] fragments = new SMarkElement[numOfFragments];        
        SPJQuery[] queries = new SPJQuery[numOfFragments];
        for (int i = 0, imax = fragments.length; i < imax; i++)
        {
            int lowerLimit = i * fragmentWidth;
            int upperLimit = ((i + 1) * fragmentWidth) - 1;
            String name = srcName + "_" + getStamp() + repetition + "FR" + i + "_from_" + lowerLimit + "_to_"
                + upperLimit;
            fragments[i] = new SMarkElement(name, new Set(), null, 0, 0);
            fragments[i].setHook(new String(getStamp() + repetition + "FR" + i + "_from_" + lowerLimit + "_to_"
                + upperLimit));
            target.addSubElement(fragments[i]);
            // create the subquery corresponding to the ith target fragment
            queries[i] = new SPJQuery();
        	// create the From Clause for each subquery
        	queries[i].getFrom().add(var.clone(),new Projection(Path.ROOT,srcNameinFull));
        	// create the Where Clause for each subquery
        	AND andCond = new AND();
        	andCond.add(new LE(new ConstantAtomicValue(new IntegerValue(lowerLimit)),attSelector));
        	andCond.add(new LE(attSelector,new ConstantAtomicValue(new IntegerValue(upperLimit))));
        	queries[i].setWhere(andCond);
        }
        
        // and now populate the src SMarkElement and the target fragments with
        // the rest of the attributes.
        for (int i = 0; i < numOfElements; i++)
        {
            String name = Modules.nameFactory.getARandomName();
            String elementName = name + "_" + getStamp() + repetition + "A" + i;
            e = new SMarkElement(elementName, Atomic.STRING, null, 0, 0);
            e.setHook(new String(getStamp() + repetition + "A" + i));
            srcElement.addSubElement(e);
            // create the Select Clause for each subquery 
            Projection sourceAtt = new Projection(var.clone(),elementName);
            for (int k = 0; k < fragments.length; k++)
            {
                e = new SMarkElement(elementName, Atomic.STRING, null, 0, 0);
                e.setHook(new String( getStamp() + repetition + "A" + i));
                fragments[k].addSubElement(e);
                SelectClauseList select = queries[k].getSelect();
                select.add(elementName, sourceAtt.clone());
                queries[k].setSelect(select);
            }
        }
        
        // add all the subqueries to the final query 
        SelectClauseList pselect = pquery.getSelect();
        SelectClauseList qselect = generatedQuery.getSelect();
        for (int i = 0; i < queries.length; i++){
        	pselect.add(fragments[i].getLabel(), queries[i]);
        	qselect.add(fragments[i].getLabel(), queries[i]);
        }
        pquery.setSelect(pselect);
        generatedQuery.setSelect(qselect);
        for (int i=0; i < queries.length; i++)
        	generatedQuery.addTarget(fragments[i].getLabel());
    }*/

    /**
     * Following requirements for the relation:
     * 1) At least two attributes (one key, one free)
     * 2) if it has a key then it should be the first attr only (could be 
     * 		changed later)
     * 
     * @throws Exception 
     */
    @Override
    protected boolean chooseSourceRels() throws Exception {
    	RelationType r = null;
    	boolean ok = false;
    	int tries = 0;
    	
    	while(!ok && tries < MAX_NUM_TRIES) {
    		r = getRandomRel(true, 2);
    		if (r == null)
    			break;
    		if (r.isSetPrimaryKey()) {	
    			int[] keyPos = model.getPKPos(r.getName(), true);
    			if (keyPos.length == 1 && keyPos[0] == 0) {
    				ok = true;
    				break;
    			}
    		}
    		else {
    			ok = true;
    			break;
    		}
    	}
    	
    	// did not find suitable relation
    	if (r == null)
    		return false;
    	// adapt fields
    	else {
    		m.addSourceRel(r);
    		// create PK if necessary
    		if (!r.isSetPrimaryKey())
    			fac.addPrimaryKey(r.getName(), 0, true);
    		randomElements = r.sizeOfAttrArray() - 1;
    		return true;
    	}
    }
    

	@Override
	protected void genSourceRels() {	
	    String srcName = randomRelName(0);
	    String[] attrs = new String[randomElements + 1];
	    String[] dTypes = new String[randomElements + 1];
	    String nameSelector = "selectorhp" + curRep;
	    
        // create the source attrs
        attrs[0] = nameSelector;
        dTypes[0] = "INT8";
        
        // and now populate the src SMarkElement and the target fragments with
        // the rest of the attributes.
        for (int i = 0; i < randomElements; i++) {
            String attName = randomAttrName(0, i); 
            attrs[i + 1] = attName;
            dTypes[i + 1] = "TEXT";
        }

        fac.addRelation(getRelHook(0), srcName, attrs, dTypes, true);
	}

	/**
	 * We need "randomFragments" number of relations with the same number 
	 * of attributes and the first attribute as key (or no key).
	 * @throws Exception 
	 */
	@Override
	protected boolean chooseTargetRels() throws Exception {
		RelationType cand = null;
		int tries = 0;
		int numAttrs = 0;
		List<RelationType> rels = new ArrayList<RelationType> (randomFragments);
		
		// first one
		while (tries < MAX_NUM_TRIES && rels.size() == 0) {
			cand = getRandomRel(false, 2);
			if (relOk(cand)) {
				rels.add(cand);
				break;
			}
		}
		
		// didn't find one? generate target relations
		if (rels.size() == 0)
			return false;
		
		numAttrs = cand.sizeOfAttrArray();

		// find additional relations with the same number of attributes 
		// and no key or the first attr as key
		while (tries < MAX_NUM_TRIES * randomFragments && cand != null 
				&& rels.size() < randomFragments) {
			cand = getRandomRelWithNumAttr(false, numAttrs);
			if (relOk(cand))
				rels.add(cand);
		}
		
		// create additional target relations
		for (int i = 0; i < rels.size(); i++)
			m.addTargetRel(rels.get(i));
		for (int i = rels.size(); i < randomFragments; i++) {
			RelationType r = createFreeRandomRel(i, numAttrs);
			rels.add(r);
			fac.addRelation(getRelHook(i), r, false);
		}
		
		// create primary keys
		for (RelationType r: rels)
			if (!r.isSetPrimaryKey())
				fac.addPrimaryKey(r.getName(), 0, false);
		
		// adapt local parameters
		randomElements = numAttrs;
		
		return true;
	}
	
	private boolean relOk (RelationType r) throws Exception {
		if (r == null)
			return false;
		if (r.isSetPrimaryKey()) {
			int[] pkPos = model.getPKPos(r.getName(), false);
			if (pkPos.length == 1 & pkPos[0] == 0)
				return true;
		}
		// no PK? we are fine
		else
			return true;
		
		return false;
	}
	
	@Override
	protected void genTargetRels() {
		String srcName = m.getSourceRels().get(0).getName();
        String[] attrs = m.getAttrIds(0, true);
        
        attrs = Arrays.copyOfRange(attrs, 1, attrs.length);
        
        for(int i = 0; i < randomFragments; i++) {
            int lowerLimit = i * fragmentWidth;
            int upperLimit = ((i + 1) * fragmentWidth) - 1;
            String hook =  getStamp() + curRep + "FR" + i + "_from_" + lowerLimit + "_to_"
                    + upperLimit;
            String name = srcName + "_" + hook;
            
        	fac.addRelation(hook, name, attrs, false);
        }
	}

	
	
	@Override
	protected void genMappings() throws Exception {
		String srcName = m.getSourceRels().get(0).getName();
		
		for(int i = 0; i < randomFragments; i++) {
			MappingType m1 = fac.addMapping(getCorrForFrag(i));
			String trgName = m.getTargetRels().get(i).getName();
			
			fac.addForeachAtom(m1.getId(), srcName, fac.getFreshVars(0, randomElements + 1));
			fac.addExistsAtom(m1.getId(), trgName, fac.getFreshVars(1, randomElements));
		}
	}
	
	private CorrespondenceType[] getCorrForFrag (int frag) {
		CorrespondenceType[] result = new CorrespondenceType[randomElements];
		
		if (!configuration.getTrampXMLOutputOption(
				TrampXMLOutputSwitch.Correspondences))
			return null;
		
		for(int i = 0; i < randomElements; i++)
			result[i] = m.getCorrs().get(frag * randomElements + i);
		
		return result;
	}
	
	@Override
	protected void genTransformations() throws Exception {
		Query q;
		
		for(int i = 0; i < randomFragments; i++) {
			String targetName = m.getTargetRels().get(i).getName();
			String map = m.getMapIds()[i];
			
			q = genQuery(i);
			q.storeCode(q.toTrampString(m.getMapIds()[i]));
			q = addQueryOrUnion(targetName, q);

			fac.addTransformation(q.getStoredCode(), new String[] {map}, targetName);
		}
	}
	
	private SPJQuery genQuery(int i) {
		SPJQuery q = new SPJQuery();
		String srcName = m.getSourceRels().get(0).getName();
		
        // create the selector attribute for the Where condition of the query
		String nameSelector = m.getSourceRels().get(0).getAttrArray()[0].getName();
        Variable var = new Variable("X");
        Projection attSelector = new Projection(var.clone(), nameSelector);
		
        int lowerLimit = i * fragmentWidth;
        int upperLimit = ((i + 1) * fragmentWidth) - 1;
        
		// create the From Clause for each subquery
		q.getFrom().add(var.clone(),
				new Projection(Path.ROOT, srcName));
		// create the Where Clause for each subquery
		AND andCond = new AND();
		andCond.add(new LE(new ConstantAtomicValue(new IntegerValue(
				lowerLimit)), attSelector));
		andCond.add(new LE(attSelector, new ConstantAtomicValue(
				new IntegerValue(upperLimit))));
		q.setWhere(andCond);
	
		String[] attrIds = m.getAttrIds(0, true);
		for(int j = 1; j <= randomElements; j++) {
			String elementName = attrIds[j];
			Projection sourceAtt = new Projection(var.clone(), elementName);
			SelectClauseList select = q.getSelect();
			select.add(elementName, sourceAtt.clone());
			q.setSelect(select);
		}
		
        return q;
	}

	@Override
	protected void genCorrespondences() {
		for(int i = 0; i < randomFragments; i++)
			for(int j = 1; j < randomElements + 1; j++)
				addCorr(0, j, i, j - 1);
	}
	
	
	@Override
	public ScenarioName getScenType() {
		return ScenarioName.HORIZPARTITION;
	}
}
