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
	/*private void createSubElements(Element sourceParent, Element targetParent,
			int numOfElements, int numOfElementsDeviation, int repetition,
			SPJQuery pquery, SPJQuery generatedQuery) {
		String randomName = Modules.nameFactory.getARandomName();
		String nameT = randomName + "_" + getStamp() + "CE" + repetition;
		SMarkElement ce = new SMarkElement(nameT, new Set(), null, 0, 0);
		ce.setHook(new String(getStamp() + "CE" + repetition));
		targetParent.addSubElement(ce);

		// decide randomly how many elements to create
		String name = null;
		SPJQuery query = new SPJQuery();
		SelectClauseList select = query.getSelect();

		int ranNumOfEl =
				Utils.getRandomNumberAroundSomething(_generator, numOfElements,
						numOfElementsDeviation);
		for (int i = 0, imax = ranNumOfEl; i < imax; i++) {
			randomName = Modules.nameFactory.getARandomName();
			name = randomName + "_" + getStamp() + "CE" + repetition + "AE" + i;
			SMarkElement et = new SMarkElement(name, Atomic.STRING, null, 0, 0);
			et.setHook(new String(getStamp() + "CE" + repetition + "AE" + i));
			ce.addSubElement(et);

			// add the constants value to the select clause
			select.add(name, new ConstantAtomicValue(
					new StringValue(randomName)));
		}

		// add everything to the final query
		query.setSelect(select);
		SelectClauseList pselect = pquery.getSelect();
		SelectClauseList gselect = generatedQuery.getSelect();
		pselect.add(nameT, query);
		gselect.add(nameT, query);
		pquery.setSelect(pselect);
		generatedQuery.setSelect(gselect);
	}*/

	@Override
	protected void chooseSourceRels() {
		
	}
	
	@Override
	protected void genSourceRels() {
	}

	
	
	@Override
	protected void chooseTargetRels() {
		RelationType r = getRandomRel(false);
		m.addTargetRel(r);
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
		SelectClauseList pselect = pquery.getSelect();
		SelectClauseList gselect = generatedQuery.getSelect();
		pselect.add(nameT, query);
		gselect.add(nameT, query);
		pquery.setSelect(pselect);
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
