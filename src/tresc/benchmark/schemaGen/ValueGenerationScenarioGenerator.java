package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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

public class ValueGenerationScenarioGenerator extends ScenarioGenerator {
	private final String _stamp = "VG";

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
	private void createSubElements(Element sourceParent, Element targetParent,
			int numOfElements, int numOfElementsDeviation, int repetition,
			SPJQuery pquery, SPJQuery generatedQuery) {
		String randomName = Modules.nameFactory.getARandomName();
		String nameT = randomName + "_" + _stamp + "CE" + repetition;
		SMarkElement ce = new SMarkElement(nameT, new Set(), null, 0, 0);
		ce.setHook(new String(_stamp + "CE" + repetition));
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
			name = randomName + "_" + _stamp + "CE" + repetition + "AE" + i;
			SMarkElement et = new SMarkElement(name, Atomic.STRING, null, 0, 0);
			et.setHook(new String(_stamp + "CE" + repetition + "AE" + i));
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
	}

	@Override
	protected void genSourceRels() {
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
