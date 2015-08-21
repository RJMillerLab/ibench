package tresc.benchmark.schemaGen;

import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;

import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.ConstantAtomicValue;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.values.StringValue;

public class ValueGenerationScenarioGenerator extends AbstractScenarioGenerator {

	public ValueGenerationScenarioGenerator() {
		;
	}

	@Override
	protected boolean chooseSourceRels() {
		return true;
	}
	
	@Override
	protected void genSourceRels() {
	}

	
	
	@Override
	protected boolean chooseTargetRels() throws Exception {
		return super.chooseTargetRels();
	}
	
	@Override
	protected void genTargetRels() {
		String relName = randomRelName(0);
		String[] attrs;
		String hook = getRelHook(0);
		
		int ranNumOfEl =Utils.getRandomNumberAroundSomething(_generator, 
				numOfElements, numOfElementsDeviation);
		attrs = new String[ranNumOfEl];
		
		for (int i = 0, imax = ranNumOfEl; i < imax; i++)
			attrs[i] = randomAttrName(0, i);	
		
		fac.addRelation(hook, relName, attrs, false);
	}

	@Override
	protected void genMappings() throws Exception {
		MappingType m1 = fac.addMapping(m.getCorrs());
		
		RelationType tRel = m.getTargetRels().get(0);
		fac.addExistsAtom(m1.getId(), tRel.getName(), 
				fac.getFreshVars(0, tRel.sizeOfAttrArray()));
	}

	@Override
	protected void genTransformations() throws Exception {
		String[] attrs = m.getAttrIds(0, false);
		String nameT = m.getTargetRels().get(0).getName();
		
		// gen query
		SPJQuery generatedQuery = new SPJQuery();
		SPJQuery query = new SPJQuery();
		SelectClauseList select = query.getSelect();

		for (String attName: attrs) {
			String randomName = Modules.nameFactory.getARandomName();
			// add the constants value to the select clause
			select.add(attName, new ConstantAtomicValue(
					new StringValue(randomName)));
		}

		// add everything to the final query
		query.setSelect(select);
//		SelectClauseList pselect = pquery.getSelect();
		SelectClauseList gselect = generatedQuery.getSelect();
//		pselect.add(nameT, query);
		gselect.add(nameT, query);
//		pquery.setSelect(pselect);
		generatedQuery.setSelect(gselect);
		
		// gen trans
		fac.addTransformation(query.toTrampString(m.getMapIds()), 
				m.getMapIds(), nameT);
	}

	@Override
	protected void genCorrespondences() {

	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.VALUEGEN;
	}
}
