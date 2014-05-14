package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SKFunction;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.MappingLanguageType;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Constants.SkolemKind;
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
 * 
 */

// PRG FIXED Bogus Generation of Random Skolem Argument Sets (method generateSKs(), SkolemKind.RANDOM )- Sep 18, 2012
// PRG FIXED Infinite Loop Bug in method generateSKs(), case SkolemKind.RANDOM  - Sep 18, 2012
// PRG Systematically using "Random Without Replacement" strategy/algorithm when dealing with SkolemKind.RANDOM mode everywhere! - Sep 21, 2012

// BORIS TO DO - Revise method genQueries() as it might be out of sync now - Sep 21, 2012

// MN ENHANCED genTargetRel to pass types of attributes as argument to addRelation (genTargetRel) - 3 May 2014

// MN ENHANCED genSourceRel to pass types of attributes as argument to addRelation (genSourceRel) (added new attribute targetReuse) - 11 May 2014

public class AddAttributeScenarioGenerator extends AbstractScenarioGenerator {

	private static final int MAX_TRIES = 20;
	
	private int numOfSrcTblAttr;
	private int numAddAttr;
	private int keySize;
	private SkolemKind sk;
	//MN ADDED boolean attribute; if it is true, it means the instance of mapping primitive is reusing
	//target relation - 11 May 2014
	//MN BEGIN
	private boolean targetReuse;
	//MN END
	
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
		numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
		numAddAttr = Utils.getRandomNumberAroundSomething(_generator, numNewAttr, numNewAttrDeviation);
		keySize = Utils.getRandomNumberAroundSomething(_generator, primaryKeySize, primaryKeySizeDeviation);
		
		numAddAttr = (numAddAttr > 0) ? numAddAttr : 1;
		numOfSrcTblAttr = (numOfSrcTblAttr > 1) ? numOfSrcTblAttr : 2;
		keySize = (keySize >= numOfSrcTblAttr) ? numOfSrcTblAttr - 1 : keySize;
		// PRG FIX - DO NOT ENFORCE KEY UNLESS EXPLICITLY REQUESTED - Sep 16, 2012
		// keySize = (keySize > 0) ? keySize : 1;
		
		sk = SkolemKind.values()[typeOfSkolem];
		
		// PRG FIX - DO NOT ENFORCE KEY UNLESS EXPLICITLY REQUESTED - Sep 16, 2012
		// PRG Added the following code to always force key generation when SkolemKind.KEY 
		if (sk == SkolemKind.KEY)
			keySize = (keySize > 0) ? keySize : 1;
		
		//MN BEGIN - 11 May 2014
		targetReuse = false;
		//MN END
	}

	// override to adapt the local fields
	/**
	 * Also set the number of source attributes
	 */
	@Override
	protected boolean chooseSourceRels() throws Exception {
		RelationType rel;
		super.chooseSourceRels();
		
		rel = m.getSourceRels().get(0);
		//MN we can consider Max_Num_Tries to add more flexibility - 11 May 2014
		//MN To do: we need to relax minimum required number of attributes - 11 May 2014
		if (rel == null)
			return false;
		
		// set number of src tbl attributes
		numOfSrcTblAttr = rel.sizeOfAttrArray();
		
		if (keySize > 0 && !rel.isSetPrimaryKey()) {
			keySize = keySize > numOfSrcTblAttr ? numOfSrcTblAttr : keySize;
			fac.addPrimaryKey(rel.getName(), 
					CollectionUtils.createSequence(0, keySize), true);
		}
		else if (rel.isSetPrimaryKey())
			keySize = rel.getPrimaryKey().sizeOfAttrArray();
		
		return true;
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
	protected boolean chooseTargetRels() throws Exception {
		RelationType cand = null;
		int tries = 0;
		int requiredNumAttrs = numAddAttr + 
				((sk == SkolemKind.KEY) ? 1 : 0) + 1;
		int freeAttrs;
		boolean ok = false;
		String relName = null;
		
		while(!ok  && tries++ < MAX_TRIES) {
			cand = getRandomRel(false, requiredNumAttrs);
			// no such cand
			if (cand == null)
				return false;
				
			relName = cand.getName();
			freeAttrs = cand.sizeOfAttrArray() - numAddAttr;
			
			if (cand.isSetPrimaryKey()) {
				for(String a: cand.getPrimaryKey().getAttrArray()) {
					int aPos = model.getRelAttrPos(relName, a, false);
					if (aPos >= freeAttrs) {
						ok = false;
						break;
					}
				}
			}
			
			ok = true;
		} 
		
		// did not find sufficient candidate
		if (!ok)
			return false;
		// source should have the same attrs as target but no skolems
		else {
			
			//MN BEGIN - 11 May 2014
			targetReuse = true;
			//MN END
			
			m.addTargetRel(cand);
			
			numOfSrcTblAttr = cand.sizeOfAttrArray() 
					- numAddAttr;
			
			// add primary key if it does not have one already
			if (sk == SkolemKind.KEY && !cand.isSetPrimaryKey())
				fac.addPrimaryKey(relName, m.getAttrId(0, 0, false), false);
			else if (cand.isSetPrimaryKey()) {
				keySize = cand.getPrimaryKey().sizeOfAttrArray();
				numOfSrcTblAttr = numOfSrcTblAttr >= (keySize + 1) ? 
						numOfSrcTblAttr : keySize + 1;
				numAddAttr = cand.sizeOfAttrArray() - numOfSrcTblAttr;
			}
		}
		
		return true;
	}
	
	@Override
	protected void genSourceRels() throws Exception {
		String srcName = randomRelName(0);
		String[] attrs = new String[numOfSrcTblAttr];
		//MN BEGIN - considerd an array to store types of attributes of source relation - 11 May 2014
		String[] attrsType = new String[numOfSrcTblAttr];
		//MN END
		
		// generate the appropriate number of keys
		String[] keys = new String[keySize];
		for (int j = 0; j < keySize; j++){
			keys[j] = randomAttrName(0, 0) + "ke" + j;
		}

		int keyCount = 0;
		for (int i = 0; i < numOfSrcTblAttr; i++) {
			String attrName = randomAttrName(0, i);

			// PRG FIX - DO NOT ENFORCE KEY UNLESS EXPLICITLY REQUESTED - Sep 16, 2012
			// if (sk == SkolemKind.KEY && keyCount < keySize)
			if ((keySize > 0 || sk == SkolemKind.KEY) && keyCount < keySize)
				attrName = keys[keyCount];
			
			keyCount++;
			
			attrs[i] = attrName;
			
			//MN BEGIN - 11 May 2014
			if(targetReuse)
				attrsType[i] = m.getTargetRels().get(0).getAttrArray(i).getDataType();
			//MN END
		}

		//MN BEGIN - 11 May 2014
		if(!targetReuse)
			fac.addRelation(getRelHook(0), srcName, attrs, true);
		else
			fac.addRelation(getRelHook(0), srcName, attrs, attrsType, true);
		//MN END

		// PRG FIX - DO NOT ENFORCE KEY UNLESS EXPLICITLY REQUESTED - Sep 16, 2012
		// if (sk == SkolemKind.KEY)
		if (keySize > 0 || sk == SkolemKind.KEY)
			fac.addPrimaryKey(srcName, keys, true);
		
		//MN BEGIN - 11 May 2014
		targetReuse = false;
		//MN END
	}

	@Override
	protected void genTargetRels() throws Exception {
		String trgName = randomRelName(0);
		String[] attrs = new String[numOfSrcTblAttr + numAddAttr];
		String[] srcAttrs = m.getAttrIds(0, true);
		//MN considered an array to store types of attributes of target relation - 3 May 2014
		//MN BEGIN
		List<String> attrsType = new ArrayList<String> ();
		//MN END
		
		// copy src attrs
		System.arraycopy(srcAttrs, 0, attrs, 0, numOfSrcTblAttr);

		//MN BEGIN - 4 May 2014
		for(int i=0; i<numOfSrcTblAttr; i++)
			attrsType.add(m.getSourceRels().get(0).getAttrArray(i).getDataType());
		//MN END
		
		// create random names for the added attrs
		for (int i = numOfSrcTblAttr; i < numOfSrcTblAttr + numAddAttr; i++){
			attrs[i] = randomAttrName(0, i);
			//MN BEGIN - 8 May 2014
			attrsType.add("TEXT");
			//MN END
		}

		//MN modified the following line - 4 May 2014
		fac.addRelation(getRelHook(0), trgName, attrs, attrsType.toArray(new String[] {}), false);
	
		String[] keys = new String[keySize];
		for (int j = 0; j < keySize; j++)
			keys[j] = srcAttrs[j];
		
		// PRG FIX - DO NOT ENFORCE KEY UNLESS EXPLICITLY REQUESTED - Sep 16, 2012
		// if (sk == SkolemKind.KEY)
		if (keySize > 0 || sk == SkolemKind.KEY)
			fac.addPrimaryKey(trgName, keys, false);
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
					fac.getFreshVars(0, numOfSrcTblAttr + numAddAttr));
			break;
		// target gets all the src variables + skolem terms for the new attrs
		case SOtgds:
			fac.addEmptyExistsAtom(m1, 0);
			fac.addVarsToExistsAtom(m1, 0, fac.getFreshVars(0, numOfSrcTblAttr));
			SkolemKind sk1 = sk;
			if(sk == SkolemKind.VARIABLE)
				sk1 = SkolemKind.values()[_generator.nextInt(4)];
			generateSKs(m1, sk1);
			break;
		}
	}
   
	// PRG Rewrote method generateSKs() to fixed Bogus Generation of Random Skolem Argument Sets - Sep 18, 2012

	/* mdangelo's Version prior Sep 18, 2012
	private void generateSKs(MappingType m1, SkolemKind sk) 
	{
		int numArgsForSkolem = numOfSrcTblAttr;

		// if we are using a key in the original relation then we base the
		// skolem on just that key
		if (sk == SkolemKind.KEY)
			for (int i = 0; i < numAddAttr; i++)
				fac.addSKToExistsAtom(m1, 0, fac.getFreshVars(0, keySize));
		else {
			// if configuration specifies that we need to randomly decide how
			// many arguments the skolem will take, generate a random number
			// generates the same random skolemization for each new attribute that we've added
			// if we want to force different skolemizations then move the random number generation into the loop
			if (sk == SkolemKind.RANDOM)
				numArgsForSkolem = Utils.getRandomNumberAroundSomething(_generator,
								numOfSrcTblAttr / 2, numOfSrcTblAttr / 2);

			// ensure that we are still within the bounds of the number of
			// source attributes
			if (numArgsForSkolem > numOfSrcTblAttr)
				numArgsForSkolem = numOfSrcTblAttr;

			// add all the source attributes as arguments for the skolem
			// function
			for (int i = 0; i < numAddAttr; i++)
				fac.addSKToExistsAtom(m1, 0, fac.getFreshVars(0, numArgsForSkolem));
		}
	} */
 
	// PRG Newly implemented method generateSKs() - Sep 18, 2012
	// NOTE: we purposely generate a different random argument set for each new attribute when sk == SkolemKind.RANDOM
	// PRG Systematically using "Random Without Replacement" strategy/algorithm when dealing with SkolemKind.RANDOM mode everywhere! - Sep 21, 2012
	
	private void generateSKs(MappingType m1, SkolemKind sk) 
	{
		int numArgsForSkolem = numOfSrcTblAttr;
		
		if (log.isDebugEnabled()) {log.debug("ADD - Method generateSKs() with totalVars = " + numOfSrcTblAttr + " and Num of New Skolems = " + numAddAttr);};

		for (int j = 0; j < numAddAttr; j++) {
			
			// if we are using a key in the original relation then we base the
			// skolem on just that key
			if (sk == SkolemKind.KEY)

				fac.addSKToExistsAtom(m1, 0, fac.getFreshVars(0, keySize));

			else if (sk == SkolemKind.RANDOM) {

				// Generate a random number of args for this Skolem (Uniform distribution between 0 (inclusive) and numOfSrcTblAttr (exclusive))
				numArgsForSkolem = Utils.getRandomUniformNumber(_generator, numOfSrcTblAttr);
				// Ensure we generate at least a random argument set of size > 0
				numArgsForSkolem = (numArgsForSkolem == 0 ? numOfSrcTblAttr : numArgsForSkolem);

				// Generate a random argument set
				// Vector<String> randomArgs = getRandomSourceVars(numArgsForSkolem, m1);
				Vector<String> randomArgs = Utils.getRandomWithoutReplacementSequence(_generator, numArgsForSkolem, model.getAllVarsInMapping(m1, true));
				
				fac.addSKToExistsAtom(m1, 0, Utils.convertVectorToStringArray(randomArgs));
					
			} else { // SkolemKind.ALL

				fac.addSKToExistsAtom(m1, 0, fac.getFreshVars(0, numArgsForSkolem));
			}
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
		for(int i = 0 ; i < numAddAttr; i++) {
			int attPos = i + numOfSrcTblAttr;
			String attName = tAttrs[attPos];
			int numArgs = 0;
			String skName;
			
			if (mapLang.equals(MappingLanguageType.SOtgds)) {
				SKFunction sk = m.getSkolemFromAtom(m1, false, 0, attPos);
				numArgs = sk.sizeOfVarArray();
				skName = sk.getSkname();
			}	
			else {
				if (sk == SkolemKind.KEY)
					numArgs = keySize;
				else {
					numArgs = numOfSrcTblAttr;
					// if configuration specifies that we need to randomly decide how
					// many arguments the skolem will take, generate a random number
					// generates the same random skolemization for each new attribute that we've added
					// if we want to force different skolemizations then move the random number generation into the loop
					if (sk == SkolemKind.RANDOM)
						numArgs = Utils.getRandomNumberAroundSomething(_generator,
										numOfSrcTblAttr / 2, numOfSrcTblAttr / 2 - 1);

					// ensure that we are still within the bounds of the number of
					// source attributes
					if (numArgs > numOfSrcTblAttr)
						numArgs = numOfSrcTblAttr;
				}
				skName = fac.getNextId("SK");
			}
			
			vtools.dataModel.expression.SKFunction stSK = 
					new vtools.dataModel.expression.SKFunction(skName);
			
			// this works because the keys are always the first attributes 
			for(int j = 0; j < numArgs; j++) {			
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
