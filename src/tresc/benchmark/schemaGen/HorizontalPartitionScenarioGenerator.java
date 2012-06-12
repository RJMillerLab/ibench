package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.TransformationType;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;

import vtools.dataModel.expression.AND;
import vtools.dataModel.expression.ConstantAtomicValue;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;
import vtools.dataModel.values.IntegerValue;
import vtools.dataModel.expression.LE;

public class HorizontalPartitionScenarioGenerator extends ScenarioGenerator
{
    private static int _currAttributeIndex = 0; // this determines the letter used for the attribute in the mapping

	private int randomElements;
	private int randomFragments;

	private int fragmentWidth;
    
    public HorizontalPartitionScenarioGenerator()
    {		;		}

//    public void generateScenario(MappingScenario scenario, Configuration configuration) throws Exception
//    {
//    	init(configuration, scenario);
//        SPJQuery generatedQuery = new SPJQuery();
//
//        for (int i = 0, imax = repetitions; i < imax; i++)
//        {
//        	initPartialMapping();
//            createHorizPartitionCase(source, target, randomElements, randomFragments, i, pquery, generatedQuery);
//        }
//        
//        setScenario(scenario, generatedQuery);
//    }
//    
    @Override
    protected void initPartialMapping () {
    	super.initPartialMapping();
        randomElements = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
                numOfElementsDeviation);
        randomFragments = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements, numOfSetElementsDeviation);
        fragmentWidth = 10000 / randomFragments;
    }

    private Character getAttrLetter(String attrName) {
    	if (attrMap.containsKey(attrName))
    		return attrMap.get(attrName);
    	Character letter = _attributes.charAt(_currAttributeIndex++);
    	attrMap.put(attrName, letter);
    	return letter;
    }

    private void resetAttrLetters() {
    	_currAttributeIndex = 0;
    	attrMap.clear();
    }

	private void setScenario(MappingScenario scenario, SPJQuery generatedQuery) throws Exception {
		SelectClauseList pselect = generatedQuery.getSelect();
		for (int i = 0; i < pselect.size(); i++) {
			String mKey = scenario.getNextMid();
			String tKey = scenario.getNextTid();

			ArrayList<String> corrsList = new ArrayList<String>();
			HashMap<String, List<Character>> sourceAttrs = new HashMap<String, List<Character>>();
			HashMap<String, List<Character>> targetAttrs = new HashMap<String, List<Character>>();

			SPJQuery e = (SPJQuery)(pselect.getTerm(i));
        	SPJQuery subQ = (SPJQuery)(pselect.getValue(i));
        	FromClauseList fcl = subQ.getFrom();
        	String sourceName="";
        	String targetName=generatedQuery.getTarget(i);
        	SelectClauseList scl = e.getSelect();
        	for (int j = 0; j < fcl.size(); j++) {
        		ArrayList<Character> attrLists = new ArrayList<Character>();
        		String key = fcl.getKey(j).toString();
        		sourceName = fcl.getValue(j).toString().substring(1);
        		String[] sclArray = scl.toString().split(",");
        		for (int k = 0; k < sclArray.length; k++) {
        			String attr = sclArray[k];
        			attr = attr.replaceFirst("\\"+key+"/", "").trim();
        			attrLists.add(getAttrLetter(attr));
        			String relAttr = sourceName + "." + attr;
        			String cKey = scenario.getNextCid();
        			String cVal = relAttr + "=" + relAttr;
        			// correspondences.put(cKey, cVal);
        			scenario.putCorrespondences(cKey, cVal);
        			corrsList.add(cKey);
        		}
        		sourceAttrs.put(sourceName, attrLists);
        		targetAttrs.put(targetName, attrLists);
        	}
        	scenario.putMappings2Correspondences(mKey, corrsList);
        	scenario.putMappings2Sources(mKey, sourceAttrs);
        	scenario.putMappings2Targets(mKey, targetAttrs);

			ArrayList<String> mList = new ArrayList<String>();
			mList.add(mKey);
			scenario.putTransformation2Mappings(tKey, mList);
			scenario.putTransformationCode(tKey, getQueryString(e, mList));
			scenario.putTransformationRelName(tKey, targetName);
			
			resetAttrLetters();
		}
	}
	
	private String getQueryString(SPJQuery origQ, List<String> mKeys) throws Exception {
		return origQ.toTrampString(mKeys.toArray(new String[] {}));
	}

    //
    // Algorithm: The schema generated is the following
    // In the source one element is created with randomElements elements
    // in the target we generate randomFragments tables each one being a copy of
    // what we had in the source.
    // In addition, the source has an attribute called selector that is used to
    // do the selection of the tuples
    //
    private void createHorizPartitionCase(Schema source, Schema target, int numOfElements, int numOfFragments,
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
        for (int i=0; i < queries.length; i++) {
        	generatedQuery.addTarget(fragments[i].getLabel());
        }
    }


	@Override
	protected void genSourceRels() {	
	    String srcName = randomRelName(0);
	    String[] attrs = new String[randomElements + 1];
	    String[] dTypes = new String[randomElements + 1];
	    String nameSelector = "selector" + getStamp() + curRep;
	    RelationType srcRel;
	    
        // create the source attrs
        attrs[0] = nameSelector;
        dTypes[0] = "INT8";
        
        // and now populate the src SMarkElement and the target fragments with
        // the rest of the attributes.
        for (int i = 0; i < randomElements; i++)
        {
            String attName = randomAttrName(0, i); 
            attrs[i + 1] = attName;
            dTypes[i + 1] = "TEXT";
        }

        srcRel = fac.addRelation(getRelHook(0), srcName, attrs, dTypes, true);
        m.addSourceRel(srcRel);
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
            
        	RelationType tRel = fac.addRelation(hook, name, attrs, false);
        	m.addTargetRel(tRel);
        }
	}

	@Override
	protected void genMappings() throws Exception {
		String srcName = m.getSourceRels().get(0).getName();
		
		for(int i = 0; i < randomFragments; i++) {
			MappingType m1 = fac.addMapping(getCorrForFrag(i));
			m.addMapping(m1);
			String trgName = m.getTargetRels().get(i).getName();
			
			fac.addForeachAtom(m1.getId(), srcName, fac.getFreshVars(0, randomElements + 1));
			fac.addExistsAtom(m1.getId(), trgName, fac.getFreshVars(1, randomElements));
		}
	}
	
	private CorrespondenceType[] getCorrForFrag (int frag) {
		CorrespondenceType[] result = new CorrespondenceType[randomElements];
		
		for(int i = 0; i < randomElements; i++)
			result[i] = m.getCorrs().get(frag * randomElements + i);
		
		return result;
	}
	
	@Override
	protected void genTransformations() throws Exception {
        SPJQuery genQuery = genQueries();
		for(int i = 0; i < randomFragments; i++) {
			TransformationType t;
			String targetName = m.getTargetRels().get(i).getName();
			String map = m.getMapIds()[i];
			SPJQuery q = (SPJQuery) genQuery.getSelect().getTerm(i);
			t = fac.addTransformation(q.toTrampString(map), new String[] {map}, targetName);
		}
	}
	
	private SPJQuery genQueries() {
		SPJQuery generatedQuery = new SPJQuery();
		SPJQuery[] queries = new SPJQuery[randomFragments];
		String srcName = m.getSourceRels().get(0).getName();
		
        // create the selector attribute for the Where condition of the query
		String nameSelector = m.getSourceRels().get(0).getAttrArray()[0].getName();
        Variable var = new Variable("X");
        Projection attSelector = new Projection(var.clone(), nameSelector);
		
		for(int i = 0; i < randomFragments; i++) {
            int lowerLimit = i * fragmentWidth;
            int upperLimit = ((i + 1) * fragmentWidth) - 1;
			queries[i] = new SPJQuery();
			// create the From Clause for each subquery
			queries[i].getFrom().add(var.clone(),
					new Projection(Path.ROOT, srcName));
			// create the Where Clause for each subquery
			AND andCond = new AND();
			andCond.add(new LE(new ConstantAtomicValue(new IntegerValue(
					lowerLimit)), attSelector));
			andCond.add(new LE(attSelector, new ConstantAtomicValue(
					new IntegerValue(upperLimit))));
			queries[i].setWhere(andCond);
		}
		
		for(int i = 0; i < randomElements; i++) {
			String elementName = m.getAttrIds(0, true)[i + 1];
			Projection sourceAtt = new Projection(var.clone(), elementName);
			for (int k = 0; k < randomFragments; k++) {
				SelectClauseList select = queries[k].getSelect();
				select.add(elementName, sourceAtt.clone());
				queries[k].setSelect(select);
			}
		}
		
		// add all the subqueries to the final query 
        SelectClauseList pselect = pquery.getSelect();
        SelectClauseList qselect = generatedQuery.getSelect();
        for (int i = 0; i < queries.length; i++){
        	String tName = m.getTargetRels().get(i).getName();
        	pselect.add(tName, queries[i]);
        	qselect.add(tName, queries[i]);
        }
        pquery.setSelect(pselect);
        generatedQuery.setSelect(qselect);
        for (int i=0; i < queries.length; i++) {
        	String tName = m.getTargetRels().get(i).getName();
        	generatedQuery.addTarget(tName);
        }
        return generatedQuery;
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
