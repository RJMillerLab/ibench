package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.vagabond.xmlmodel.MappingType;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Constants.ScenarioName;
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

public class SurrogateKeysScenarioGenerator extends AbstractScenarioGenerator
{

	private int params;
	private int elements;
    
    public SurrogateKeysScenarioGenerator()
    {
        ;
    }

    @Override
	protected void initPartialMapping() {
		super.initPartialMapping();
		elements = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
						numOfElementsDeviation);
		params = Utils.getRandomNumberAroundSomething(_generator, numOfParams,
						numOfParamsDeviation);
		// make sure params are at least 2
		params = (params < 2) ? 2 : params;
		// and the elements are at least as many as the the params
		if (params > elements)
			elements = params;
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
            String name = randomName + "_" + getStamp() + "AE" + i;
            if (i < numOfParams)
                args[i] = name;
            keyArgs[i] = name;
            // create the atomic element in the source and the target
            SMarkElement es = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            es.setHook(new String(getStamp() + "AE" + i));
            sourceParent.addSubElement(es);
            SMarkElement et = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            et.setHook(new String(getStamp() + "AE" + i));
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
        String name = randomName + "_" + getStamp() + "IDindep";
        SMarkElement id = new SMarkElement(name, Atomic.STRING, null, 0, 0);
        id.setHook(new String( getStamp() + "IDindep"));
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
        name = randomName + "_" + getStamp() + "IDOnFirst" + numOfParams + "elems";
        id = new SMarkElement(name, Atomic.STRING, null, 0, 0);
        id.setHook(new String( getStamp() + "IDOnFirst" + numOfParams + "elems"));
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


	@Override
	protected void genSourceRels() {
		String relName = randomRelName(0);
		String[] attrs = new String[elements];
		
		for(int i =0; i < elements; i++) {
			attrs[i] = randomAttrName(0, i);
		}
		
		fac.addRelation(getRelHook(0), relName, attrs, true);
	}

	@Override
	protected void genTargetRels() {
		String tRelName = randomRelName(0);
		int numTargetEl = elements + 2;
		String[] attrs = new String[numTargetEl];
		
		for(int i = 0; i < elements; i++)
			attrs[i] = m.getAttrId(0, i, true);
		attrs[elements] = randomAttrName(0, elements) + "IDindep";
		attrs[elements + 1] = randomAttrName(0, elements + 1) + "IDOnFirst";
		
		fac.addRelation(getRelHook(0), tRelName, attrs, false);
	}
	
	@Override
	protected void genMappings() throws Exception {
		MappingType m1 = fac.addMapping(m.getCorrs());
		
		fac.addForeachAtom(m1, 0, fac.getFreshVars(0, elements));
		fac.addExistsAtom(m1, 0, fac.getFreshVars(0, elements + 2));
	}
	
	@Override
	protected void genTransformations() throws Exception {
		SPJQuery q;
		String creates = m.getRelName(0, false);
		
		q = genQueries();
		fac.addTransformation(q.toTrampString(m.getMapIds()), m.getMapIds(), creates);
	}
	
	private SPJQuery genQueries() {
		String sourceName = m.getRelName(0, true);
		String targetName = m.getRelName(0, false);
		String[] attrNames = m.getAttrIds(0, false);
		
		// create the intermediate query
		SPJQuery query = new SPJQuery();

		// create the From Clause of the query
		query.getFrom().add(new Variable("X"),
				new Projection(Path.ROOT, sourceName));

		SelectClauseList select = query.getSelect();
		String[] args = new String[numOfParams];
		String[] keyArgs = new String[elements];
		
		for (int i = 0; i < elements; i++) {
			// create the atomic element in the source and the target
			// add the subelements as attributes to the Select clause of the
			// query
			Projection att = new Projection(new Variable("X"), attrNames[i]);
			select.add(attrNames[i], att);
		}

		// create the surrogate key elements now. The first one is the one that
		// accepts no arguments
		// create the Function corresponding to the key
		// and add it to the select clause of query
		SKFunction f = new SKFunction(fac.getNextId("SK"));
		for (int i = 0; i < elements; i++)
			f.addArg(new Projection(new Variable("X"), attrNames[i]));
		select.add(attrNames[elements], f);

		// create the Function corresponding to the key
		// and add it to the select clause of query
		f = new SKFunction(fac.getNextId("SK"));
		for (int i = 0; i < numOfParams; i++)
			f.addArg(new Projection(new Variable("X"), attrNames[i]));
		select.add(attrNames[elements + 1], f);

		// add the subquery to the final transformation query
		query.setSelect(select);
		SelectClauseList pselect = pquery.getSelect();
		pselect.add(targetName, query);
		pquery.setSelect(pselect);
		
		return query;
	}

	@Override
	protected void genCorrespondences() {
		for(int i = 0; i < elements; i++)
			addCorr(0, i, 0, i);
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.SURROGATEKEY;
	}
}
