package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.AND;
import vtools.dataModel.expression.EQ;
import vtools.dataModel.expression.ForeignKey;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.Key;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;

public class SelfJoinScenarioGenerator extends ScenarioGenerator
{
    private Random _generator;

    private final String _stamp = "SJ";

    private static int _currAttributeIndex = 0; // this determines the letter used for the attribute in the mapping
    
    public SelfJoinScenarioGenerator()
    {
        ;
    }

    public void generateScenario(MappingScenario scenario, Configuration configuration)
    {
        // generate the generator based on the seed
        // long seed =
        // configuration.getScenarioSeeds(Constants.ScenarioName.SELFJOINS.ordinal());
        // seed = (seed == 0) ? System.currentTimeMillis() : seed;
        // _generator = new Random(seed);

        if (configuration.getScenarioRepetitions(Constants.ScenarioName.GLAV.ordinal()) != 0) { return; }


        _generator = configuration.getRandomGenerator();

        Schema source = scenario.getSource();
        Schema target = scenario.getTarget();
        SPJQuery pquery = scenario.getTransformation();

        int repetitions = configuration.getScenarioRepetitions(Constants.ScenarioName.SELFJOINS.ordinal());
        int numOfElements = configuration.getParam(Constants.ParameterName.NumOfSubElements);
        int numOfElementsDeviation = configuration.getDeviation(Constants.ParameterName.NumOfSubElements);
        int numOfKeys = configuration.getParam(Constants.ParameterName.NumOfJoinAttributes);
        int numOfKeysDeviation = configuration.getDeviation(Constants.ParameterName.NumOfJoinAttributes);
        int joinSize = configuration.getParam(Constants.ParameterName.JoinSize);
        int joinSizeDeviation = configuration.getDeviation(Constants.ParameterName.JoinSize);
        for (int i = 0, imax = repetitions; i < imax; i++)
        {
            SPJQuery generatedQuery = new SPJQuery();

            // decide the number of elements
            int E = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
            // decide the number of keys,i.e. the number of join attributes
            int K = Utils.getRandomNumberAroundSomething(_generator, numOfKeys, numOfKeysDeviation);
            E = (E < ((2 * K) + 1)) ? ((2 * K) + 1) : E;
            // decide the size of the join
            int JN = Utils.getRandomNumberAroundSomething(_generator, joinSize, joinSizeDeviation);

            if (JN == 0)
                continue;

            SMarkElement srcRel = createSubElements(source, target, E, K, JN, i, pquery, generatedQuery);
            
            setScenario(scenario, generatedQuery, pquery, srcRel);
        }
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

	private void setScenario(MappingScenario scenario, SPJQuery generatedQuery, SPJQuery pquery, SMarkElement srcRel) {
		SelectClauseList gselect = generatedQuery.getSelect();
		HashMap<String, ArrayList<Character>> sourceAttrs = new HashMap<String, ArrayList<Character>>();
		HashMap<String, ArrayList<Character>> targetAttrs = new HashMap<String, ArrayList<Character>>();
		
		String mKey = scenario.getNextMid();

		ArrayList<String> corrsList = new ArrayList<String>();
		
		ArrayList<Character> sourceRelAttrs = new ArrayList<Character>();
		for (int j = 0; j < srcRel.size(); j++) {
			Element attr = srcRel.getSubElement(j);
			sourceRelAttrs.add(getAttrLetter(attr.getLabel()));
		}
    	String sourceName = srcRel.getLabel();
		sourceAttrs.put(sourceName, sourceRelAttrs);
		
		ArrayList<String> targets = generatedQuery.getTargets();

		for (int i = 0; i < targets.size(); i++) {
			String tKey = scenario.getNextTid();
			String targetName = targets.get(i);
			ArrayList<Character> targetRelAttrs = new ArrayList<Character>();

			SPJQuery e = (SPJQuery)(gselect.getTerm(i));
        	FromClauseList fcl = e.getFrom();
        	SelectClauseList scl = e.getSelect();
        	String key = fcl.getKey(0).toString();
        	String[] sclArray = scl.toString().split(",");
        	for (int k = 0; k < sclArray.length; k++) {
        		String attr = sclArray[k];
        		if (attr.contains(key)) {  // there is a correspondence
        			attr = attr.replaceFirst("\\"+key+"/", "").trim();
        			String sourceRelAttr = sourceName + "." + attr;
        			String targetRelAttr = targetName + "." + attr;
        			String cKey = scenario.getNextCid();
        			String cVal = sourceRelAttr + "=" + targetRelAttr;
        			scenario.putCorrespondences(cKey, cVal);
        			corrsList.add(cKey);
        		}
    			targetRelAttrs.add(getAttrLetter(attr));
        	}
        	targetAttrs.put(targetName, targetRelAttrs);
    		ArrayList<String> mList = new ArrayList<String>();
    		mList.add(mKey);
    		scenario.putTransformation2Mappings(tKey, mList);
    		// scenario.putTransformationCode(tKey, getQueryString(realQ));
    		scenario.putTransformationCode(tKey, getQueryString(e, mKey));
    		scenario.putTransformationRelName(tKey, targetName);
    			
		}
        	
		scenario.putMappings2Correspondences(mKey, corrsList);
        scenario.putMappings2Sources(mKey, sourceAttrs);
        scenario.putMappings2Targets(mKey, targetAttrs);

		resetAttrLetters();
	}
	
	private String getQueryString(SPJQuery origQ, String mKey) {
		String retVal = origQ.toString();
		FromClauseList from = origQ.getFrom();
		for (int i = 0; i < from.size(); i++) {
			String key = from.getKey(i).toString();
			String relAlias = key.replace("$", "");
			retVal = retVal.replace(key+"/", relAlias+".");
			retVal = retVal.replace("${" + i + "}", mKey);
		}
		retVal = retVal.replaceAll("/", "");
		
		return retVal;
	}

    // the Source schema has one table with E number of elements, from which K
    // are keys, other K are foreign keys
    // and the rest until E will be free elements
    private SMarkElement createSubElements(Schema source, Schema target, int E, int K, int JN, int repetition,
            SPJQuery pquery, SPJQuery generatedQuery)
    {
        String[] keyS = new String[K];
        String[] FkeyS = new String[K];
        // create the source table
        String name = Modules.nameFactory.getARandomName();
        String nameS = name + "_" + _stamp + repetition;
        SMarkElement srcEl = new SMarkElement(nameS, new Set(), null, 0, 0);
        srcEl.setHook(new String(_stamp + repetition));
        source.addSubElement(srcEl);

        // create the first table in the target schema; it contains the keys
        // and the free attributes from the source table;
        // it is the Basic target table
        String nameT = nameS + "_B";
        SMarkElement trgEl = new SMarkElement(nameT, new Set(), null, 0, 0);
        trgEl.setHook(new String(_stamp + repetition+ "_B"));
        target.addSubElement(trgEl);
        // create the first intermediate query
        SPJQuery query = new SPJQuery();
        // create the from clause of the query
        Variable var = new Variable("X");
        query.getFrom().add(var.clone(), new Projection(Path.ROOT,nameS));

        // generate the keys in the source and Basic target table
        // add the keys constraints to the source and to the target
        SelectClauseList select = query.getSelect();
        Variable varKey = new Variable("K");
        // the key constraint in the source
        Key keySrc = new Key();
        keySrc.addLeftTerm(varKey.clone(), new Projection(Path.ROOT,nameS));
        keySrc.setEqualElement(varKey.clone());
        // the key constraint in the target
        Key keyTrg = new Key();
        keyTrg.addLeftTerm(varKey.clone(), new Projection(Path.ROOT,nameT));
        keyTrg.setEqualElement(varKey.clone());
        for (int i = 0; i < K; i++)
        {
            name = Modules.nameFactory.getARandomName();
            name = name + "_" + _stamp + repetition + "KE" + i;
            keyS[i] = name;
            SMarkElement el = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            el.setHook(new String( _stamp + repetition + "KE" + i));
            srcEl.addSubElement(el);
            // add the attribute that is part of the key constraint of the source
            keySrc.addKeyAttr(new Projection(varKey.clone(),name));
            
            el = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            el.setHook(new String( _stamp + repetition + "KE" + i));
            trgEl.addSubElement(el);
            // add the attribute that is part of the key constraint of the target
            keyTrg.addKeyAttr(new Projection(varKey.clone(),name));
            
            // add the keys to the select clause of the query
            Projection att = new Projection(var.clone(), name);
            select.add(name, att);
        }
        source.addConstraint(keySrc);
        target.addConstraint(keyTrg);

        // generate the foreign key in the source table; the Basic target table
        // does not contain foreign keys
        Variable varKey1 = new Variable("F");
        Variable varKey2 = new Variable("K");
        ForeignKey fKeySrc = new ForeignKey();
        fKeySrc.addLeftTerm(varKey1.clone(), new Projection(Path.ROOT,nameS));
        fKeySrc.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,nameS));
        for (int i = 0; i < K; i++)
        {
            name = Modules.nameFactory.getARandomName();
            name = name + "_" + _stamp + repetition + "FK" + i;
            FkeyS[i] = name;
            SMarkElement el = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            el.setHook(new String( _stamp + repetition + "FE" + i));
            srcEl.addSubElement(el);
            // add the attributes that make up the foreign key
            fKeySrc.addFKeyAttr(new Projection(varKey2.clone(),keyS[i]), 
                                new Projection(varKey1.clone(),FkeyS[i]));
        }
        source.addConstraint(fKeySrc);
        source.addConstraint(fKeySrc); // This just duplicates the foreign key to make it consistent with other senarios.
        
        // generate the free elements in the source table and in the Basic
        // target table only
        int F = E - (2 * K);
        for (int i = 0; i < F; i++)
        {
            name = Modules.nameFactory.getARandomName();
            name = name + "_" + _stamp + repetition + "FE" + i;
            SMarkElement el = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            el.setHook(new String( _stamp + repetition + "FE" + i));
            srcEl.addSubElement(el);
            el = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            el.setHook(new String( _stamp + repetition + "FE" + i));
            trgEl.addSubElement(el);
            // add the free elements to the select clause of the query
            Projection att = new Projection(var.clone(), name);
            select.add(name, att);
        }

        // add the first query to the final query
        query.setSelect(select);
        SelectClauseList pselect = pquery.getSelect();
        SelectClauseList gselect = generatedQuery.getSelect();
        pselect.add(nameT, query);
        gselect.add(nameT, query);
        pquery.setSelect(pselect);
        generatedQuery.setSelect(gselect);
        generatedQuery.addTarget(nameT);

        if (JN == 1)
            return srcEl;

        // create the second table in the target schema;
        // it is the Join target table it contains the keys
        // of the source table and references to the foreign
        // keys of the source table;
        // it is obtained by self-join-ing the source table for JN times
        String nameT2 = nameS + "_J";
        SMarkElement trgEl2 = new SMarkElement(nameT2, new Set(), null, 0, 0);
        trgEl2.setHook(new String(_stamp + repetition+ "_J"));
        
        target.addSubElement(trgEl2);
        // create the second intermediate query
        SPJQuery query2 = new SPJQuery();
        // create the from clause of the second query
        for (int i = 1; i <= JN; i++)
        {
            query2.getFrom().add(new Variable("X" + i), new Projection(Path.ROOT, nameS));
        }

        // generate the keys in the Join target table
        // add the key constraint for the Join target table     
        keyTrg = new Key();
        keyTrg.addLeftTerm(varKey.clone(), new Projection(Path.ROOT,nameT2));
        keyTrg.setEqualElement(varKey.clone());
        // add the foreign key constraint for the Join target table
        varKey1 = new Variable("F");
        varKey2 = new Variable("K");
        ForeignKey fKeyTrg = new ForeignKey();
        fKeyTrg.addLeftTerm(varKey1.clone(), new Projection(Path.ROOT,nameT2));
        fKeyTrg.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,nameT));
        for (int i = 0; i < K; i++)
        {
            SMarkElement el = new SMarkElement(keyS[i], Atomic.STRING, null, 0, 0);
            String hook = keyS[i].substring(keyS[i].indexOf("_"));
            el.setHook(hook);
            trgEl2.addSubElement(el);
            // add the attribute that is part of the key constraint of the target
            keyTrg.addKeyAttr(new Projection(varKey.clone(),keyS[i]));
            // add the attributes that makes up the foreign key of the target
            fKeyTrg.addFKeyAttr(new Projection(varKey2.clone(),keyS[i]), 
                                new Projection(varKey1.clone(),keyS[i]));
        }
        target.addConstraint(keyTrg);
        target.addConstraint(fKeyTrg);
        target.addConstraint(fKeyTrg); // Same as above. Duplicate the foreign key to make the XML print correct.
        
        // generate the first part of the Select clause of the second query
        // add as attr all the keys that belong to the first relation
        // that appears in the From clause
        SelectClauseList select2 = query2.getSelect();
        for (int i = 0; i < K; i++)
        {
            Projection att = new Projection(new Variable("X1"), keyS[i]);
            select2.add(keyS[i], att);
        }

        // generate in the Join target table the pointers to the keys
        // of the source; RE stands for Reference element
        // also generate the second part of the Select clause of the second
        // query by adding as attr all the keys that
        // belong to the last relation that appears in the From clause
        for (int i = 0; i < K; i++)
        {
            name = Modules.nameFactory.getARandomName();
            name = name + "_" + _stamp + repetition + "RE" + i;
            SMarkElement el = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            el.setHook(new String(_stamp + repetition + "RE" + i));
            trgEl2.addSubElement(el);
            Projection att = new Projection(new Variable("X" + JN), keyS[i]);
            select2.add(name, att);
        }

        // generate the Where clause of the second query; that
        // constructs the joining of the source for JN times
        AND where = new AND();
        for (int j = 1; j < JN; j++)
            for (int i = 0; i < K; i++)
            {
                Projection att1 = new Projection(new Variable("X" + (j + 1)), FkeyS[i]);
                Projection att2 = new Projection(new Variable("X" + j), keyS[i]);
                where.add(new EQ(att1, att2));
            }

        // add the second query to the final query
        query2.setSelect(select2);
        query2.setWhere(where);
        pselect = pquery.getSelect();
        gselect = generatedQuery.getSelect();
        pselect.add(nameT2, query2);
        gselect.add(nameT2, query2);
        pquery.setSelect(pselect);
        // gselect.add(trgEl2.getLabel(), query);
        generatedQuery.setSelect(gselect);
        generatedQuery.addTarget(nameT2);
        return srcEl;
    }
}
