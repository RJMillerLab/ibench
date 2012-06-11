package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;

import vtools.dataModel.expression.ConstantAtomicValue;
import vtools.dataModel.expression.Expression;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;
import vtools.dataModel.values.StringValue;

public class ValueGenerationScenarioGenerator extends ScenarioGenerator
{
    private final String _stamp = "VG";

    public ValueGenerationScenarioGenerator()
    {
        ;
    }

    public void generateScenario(MappingScenario scenario, Configuration configuration)
    {
    	init(configuration, scenario);
        SPJQuery generatedQuery = new SPJQuery();

        for (int i = 0, imax = repetitions; i < imax; i++)
        	createSubElements(source, target, numOfElements, numOfElementsDeviation, i, pquery, generatedQuery);
        
        setScenario(scenario, generatedQuery);
    }
    
    private void setScenario(MappingScenario scenario, SPJQuery gquery) {
		SelectClauseList scl = gquery.getSelect();
    	
		for (int i = 0; i < scl.size(); i++) {
			String tKey = scenario.getNextTid();
			String mKey = scenario.getNextMid();
			String targetName = scl.getTermName(i);
			SelectClauseList atts = ((SPJQuery) scl.getTerm(i)).getSelect();
			
			HashMap<String, List<Character>> targetAttrs = new HashMap<String , List<Character>> ();
			targetAttrs.put(targetName, new ArrayList<Character> ());
			for(int j = 0; j < atts.size(); j++) {
				char attr = (char) ('a' + j);
				targetAttrs.get(targetName).add(attr);
			}
//			sourceName = target.get fcl.getValue(j).toString().substring(1);
//			targetName = sourceName + "Copy";
//			String[] sclArray = scl.toString().split(",");
//			for (int k = 0; k < sclArray.length; k++) {
//				String attr = sclArray[k];
//				attr = attr.replaceFirst("\\" + key + "/", "").trim();
//				attrs.add(attr);
//				attrLists.add(getAttrLetter(attr));
//				//
//			}
//			targetAttrs.put(targetName, attrLists);
			
			
			scenario.putTransformation2Mappings(tKey, Collections.singletonList(mKey));
			scenario.putTransformationCode(tKey, getQueryString(scl.getValue(i).toString()));
			scenario.putTransformationRelName(tKey, targetName);
			scenario.putMappings2Correspondences(mKey, null);
			scenario.putMappings2Sources(mKey, null);
			scenario.putMappings2Targets(mKey, targetAttrs);
		}
   }
    
   private String getQueryString(String origQ) {
		return origQ.replace("(", "").replace(")", ""); // remove brackets
   }

    //
    // Algorithm: Schema generated is the following
    // Target
    // _DataSetVGCE0
    // _____AttributeVGCE0AE0
    // _____AttributeVGCE0AE1
    // _____AttributeVGCE0AE2
    // ... ...
    // _DataSetVGCE1
    // _____AttributeVGCE1AE0
    // _____AttributeVGCE1AE1
    // _____AttributeVGCE1AE2
    // .....
    private void createSubElements(Element sourceParent, Element targetParent, int numOfElements,
            int numOfElementsDeviation, int repetition, SPJQuery pquery, SPJQuery generatedQuery)
    {
        String randomName = Modules.nameFactory.getARandomName();
        String nameT = randomName + "_" + _stamp + "CE" + repetition;
        SMarkElement ce = new SMarkElement(nameT, new Set(), null, 0,0);
        ce.setHook(new String(_stamp + "CE" + repetition));
        targetParent.addSubElement(ce);

        // decide randomly how many elements to create
        String name = null;
        SPJQuery query = new SPJQuery();
        SelectClauseList select = query.getSelect();
        
        int ranNumOfEl = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
        for (int i = 0, imax = ranNumOfEl; i < imax; i++)
        {
            randomName = Modules.nameFactory.getARandomName();
            name = randomName + "_" + _stamp + "CE" + repetition + "AE" + i;
            SMarkElement et = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            et.setHook(new String(_stamp + "CE" + repetition + "AE" + i));
            ce.addSubElement(et);
            
            // add the constants value to the select clause
            select.add(name, new ConstantAtomicValue(new StringValue(randomName)));
        }
        
        //  add everything to the final query
        query.setSelect(select);
        SelectClauseList pselect = pquery.getSelect();
        SelectClauseList gselect = generatedQuery.getSelect();
        pselect.add(nameT, query);
        gselect.add(nameT, query);
        pquery.setSelect(pselect);
        generatedQuery.setSelect(gselect);
    }

	@Override
	protected void genMapsAndTrans() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void genSourceRels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void genTargetRels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.VALUEGEN;
	}
}
