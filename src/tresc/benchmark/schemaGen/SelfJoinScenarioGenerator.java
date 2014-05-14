package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;

import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.AND;
import vtools.dataModel.expression.EQ;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;

//MN FIXED "K"; it had not been set correctly - 6 May 2014
//MN ENHANCINED source reusability - 6 May 2014

public class SelfJoinScenarioGenerator extends AbstractScenarioGenerator
{
	static Logger log = Logger.getLogger(SelfJoinScenarioGenerator.class);
	
	public static final int MAX_NUM_TRIES = 10;
	
	private int JN;
	private int K;
	private int E;
	private int F;
	private String[] keys;
	private String[] fks;
    private int[] keyPos;
    private int[] fkPos;
//    private int[] normalPos;
    
    public SelfJoinScenarioGenerator()
    {
        ;
    }
    
    protected void initPartialMapping () {
    	super.initPartialMapping();
        E = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
        
        //MN modified the code so that K works correctly - 6 May 2014
        K = Utils.getRandomNumberAroundSomething(_generator, primaryKeySize, primaryKeySizeDeviation);
        
        E = (E < ((2 * K) + 1)) ? ((2 * K) + 1) : E;
        JN = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements, numOfSetElementsDeviation);
        
        JN = (JN < 1) ? 1 : JN;
        F = E - (2 * K);
    }

    //MN modified chooseSoruceRels - 6 May 2014
    //MN Question: I don't get some parts of the code - 6 May 2014
    //MN the goal is to preserve value of Key - 6 May 2014
    @Override
    protected boolean chooseSourceRels() throws Exception {
    	int numTries = 0;
    	RelationType rel = null;
    	String srcName;
    	
    	// fetch random rel with enough attrs
    	while(numTries < MAX_NUM_TRIES && rel == null){
    		rel = getRandomRel(true, K + K + 1);
    		numTries++;
    	}
    	
    	//TODO try to reduce number of keys and foreign keys?
    	
    	keys = new String[K];
    	keyPos = new int[K];
    	fks = new String[K];
    	fkPos = new int[K];
    	
    	if (rel == null)
    		return false;
    	
    	//MN BEGIN
    	E = rel.sizeOfAttrArray();
    	//MN END
    	
    	F = rel.sizeOfAttrArray() - 2 * K;
//    	normalPos = new int[F];
    	m.addSourceRel(rel);
    	srcName = rel.getName();

    	// already has PK, get positions of PK attrs
    	if (rel.isSetPrimaryKey()) {
    		keyPos = model.getPKPos(srcName, true);
    		keys = model.getPK(srcName, true);

    		// find attributes to use as fk
    		int fkDone = 0, pos = 0;
    		//MN I have trouble in understanding the following piece of code - 6 May 2014
    		while(fkDone < K) {
    			// is pk position?
    			if (Arrays.binarySearch(keyPos, pos) < 0) {
    				fkPos[fkDone] = pos;
    				fks[fkDone] = m.getAttrId(0, pos, true); 
    				fkDone++;
    			}
    			pos++;
    		}
    	}
    	else {
    		keyPos = CollectionUtils.createSequence(0, K);
    		fkPos = CollectionUtils.createSequence(K, K);
    		for(int i = 0; i < K; i++) {
    			keys[i] = rel.getAttrArray(i).getName();
    			fks[i] = rel.getAttrArray(K + i).getName();
    		}
//    		normalPos = CollectionUtils.createSequence(2 * K, F);

    		fac.addPrimaryKey(srcName, CollectionUtils.createSequence(0, K), true);
    		fac.addForeignKey(srcName, fks, srcName, keys, true);
    	}
    	
    	return true;
    }
    
	
	@Override
	protected void genSourceRels() throws Exception {
		String srcName = randomRelName(0);
		String[] attrs = new String[E];
		keys = new String[K];
		fks = new String[K];
		keyPos = new int[K];
		fkPos = new int[K];
//		normalPos = new int[F];
		
		String hook = getRelHook(0);
		
		// create key and foreign key attrs
		for(int i = 0; i < K; i++) {
			String randAtt = randomAttrName(0, i);
			keys[i] = randAtt + "ke";
			keyPos[i] = i;
			fks[i] = randAtt + "fk";
			fkPos[i] = i + K;
			attrs[i] = keys[i];
			attrs[i + K] = fks[i];
		}
		// create free attrs
		for(int i = 2 * K; i < E; i++)
			attrs[i] = randomAttrName(0, i);
//		normalPos = CollectionUtils.createSequence(2 * K, F);
		
		fac.addRelation(hook, srcName, attrs, true);
		fac.addPrimaryKey(srcName, keys, true);
		fac.addForeignKey(srcName, fks, srcName, keys, true);
	}

	@Override
	protected void genTargetRels() throws Exception {
		String bRelName = m.getRelName(0, true) + "_b";
		String fkRelName = m.getRelName(0, true) + "_fk";
		String[] bAttrs = new String[K + F];
		String[] fkAttrs = new String[2 * K];
		//MN ADDED support for types of attributes - 4 May 2014 (perhaps we do this for all mapping primitives)!
		List<String> attrsType1 = new ArrayList<String> ();
		List<String> attrsType2 = new ArrayList<String> ();
		
		// add keys to basic table and keys and fks to fk table
		for(int i = 0; i < K; i++) {
			//MN BEGIN - 6 May 2014
			//bAttrs
			attrsType1.add(m.getSourceRels().get(0).getAttrArray(i).getDataType());
			//fkAttrs
			attrsType2.add(m.getSourceRels().get(0).getAttrArray(i).getDataType());
			//MN END
			bAttrs[i] = m.getAttrId(0, i, true);
			fkAttrs[i] = m.getAttrId(0, i, true);
			fkAttrs[i + K] = m.getAttrId(0, i + K, true);
		}
		
		//MN BEGIN - 6 May 2014
		//fkAttrs
		for(int i=0; i<K; i++)
			attrsType2.add(m.getSourceRels().get(0).getAttrArray(i + K).getDataType());
		//MN END
		
		// add free attrs to basic table
		for(int i = 2 * K; i < E; i++){
			bAttrs[i - K] = m.getAttrId(0, i, true);
			//MN BEGIN - 6 May 2014
			//bAttrs
			attrsType1.add(m.getSourceRels().get(0).getAttrArray(i).getDataType());
			//MN END
		}
		
		// create relations and foreign keys
		//MN modified the method to support type checking - 6 May 2014
		fac.addRelation(getRelHook(0), bRelName, bAttrs, attrsType1.toArray(new String[] {}), false);
		fac.addRelation(getRelHook(1), fkRelName, fkAttrs, attrsType2.toArray(new String[] {}), false);
		
		fac.addPrimaryKey(bRelName, keys, false);
		fac.addPrimaryKey(fkRelName, keys, false);
		
		addFK(1, fks, 0, keys, false);
	}

	@Override
	protected void genMappings() throws Exception {
		String[] keyVars = fac.getFreshVars(0, K);
		String[] fkVars = fac.getFreshVars(K, K);
		String[] fVars = fac.getFreshVars(2 * K, F);

		MappingType m1 = fac.addMapping(m.getCorrs(0, false));
		fac.addForeachAtom(m1, 0, CollectionUtils.concatArrays(keyVars, fkVars, 
				fVars));
		fac.addExistsAtom(m1, 0, CollectionUtils.concatArrays(keyVars, fVars));
		
		MappingType m2 = fac.addMapping(m.getCorrs(1, false));
		fac.addForeachAtom(m2, 0, CollectionUtils.concatArrays(keyVars, fac.getFreshVars(2 * K, E - K)));
		fac.addForeachAtom(m2, 0, CollectionUtils.concatArrays(fkVars, keyVars,
				fac.getFreshVars(E, F)));
		fac.addExistsAtom(m2, 1, CollectionUtils.concatArrays(keyVars, fkVars));
	}
	
	@Override
	protected void genTransformations() throws Exception {
		SPJQuery genQuery = new SPJQuery();
		SPJQuery q;
		String mapId;
		genQueries(genQuery);
		
		q = (SPJQuery) genQuery.getSelect().getTerm(0);
		mapId = m.getMaps().get(0).getId();
		fac.addTransformation(q.toTrampStringOneMap(mapId), mapId, 
				m.getRelName(0, false));
		
		q = (SPJQuery) genQuery.getSelect().getTerm(1);
		mapId = m.getMaps().get(1).getId();
		fac.addTransformation(q.toTrampStringOneMap(mapId), mapId, 
				m.getRelName(1, false));
	}
	
	
	
	private void genQueries(SPJQuery generatedQuery) {
		String nameS = m.getRelName(0, true);
		String nameTB = m.getRelName(0, false);
		String nameTFK = m.getRelName(1, false);
		String[] sAttrs = m.getAttrIds(0, true);
		// create the first query mapping to the K, F table
		SPJQuery query = new SPJQuery();
		Variable var = new Variable("X");
		query.getFrom().add(var.clone(), new Projection(Path.ROOT, nameS));

		// generate the keys in the source and Basic target table
		// add the keys constraints to the source and to the target
		SelectClauseList select = query.getSelect();
		//Variable varKey = new Variable("K");
		// the key constraint in the source
		for (int i = 0; i < K; i++) {
			// add the keys to the select clause of the query
			Projection att = new Projection(var.clone(), keys[i]);
			select.add(keys[i], att);
		}

		// generate the free elements in the source table and in the Basic
		// target table only
		for (int i = 0; i < F; i++) {
			// add the free elements to the select clause of the query
			String attName = sAttrs[2 * K + i];
			Projection att = new Projection(var.clone(), attName);
			select.add(attName, att);
		}

		// add the first query to the final query
		query.setSelect(select);
		SelectClauseList pselect = pquery.getSelect();
		SelectClauseList gselect = generatedQuery.getSelect();
		pselect.add(nameTB, query);
		gselect.add(nameTB, query);
		pquery.setSelect(pselect);
		generatedQuery.setSelect(gselect);
		generatedQuery.addTarget(nameTB);
		
        // create the second intermediate query
		SPJQuery query2 = new SPJQuery();
		// create the from clause of the second query
		for (int i = 1; i <= JN; i++)
			query2.getFrom().add(new Variable("X" + i), new Projection(Path.ROOT, nameS));
	        
		// generate the first part of the Select clause of the second query
		// add as attr all the keys that belong to the first relation
		// that appears in the From clause
		SelectClauseList select2 = query2.getSelect();
		for (int i = 0; i < K; i++) {
			Projection att = new Projection(new Variable("X1"), keys[i]);
			select2.add(keys[i], att);
		}

		// generate in the Join target table the pointers to the keys
		// of the source; RE stands for Reference element
		// also generate the second part of the Select clause of the second
		// query by adding as attr all the keys that
		// belong to the last relation that appears in the From clause
		for (int i = 0; i < K; i++) {
			Projection att = new Projection(new Variable("X" + JN), fks[i]);
			select2.add(fks[i], att);
		}

		// generate the Where clause of the second query; that
		// constructs the joining of the source for JN times
		AND where = new AND();
		for (int j = 1; j < JN; j++)
			for (int i = 0; i < K; i++) {
				Projection att1 =
						new Projection(new Variable("X" + (j + 1)), keys[i]);
				Projection att2 =
						new Projection(new Variable("X" + j), fks[i]);
				where.add(new EQ(att1, att2));
			}

		// add the second query to the final query
		query2.setSelect(select2);
		query2.setWhere(where);
		pselect = pquery.getSelect();
		gselect = generatedQuery.getSelect();
		pselect.add(nameTFK, query2);
		gselect.add(nameTFK, query2);
		pquery.setSelect(pselect);
		generatedQuery.setSelect(gselect);
		generatedQuery.addTarget(nameTFK);
	}

	@Override
	protected void genCorrespondences() {
		// keys from source to both target relations
		for(int i = 0; i < K; i++) {
			addCorr(0, i, 0, i);
			addCorr(0, i, 1, i);
		}
		// FKs from source to target FK relation
		for(int i = 0; i < K; i++)
			addCorr(0, i + K, 1, i + K);
		// free attrs from source to basic target relation
		for(int i = 0; i < F; i++)
			addCorr(0, i + (2 * K), 0, i + K);
	}
	
	@Override
	public ScenarioName getScenType() {
		return ScenarioName.SELFJOINS;
	}
}
