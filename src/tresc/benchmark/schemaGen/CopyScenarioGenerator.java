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
		
		log.debug("-----BEFORE-----");
		log.debug("Atomic Elements: " + A);
		log.debug("Key Size: " + keySize);
		
		A = (A > 1) ? A : 2;
		keySize = (keySize >= A) ? A - 1 : keySize;
		keySize = (keySize > 0) ? keySize : 1;
		
		log.debug("-----AFTER-----");
		log.debug("Atomic Elements: " + A);
		log.debug("Key Size: " + keySize);
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
	protected void genCorrespondences () {
		RelationType source = m.getSourceRels().get(0);
		RelationType target = m.getTargetRels().get(0);
		
		for(AttrDefType attr: source.getAttrArray()) {
			fac.addCorrespondence(source.getName(), 
					attr.getName(), target.getName(), attr.getName());
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
		
		// generate the appropriate number of keys
		String[] keys = new String[keySize];
		for (int j = 0; j < keySize; j++)
			keys[j] = randomAttrName(0, 0) + "ke" + j;
		
		int keyCount = 0;
		for (int i = 0; i < A; i++) {
			String attrName = randomAttrName(0, i);

			if (keyCount < keySize)
				attrName = keys[keyCount];
			
			keyCount++;
			
			attrs[i] = attrName;
		}
		
		RelationType rel = fac.addRelation(hook, relName, attrs, true);
		fac.addPrimaryKey(rel.getName(), keys, true);
	}

	@Override
	protected void genTargetRels() throws Exception {
		RelationType s = m.getSourceRels().get(0);
		String[] attrs = new String[s.getAttrArray().length];
		String relName = s.getName() + "copy" + curRep;
		String hook = getRelHook(0);
		
		for(int i = 0; i < s.getAttrArray().length; i++)
			attrs[i] = s.getAttrArray()[i].getName();
		fac.addRelation(hook, relName, attrs, false);
		
		String[] keys = new String[keySize];
		for (int j = 0; j < keySize; j++)
			keys[j] = attrs[j];
		
		fac.addPrimaryKey(relName, keys, false);
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.COPY;
	}
}
