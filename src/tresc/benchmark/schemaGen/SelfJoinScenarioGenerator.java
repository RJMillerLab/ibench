package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.MappingType;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.AND;
import vtools.dataModel.expression.EQ;
import vtools.dataModel.expression.ForeignKey;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.Key;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;

public class SelfJoinScenarioGenerator extends ScenarioGenerator
{
	private int JN;
	private int K;
	private int E;
	private int F;
	private String[] keys;
	private String[] fks;
    
    public SelfJoinScenarioGenerator()
    {
        ;
    }
    
    protected void initPartialMapping () {
    	super.initPartialMapping();
        E = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
        K = Utils.getRandomNumberAroundSomething(_generator, keyWidth, keyWidthDeviation);
        E = (E < ((2 * K) + 1)) ? ((2 * K) + 1) : E;
        JN = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements, numOfSetElementsDeviation);
        
        if (JN == 0)
            JN =1;
        
        F = E - (2 * K);
    }


    // the Source schema has one table with E number of elements, from which K
    // are keys, other K are foreign keys
    // and the rest until E will be free elements
    private SMarkElement createSubElements(Schema source, Schema target, int E, int K, int JN, int repetition,
            SPJQuery pquery, SPJQuery generatedQuery)
    {
        String[] keyS = new String[K];
        String[] FkeyS = new String[K];
        // create the source table
        String name = Modules.nameFactory.getARandomName();
        String nameS = name + "_" + getStamp() + repetition;
        SMarkElement srcEl = new SMarkElement(nameS, new Set(), null, 0, 0);
        srcEl.setHook(new String(getStamp() + repetition));
        source.addSubElement(srcEl);

        // create the first table in the target schema; it contains the keys
        // and the free attributes from the source table;
        // it is the Basic target table
        String nameT = nameS + "_B";
        SMarkElement trgEl = new SMarkElement(nameT, new Set(), null, 0, 0);
        trgEl.setHook(new String(getStamp() + repetition+ "_B"));
        target.addSubElement(trgEl);
        // create the first intermediate query
        SPJQuery query = new SPJQuery();
        // create the from clause of the query
        Variable var = new Variable("X");
        query.getFrom().add(var.clone(), new Projection(Path.ROOT,nameS));

        // generate the keys in the source and Basic target table
        // add the keys constraints to the source and to the target
        SelectClauseList select = query.getSelect();
        Variable varKey = new Variable("K");
        // the key constraint in the source
        Key keySrc = new Key();
        keySrc.addLeftTerm(varKey.clone(), new Projection(Path.ROOT,nameS));
        keySrc.setEqualElement(varKey.clone());
        // the key constraint in the target
        Key keyTrg = new Key();
        keyTrg.addLeftTerm(varKey.clone(), new Projection(Path.ROOT,nameT));
        keyTrg.setEqualElement(varKey.clone());
        for (int i = 0; i < K; i++)
        {
            name = Modules.nameFactory.getARandomName();
            name = name + "_" + getStamp() + repetition + "KE" + i;
            keyS[i] = name;
            SMarkElement el = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            el.setHook(new String( getStamp() + repetition + "KE" + i));
            srcEl.addSubElement(el);
            // add the attribute that is part of the key constraint of the source
            keySrc.addKeyAttr(new Projection(varKey.clone(),name));
            
            el = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            el.setHook(new String( getStamp() + repetition + "KE" + i));
            trgEl.addSubElement(el);
            // add the attribute that is part of the key constraint of the target
            keyTrg.addKeyAttr(new Projection(varKey.clone(),name));
            
            // add the keys to the select clause of the query
            Projection att = new Projection(var.clone(), name);
            select.add(name, att);
        }
        source.addConstraint(keySrc);
        target.addConstraint(keyTrg);

        // generate the foreign key in the source table; the Basic target table
        // does not contain foreign keys
        Variable varKey1 = new Variable("F");
        Variable varKey2 = new Variable("K");
        ForeignKey fKeySrc = new ForeignKey();
        fKeySrc.addLeftTerm(varKey1.clone(), new Projection(Path.ROOT,nameS));
        fKeySrc.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,nameS));
        for (int i = 0; i < K; i++)
        {
            name = Modules.nameFactory.getARandomName();
            name = name + "_" + getStamp() + repetition + "FK" + i;
            FkeyS[i] = name;
            SMarkElement el = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            el.setHook(new String( getStamp() + repetition + "FE" + i));
            srcEl.addSubElement(el);
            // add the attributes that make up the foreign key
            fKeySrc.addFKeyAttr(new Projection(varKey2.clone(),keyS[i]), 
                                new Projection(varKey1.clone(),FkeyS[i]));
        }
        source.addConstraint(fKeySrc);
        source.addConstraint(fKeySrc); // This just duplicates the foreign key to make it consistent with other senarios.
        
        // generate the free elements in the source table and in the Basic
        // target table only
        int F = E - (2 * K);
        for (int i = 0; i < F; i++)
        {
            name = Modules.nameFactory.getARandomName();
            name = name + "_" + getStamp() + repetition + "FE" + i;
            SMarkElement el = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            el.setHook(new String( getStamp() + repetition + "FE" + i));
            srcEl.addSubElement(el);
            el = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            el.setHook(new String( getStamp() + repetition + "FE" + i));
            trgEl.addSubElement(el);
            // add the free elements to the select clause of the query
            Projection att = new Projection(var.clone(), name);
            select.add(name, att);
        }

        // add the first query to the final query
        query.setSelect(select);
        SelectClauseList pselect = pquery.getSelect();
        SelectClauseList gselect = generatedQuery.getSelect();
        pselect.add(nameT, query);
        gselect.add(nameT, query);
        pquery.setSelect(pselect);
        generatedQuery.setSelect(gselect);
        generatedQuery.addTarget(nameT);

        if (JN == 1)
            return srcEl;

        // create the second table in the target schema;
        // it is the Join target table it contains the keys
        // of the source table and references to the foreign
        // keys of the source table;
        // it is obtained by self-join-ing the source table for JN times
        String nameT2 = nameS + "_J";
        SMarkElement trgEl2 = new SMarkElement(nameT2, new Set(), null, 0, 0);
        trgEl2.setHook(new String(getStamp() + repetition+ "_J"));
        
        target.addSubElement(trgEl2);
        // create the second intermediate query
        SPJQuery query2 = new SPJQuery();
        // create the from clause of the second query
        for (int i = 1; i <= JN; i++)
        {
            query2.getFrom().add(new Variable("X" + i), new Projection(Path.ROOT, nameS));
        }

        // generate the keys in the Join target table
        // add the key constraint for the Join target table     
        keyTrg = new Key();
        keyTrg.addLeftTerm(varKey.clone(), new Projection(Path.ROOT,nameT2));
        keyTrg.setEqualElement(varKey.clone());
        // add the foreign key constraint for the Join target table
        varKey1 = new Variable("F");
        varKey2 = new Variable("K");
        ForeignKey fKeyTrg = new ForeignKey();
        fKeyTrg.addLeftTerm(varKey1.clone(), new Projection(Path.ROOT,nameT2));
        fKeyTrg.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,nameT));
        for (int i = 0; i < K; i++)
        {
            SMarkElement el = new SMarkElement(keyS[i], Atomic.STRING, null, 0, 0);
            String hook = keyS[i].substring(keyS[i].indexOf("_"));
            el.setHook(hook);
            trgEl2.addSubElement(el);
            // add the attribute that is part of the key constraint of the target
            keyTrg.addKeyAttr(new Projection(varKey.clone(),keyS[i]));
            // add the attributes that makes up the foreign key of the target
            fKeyTrg.addFKeyAttr(new Projection(varKey2.clone(),keyS[i]), 
                                new Projection(varKey1.clone(),keyS[i]));
        }
        target.addConstraint(keyTrg);
        target.addConstraint(fKeyTrg);
        target.addConstraint(fKeyTrg); // Same as above. Duplicate the foreign key to make the XML print correct.
        
        // generate the first part of the Select clause of the second query
        // add as attr all the keys that belong to the first relation
        // that appears in the From clause
        SelectClauseList select2 = query2.getSelect();
        for (int i = 0; i < K; i++)
        {
            Projection att = new Projection(new Variable("X1"), keyS[i]);
            select2.add(keyS[i], att);
        }

        // generate in the Join target table the pointers to the keys
        // of the source; RE stands for Reference element
        // also generate the second part of the Select clause of the second
        // query by adding as attr all the keys that
        // belong to the last relation that appears in the From clause
        for (int i = 0; i < K; i++)
        {
            name = Modules.nameFactory.getARandomName();
            name = name + "_" + getStamp() + repetition + "RE" + i;
            SMarkElement el = new SMarkElement(name, Atomic.STRING, null, 0, 0);
            el.setHook(new String(getStamp() + repetition + "RE" + i));
            trgEl2.addSubElement(el);
            Projection att = new Projection(new Variable("X" + JN), keyS[i]);
            select2.add(name, att);
        }

        // generate the Where clause of the second query; that
        // constructs the joining of the source for JN times
        AND where = new AND();
        for (int j = 1; j < JN; j++)
            for (int i = 0; i < K; i++)
            {
                Projection att1 = new Projection(new Variable("X" + (j + 1)), FkeyS[i]);
                Projection att2 = new Projection(new Variable("X" + j), keyS[i]);
                where.add(new EQ(att1, att2));
            }

        // add the second query to the final query
        query2.setSelect(select2);
        query2.setWhere(where);
        pselect = pquery.getSelect();
        gselect = generatedQuery.getSelect();
        pselect.add(nameT2, query2);
        gselect.add(nameT2, query2);
        pquery.setSelect(pselect);
        // gselect.add(trgEl2.getLabel(), query);
        generatedQuery.setSelect(gselect);
        generatedQuery.addTarget(nameT2);
        return srcEl;
    }



	@Override
	protected void genSourceRels() throws Exception {
		String srcName = randomRelName(0);
		String[] attrs = new String[E];
		keys = new String[K];
		fks = new String[K];
		String hook = getRelHook(0);
		
		// create key and foreign key attrs
		for(int i = 0; i < K; i++) {
			String randAtt = randomAttrName(0, i);
			keys[i] = randAtt + "KE";
			fks[i] = randAtt + "FK";
			attrs[i] = keys[i];
			attrs[i + K] = fks[i];
		}
		// create free attrs
		for(int i = 2 * K; i < E; i++)
			attrs[i] = randomAttrName(0, i);
		
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

		// add keys to basic table and keys and fks to fk table
		for(int i = 0; i < K; i++) {
			bAttrs[i] = m.getAttrId(0, i, true);
			fkAttrs[i] = m.getAttrId(0, i, true);
			fkAttrs[i + K] = m.getAttrId(0, i + K, true);
		}
		// add free attrs to basic table
		for(int i = 2 * K; i < E; i++)
			bAttrs[i - K] = m.getAttrId(0, i, true);
		
		// create relations and foreign keys
		fac.addRelation(getRelHook(0), bRelName, bAttrs, false);
		fac.addRelation(getRelHook(1), fkRelName, fkAttrs, false);
		
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
		fac.addForeachAtom(m1, 0, CollectionUtils.concatArrays(keyVars, fkVars, 
				fVars));
		fac.addForeachAtom(m1, 0, CollectionUtils.concatArrays(keyVars, 
				fac.getFreshVars(E, E - K)));
		fac.addExistsAtom(m2, 0, CollectionUtils.concatArrays(keyVars, fkVars));
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
		Variable varKey = new Variable("K");
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
