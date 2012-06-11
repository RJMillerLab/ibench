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

import vtools.dataModel.expression.ConstantAtomicValue;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.Function;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SKFunction;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;
import vtools.dataModel.values.StringValue;

public class SurrogateKeysScenarioGenerator extends ScenarioGenerator
{
    private Random _generator;

    private final String _stamp = "SK";

    private static int _currAttributeIndex = 0; // this determines the letter used for the attribute in the mapping
    
    public SurrogateKeysScenarioGenerator()
    {
        ;
    }

    public void generateScenario(MappingScenario scenario, Configuration configuration) throws Exception
    {
        // generate the generator based on the seed
        //long seed = configuration.getScenarioSeeds(Constants.ScenarioName.SURROGATEKEY.ordinal());
        //_generator = (seed == 0) ? new Random() : new Random(seed);
    	
    	_generator=configuration.getRandomGenerator();
    	
        Schema source = scenario.getSource();
        Schema target = scenario.getTarget();
        SPJQuery pquery = scenario.getTransformation();
        SPJQuery generatedQuery = new SPJQuery();
        
        int repetitions = configuration.getScenarioRepetitions(Constants.ScenarioName.SURROGATEKEY.ordinal());
        int numOfElements = configuration.getParam(Constants.ParameterName.NumOfSubElements);
        int numOfElementsDeviation = configuration.getDeviation(Constants.ParameterName.NumOfSubElements);
        int numOfParams = configuration.getParam(Constants.ParameterName.NumOfParamsInFunctions);
        int numOfParamsDeviation = configuration.getDeviation(Constants.ParameterName.NumOfParamsInFunctions);

        for (int i = 0, imax = repetitions; i < imax; i++)
        {
            int elements = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
            int params = Utils.getRandomNumberAroundSomething(_generator, numOfParams, numOfParamsDeviation);
            // make sure params are at least 2
            params = (params < 2) ? 2 : params;
            // and the elements are at least as many as the the params
            if (params > elements)
                elements = params;
            String randomName = Modules.nameFactory.getARandomName();
            String name = randomName + "_" + _stamp + "CE" + i;
            SMarkElement srcElem = new SMarkElement(name, new Set(), null, 0, 0);
            srcElem.setHook(new String(_stamp + "CE" + i));
            source.addSubElement(srcElem);
            SMarkElement trgElem = new SMarkElement(name + "Skey", new Set(), null, 0, 0);
            trgElem.setHook(new String(_stamp + "CE" + i));
            target.addSubElement(trgElem);
            createSubElements(srcElem, trgElem, elements, params, pquery, generatedQuery, scenario);
        }
        
        setScenario(scenario, generatedQuery, pquery);
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

	private void setScenario(MappingScenario scenario, SPJQuery generatedQuery, SPJQuery pquery) throws Exception {
		SelectClauseList gselect = generatedQuery.getSelect();
		for (int i = 0; i < gselect.size(); i++) {
			String mKey = scenario.getNextMid();
			String tKey = scenario.getNextTid();

			ArrayList<String> corrsList = new ArrayList<String>();
			HashMap<String, List<Character>> sourceAttrs = new HashMap<String, List<Character>>();
			HashMap<String, List<Character>> targetAttrs = new HashMap<String, List<Character>>();

			SPJQuery e = (SPJQuery)(gselect.getTerm(i));
			SPJQuery realQ = (SPJQuery)(pquery.getSelect().getTerm(i));
        	SPJQuery subQ = (SPJQuery)(gselect.getValue(i));
        	FromClauseList fcl = subQ.getFrom();
        	String sourceName="";
        	// String targetName=generatedQuery.getTarget(i);
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
        		// targetAttrs.put(targetName, attrLists);
        		targetAttrs.put(sourceName, attrLists);
        	}
        	scenario.putMappings2Correspondences(mKey, corrsList);
        	scenario.putMappings2Sources(mKey, sourceAttrs);
        	scenario.putMappings2Targets(mKey, targetAttrs);

			ArrayList<String> mList = new ArrayList<String>();
			mList.add(mKey);
			scenario.putTransformation2Mappings(tKey, mList);
			scenario.putTransformationCode(tKey, getQueryString(realQ, mKey));
			scenario.putTransformationRelName(tKey, sourceName);
			
			resetAttrLetters();
		}
	}
	
	private String getQueryString(SPJQuery origQ, String mKey) throws Exception {
		return origQ.toTrampStringOneMap(mKey);
//		String retVal = origQ.toString();
//		FromClauseList from = origQ.getFrom();
//		for (int i = 0; i < from.size(); i++) {
//			String key = from.getKey(i).toString();
//			String relName = from.getValue(i).toString();
//			relName = relName.substring(1)+"."; // remove the first "/"
//			retVal = retVal.replace(key, relName).replace("/", "");
//			retVal = retVal.replace(key.substring(1), "");
//			retVal = retVal.replace("${" + i + "}", mKey);
//		}
//		
//		return retVal;
	}

    // it generates a copy case in which there are 2 keys one that is
    // independent of attributes and one that depends on the
    // first two attributes.
    private void createSubElements(Element sourceParent, Element targetParent, int numOfElements, 
    								int numOfParams, SPJQuery pquery, SPJQuery generatedQuery, MappingScenario scenario)
    {
        // create the intermediate query
    	SPJQuery query = new SPJQuery();
    	SPJQuery tmpQ = new SPJQuery(); // temporary query holding only bookkeeping info
        // create the From Clause of the query
        query.getFrom().add(new Variable("X"), new Projection(Path.ROOT,sourceParent.getLabel()));
        tmpQ.getFrom().add(new Variable("X"), new Projection(Path.ROOT,sourceParent.getLabel()));
    	
        SelectClauseList select = query.getSelect();
        SelectClauseList tmpSelect = tmpQ.getSelect();
    	String[] args = new String[numOfParams];
    	String[] keyArgs = new String[numOfElements];
        for (int i = 0; i < numOfElements; i++)
        {
            String randomName = Modules.nameFactory.getARandomName();
            String name = randomName + "_" + _stamp + "AE" + i;
            if (i < numOfParams)
                args[i] = name;
            keyArgs[i] = name;
            // create the atomic element in the source and the target
            SMarkElement es = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            es.setHook(new String(_stamp + "AE" + i));
            sourceParent.addSubElement(es);
            SMarkElement et = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            et.setHook(new String(_stamp + "AE" + i));
            targetParent.addSubElement(et);
            // add the subelements as attributes to the Select clause of the query
            Projection att = new Projection(new Variable("X"),name);
            select.add(name,att);
            tmpSelect.add(name, att);
        }

        // create bookkeeping info for correspondences, mappings and transformations
        tmpQ.setSelect(tmpSelect);
        SelectClauseList gselect = generatedQuery.getSelect();
        gselect.add(targetParent.getLabel(), tmpQ);
        generatedQuery.setSelect(gselect);

        // create the surrogate key elements now. The first one is the one that
        // accepts no arguments
        String randomName = Modules.nameFactory.getARandomName();
        String name = randomName + "_" + _stamp + "IDindep";
        SMarkElement id = new SMarkElement(name, Atomic.STRING, null, 0, 0);
        id.setHook(new String( _stamp + "IDindep"));
        targetParent.addSubElement(id);
        // create the Function corresponding to the key
        // and add it to the select clause of query
        SKFunction f = new SKFunction(scenario.getNextSK());
        for(int i = 0; i < numOfElements; i++)
        	f.addArg(new ConstantAtomicValue(new StringValue(keyArgs[i])));
        select.add(name, f);
        
        
        // and here goes the second one that takes many elements as arguments.
        // It is supposed to be the id/skolem of the first numOfParams
        // attributes of the respective source complex element
        randomName = Modules.nameFactory.getARandomName();
        name = randomName + "_" + _stamp + "IDOnFirst" + numOfParams + "elems";
        id = new SMarkElement(name, Atomic.STRING, null, 0, 0);
        id.setHook(new String( _stamp + "IDOnFirst" + numOfParams + "elems"));
        targetParent.addSubElement(id);
        // create the Function corresponding to the key
        // and add it to the select clause of query
        f = new SKFunction(scenario.getNextSK());
        for(int i=0; i< numOfParams; i++)
        	f.addArg(new ConstantAtomicValue(new StringValue(args[i])));
        select.add(name, f);
        
        // add the subquery to the final transformation query
        query.setSelect(select);
        SelectClauseList pselect = pquery.getSelect();
        pselect.add(targetParent.getLabel(), query);
        pquery.setSelect(pselect);
    }
}
