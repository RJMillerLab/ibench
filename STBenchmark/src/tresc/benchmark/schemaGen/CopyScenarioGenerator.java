package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.apache.log4j.Logger;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.Expression;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;

public class CopyScenarioGenerator extends ScenarioGenerator
{
	static Logger log = Logger.getLogger(CopyScenarioGenerator.class);
	
    private Random _generator;

    private final String _stamp = "CO";
    
    private static int _currAttributeIndex = 0; // this determines the letter used for the attribute in the mapping
    
    public CopyScenarioGenerator() {     }

    public void generateScenario(MappingScenario scenario, Configuration configuration)
    {
        // generate the generator based on the seed
        //long seed = configuration.getScenarioSeeds(Constants.ScenarioName.COPY.ordinal());
        //_generator = (seed == 0) ? new Random() : new Random(seed);
    	
    	_generator=configuration.getRandomGenerator();

        Schema source = scenario.getSource();
        Schema target = scenario.getTarget();        
        SPJQuery pquery = scenario.getTransformation();
        
        int repetitions = configuration.getScenarioRepetitions(Constants.ScenarioName.COPY.ordinal());
        int numOfElements = configuration.getParam(Constants.ParameterName.NumOfSubElements);
        int numOfElementsDeviation = configuration.getDeviation(Constants.ParameterName.NumOfSubElements);
        int nesting = configuration.getParam(Constants.ParameterName.NestingDepth);
        int nestingDeviation = configuration.getDeviation(Constants.ParameterName.NestingDepth);
        
        SelectClauseList pselect = new SelectClauseList();
        for (int i = 0, imax = repetitions; i < imax; i++)
        {
            SPJQuery lquery = new SPJQuery();
            int randomNesting = Utils.getRandomNumberAroundSomething(_generator, nesting, nestingDeviation);
            createSubElements(source, target, 0, randomNesting, i, numOfElements, numOfElementsDeviation, lquery);
            // each local query will be added to the transformation query 
            pselect = pquery.getSelect();
            pselect.add(lquery.getSelect().getTermName(0), lquery.getSelect().getTerm(0));
        }
        
        setScenario(scenario, pselect);
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

	private void setScenario(MappingScenario scenario, SelectClauseList pselect) {
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
        		targetAttrs.put(sourceName, attrLists);
        	}
        	scenario.putMappings2Correspondences(mKey, corrsList);
        	scenario.putMappings2Sources(mKey, sourceAttrs);
        	scenario.putMappings2Targets(mKey, targetAttrs);

			ArrayList<String> mList = new ArrayList<String>();
			mList.add(mKey);
			scenario.putTransformation2Mappings(tKey, mList);
			scenario.putTransformationCode(tKey, getQueryString(e, mKey));
			scenario.putTransformationRelName(tKey, sourceName);
			
			resetAttrLetters();
		}
	}
	
	private String getQueryString(SPJQuery origQ, String mKey) {
		String retVal = origQ.toString();
		FromClauseList from = origQ.getFrom();
		for (int i = 0; i < from.size(); i++) {
			String key = from.getKey(i).toString();
			String relName = from.getValue(i).toString();
			relName = relName.substring(1)+"."; // remove the first "/"
			retVal = retVal.replace(key, relName).replace("/", "");
			retVal = retVal.replace(key.substring(1), "");
			retVal = retVal.replace("${" + i + "}", mKey);
		}
		
		return retVal;
	}

    // Algorithm: Schema generated is the following
    // Source
    // _ProteinN0C0
    // _____NameN1A0
    // _____NameN1A1
    // _____NameN1A2
    // _____ProteinN1C1
    // _________NameN2A0
    // _________NameN2A1
    // _________ProteinN2C0
    // _____________Name ...
    // _____________...
    // _________ProteinN2C1
    // _____________Name ....
    // _____ProteinN1C1 .....
    // 
    // C goes from C0 to CN where N is the repetitions A goes from A0 to AN
    // where N is the numOfElements L goes from L0 to LN where N is the nesting
    // level
    //
    private void createSubElements(Element sourceParent, Element targetParent, int nestingLevel, int maxNesting,
            int repetition, int numOfElements, int numOfElementsDeviation, SPJQuery lquery)
    {
    	SelectClauseList lselect = lquery.getSelect();
    	// decide randomly how many elements to create
        int N = Utils.getRandomNumberAroundSomething(_generator, numOfElements,numOfElementsDeviation);
        // number of set elements
        int S = 0;
        if (maxNesting > 0)
        {
            S = (int) 0.3 * N;
            S = (S == 0) ? 1 : S;
        }
        // number of atomic elements 
        int A = N - S;
        A = (A == 0) ? 1 : A;
        if (nestingLevel == 0)
        {
            A = 0;
            S = 1;
        }
        N = A+S;
    	
    	/*
         * If we are at the nesting level 0 it means that we are right under the
         * schema elements. In that case we do not create the atomic elements.
         */
         for (int i = 0; i < A; i++)
         {
                String randomName = Modules.nameFactory.getARandomName();
                String name = randomName + "_" + _stamp + repetition + "NL" + nestingLevel + "AE" + i;
                SMarkElement es = new SMarkElement(name, Atomic.STRING, null, 0, 0);
                es.setHook(new String(_stamp+repetition + "NL" + nestingLevel + "AE" + i));
                SMarkElement et = new SMarkElement(name, Atomic.STRING, null, 0, 0);
                et.setHook(new String(_stamp+repetition + "NL" + nestingLevel + "AE" + i));
                sourceParent.addSubElement(es);
                targetParent.addSubElement(et);
                
                int lastRelation = lquery.getFrom().size();
                Variable varLastRelation = lquery.getFrom().getExprVar(lastRelation-1);
                lselect.add(name, new Projection(varLastRelation, name)); 
          }

        if (nestingLevel <= maxNesting)
        {
            for (int i = 0; i < S; i++)
            {
                String randomName = Modules.nameFactory.getARandomName();
                String name = randomName + "_" + _stamp + repetition + "NL" + nestingLevel + "CE" + i;
                SMarkElement es = new SMarkElement(name, new Set(), null, 0, 0);
                es.setHook(new String(_stamp+repetition + "NL" + nestingLevel + "CE" + i));
                SMarkElement et = new SMarkElement(name, new Set(), null, 0, 0);
                et.setHook(new String(_stamp+repetition + "NL" + nestingLevel + "CE" + i));
                
                // create the subquery that will be added to the parent query
                SPJQuery query = new SPJQuery();
                Variable var = new Variable("XL"+nestingLevel+"V"+i);
                if (nestingLevel == 0)
                	query.getFrom().add(var,new Projection(Path.ROOT, name)); 
                else{
                	int lastRelation = lquery.getFrom().size();
                	Variable varLastRelation = lquery.getFrom().getExprVar(lastRelation-1);
                	query.getFrom().add(var, new Projection(varLastRelation, name));
                }
                lselect.add(name, query);
                
                createSubElements(es, et, nestingLevel + 1, maxNesting, i, numOfElements, numOfElementsDeviation, query);
                sourceParent.addSubElement(es);
                targetParent.addSubElement(et);
            }
        }
    }
}
