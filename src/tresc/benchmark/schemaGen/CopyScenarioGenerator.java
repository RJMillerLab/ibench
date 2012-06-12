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

public class CopyScenarioGenerator extends ScenarioGenerator {

	static Logger log = Logger.getLogger(CopyScenarioGenerator.class);

	private static int _currAttributeIndex = 0; // this determines the letter
												// used for the attribute in the
												// mapping

	public CopyScenarioGenerator() {
	}

//	public void generateScenario(MappingScenario scenario,
//			Configuration configuration) throws Exception {
//		init(configuration, scenario);
//
//		SelectClauseList pselect = new SelectClauseList();
//		for (int i = 0, imax = getRepetitions(); i < imax; i++) {
//			SPJQuery lquery = new SPJQuery();
//			int randomNesting =
//					Utils.getRandomNumberAroundSomething(_generator, nesting,
//							getNestingDeviation());
//			createSubElements(source, target, 0, randomNesting, i,
//					numOfElements, numOfElementsDeviation, lquery);
//			// each local query will be added to the transformation query
//			pselect = pquery.getSelect();
//			pselect.add(lquery.getSelect().getTermName(0), lquery.getSelect()
//					.getTerm(0));
//		}
//
//		setScenario(scenario, pselect);
//	}

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

	private void setScenario(MappingScenario scenario, SelectClauseList pselect)
			throws Exception {
		for (int i = 0; i < pselect.size(); i++) {
			String mKey = scenario.getNextMid();
			String tKey = scenario.getNextTid();

			List<CorrespondenceType> corrs =
					new ArrayList<CorrespondenceType>();
			List<RelationType> rels = new ArrayList<RelationType>();
			ArrayList<String> corrsList = new ArrayList<String>();
			HashMap<String, List<Character>> sourceAttrs =
					new HashMap<String, List<Character>>();
			HashMap<String, List<Character>> targetAttrs =
					new HashMap<String, List<Character>>();

			SPJQuery e = (SPJQuery) (pselect.getTerm(i));
			SPJQuery subQ = (SPJQuery) (pselect.getValue(i));
			FromClauseList fcl = subQ.getFrom();
			String sourceName, targetName = "";
			SelectClauseList scl = e.getSelect();
			for (int j = 0; j < fcl.size(); j++) {
				ArrayList<Character> attrLists = new ArrayList<Character>();
				List<String> attrs = new ArrayList<String>();
				String key = fcl.getKey(j).toString();
				sourceName = fcl.getValue(j).toString().substring(1);
				targetName = sourceName + "Copy";
				String[] sclArray = scl.toString().split(",");
				for (int k = 0; k < sclArray.length; k++) {
					String attr = sclArray[k];
					attr = attr.replaceFirst("\\" + key + "/", "").trim();
					attrs.add(attr);
					attrLists.add(getAttrLetter(attr));
					String relAttr = sourceName + "." + attr;
					String cKey = scenario.getNextCid();
					String cVal = relAttr + "=" + relAttr;
					corrsList.add(cKey);
					//
					scenario.putCorrespondences(cKey, cVal);
					corrs.add(fac.addCorrespondence(
							sourceName, attr, targetName, attr));
				}
				sourceAttrs.put(sourceName, attrLists);
				targetAttrs.put(targetName, attrLists);

				//
//				rels.add(fac.addRelation(sourceName,
//						attrs.toArray(new String[] {}), true));
//				rels.add(fac.addRelation(targetName,
//						attrs.toArray(new String[] {}), false));
			}
			scenario.putMappings2Correspondences(mKey, corrsList);
			scenario.putMappings2Sources(mKey, sourceAttrs);
			scenario.putMappings2Targets(mKey, targetAttrs);

			ArrayList<String> mList = new ArrayList<String>();
			mList.add(mKey);
			scenario.putTransformation2Mappings(tKey, mList);
			scenario.putTransformationCode(tKey, getQueryString(e, mKey));
			scenario.putTransformationRelName(tKey, targetName);

//			createMapping(rels.get(0), rels.get(1),
//					corrs.toArray(new CorrespondenceType[] {}));

			resetAttrLetters();
		}
	}


	

	private String getQueryString(SPJQuery origQ, String mKey) throws Exception {
		return origQ.toTrampString().replace("${" + 0 + "}", mKey);
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
	private void createSubElements(Element sourceParent, Element targetParent,
			int nestingLevel, int maxNesting, int repetition,
			int numOfElements, int numOfElementsDeviation, SPJQuery lquery) {
		SelectClauseList lselect = lquery.getSelect();
		// decide randomly how many elements to create
		int N = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
						numOfElementsDeviation);
		// number of set elements
		int S = 0;
		if (maxNesting > 0) {
			S = (int) 0.3 * N;
			S = (S == 0) ? 1 : S;
		}
		// number of atomic elements
		int A = N - S;
		A = (A == 0) ? 1 : A;
		if (nestingLevel == 0) {
			A = 0;
			S = 1;
		}
		N = A + S;

		/*
		 * If we are at the nesting level 0 it means that we are right under the
		 * schema elements. In that case we do not create the atomic elements.
		 */
		for (int i = 0; i < A; i++) {
			curRep = repetition;
			String name = randomAttrName(0, i);
			SMarkElement es = new SMarkElement(name, Atomic.STRING, null, 0, 0);
			es.setHook(new String(getStamp() + repetition + "NL" + nestingLevel
					+ "AE" + i));
			SMarkElement et = new SMarkElement(name, Atomic.STRING, null, 0, 0);
			et.setHook(new String(getStamp() + repetition + "NL" + nestingLevel
					+ "AE" + i));
			sourceParent.addSubElement(es);
			targetParent.addSubElement(et);

			int lastRelation = lquery.getFrom().size();
			Variable varLastRelation =
					lquery.getFrom().getExprVar(lastRelation - 1);
			lselect.add(name, new Projection(varLastRelation, name));
		}

		if (nestingLevel <= maxNesting) {
			for (int i = 0; i < S; i++) {
				String name = randomRelName(i);
				SMarkElement es = new SMarkElement(name, new Set(), null, 0, 0);
				es.setHook(new String(getStamp() + repetition + "NL" + nestingLevel
						+ "CE" + i));
				SMarkElement et = new SMarkElement(name + "Copy", new Set(), null, 0, 0);
				et.setHook(new String(getStamp() + repetition + "NL" + nestingLevel
						+ "CE" + i));

				// create the subquery that will be added to the parent query
				SPJQuery query = new SPJQuery();
				Variable var = new Variable("XL" + nestingLevel + "V" + i);
				if (nestingLevel == 0)
					query.getFrom().add(var, new Projection(Path.ROOT, name));
				else {
					int lastRelation = lquery.getFrom().size();
					Variable varLastRelation =
							lquery.getFrom().getExprVar(lastRelation - 1);
					query.getFrom().add(var,
							new Projection(varLastRelation, name));
				}
				lselect.add(name, query);

				createSubElements(es, et, nestingLevel + 1, maxNesting, i,
						numOfElements, numOfElementsDeviation, query);
				sourceParent.addSubElement(es);
				targetParent.addSubElement(et);
			}
		}
	}

	



	@Override
	protected void genMappings() throws Exception {
		RelationType source = m.getSourceRels().get(0);
		RelationType target = m.getTargetRels().get(0);
		
		String[] vars = fac.getFreshVars(0, source.getAttrArray().length);
		MappingType m1 = fac.addMapping(m.getCorrs());
		fac.addForeachAtom(m1.getId(), source.getName(), vars);
		fac.addExistsAtom(m1.getId(), target.getName(), vars);
		
		m.addMapping(m1);	
	}

	@Override
	protected void genCorrespondences () {
		RelationType source = m.getSourceRels().get(0);
		RelationType target = m.getTargetRels().get(0);
		
		for(AttrDefType attr: source.getAttrArray()) {
			CorrespondenceType c = fac.addCorrespondence(source.getName(), 
					attr.getName(), target.getName(), attr.getName());
			m.addCorr(c);
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
		m.addTrans(t);
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
		m.addSourceRel(rel);
	}

	@Override
	protected void genTargetRels() {
		RelationType s = m.getSourceRels().get(0);
		String[] attrs = new String[s.getAttrArray().length];
		String relName = s.getName() + "copy";
		String hook = getRelHook(0);
		
		for(int i = 0; i < s.getAttrArray().length; i++)
			attrs[i] = s.getAttrArray()[i].getName();
		m.addTargetRel(fac.addRelation(hook, relName, attrs, false));
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.COPY;
	}
}
