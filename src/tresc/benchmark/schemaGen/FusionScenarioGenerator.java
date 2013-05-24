package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.MappingLanguageType;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.ConstantAtomicValue;
import vtools.dataModel.expression.Function;
import vtools.dataModel.expression.Key;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Query;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Union;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.values.AtomicValue;

// PRG ADDED LIMIT on number of generated fragments to avoid exponential blowup - Oct 6, 2012
// Note: By design, a Fusion scenario generate 2^N - 1 mappings. For N=4 for example, this means generating 15 mappings per repetition.

public class FusionScenarioGenerator extends AbstractScenarioGenerator {

	private static int NUM_TRIES = 20;

	private int depth;
	private int N; // #fragments (source relations)
	private int K; // #Key attributes
	private int S; //
	private int E; // # attributes
	private int F; // #free attributes in source relations
	private String[][] freeAttrs;
	private String[][] keyAttrs;
	private int[][] keyAttrPos;
	private int targetExistsNum = 0;

	public FusionScenarioGenerator() {
		;
	}

	@Override
	public void init(Configuration configuration,
			MappingScenario mappingScenario) {
		super.init(configuration, mappingScenario);
		depth =
				Utils.getRandomNumberAroundSomething(_generator, nesting,
						nestingDeviation);
	}

	@Override
	protected void initPartialMapping() {
		super.initPartialMapping();
		N = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements, numOfSetElementsDeviation);
		N = (N < 2) ? 2 : N;
		// PRG ADDED LIMIT on number of generated fragments to avoid exponential blowup - Oct 6, 2012
		// Note: By design, a Fusion scenario generate 2^N - 1 mappings. Thus we intend to generate a max of 15 mappings per scenario repetition. 
		N = (N > 4) ? 4 : N;
		K = Utils.getRandomNumberAroundSomething(_generator, keyWidth, keyWidthDeviation);
		K = (K < 1) ? 1 : K;
		S =	Utils.getRandomNumberAroundSomething(_generator, numOfSetElements, numOfSetElementsDeviation);
		S = (S < 2) ? 2 : S;
		// if the nesting depth is 0 then the nr of subsets is 0
		if (depth == 0)
			S = 0;
		E = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
		E = (E <= (K + S)) ? (K + S)+1 : E;
		F = E - K - S;
	}

	/**
	 * Have to choose N source relations with the same number of attributes and
	 * either no key or the key at the same position. We choose one relation if
	 * it does not have a key we find another one with the same number of
	 * attributes. Until we have one with a key or done. Add keys for the
	 * relations without key and then continue to find ones with the same key
	 * position. If we do not within NUM_TRIES draws of relations from the
	 * source schema we create the remaining ones from scratch
	 * 
	 * @throws Exception
	 */
	@Override
	protected boolean chooseSourceRels() throws Exception {
		List<RelationType> rels = new ArrayList<RelationType>();
		RelationType cand;
		int tries = 0;
		boolean foundKey = false;
		int numAttrs, numKeyAttr = -1;
		keyAttrPos = new int[N][K];
		int[] templateKeyPos = null;

		// first one
		cand = getRandomRel(true);
		rels.add(cand);
		numAttrs = cand.sizeOfAttrArray();
		foundKey = cand.isSetPrimaryKey();
		if (foundKey) {
			keyAttrPos[0] = model.getPKPos(cand.getName(), true);
			numKeyAttr = keyAttrPos[0].length;
			templateKeyPos = keyAttrPos[0];
			foundKey = true;
		}

		// as long as there is no key and we still find rels with the same size
		// -> add them
		while (tries < NUM_TRIES * N && cand != null && rels.size() < N) {
			cand = getRandomRelWithNumAttr(true, numAttrs, rels);
			if (cand != null) {
				if (foundKey && cand.isSetPrimaryKey())
					break;

				rels.add(cand);
				if (cand.isSetPrimaryKey()) {
					int i = rels.size() - 1;
					foundKey = true;
					keyAttrPos[i] = model.getPKPos(cand.getName(), true);
					numKeyAttr = keyAttrPos[i].length;
					templateKeyPos = keyAttrPos[i];
				}
			}
		}

		// we have a key or no more rels, add rels with the same key length or no key
		while (tries < NUM_TRIES * N && cand != null && rels.size() < N) {
			cand = getRandomRelWithNumAttr(true, numAttrs, rels);
			if (cand != null) {
				if (cand.isSetPrimaryKey()) {
					// has PK, but of wrong length
					if (cand.getPrimaryKey().sizeOfAttrArray() != numKeyAttr)
						break;
					// has ok PK add to result
					else {
						rels.add(cand);
						int i = rels.size() - 1;
						foundKey = true;
						keyAttrPos[i] = model.getPKPos(cand.getName(), true);
						numKeyAttr = keyAttrPos[i].length;
						templateKeyPos = keyAttrPos[i];	
					}
				}
				else
					rels.add(cand);
			}
		}

		// generate the remaining ones
		for (int i = 0; i < rels.size(); i++)
			m.addSourceRel(rels.get(i));
		for (int i = rels.size(); i < N; i++) {
			RelationType r = createFreeRandomRel(i, numAttrs);
			fac.addRelation(getRelHook(i), r, true);
		}
		
		// create template key if necessary
		if (!foundKey) {
			templateKeyPos = new int[K];
			for (int i = 0; i < K; i++)
				templateKeyPos[i] = i;
		}

		// create missing keys and record names and positions
		for (int i = 0; i < rels.size(); i++) {
			RelationType r = rels.get(i);
			if (!r.isSetPrimaryKey())
				fac.addPrimaryKey(r.getName(), templateKeyPos, true);
			keyAttrPos[i] = model.getPKPos(r.getName(), true);
			keyAttrs[i] = model.getAttrNames(r.getName(), keyAttrPos[i], true);
		}

		// create and adapt the data structures required by the remainder of
		// the generation algorithm
		K = keyAttrPos[0].length;
		E = numAttrs;
		F = (E - K);
		freeAttrs = new String[N][F];
		for(int i = 0; i < N; i++) {
			for(int j = 0; j < F; j++)
				freeAttrs[i][j] = m.getAttrId(i, getFreeAttrPos(i, j), true);
		}
		
		return true;
	}

	private int getFreeAttrPos(int i, int j) {
		int attPos = 0, keyPos = 0;
		for(int pos = 0; pos <= j; pos++, attPos++) {
			while(keyPos < K && attPos == keyAttrPos[i][keyPos]) {
				keyPos++;
				attPos++;
			}
		}
		return --attPos;
	}

	@Override
	protected boolean chooseTargetRels() throws Exception {
		int minAttrs = N + K;
		RelationType cand = getRandomRel(false, minAttrs);
		int tries = 0;
		
		while(cand != null && tries < NUM_TRIES) {
			if (!cand.isSetPrimaryKey()) {
				break;
			}
			else {
				int numK =  cand.getPrimaryKey().sizeOfAttrArray();
			
				// has key as first attributes 
				//TODO change other code to be more flexible on that
				if (Arrays.equals(model.getPKPos(cand.getName(), false), 
						CollectionUtils.createSequence(0, numK))) {
					break;
				}
			}
			
			cand = getRandomRel(false, minAttrs);
		}
		
		// found ok one
		if (cand != null) {
			m.addTargetRel(cand);
			if (!cand.isSetPrimaryKey())
				fac.addPrimaryKey(cand.getName(), 
						CollectionUtils.createSequence(0, K), false);
			else
				K = cand.getPrimaryKey().sizeOfAttrArray();
			E = cand.sizeOfAttrArray();
			F = (E - K) / N;
			targetExistsNum  = m.getNumRelAttr(0, false) - K - (F * N);
			return true;
		}
		// create new one
		return false;
	}
	
	@Override
	protected void genSourceRels() throws Exception {
		String[] fragNames = new String[N];
		keyAttrs = new String[N][K];
		keyAttrPos = new int[N][K];
		Key[] keyS = new Key[N];
		String[] attrs = new String[K + F];

		// generate names for the keyAttrs
		for (int i = 0; i < K; i++) {
			keyAttrs[0][i] = randomAttrName(0, i) + ("_" + getStamp() + curRep + "ke" + i).toLowerCase();
			keyAttrPos[0][i] = i;
			for (int j = 1; j < N; j++) {
				keyAttrs[j][i] = keyAttrs[0][i];
				keyAttrPos[j][i] = i;
			}
		}

		freeAttrs = new String[N][F];
		for (int frag = 0; frag < N; frag++) {
			// gen name
			String elName = randomRelName(frag);

			fragNames[frag] = elName;
			String hook = getRelHook(frag);

			// gen attributes Key + free ones
			System.arraycopy(keyAttrs[frag], 0, attrs, 0, keyAttrs[frag].length);

			for (int i = K; i < F + K; i++) {
				String freeName = randomAttrName(frag, i);
				attrs[i] = freeName;
				freeAttrs[frag][i - K] = freeName;
			}

			// create relation
			fac.addRelation(hook, elName, attrs, true);

			// add key
			keyS[frag] = fac.addPrimaryKey(elName, keyAttrs[frag], true);
		}
	}

	
	
	@Override
	protected void genTargetRels() throws Exception {
		String[] attrs = new String[K + (F * N)];
		String relName = randomRelName(0) + "T";
		String hook = getRelHook(0);

		// create attrs
		for (int i = 0; i < K; i++) {
			attrs[i] = keyAttrs[0][i];
		}
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < F; j++)
				attrs[(i * F) + j + K] = freeAttrs[i][j];
		}

		fac.addRelation(hook, relName, attrs, false);
		fac.addPrimaryKey(relName, keyAttrs[0], false);
	}
//
//	@Override
//	protected void genMappings() throws Exception {
//		String[] keyVars = fac.getFreshVars(0, K);
//		String[] attrVars = fac.getFreshVars(K, F);
//		String[] exists = fac.getFreshVars(E, (N - 1) * F);
//		String[] freeTVars = fac.getFreshVars(N * F + K, targetExistsNum);
//		String tRelName= m.getRelName(0, false);
//		
//		/*
//		 * FO-tgds:
//		 * The vars are fixed, just need to put them at the correct positions:
//		 * Source: R( A_1, A_2, A_3, K_1, K_2) S(B_1, B_2, B_3, K_1, K_2)
//		 * Target: T(K_1, K_2, A_1, A_2, A_3, B_1, B_2, B_3) 
//		 * -> M1: R(c,d,e,a,b) -> T(a,b,c,d,e,f,g,h)
//		 * 	  M1: S(c,d,e,a,b) -> T(a,b,f,g,h,c,d,e)
//		 */
//		if (mapLang.equals(MappingLanguageType.FOtgds)) {
//			for (int i = 0; i < N; i++) {
//				String[] vars, tVars;
//	
//				MappingType m1 = fac.addMapping(m.getCorrs(i, true));
//	
//				// create foreach
//				vars = CollectionUtils.insertAtPositions(attrVars, keyVars,
//								keyAttrPos[i]);
//				fac.addForeachAtom(m1.getId(), m.getRelName(i, true), vars);
//	
//				// create exists
//				tVars = CollectionUtils.insertAtPositions(exists, attrVars,
//								CollectionUtils.createSequence(F * i, F));
//				tVars = CollectionUtils.concatArrays(keyVars, tVars, freeTVars);
//				fac.addExistsAtom(m1.getId(), tRelName, tVars);
//			}
//		}
//		/*
//		 * SO-tgds:
//		 * Use skolem function for each attribute. Input is the key
//		 */
//		else {
//			String[] skIds = new String[N * F];
//			for(int i = 0; i < skIds.length; i++)
//				skIds[i] = fac.getNextId("SK");
//			
//			for (int i = 0; i < N; i++) {
//				String[] vars, tVars;
//				MappingType m1 = fac.addMapping(m.getCorrs(i, true));
//				
//				// create foreach
//				vars = CollectionUtils.insertAtPositions(attrVars, keyVars,
//								keyAttrPos[i]);
//				fac.addForeachAtom(m1.getId(), m.getRelName(i, true), vars);
//	
//				// create exists
//				fac.addEmptyExistsAtom(m1, 0);
//				// add key Vars
//				fac.addVarsToExistsAtom(m1, 0, keyVars);
//				// create additional SKs or Vars
//				for(int j = 0; j < N * F; j++) {
//					// adding the attributes from the source relation
//					if (j >= i * F && j < (i + 1) * F)
//						fac.addVarToExistsAtom(m1, 0, attrVars[j % F]);
//					// adding a skolem term for an existential
//					else
//						fac.addSKToExistsAtom(m1, 0, keyVars, skIds[j]);
//				}
//				for (int j = 0; j < targetExistsNum; i++)
//					fac.addVarToExistsAtom(m1, 0, freeTVars[i]);
//			}
//		}
//	}
//	
	
	/**
	 *  Create all mappings 2^N - 1 to model the different combinations of tuples with a certain
	 *  key being (not) present in the source relations.
	 * @throws Exception 
	 */ 
	@Override
	protected void genMappings() throws Exception {
		boolean[] activeRels = new boolean[N]; // which relations are in a specific mapping
		Arrays.fill(activeRels, false);
		activeRels[activeRels.length - 1] = true;
		boolean[] allFalse = new boolean[N];
		Arrays.fill(allFalse, false);
		
		while(!Arrays.equals(activeRels,allFalse)) {
			if (mapLang.equals(MappingLanguageType.FOtgds))
				genOneMappingFO(activeRels);	
			else
				genOneMappingSO(activeRels);
			
			increase(activeRels);
		}
	}


	/**
	 * Create one mapping for a given list of source relations (boolean array)
	 * @param sourceRels
	 * @throws Exception
	 */
	private void genOneMappingFO (boolean[] sourceRels) throws Exception {
		String[] keyVars = fac.getFreshVars(0, K);
		String[] freeVars = fac.getFreshVars(K, N * F);
		String[] freeTVars = fac.getFreshVars(N * F + K, targetExistsNum);
		String[] vars;
		String tRelName= m.getRelName(0, false);
		
		// create mapping
		List<CorrespondenceType> cs = new ArrayList<CorrespondenceType> ();
		for(int i = 0; i < sourceRels.length; i++)
			if (sourceRels[i])
				cs.addAll(m.getCorrs(i, true));
		
		MappingType m1 = fac.addMapping(cs);
		
		// create mapping
		for(int i = 0; i < sourceRels.length; i++) {
			// create foreach atom for ith relation
			if (sourceRels[i]) {
				vars = CollectionUtils.insertAtPositions(
						Arrays.copyOfRange(freeVars, i * F, (i + 1) * F), 
						keyVars,
						keyAttrPos[i]);
				fac.addForeachAtom(m1.getId(), m.getRelName(i, true), vars);	
			}
		}
		
		// create exists
		fac.addExistsAtom(m1.getId(), tRelName, 
				CollectionUtils.concatArrays(keyVars, freeVars, freeTVars));
	}
	
	/**
	 * Create one mapping for a given list of source relations (boolean array)
	 * @param sourceRels
	 * @throws Exception
	 */
	private void genOneMappingSO (boolean[] sourceRels) throws Exception {
		String[] keyVars = fac.getFreshVars(0, K);
		String[] freeVars = fac.getFreshVars(K, N * F);
		String[] freeTVars = fac.getFreshVars(N * F + K, targetExistsNum);
		String[] vars;
		
		// create mapping
		List<CorrespondenceType> cs = new ArrayList<CorrespondenceType> ();
		for(int i = 0; i < sourceRels.length; i++)
			if (sourceRels[i])
				cs.addAll(m.getCorrs(i, true));
		
		MappingType m1 = fac.addMapping(cs);
		
		// create mapping
		for(int i = 0; i < sourceRels.length; i++) {
			// create foreach atom for ith relation
			if (sourceRels[i]) {
				vars = CollectionUtils.insertAtPositions(
						Arrays.copyOfRange(freeVars, i * F, (i + 1) * F), 
						keyVars,
						keyAttrPos[i]);
				fac.addForeachAtom(m1.getId(), m.getRelName(i, true), vars);	
			}
		}
		
		// create exists
		String[] skIds = new String[N * F];
		for(int i = 0; i < skIds.length; i++)
			skIds[i] = fac.getNextId("SK");
		fac.addEmptyExistsAtom(m1, 0);
		
		// add key Vars
		fac.addVarsToExistsAtom(m1, 0, keyVars);
		
		// create additional SKs or Vars
		for(int i = 0; i < N; i++) {
			for(int j = 0; j < F; j++) {
				int offset = i * F + j;
				if (sourceRels[i])
					fac.addVarToExistsAtom(m1, 0, freeVars[offset]);
				else
					fac.addSKToExistsAtom(m1, 0, keyVars, skIds[offset]);
			}
		}
		
		// create new
		for(int i = 0; i < freeTVars.length; i++)
			fac.addVarToExistsAtom(m1, 0, freeTVars[i]);
	}
	
	private boolean[] increase (boolean[] in) {
		for(int i = in.length - 1; i >= 0; i--) {
			if (!in[i]) {
				in[i] = true;
				return in;
			}
			else
				in[i] = false;
		}
		return in;
	}

	@Override
	protected void genTransformations() throws Exception {
		String creates = m.getRelName(0, false);
		boolean[] activeRels = new boolean[N];
		Arrays.fill(activeRels, false);
		activeRels[activeRels.length - 1] = true;
		boolean[] allFalse = new boolean[N];
		Arrays.fill(allFalse, false);
		String[] mappingLists = new String[N];
		List<String>[] mapIds = new List[N];
		int mapPos = 0;
		Query q;
		
		for(int i = 0; i < N; i++)
			mapIds[i] = new ArrayList<String> ();

		while(!Arrays.equals(activeRels,allFalse)) {
			String mapId = m.getMapIds()[mapPos++];
			
			for(int i = 0; i < activeRels.length; i++) {
				if (activeRels[i])
					mapIds[i].add(mapId);
			}
			
			increase(activeRels);
		}
		
		for(int i = 0; i < N; i++) {
			mappingLists[i] = mapIds[i].get(0);
			for(int j = 1; j < mapIds[i].size(); j++)
				mappingLists[i] += "," + mapIds[i].get(j);
		}
		
		q = genQuery();
		q.storeCode(q.toTrampString(mappingLists));
		q = addQueryOrUnion(creates, q);

		fac.addTransformation(q.getStoredCode(), m.getMapIds(), creates);
	}

	private SPJQuery genQuery() {
		// ******* the first part of the query that represents the
		// transformation ********
		// create the Fquery that is the union of all the attributes (keys and
		// non-keys)
		// from each fragment of the source
		SPJQuery[] Uquery = new SPJQuery[N];
		for (int k = 0; k < N; k++) {
			Uquery[k] = new SPJQuery();
			// add each fragment to the from clause
			Uquery[k].getFrom().add(
					new Variable("F" + k),
					new Projection(Path.ROOT, m.getSourceRels().get(k)
							.getName()));
			// create the select clause
			SelectClauseList select = Uquery[k].getSelect();
			Variable var = new Variable("F" + k);
			Projection att;
			// add the key attributes to the select clause
			for (int i = 0; i < K; i++) {
				att = new Projection(var.clone(), keyAttrs[k][i]);
				select.add(keyAttrs[0][i], att);
			}
			// add all the free attributes from all the fragments to the select
			// clause.
			// all the free elements form all the fragments will be added it to
			// the
			// select clause.
			// given a fragment, use NULL for each missing global free element
			for (int i = 0; i < N; i++)
				if (i == k) {
					for (int j = 0; j < F; j++) {
						att = new Projection(var.clone(), freeAttrs[k][j]);
						select.add(freeAttrs[k][j], att);
					}
				}
				else {
					for (int j = 0; j < F; j++) {
						select.add(freeAttrs[i][j], new ConstantAtomicValue(
								AtomicValue.NULL));
					}
				}
			Uquery[k].setSelect(select);
		}
		Union Fquery = new Union();
		for (int k = 0; k < N; k++) {
			Fquery.add(Uquery[k]);
		}
		// *************** the final query is a group-by query
		// ********************
		// add the subquery representing the union of the fragments at the final
		// query
		SPJQuery query = new SPJQuery();
		// add the Fquery to the from clause
		query.getFrom().add(new Variable("FG"), Fquery);
		// create the group-by clause that includes all the keys of the Fquery
		Variable var = new Variable("FG");
		for (int i = 0; i < K; i++) {
			Projection term = new Projection(var.clone(), keyAttrs[0][i]);
			query.addGroupByTerm(term);
		}
		// add the attributes of Fquery to the select clause of the final query
		SelectClauseList select = query.getSelect();
		Projection att;
		// add the key attributes of Fquery to the select clause
		for (int i = 0; i < K; i++) {
			att = new Projection(var.clone(), keyAttrs[0][i]);
			select.add(keyAttrs[0][i], att);
		}
		// add the non-key attributes of Fquery as MAX(attribute)
		for (int i = 0; i < N; i++)
			for (int j = 0; j < F; j++) {
				att = new Projection(var.clone(), freeAttrs[i][j]);
				Function fmax = new Function("MAX");
				fmax.addArg(att);
				select.add(freeAttrs[i][j], fmax);
			}

		query.setSelect(select);
		// the final query will be returned to the main method
		return query;
	}

	@Override
	protected void genCorrespondences() {		
		// for keys
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < K; j++)
				addCorr(i, keyAttrPos[i][j], 0, j);
		}

		// for free attributes
		for (int i = 0; i < N; i++) {
			int srcOffset = 0, keyPos = 0;
			for (int j = 0; j < F; j++, srcOffset++) {
				int tAttr = K + (i * F) + j;
				
				// skip keys in the source
				if (keyPos < K && srcOffset == keyAttrPos[i][keyPos]) {
					srcOffset++;
					keyPos++;
				}
				addCorr(i, srcOffset, 0, tAttr);
			}
		}
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.FUSION;
	}
}
