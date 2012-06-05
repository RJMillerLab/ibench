package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Modules;
import tresc.benchmark.Constants.JoinKind;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.ForeignKey;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.Function;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;
import vtools.dataModel.types.Rcd;

// very similar to merging scenario generator, with source and target schemas swapped
public class VerticalPartitionScenarioGenerator extends ScenarioGenerator
{
    private Random _generator;

    private final String _stamp = "VP";

    private static int _currAttributeIndex = 0; // this determines the letter used for the attribute in the mapping
    
    public VerticalPartitionScenarioGenerator()
    {
        ;
    }

    public void generateScenario(MappingScenario scenario, Configuration configuration)
    {
        _generator = configuration.getRandomGenerator();

        Schema source = scenario.getSource();
        Schema target = scenario.getTarget();
        SPJQuery pquery = scenario.getTransformation();

        // first let's read the parameters
        int repetitions = configuration.getScenarioRepetitions(Constants.ScenarioName.VERTPARTITION.ordinal());
        // How many elements to have in each table
        int numOfElements = configuration.getParam(Constants.ParameterName.NumOfSubElements);
        int numOfElementsDeviation = configuration.getDeviation(Constants.ParameterName.NumOfSubElements);
        // how many tables to have
        int numOfFragments = configuration.getParam(Constants.ParameterName.JoinSize);
        int numOfFragmentsDeviation = configuration.getDeviation(Constants.ParameterName.JoinSize);
        // whether we do star of chain joins
        int joinKind = configuration.getParam(Constants.ParameterName.JoinKind);

        for (int i = 0, imax = repetitions; i < imax; i++)
        {
            SPJQuery generatedQuery = new SPJQuery();

            // decide how many attributes will the source table have
            int numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
                numOfElementsDeviation);

            // number of tables we will use in the target
            int numOfTgtTables = Utils.getRandomNumberAroundSomething(_generator, numOfFragments,
                numOfFragmentsDeviation);

            // decide the kind of join we will follow.
            JoinKind jk = JoinKind.values()[joinKind];
            if (jk == JoinKind.VARIABLE)
            {
                int tmp = Utils.getRandomNumberAroundSomething(_generator, 0, 1);
                if (tmp < 0)
                    jk = JoinKind.STAR;
                else jk = JoinKind.CHAIN;
            }
            SMarkElement srcRel = createSubElements(source, target, numOfSrcTblAttr, numOfTgtTables, jk, i, pquery, generatedQuery);
            
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
			String relName = from.getValue(i).toString();
			relName = relName.substring(1)+"."; // remove the first "/"
			retVal = retVal.replace(key, relName).replace("/", "");
			retVal = retVal.replace(key.substring(1), "");
			retVal = retVal.replace("${" + i + "}", mKey);
		}
		
		return retVal;
	}

    /**
     * This is the main function. It generates a table in the source, a number
     * of tables in the target and a respective number of queries.
     */
    private SMarkElement createSubElements(Schema source, Schema target, int numOfSrcTblAttr, int numOfTgtTables,
            JoinKind jk, int repetition, SPJQuery pquery, SPJQuery generatedQuery)
    {
        // First create the source table
        String sourceRelName = Modules.nameFactory.getARandomName();
        String coding = _stamp + repetition;
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
            coding = _stamp + repetition + "A" + i;
            String srcAttName = namePrefix + "_" + coding;
            SMarkElement el = new SMarkElement(srcAttName, Atomic.STRING, null, 0, 0);
            el.setHook(new String(coding));
            srcRel.addSubElement(el);
            attNames[i] = srcAttName;
        }



        // create the set of the partial (intermediate) queries
        // each query populates a target table. We also create the target tables
        SMarkElement[] trgTables = new SMarkElement[numOfTgtTables];
        SPJQuery[] queries = new SPJQuery[numOfTgtTables];
        for (int i = 0; i < numOfTgtTables; i++)
        {
            SPJQuery q = new SPJQuery();
            q.getFrom().add(new Variable("X"), new Projection(Path.ROOT, sourceRelName));
            queries[i] = q;

            String targetRelNamePrefix = Modules.nameFactory.getARandomName();
            coding = _stamp + repetition + "TT" + i;
            String targetRelName = targetRelNamePrefix + "_" + coding;
            SMarkElement tgtRel = new SMarkElement(targetRelName, new Set(), null, 0, 0);
            tgtRel.setHook(new String(coding));
            target.addSubElement(tgtRel);
            trgTables[i] = tgtRel;
            generatedQuery.addTarget(tgtRel.getLabel());
        }

        // we distribute the source atomic elements among the target relations
        // we add all the atomic elements in the partial queries
        // int attsPerTargetRel = (int) Math.ceil((float) numOfSrcTblAttr /
        // numOfTgtTables);
        int attsPerTargetRel = numOfSrcTblAttr / numOfTgtTables;
        int attrPos = 0;
        for (int ti = 0; ti < numOfTgtTables; ti++)
        {
            SelectClauseList sel = queries[ti].getSelect();
            SMarkElement tgtRel = trgTables[ti];
            for (int i = 0, imax = attsPerTargetRel; i < imax; i++)
            {
                String trgAttrName = attNames[attrPos];
                attrPos++;
                SMarkElement tgtAtomicElt = new SMarkElement(trgAttrName, Atomic.STRING, null, 0, 0);
                String hook = trgAttrName.substring(trgAttrName.indexOf("_"));
                tgtAtomicElt.setHook(hook);
                tgtRel.addSubElement(tgtAtomicElt);

                // since we added an attr in the target, we add an entry in the
                // respective select clause
                Projection att = new Projection(new Variable("X"), trgAttrName);
                sel.add(trgAttrName, att);
            }
        }

        // it may be the case that some elements are left over due to not
        // perfect division between integers. We add them all in the last
        // fragment
        for (int i = attrPos, imax = attNames.length; i < imax; i++)
        {
            String trgAttrName = attNames[i];
            SMarkElement tgtAtomicElt = new SMarkElement(trgAttrName, Atomic.STRING, null, 0, 0);
            String hook = trgAttrName.substring(trgAttrName.indexOf("_"));
            tgtAtomicElt.setHook(hook);
            trgTables[numOfTgtTables - 1].addSubElement(tgtAtomicElt);

            // since we added an attr in the target, we add an entry in the
            // respective select clause
            Projection att = new Projection(new Variable("X"), trgAttrName);
            queries[numOfTgtTables - 1].getSelect().add(trgAttrName, att);
        }



        // now we generate the join attributes in the target tables
        if (jk == JoinKind.STAR)
        {
            coding = _stamp + repetition + "JoinAtt";
            String joinAttName = Modules.nameFactory.getARandomName() + "_" + coding;
            String joinAttNameRef = joinAttName + "Ref";

            SMarkElement joinAttElement = new SMarkElement(joinAttName, Atomic.STRING, null, 0, 0);
            joinAttElement.setHook(new String(coding));
            target.getSubElement(0).addSubElement(joinAttElement);
            // add to the first partial query a skolem function to generate
            // the join attribute in the first target table
            SelectClauseList sel0 = queries[0].getSelect();
            Function f0 = new Function("SK");
            for (int k = 0; k < numOfSrcTblAttr; k++)
            {
                Projection att = new Projection(new Variable("X"), attNames[k]);
                f0.addArg(att);
            }
            sel0.add(joinAttName, f0);
            queries[0].setSelect(sel0);

            for (int i = 1; i < numOfTgtTables; i++)
            {
                SMarkElement joinAttRefElement = new SMarkElement(joinAttNameRef, Atomic.STRING, null, 0, 0);
                joinAttRefElement.setHook(new String(coding + "Ref"));
                target.getSubElement(i).addSubElement(joinAttRefElement);
                // we create the target constraint(i.e. foreign key both ways )
                Variable varKey1 = new Variable("F");
                Variable varKey2 = new Variable("K");
                ForeignKey fKeySrc1 = new ForeignKey();
                fKeySrc1.addLeftTerm(varKey1.clone(),
                    new Projection(Path.ROOT, target.getSubElement(0).getLabel()));
                fKeySrc1.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,
                    target.getSubElement(i).getLabel()));
                fKeySrc1.addFKeyAttr(new Projection(varKey2.clone(), joinAttNameRef), new Projection(
                    varKey1.clone(), joinAttName));
                target.addConstraint(fKeySrc1);
                ForeignKey fKeySrc2 = new ForeignKey();
                fKeySrc2.addLeftTerm(varKey1.clone(),
                    new Projection(Path.ROOT, target.getSubElement(i).getLabel()));
                fKeySrc2.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,
                    target.getSubElement(0).getLabel()));
                fKeySrc2.addFKeyAttr(new Projection(varKey2.clone(), joinAttName), new Projection(varKey1.clone(),
                    joinAttNameRef));
                target.addConstraint(fKeySrc2);
                // add to the each partial query a skolem function to generate
                // the join
                // reference attribute in all the other target tables
                SelectClauseList seli = queries[i].getSelect();
                Function fi = (Function) f0.clone();
                seli.add(joinAttNameRef, fi);
                queries[i].setSelect(seli);
            }
        }

        if (jk == JoinKind.CHAIN)
        {
            // create a skolem function which has all the
            // source attributes as arguments
            Function f = new Function("SK");
            for (int k = 0; k < numOfSrcTblAttr; k++)
            {
                Projection att = new Projection(new Variable("X"), attNames[k]);
                f.addArg(att);
            }

            for (int i = 0; i < numOfTgtTables - 1; i++)
            {
            	int tgtPos = repetition * numOfTgtTables + i;
                coding = _stamp + repetition + "JoinAtt";
                String joinAttName = Modules.nameFactory.getARandomName() + "_" + coding;
                String joinAttNameRef = joinAttName + "Ref";

                SMarkElement joinAttElement = new SMarkElement(joinAttName, Atomic.STRING, null, 0, 0);
                joinAttElement.setHook(new String(coding));
                SMarkElement joinAttRefElement = new SMarkElement(joinAttNameRef, Atomic.STRING, null, 0, 0);
                joinAttRefElement.setHook(new String(coding + "Ref"));

                target.getSubElement(tgtPos).addSubElement(joinAttElement);
                target.getSubElement(tgtPos + 1).addSubElement(joinAttRefElement);
                // we create the target constraint(i.e. foreign key both ways )
                Variable varKey1 = new Variable("F");
                Variable varKey2 = new Variable("K");
                ForeignKey fKeySrc1 = new ForeignKey();
                fKeySrc1.addLeftTerm(varKey1.clone(),
                    new Projection(Path.ROOT, target.getSubElement(tgtPos).getLabel()));
                fKeySrc1.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,
                    target.getSubElement(tgtPos + 1).getLabel()));
                fKeySrc1.addFKeyAttr(new Projection(varKey2.clone(), joinAttNameRef), new Projection(
                    varKey1.clone(), joinAttName));
                target.addConstraint(fKeySrc1);
                ForeignKey fKeySrc2 = new ForeignKey();
                fKeySrc2.addLeftTerm(varKey1.clone(), new Projection(Path.ROOT,
                    target.getSubElement(tgtPos + 1).getLabel()));
                fKeySrc2.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,
                    target.getSubElement(tgtPos).getLabel()));
                fKeySrc2.addFKeyAttr(new Projection(varKey2.clone(), joinAttName), new Projection(varKey1.clone(),
                    joinAttNameRef));
                target.addConstraint(fKeySrc2);
                // add to each partial query the skolem function that generates
                // the join attribute
                // and the join reference attribute in each target table
                SelectClauseList sel1 = queries[i].getSelect();
                Function f1 = (Function) f.clone();
                sel1.add(joinAttName, f1);
                queries[i].setSelect(sel1);
                SelectClauseList sel2 = queries[i + 1].getSelect();
                Function f2 = (Function) f.clone();
                sel2.add(joinAttNameRef, f2);
                queries[i + 1].setSelect(sel2);
            }
        }

        // add the partial queries to the parent query
        // to form the whole transformation
        SelectClauseList pselect = pquery.getSelect();
        SelectClauseList gselect = generatedQuery.getSelect();
        for (int i = 0; i < numOfTgtTables; i++)
        {
            String tblTrgName = trgTables[i].getLabel();
            pselect.add(tblTrgName, queries[i]);
            gselect.add(tblTrgName, queries[i]);
        }
        pquery.setSelect(pselect);
        generatedQuery.setSelect(gselect);
        return srcRel;
    }
}
