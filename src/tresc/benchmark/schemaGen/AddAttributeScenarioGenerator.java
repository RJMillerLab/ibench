package tresc.benchmark.schemaGen;

import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SKFunction;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.SkolemKind;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Query;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;

/**
 * Copies the source relation and adds a new attribute (whose value is a skolem function with variable arguments) to the target relation.
 * 
 * @author mdangelo
 */
public class AddAttributeScenarioGenerator extends AbstractScenarioGenerator {

	private static final int MAX_TRIES = 20;
	
	private int numOfSrcTblAttr;
	private SkolemKind sk;

	public AddAttributeScenarioGenerator() {
		;
	}

	@Override
	public void init(Configuration configuration, MappingScenario scenario) {
		super.init(configuration, scenario);
	}

	@Override
	protected void initPartialMapping() {
		super.initPartialMapping();
		numOfSrcTblAttr =
				Utils.getRandomNumberAroundSomething(_generator, numOfElements,
						numOfElementsDeviation);
		
		sk = SkolemKind.values()[typeOfSkolem];
		
		System.out.println("sk: " + sk.ordinal());
	}

	/**
	 * This is the main function. It generates a table in the source, a number
	 * of tables in the target and a respective number of queries.
	 */
	/*private void createSubElements(Schema source, Schema target,
			int numOfSrcTblAttr, int numNewAttr, int typeOfSkolem,
			int repetition, SPJQuery pquery) {

		String coding = getStamp() + repetition;
		int curTbl = repetition;

		// First create the source table
		String sourceRelName = Modules.nameFactory.getARandomName();
		sourceRelName = sourceRelName + "_" + coding;
		SMarkElement srcRel =
				new SMarkElement(sourceRelName, new Set(), null, 0, 0);
		srcRel.setHook(new String(coding));
		source.addSubElement(srcRel);

		// create the target table
		String targetRelName = Modules.nameFactory.getARandomName();
		targetRelName = targetRelName + "_" + coding;
		SMarkElement tgtRel =
				new SMarkElement(targetRelName, new Set(), null, 0, 0);
		tgtRel.setHook(new String(coding));
		target.addSubElement(tgtRel);

		// generate random key name even though it may not be used to avoid
		// variable may not have been initialized errors
		String randomName = Modules.nameFactory.getARandomName();
		String keyName = randomName + "_" + getStamp() + repetition + "KE0";

		if (sk == SkolemKind.KEY) {
			// create key for source table
			Key srcKey = new Key();
			srcKey.addLeftTerm(new Variable("X"), new Projection(Path.ROOT,
					source.getSubElement(curTbl).getLabel()));
			srcKey.setEqualElement(new Variable("X"));

			// create key for target table
			Key tgtKey = new Key();
			tgtKey.addLeftTerm(new Variable("Y"), new Projection(Path.ROOT,
					target.getSubElement(curTbl).getLabel()));
			tgtKey.setEqualElement(new Variable("Y"));

			// create the actual key and add it to the source schema
			SMarkElement es =
					new SMarkElement(keyName, Atomic.STRING, null, 0, 0);
			es.setHook(new String(getStamp() + repetition + "KE0"));
			source.getSubElement(curTbl).addSubElement(es);
			// add the key attribute to the source key
			srcKey.addKeyAttr(new Projection(new Variable("X"), keyName));

			// add the key to the target schema
			SMarkElement et =
					new SMarkElement(keyName, Atomic.STRING, null, 0, 0);
			et.setHook(new String(getStamp() + repetition + "KE0"));
			target.getSubElement(curTbl).addSubElement(et);
			// add the key attribute to the target key
			tgtKey.addKeyAttr(new Projection(new Variable("Y"), keyName));

			// add constraints to the source and target
			source.addConstraint(srcKey);
			target.addConstraint(tgtKey);

			// since we added a key to the table, we add one less free element
			// to the source and target
			numOfSrcTblAttr--;
		}

		// Populate the source with elements. The array attNames, keeps the
		// coding of these elements
		String[] attNames = new String[numOfSrcTblAttr];
		for (int i = 0; i < numOfSrcTblAttr; i++) {
			String namePrefix = Modules.nameFactory.getARandomName();
			coding = getStamp() + repetition + "A" + i;
			String srcAttName = namePrefix + "_" + coding;
			SMarkElement el =
					new SMarkElement(srcAttName, Atomic.STRING, null, 0, 0);
			el.setHook(new String(coding));
			srcRel.addSubElement(el);
			attNames[i] = srcAttName;
		}

		// create the query for the target table
		SPJQuery q = new SPJQuery();
		q.getFrom().add(new Variable("X"),
				new Projection(Path.ROOT, sourceRelName));

		// populate this table with the same element created above for the
		// source
		SelectClauseList sel = q.getSelect();

		// go through all the attributes put in the source table and pop them
		// into the target
		for (int i = 0, imax = attNames.length; i < imax; i++) {
			String tgtAttrName = attNames[i];
			SMarkElement tgtAtomicElt =
					new SMarkElement(tgtAttrName, Atomic.STRING, null, 0, 0);
			String hook = tgtAttrName.substring(tgtAttrName.indexOf("_"));
			tgtAtomicElt.setHook(hook);
			tgtRel.addSubElement(tgtAtomicElt);

			// since we added an attr in the target, we add an entry in the
			// respective select clause
			Projection att = new Projection(new Variable("X"), tgtAttrName);
			sel.add(tgtAttrName, att);
		}

		// now we need to add a fixed number of attributes to the target
		coding = getStamp() + repetition + "NewAtt";

		// by default use all elements in the table as arguments for skolem
		// generation
		int numArgsForSkolem = numOfSrcTblAttr;

		for (int j = 0; j < numNewAttr; j++) {
			String newAttName =
					Modules.nameFactory.getARandomName() + "_" + coding;
			SMarkElement newAttElement =
					new SMarkElement(newAttName, Atomic.STRING, null, 0, 0);
			newAttElement.setHook(new String(coding));

			// here we take the correct table (a subelement which is a set) from
			// the target relation and add a new attribute to it
			target.getSubElement(curTbl).addSubElement(newAttElement);

			// add to the first partial query a skolem function to generate
			// the join attribute in the first target table
			SelectClauseList sel0 = q.getSelect();
			String skolemName = "SK" + String.valueOf(skolemCounter);
			skolemCounter++;
			Function f0 = new Function(skolemName);

			// if we are using a key in the original relation then we base the
			// skolem on just that key
			if (sk == SkolemKind.KEY) {
				Projection att = new Projection(new Variable("X"), keyName);
				f0.addArg(att);
			}

			else {
				// if configuration specifies that we need to randomly decide
				// how many arguments the skolem will take, generate a random
				// number
				if (sk == SkolemKind.RANDOM)
					numArgsForSkolem =
							Utils.getRandomNumberAroundSomething(_generator,
									numOfSrcTblAttr / 2, numOfSrcTblAttr / 2);

				// ensure that we are still within the bounds of the number of
				// source attributes
				if (numArgsForSkolem > numOfSrcTblAttr)
					numArgsForSkolem = numOfSrcTblAttr;

				// add all the source attributes as arguments for the skolem
				// function
				for (int k = 0; k < numArgsForSkolem; k++) {
					Projection att =
							new Projection(new Variable("X"), attNames[k]);
					f0.addArg(att);
				}
			}

			sel0.add(newAttName, f0);
			q.setSelect(sel0);
		}

		// add the partial queries to the parent query
		// to form the whole transformation
		SelectClauseList pselect = pquery.getSelect();
		String tblTrgName = tgtRel.getLabel();
		pselect.add(tblTrgName, q);

		pquery.setSelect(pselect);
	}*/

	// override to adapt the local fields
	/**
	 * Also set the number of source attributes
	 */
	@Override
	protected void chooseSourceRels() throws Exception {
		super.chooseSourceRels();
		// set number of src tbl attributes
		numOfSrcTblAttr = m.getNumRelAttr(0, true);
	}
	
	/**
	 * Repeat picking until a target relation that is big enough has been found.
	 * 
	 * Conditions:
	 * 	1) has at least number of skolems + 1 (free attr) + 1 (key if there).
	 * 		-> needed to create a source with one free attribute
	 *  2) if it has a key then the key shouldn't be one of the attributes reserved
	 *  	for skolems.
	 *  	-> the logic of adding preserves the source key, so the values for this
	 *  	attribute in the target cannot be skolem terms
	 * @throws Exception 
	 */
	@Override
	protected void chooseTargetRels() throws Exception {
		RelationType cand = null;
		int tries = 0;
		int requiredNumAttrs = numNewAttr + 
				((sk == SkolemKind.KEY) ? 1 : 0) + 1;
		int freeAttrs;
		boolean ok = false;
		String relName = null;
		
		while(!ok  && tries++ < MAX_TRIES) {
			cand = getRandomRel(false, requiredNumAttrs);
			// no such cand
			if (cand == null)
				break;
				
			relName = cand.getName();
			freeAttrs = cand.sizeOfAttrArray() - numNewAttr;
			
			if (cand.isSetPrimaryKey()) {
				for(String a: cand.getPrimaryKey().getAttrArray()) {
					int aPos = model.getRelAttrPos(relName, a, false);
					if (aPos >= freeAttrs) {
						ok = false;
						break;
					}
				}
			}	
		} 
		
		// did not find sufficient candidate
		if (!ok)
			genTargetRels();
		// source should have the same attrs as target but no skolems
		else {
			m.addTargetRel(cand);
			
			numOfSrcTblAttr = cand.getAttrArray().length 
					- numNewAttr;
			
			// add primary key if it does not have one already
			if (sk == SkolemKind.KEY && !cand.isSetPrimaryKey())
				fac.addPrimaryKey(relName, m.getAttrId(0, 0, false), false);
		}
	}
	
	@Override
	protected void genSourceRels() throws Exception {
		String srcName = randomRelName(0);
		String[] attrs = new String[numOfSrcTblAttr];
		String keyName = randomAttrName(0, 0) + "KE0";

		for (int i = 0; i < numOfSrcTblAttr; i++) {
			String attrName = randomAttrName(0, i);

			if (sk == SkolemKind.KEY && i == 0)
				attrName = keyName;
			attrs[i] = attrName;
		}

		fac.addRelation(getRelHook(0), srcName, attrs, true);

		if (sk == SkolemKind.KEY)
			fac.addPrimaryKey(srcName, new String[] { keyName }, true);
	}

	@Override
	protected void genTargetRels() throws Exception {
		String trgName = randomRelName(0);
		String[] attrs = new String[numOfSrcTblAttr + numNewAttr];
		String[] srcAttrs = m.getAttrIds(0, true);

		// copy src attrs
		System.arraycopy(srcAttrs, 0, attrs, 0, numOfSrcTblAttr);

		// create random names for the added attrs
		for (int i = numOfSrcTblAttr; i < numOfSrcTblAttr + numNewAttr; i++)
			attrs[i] = randomAttrName(0, i);

		fac.addRelation(getRelHook(0), trgName, attrs, false);

		if (sk == SkolemKind.KEY)
			fac.addPrimaryKey(trgName, new String[] { srcAttrs[0] }, false);
	}

	@Override
	protected void genCorrespondences() {
		for (int i = 0; i < numOfSrcTblAttr; i++)
			addCorr(0, i, 0, i);
	}

	@Override
	protected void genMappings() throws Exception {
		MappingType m1 = fac.addMapping(m.getCorrs());

		// source table get fresh variables
		fac.addForeachAtom(m1, 0, fac.getFreshVars(0, numOfSrcTblAttr));

		switch (mapLang) {
		// target tables gets fresh vars for the new attrs
		case FOtgds:
			fac.addExistsAtom(m1, 0,
					fac.getFreshVars(0, numOfSrcTblAttr + numNewAttr));
			break;
		// target gets all the src variables + skolem terms for the new attrs
		case SOtgds:
			fac.addEmptyExistsAtom(m1, 0);
			fac.addVarsToExistsAtom(m1, 0, fac.getFreshVars(0, numOfSrcTblAttr));
			generateSKs(m1);
			break;
		}
	}

	private void generateSKs(MappingType m1) {
		int numArgsForSkolem = numOfSrcTblAttr;

		// if we are using a key in the original relation then we base the
		// skolem on just that key
		if (sk == SkolemKind.KEY)
			for (int i = 0; i < numNewAttr; i++)
				fac.addSKToExistsAtom(m1, 0, fac.getFreshVars(0, 1));
		else {
			// if configuration specifies that we need to randomly decide how
			// many arguments the skolem will take, generate a random number
			if (sk == SkolemKind.RANDOM)
				numArgsForSkolem = Utils.getRandomNumberAroundSomething(_generator,
								numOfSrcTblAttr / 2, numOfSrcTblAttr / 2);

			// ensure that we are still within the bounds of the number of
			// source attributes
			if (numArgsForSkolem > numOfSrcTblAttr)
				numArgsForSkolem = numOfSrcTblAttr;

			// add all the source attributes as arguments for the skolem
			// function
			for (int i = 0; i < numNewAttr; i++)
				fac.addSKToExistsAtom(m1, 0, fac.getFreshVars(0, numArgsForSkolem));
		}
	}

	@Override
	protected void genTransformations() throws Exception {
		String creates = m.getRelName(0, false);
		Query q;
		
		q = genQueries();
		q.storeCode(q.toTrampString(m.getMapIds()));
		q = addQueryOrUnion(creates, q);
		fac.addTransformation(q.getStoredCode(), m.getMapIds(), creates);
	}
	
	private Query genQueries() throws Exception {
		String sourceRelName = m.getRelName(0, true);
		String[] attNames = m.getAttrIds(0, true);
		String[] tAttrs = m.getAttrIds(0, false);
		MappingType m1 = m.getMaps().get(0);
		
		// create the query for the source? table
		SPJQuery q = new SPJQuery();
		q.getFrom().add(new Variable("X"),
				new Projection(Path.ROOT, sourceRelName));

		SelectClauseList sel = q.getSelect();

		// add all attribute names to the select clause
		for (String a: attNames) {
			Projection att = new Projection(new Variable("X"), a);
			sel.add(a, att);
		}
		
		// retrieve skolems for the new attributes from what was generated in genMappings - this is basically just a way of cloning the existing skolem
		for(int i = 0 ; i < numNewAttr; i++) {
			int attPos = i + numOfSrcTblAttr;
			String attName = tAttrs[attPos];
			SKFunction sk = m.getSkolemFromAtom(m1, false, 0, attPos);
			
			vtools.dataModel.expression.SKFunction stSK = 
					new vtools.dataModel.expression.SKFunction(sk.getSkname());
			
			// this works because the key is always the first attribute 
			for(int j = 0; j < sk.getVarArray().length; j++) {			
				String sAttName = m.getAttrId(0, j, true);
				Projection att = new Projection(new Variable("X"), sAttName);
				stSK.addArg(att);
			}
			
			sel.add(attName, stSK);
			q.setSelect(sel);
		}

		return q;
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.ADDATTRIBUTE;
	}

}
