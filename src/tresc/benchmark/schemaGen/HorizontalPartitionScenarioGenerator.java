package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
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
    private Random _generator;

    private final String _stamp = "HP";

    private static int _currAttributeIndex = 0; // this determines the letter used for the attribute in the mapping
    
    public HorizontalPartitionScenarioGenerator()
    {		;		}

    public void generateScenario(MappingScenario scenario, Configuration configuration)
    {
        // generate the generator based on the seed
        //long seed = configuration.getScenarioSeeds(Constants.ScenarioName.HORIZPARTITION.ordinal());
        //_generator = (seed == 0) ? new Random() : new Random(seed);

    	_generator=configuration.getRandomGenerator();
    	
        Schema source = scenario.getSource();
        Schema target = scenario.getTarget();
        SPJQuery pquery = scenario.getTransformation();
        SPJQuery generatedQuery = new SPJQuery();

        int repetitions = configuration.getScenarioRepetitions(Constants.ScenarioName.HORIZPARTITION.ordinal());
        int numOfElements = configuration.getParam(Constants.ParameterName.NumOfSubElements);
        int numOfElementsDeviation = configuration.getDeviation(Constants.ParameterName.NumOfSubElements);
        int joinSize = configuration.getParam(Constants.ParameterName.JoinSize);
        int joinSizeDeviation = configuration.getDeviation(Constants.ParameterName.JoinSize);
        for (int i = 0, imax = repetitions; i < imax; i++)
        {
            int randomElements = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
                numOfElementsDeviation);
            int randomFragments = Utils.getRandomNumberAroundSomething(_generator, joinSize, joinSizeDeviation);
            createHorizPartitionCase(source, target, randomElements, randomFragments, i, pquery, generatedQuery);
        }
        
        setScenario(scenario, generatedQuery);
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

	private void setScenario(MappingScenario scenario, SPJQuery generatedQuery) {
		SelectClauseList pselect = generatedQuery.getSelect();
		for (int i = 0; i < pselect.size(); i++) {
			String mKey = scenario.getNextMid();
			String tKey = scenario.getNextTid();

			ArrayList<String> corrsList = new ArrayList<String>();
			HashMap<String, ArrayList<Character>> sourceAttrs = new HashMap<String, ArrayList<Character>>();
			HashMap<String, ArrayList<Character>> targetAttrs = new HashMap<String, ArrayList<Character>>();

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
			scenario.putTransformationRelName(tKey, sourceName);
			
			resetAttrLetters();
		}
	}
	
	private String getQueryString(SPJQuery origQ, List<String> mKeys) {
		String retVal = origQ.toString();
		FromClauseList from = origQ.getFrom();
		for (int i = 0; i < from.size(); i++) {
			String key = from.getKey(i).toString();
			String relName = from.getValue(i).toString();
			String mKey = mKeys.get(i);
			relName = relName.substring(1)+"."; // remove the first "/"
			retVal = retVal.replace(key, relName).replace("/", "");
			retVal = retVal.replace(key.substring(1), "");
			retVal = retVal.replace("${" + i + "}", mKey);
		}
		
		return retVal;
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
        String srcNameinFull = srcName + "_" + _stamp + repetition;
        // This is used to decide how we will split the tuples. It has nothing
        // to do with the schema
        int fragmentWidth = 10000 / numOfFragments;

        // create the source table
        SMarkElement srcElement = new SMarkElement(srcNameinFull, new Set(), null, 0, 0);
        srcElement.setHook(new String(_stamp + repetition));
        source.addSubElement(srcElement);

        // generate the selector attribute
        String nameSelector = "selector" + _stamp + repetition;
        SMarkElement e = new SMarkElement(nameSelector, Atomic.INTEGER, null, 0, 0);
        e.setHook(new String(_stamp + repetition));
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
            String name = srcName + "_" + _stamp + repetition + "FR" + i + "_from_" + lowerLimit + "_to_"
                + upperLimit;
            fragments[i] = new SMarkElement(name, new Set(), null, 0, 0);
            fragments[i].setHook(new String(_stamp + repetition + "FR" + i + "_from_" + lowerLimit + "_to_"
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
            String elementName = name + "_" + _stamp + repetition + "A" + i;
            e = new SMarkElement(elementName, Atomic.STRING, null, 0, 0);
            e.setHook(new String(_stamp + repetition + "A" + i));
            srcElement.addSubElement(e);
            // create the Select Clause for each subquery 
            Projection sourceAtt = new Projection(var.clone(),elementName);
            for (int k = 0; k < fragments.length; k++)
            {
                e = new SMarkElement(elementName, Atomic.STRING, null, 0, 0);
                e.setHook(new String( _stamp + repetition + "A" + i));
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
}
