package tresc.benchmark.schemaGen;

import org.apache.log4j.Logger;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;

import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Query;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;

public class CopyScenarioGenerator extends AbstractScenarioGenerator {

	private int keySize;
	private int A;
	static Logger log = Logger.getLogger(CopyScenarioGenerator.class);
	
	@Override
	protected void initPartialMapping() {
		super.initPartialMapping();
		A = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
		keySize = Utils.getRandomNumberAroundSomething(_generator, primaryKeySize, primaryKeySizeDeviation);
		
		if (log.isDebugEnabled()) {log.debug("-----BEFORE-----");};
		if (log.isDebugEnabled()) {log.debug("Atomic Elements: " + A);};
		if (log.isDebugEnabled()) {log.debug("Key Size: " + keySize);};
		
		A = (A > 1) ? A : 2;//At least have 2 attr
		keySize = (keySize >= A) ? A - 1 : keySize;//There is at least one attr other than PK 
		// PRG FIX - DO NOT ENFORCE KEY UNLESS EXPLICITLY REQUESTED - Sep 16, 2012
		// keySize = (keySize > 0) ? keySize : 1;
		
		if (log.isDebugEnabled()) {log.debug("-----AFTER-----");};
		if (log.isDebugEnabled()) {log.debug("Atomic Elements: " + A);};
		if (log.isDebugEnabled()) {log.debug("Key Size: " + keySize);};
	}

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
	protected boolean chooseSourceRels() throws Exception {
		RelationType rel = getRandomRel(true);
		if (rel == null)
			return false;
		
		m.addSourceRel(rel);
		A = rel.sizeOfAttrArray();
		keySize = rel.isSetPrimaryKey() ? rel.getPrimaryKey().sizeOfAttrArray() : 0; 
		
		return true;
	}
	
	@Override
	protected boolean chooseTargetRels() throws Exception {
		RelationType rel = getRandomRel(false);
		if (rel == null)
			return false;
		
		m.addTargetRel(rel);
		A = rel.sizeOfAttrArray();
		keySize = rel.isSetPrimaryKey() ? rel.getPrimaryKey().sizeOfAttrArray() : 0; 
		
		return true;
	}
	
	@Override
	protected void genCorrespondences () {
		RelationType source = m.getSourceRels().get(0);
		RelationType target = m.getTargetRels().get(0);
		
		for(int i = 0; i < source.sizeOfAttrArray(); i++) {
			AttrDefType sAttr = source.getAttrArray(i);
			AttrDefType tAttr = target.getAttrArray(i);
		
			fac.addCorrespondence(source.getName(), 
					sAttr.getName(), target.getName(), tAttr.getName());
		}
	}
	
	@Override
	protected void genTransformations () throws Exception {
		String creates = m.getRelName(0, false);
		Query q;
		
		q = genQuery();
		q.storeCode(q.toTrampString(m.getMapIds()));
		q = addQueryOrUnion(creates, q);
		
		fac.addTransformation(q.getStoredCode(), m.getMapIds(), creates);
	}
	
	private Query genQuery () {
		String sRelName = m.getRelName(0, true);
		int numAttrs = m.getNumRelAttr(0, true);
		
		SPJQuery query = new SPJQuery();
		SelectClauseList qselect = query.getSelect();
		Variable var = new Variable(("XL" + 0 + "V" + 0).toLowerCase());
		query.getFrom().add(var, new Projection(Path.ROOT, sRelName));
		
		for(int i = 0; i < numAttrs; i++) {
			String srcA =  m.getAttrId(0, i, true);
			String targetA  = m.getAttrId(0, i, false);
			qselect.add(targetA, new Projection(var, srcA));
		}
		
		return query;
	}
	
	
	@Override
	protected void genSourceRels() throws Exception {
		String[] attrs = new String[A];
		String relName = randomRelName(0);
		String hook = getRelHook(0);
		String[] keys = new String[keySize];
		
		// generate the appropriate number of keys
		for (int i = 0; i < A; i++) {
			String attrName = randomAttrName(0,i);
			if (i < keySize) {
				attrName = attrName + "ke" + i;
				keys[i] = attrName;
			}
			attrs[i] = attrName;
		}
		
		
//		for (int j = 0; j < keySize; j++)
//			keys[j] = randomAttrName(0, 0) + "ke" + j;
//		
//		int keyCount = 0;
//		for (int i = 0; i < A; i++) {
//			String attrName = randomAttrName(0, i);
//
//			if (keyCount < keySize)
//				attrName = keys[keyCount];
//			
//			keyCount++;
//			
//			attrs[i] = attrName;
//		}
		
		RelationType rel = fac.addRelation(hook, relName, attrs, true);
		// PRG FIX - DO NOT ENFORCE KEY UNLESS EXPLICITLY REQUESTED - Sep 16, 2012
		if (keySize > 0)
			fac.addPrimaryKey(rel.getName(), keys, true);
	}

	@Override
	protected void genTargetRels() throws Exception {
		RelationType s = m.getSourceRels().get(0);
		String[] attrs = new String[A];
		String relName = s.getName() + "copy" + curRep + "_" + fac.getNextId("R");
		String hook = getRelHook(0);
		
		for(int i = 0; i < s.getAttrArray().length; i++)
			attrs[i] = s.getAttrArray(i).getName();
		fac.addRelation(hook, relName, attrs, false);
		
		String[] keys = new String[keySize];
		for (int j = 0; j < keySize; j++)
			keys[j] = attrs[j];
		
		// PRG FIX - DO NOT ENFORCE KEY UNLESS EXPLICITLY REQUESTED - Sep 16, 2012
		if (keySize > 0)
			fac.addPrimaryKey(relName, keys, false);
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.COPY;
	}
}
