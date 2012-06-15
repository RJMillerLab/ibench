package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.CorrespondenceType;
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
import vtools.dataModel.expression.Expression;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Query;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;

public class CopyScenarioGenerator extends AbstractScenarioGenerator {

	static Logger log = Logger.getLogger(CopyScenarioGenerator.class);

	public CopyScenarioGenerator() {
	}

	@Override
	protected void genMappings() throws Exception {
		RelationType source = m.getSourceRels().get(0);
		RelationType target = m.getTargetRels().get(0);
		
		String[] vars = fac.getFreshVars(0, source.getAttrArray().length);
		MappingType m1 = fac.addMapping(m.getCorrs());
		fac.addForeachAtom(m1.getId(), source.getName(), vars);
		fac.addExistsAtom(m1.getId(), target.getName(), vars);	
	}

	@Override
	protected void genCorrespondences () {
		RelationType source = m.getSourceRels().get(0);
		RelationType target = m.getTargetRels().get(0);
		
		for(AttrDefType attr: source.getAttrArray()) {
			CorrespondenceType c = fac.addCorrespondence(source.getName(), 
					attr.getName(), target.getName(), attr.getName());
		}
	}
	
	@Override
	protected void genTransformations () throws Exception {
		TransformationType t;
		RelationType target = m.getTargetRels().get(0);
		RelationType source = m.getSourceRels().get(0);
		String tRelName = target.getName();
		String sRelName = source.getName();
		
		// create STBench query
		SPJQuery lquery = new SPJQuery();
		SelectClauseList lselect = lquery.getSelect();
		
		SPJQuery query = new SPJQuery();
		SelectClauseList qselect = query.getSelect();
		Variable var = new Variable(("XL" + 0 + "V" + 0).toLowerCase());
		query.getFrom().add(var, new Projection(Path.ROOT, sRelName));
		lselect.add(sRelName, query);
		
		for(int i = 0; i < target.getAttrArray().length; i++) {
			String attrName = target.getAttrArray()[i].getName();
			qselect.add(attrName, new Projection(var, attrName));
		}
		
		SelectClauseList pselect = pquery.getSelect();
		pselect.add(lquery.getSelect().getTermName(0), lquery.getSelect()
				.getTerm(0));
		SPJQuery q = (SPJQuery) pselect.getTerm(curRep);
		
		m.addQuery(q);
		
		// add Tramp transformation
		t = fac.addTransformation(q.toTrampString(m.getMapIds()), m.getMapIds(), tRelName);
	}
	
	
	@Override
	protected void genSourceRels() throws Exception {
		int A = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
				numOfElementsDeviation);
		String[] attrs = new String[A];
		String relName = randomRelName(0);
		String hook = getRelHook(0);
		
		for(int i = 0; i < A; i++) {
			attrs[i] = randomAttrName(0, i);
		}
		RelationType rel = fac.addRelation(hook, relName, attrs, true);
		fac.addPrimaryKey(rel.getName(), new String[] {attrs[0]}, true);
	}

	@Override
	protected void genTargetRels() {
		RelationType s = m.getSourceRels().get(0);
		String[] attrs = new String[s.getAttrArray().length];
		String relName = s.getName() + "copy";
		String hook = getRelHook(0);
		
		for(int i = 0; i < s.getAttrArray().length; i++)
			attrs[i] = s.getAttrArray()[i].getName();
		fac.addRelation(hook, relName, attrs, false);
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.COPY;
	}
}
