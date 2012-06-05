package tresc.benchmark.schemaGen;

import java.util.Random;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;

import vtools.dataModel.expression.AND;
import vtools.dataModel.expression.ConstantAtomicValue;
import vtools.dataModel.expression.EQ;
import vtools.dataModel.expression.Function;
import vtools.dataModel.expression.Key;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Union;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;
import vtools.dataModel.values.AtomicValue;
import vtools.dataModel.values.NULL;

public class FusionScenarioGenerator extends ScenarioGenerator
{
    private Random _generator;

    private final String _stamp = "FU";

    public FusionScenarioGenerator()
    {
        ;
    }

    public void generateScenario(MappingScenario scenario, Configuration configuration)
    {
        // generate the generator based on the seed
        // long seed =
        // configuration.getScenarioSeeds(Constants.ScenarioName.FUSION.ordinal());
        // _generator = (seed == 0) ? new Random() : new Random(seed);

        _generator = configuration.getRandomGenerator();

        Schema source = scenario.getSource();
        Schema target = scenario.getTarget();
        SPJQuery pquery = scenario.getTransformation();

        int repetitions = configuration.getScenarioRepetitions(Constants.ScenarioName.FUSION.ordinal());
        // first decide the nesting depth
        int nesting = configuration.getParam(Constants.ParameterName.NestingDepth);
        int nestingDeviation = configuration.getDeviation(Constants.ParameterName.NestingDepth);
        int depth = Utils.getRandomNumberAroundSomething(_generator, nesting, nestingDeviation);
        // Find the parameters affecting the structure of the elements created
        int numOfElements = configuration.getParam(Constants.ParameterName.NumOfSubElements);
        int numOfElementsDeviation = configuration.getDeviation(Constants.ParameterName.NumOfSubElements);
        int keyWidth = configuration.getParam(Constants.ParameterName.NumOfJoinAttributes);
        int keyWidthDeviation = configuration.getDeviation(Constants.ParameterName.NumOfJoinAttributes);
        int numOfSetElements = configuration.getParam(Constants.ParameterName.JoinSize);
        int numOfSetElementsDeviation = configuration.getDeviation(Constants.ParameterName.JoinSize);

        for (int i = 0, imax = repetitions; i < imax; i++)
        {
            createSubElements(source, target, i, numOfElements, numOfElementsDeviation, keyWidth,
                keyWidthDeviation, numOfSetElements, numOfSetElementsDeviation, depth, pquery);
        }
    }

    // There are at most 2 levels. At each level, there will be E elements.
    // From these elements , K will be forming a key (KE),
    // F will be free elements (FE) and S will be nested set elements (NE).
    // Theoretically K + F + S = E
    // K>=1, N>=2, hence, E>=3. If E< K+N, then E becomes = K + N
    private void createSubElements(Schema source, Schema target, int repetition, int numOfElements, 
    		     int numOfElementsDeviation, int keyWidth,int keyWidthDeviation, int numOfSetElements, 
    		     int numOfSetElementsDeviation, int depth, SPJQuery pquery)
    {   
    	// decide the nr. of fragments
    	int N = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements, numOfSetElementsDeviation);
        N = (N < 2) ? 2 : N;
        // generate the fragments in the source schema
        SMarkElement[] srcTbls = new SMarkElement[N];
        String[] fragNames = new String[N];
        for (int k = 0; k < N; k++)
        {
            String randomName = Modules.nameFactory.getARandomName();
            String elName = randomName + "_" + _stamp + repetition + "NE" + k;
            SMarkElement e = new SMarkElement(elName, new Set(), null, 0, 0);
            e.setHook(new String(_stamp + repetition + "NE" + k));
            source.addSubElement(e);
            srcTbls[k] = e;
            fragNames[k] = elName;
        }
        // generate the fragment in the target source
        String randomName = Modules.nameFactory.getARandomName();
        String elTrgName = randomName + "_" + _stamp + repetition + "NE";
        SMarkElement trgTbl = new SMarkElement(elTrgName, new Set(), null, 0, 0);
        trgTbl.setHook(new String(_stamp + repetition + "NE"));
        target.addSubElement(trgTbl);

    	// first we decide the parameters we will use.
        int K = Utils.getRandomNumberAroundSomething(_generator, keyWidth, keyWidthDeviation);
        K = (K < 1) ? 1 : K;
        int S = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements, numOfSetElementsDeviation);
        S = (S < 2) ? 2 : S;
        // if the nesting depth is 0 then the nr of subsets is 0
        if ( depth==0 ) S =0;
        int E = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
        E = (E < (K + S)) ? (K + S) : E;
        int F = E - K - S;

        // We create the key elements. All the fragments will have the same key.
        // The target element will contain a copy of each key element.
        // Each key element contains K attributes.
        // Also, we create the key constraint for the source and target schema.
        String[] keyAttr = new String[K];
        // array with the keys for each fragment in the source
        Key[] keyS = new Key[N];
        for (int k = 0; k < N; k++)
        {
            keyS[k] = new Key();
            keyS[k].addLeftTerm(new Variable("X"), new Projection(Path.ROOT,srcTbls[k].getLabel()));
            keyS[k].setEqualElement(new Variable("X"));
        }    
        // key constraint for the target
        Key keyT = new Key();
        keyT.addLeftTerm(new Variable("Y"), new Projection(Path.ROOT,trgTbl.getLabel()));
        keyT.setEqualElement(new Variable("Y"));
        for (int i = 0; i < K; i++)
        {
            randomName = Modules.nameFactory.getARandomName();
            String keyName = randomName + "_" + _stamp + repetition + "KE" + i;
            for (int k = 0; k < N; k++)
            {
                SMarkElement e = new SMarkElement(keyName, Atomic.STRING, null, 0, 0);
                e.setHook(new String(_stamp + repetition + "KE" + i));
                srcTbls[k].addSubElement(e);
                // add the key attribute to the source key
                keyS[k].addKeyAttr(new Projection(new Variable("X"),keyName));
            }
            SMarkElement e = new SMarkElement(keyName, Atomic.STRING, null, 0, 0);
            e.setHook(new String(_stamp + repetition + "KE" + i));
            trgTbl.addSubElement(e);
            // add the key attribute to the target key
            keyT.addKeyAttr(new Projection(new Variable("Y"),keyName));
            
            keyAttr[i] = keyName;
        }
        for (int k = 0; k < N; k++)
                source.addConstraint(keyS[k]);
        target.addConstraint(keyT);
        
        // Now we create the free elements (if any). Each fragment will have
        // different free elements.
        // All the free elements from all the source fragments will be copied in
        // the target source.
        String[][] freeAttr = new String[N][F];
        for (int k = 0; k < N; k++)
            for (int i = 0; i < F; i++)
            {
            	randomName = Modules.nameFactory.getARandomName();
                String freeName = randomName + "_" + _stamp + repetition + "FE" + i;
            	SMarkElement e = new SMarkElement(freeName, Atomic.STRING, null, 0, 0);
            	e.setHook(new String(_stamp + repetition + "FE" + i));
                srcTbls[k].addSubElement(e);
                trgTbl.addSubElement(e);
                freeAttr[k][i] = freeName;
            }
        
        // ******* the first part of the query that represents the
        // transformation ********
        // create the Fquery that is the union of all the attributes (keys and
        // non-keys)
        // from each fragment of the source
        SPJQuery[] Uquery = new SPJQuery[N];
        for(int k=0; k<N; k++) 
        {
        	Uquery[k] = new SPJQuery();
        	// add each fragment to the from clause
        	Uquery[k].getFrom().add(new Variable("F"+k),new Projection(Path.ROOT,fragNames[k]));
        	// create the select clause
        	SelectClauseList select = Uquery[k].getSelect();
        	Variable var = new Variable("F"+k);
        	Projection att;
        	// add the key attributes to the select clause
        	for(int i=0; i<K; i++){
        	    att = new Projection(var.clone(), keyAttr[i]);
        	    select.add(keyAttr[i], att);
        	}
        	// add all the free attributes from all the fragments to the select
            // clause.
        	// all the free elements form all the fragments will be added it to
            // the
        	// select clause.
        	// given a fragment, use NULL for each missing global free element
        	for(int i= 0; i<N; i++)
        		if (i==k){
        			for(int j=0; j<F; j++){
        			    att = new Projection(var.clone(), freeAttr[k][j]);
                		select.add(freeAttr[k][j], att);
        			}
        		}
        		else {
        			for(int j=0; j<F; j++){
                		select.add(freeAttr[i][j], new ConstantAtomicValue(AtomicValue.NULL));
        			}
        		}
        	Uquery[k].setSelect(select);
        }
        Union Fquery = new Union();
        for(int k=0; k<N; k++)
        {
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
        for(int i=0; i<K; i++)
        { 	
        	Projection term = new Projection(var.clone(),keyAttr[i]);
        	query.addGroupByTerm(term);
        }
        // add the attributes of Fquery to the select clause of the final query
        SelectClauseList select = query.getSelect();
        Projection att;
        // add the key attributes of Fquery to the select clause
        for(int i=0; i<K; i++)
        {
    		att =  new Projection(var.clone(), keyAttr[i]);
            select.add(keyAttr[i], att);
        }
        // add the non-key attributes of Fquery as MAX(attribute)
        for(int i=0; i<N; i++)
        	for(int j=0; j<F; j++)
        	{
        	    att =  new Projection(var.clone(), freeAttr[i][j]);
        	    Function fmax = new Function("MAX");
        		fmax.addArg(att);
        		select.add(freeAttr[i][j], fmax);
        	}
        // *********************************************************************************
        // *********************************************************************************

        // ////////////////////////////////////////////////////////////////////////////////////////////////
        // And now the first nesting level (and the last if any)
        // First we generate the names of the sets, the keys and the free
        // elements
        // for each fragment and then we create the structure.
        // Each fragment will have a maximum of S sets( randomly is decided
        // which of the S sets will be created for each fragment in the source)
        
        // generate the names of the sets for each fragment in the source schema
        String[] setNames = new String[S];
        for (int i = 0; i < S; i++)
        {
            randomName = Modules.nameFactory.getARandomName();
            setNames[i] = randomName + "_" + _stamp + repetition + "NE" + i;
        }
        // generate the names of the keys. Each set has different keys.
        // but the same set used into different fragments will
        // have the same key.
        // The number of key attributes for each set is randomly chosen.
        String[][] setKeyAttr = new String[S][];
        for (int i = 0; i < S; i++)
        {	
        	int KS = Utils.getRandomNumberAroundSomething(_generator, keyWidth, keyWidthDeviation);
        	KS = (KS < 1) ? 1 : KS;
        	setKeyAttr[i] = new String[KS];
        	for (int k = 0; k < KS; k++)
            {
        		randomName = Modules.nameFactory.getARandomName();
                setKeyAttr[i][k] = randomName + "_" + _stamp + repetition + "KE" + k;  
            }
        }
        // generate the names of the free attributes. All the free attributes
        // are different no matter in which set/fragment they appear.
        String[][][] setFreeAttr = new String[N][S][];
        for (int i = 0; i < S; i++)
        {	
        	for (int j=0; j<N; j++)
        	{	
        		int ES = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
            	int KS = setKeyAttr[i].length;
                ES = (ES < KS) ? (KS +1) : ES;
                int FS = ES - KS;
                setFreeAttr[j][i] = new String[FS]; 
        		for (int k = 0; k < FS; k++)
        		{
        			randomName = Modules.nameFactory.getARandomName();
        			setFreeAttr[j][i][k] = randomName + "_" + _stamp + repetition + "FE" + k; 
        		}
        	}	
        }	
        // generate the matrix that keeps track of which set appears in which
        // fragment
        boolean[][]  setUse = new boolean[N][S];
        for (int i = 0; i < S; i++)	
        {
            int appearances = 0;
        	for (int j = 0; j<N; j++)
        	{
        		setUse[j][i] = _generator.nextBoolean();
        		if (setUse[j][i]) appearances++;
        	}
        	// if it happened that a set appears in no fragment then we chose
            // one fragment randomly
        	if (appearances == 0)
        	{
        	    int pos = _generator.nextInt(N);
        	    setUse[pos][i] = true;
        	}
        }
        // now create the sets for each fragment in the source.
        // also, we add the key constraints in the source.
        int[] t = new int[S];
        // matrix with the keys for each subset in each fragment of the source
        Key[][] keySS = new Key[N][S];
        for (int i = 0; i < S; i++)
            for (int j = 0; j < N; j++)
            {
                if (setUse[j][i] == false) continue;
                keySS[j][i] = new Key();
                keySS[j][i].addLeftTerm(new Variable("X"), 
                    new Projection(new Projection(Path.ROOT,srcTbls[j].getLabel()),setNames[i]));
                keySS[j][i].setEqualElement(new Variable("X"));
            }    
           
        for (int i = 0; i < S; i++)
        {	
        	t[i] = 0;
        	for (int j = 0; j<N; j++)
        	{
        		if (setUse[j][i] == false) continue;
        		t[i] = 1;
        		SMarkElement s = new SMarkElement(setNames[i], new Set(), null, 0, 0);
        		String hook = setNames[i].substring(setNames[i].indexOf("_"));
        		s.setHook(hook);
        		for (int k = 0, KS = setKeyAttr[i].length; k < KS; k++){
        			SMarkElement e = new SMarkElement(setKeyAttr[i][k], Atomic.STRING, null, 0, 0);
        			hook = setKeyAttr[i][k].substring(setKeyAttr[i][k].indexOf("_"));
        			e.setHook(hook);
        			s.addSubElement(e);
        			// add the key attribute to the key of the set-fragment of
                    // the source
                    keySS[j][i].addKeyAttr(new Projection(new Variable("X"), setKeyAttr[i][k]));
        		}
        		for (int k = 0, FS = setFreeAttr[j][i].length; k < FS; k++){
        			SMarkElement e = new SMarkElement(setFreeAttr[j][i][k], Atomic.STRING, null, 0, 0);
        			hook = setFreeAttr[j][i][k].substring(setFreeAttr[j][i][k].indexOf("_"));
        			e.setHook(hook);
        			s.addSubElement(e);
        		}
        		srcTbls[j].addSubElement(s);
        	}
        }
        for (int i = 0; i < S; i++)
            for (int j = 0; j < N; j++)
            {
                if (setUse[j][i] == false) continue;
                source.addConstraint(keySS[j][i]);
            }    
        // now create in the target a set element for each set from the S source
        // sets
        // that appears in at least one fragment. Each target set will contain
        // the keys
        // and the union of the free elements that correspond to a given source
        // set.
        // add the key constraint to the target schema.
        SMarkElement[] trgTbls = new SMarkElement[S];
        // array with the keys for each set-fragment in the target
        Key[] keyTS = new Key[S];
        for (int i = 0; i < S; i++)
        {
            if (t[i] == 0) continue;
            keyTS[i] = new Key();
            keyTS[i].addLeftTerm(new Variable("Y"), 
                new Projection(new Projection(Path.ROOT,trgTbl.getLabel()),setNames[i]));
            keyTS[i].setEqualElement(new Variable("Y"));
        }
        for (int i = 0; i < S; i++)
        {
        	if (t[i] == 0) continue;
        	trgTbls[i] = new SMarkElement(setNames[i], new Set(), null, 0, 0);
        	String hook = setNames[i].substring(setNames[i].indexOf("_"));
        	trgTbls[i].setHook(hook);
        	for (int k = 0, KS = setKeyAttr[i].length; k < KS; k++){
    			SMarkElement e = new SMarkElement(setKeyAttr[i][k], Atomic.STRING, null, 0, 0);
    			hook = setKeyAttr[i][k].substring(setKeyAttr[i][k].indexOf("_"));
                e.setHook(hook);
    			trgTbls[i].addSubElement(e);
    			// add the key attribute to the target
    			keyTS[i].addKeyAttr(new Projection(new Variable("Y"), setKeyAttr[i][k]));
    		}
        	for (int j = 0; j<N; j++)
        		for (int k = 0, FS = setFreeAttr[j][i].length; k < FS; k++){
        			if (setUse[j][i] == false) continue;
        			SMarkElement e = new SMarkElement(setFreeAttr[j][i][k], Atomic.STRING, null, 0, 0);
        			hook = setFreeAttr[j][i][k].substring(setFreeAttr[j][i][k].indexOf("_"));
                    e.setHook(hook);
        			trgTbls[i].addSubElement(e);
        		}
        	trgTbl.addSubElement(trgTbls[i]);
        }
        for (int i = 0; i < S; i++)
        {
            if (t[i] == 0) continue;
            target.addConstraint(keyTS[i]);
        }    
       
        // ********** second part of the query that represents the
        // transformation ********
        // create a subquery that for a given source set merges all the copies
        // that appear in all the fragments of the source
        for(int i=0; i<S; i++)
        {
        	// *********************************************************
        	// create the union query (i.e keys and non-keys elem) of set ith
        	// from each fragment in which it appears
        	SPJQuery[] Uiquery = new SPJQuery[N];
        	Variable var1 = new Variable("P");
            Variable var2 = new Variable("S");
        	for(int j=0; j<N; j++)
        	{
        		if (setUse[j][i] == false) { 
        			 Uiquery[j] = null;
        			 continue;
        		}	 
        		Uiquery[j] = new SPJQuery();
        		// ******add the from clause for each subquery of the union
        		// first relation in from clause is the fragment table,
        		Uiquery[j].getFrom().add(new Variable("P"), new Projection(Path.ROOT, fragNames[j]));
        		// second relation in from clause is the set i
        		Uiquery[j].getFrom().add(new Variable("S"), new Projection(var1.clone(),setNames[i]));
        		// ******create the select clause for each subquery of the union
        		SelectClauseList sel = Uiquery[j].getSelect();
        		// add the keys of the set i to the select clause
        		for(int k=0, KS=setKeyAttr[i].length; k<KS; k++)
        		{
        			att = new Projection(var2.clone(), setKeyAttr[i][k]);
            		sel.add(setKeyAttr[i][k], att);
        		}
        		// all the free elements of set i (form all the fragments) will
                // be added it to the
            	// select clause. Given the occurrence of set i in a fragment,
        		// use NULL for each missing global free element
            	for(int f= 0; f<N; f++)
            	{
            		if (setUse[f][i] == false)  continue;
            		if (f==j){
            			for(int k=0, FS=setFreeAttr[j][i].length; k<FS; k++){
            				att = new Projection(var2.clone(), setFreeAttr[j][i][k]);
            			    sel.add(setFreeAttr[j][i][k], att);
            			}
            		}
            		else {
            			for(int k=0, FS=setFreeAttr[f][i].length; k<FS; k++)
            					sel.add(setFreeAttr[f][i][k], new ConstantAtomicValue(AtomicValue.NULL));
            		}
            	}
            	Uiquery[j].setSelect(sel);
            	// ******create the where clause for each subquery of the union
            	// s.t. the keys of each fragment are equal to the keys of the
            	// final query
            	AND andCond = new AND();
            	for(int k=0, KS=keyAttr.length; k<KS; k++)
            	{
            		Projection att1 = new Projection(var1.clone(), keyAttr[k]);
            		Projection att2 = new Projection(var.clone(), keyAttr[k]);
            		andCond.add(new EQ(att1,att2));
            	}
            	Uiquery[j].setWhere(andCond);
        	}	
        	Union Squery = new Union();
            for(int j=0; j<N; j++)
            {
            	if (Uiquery[j] == null) continue;
            	Squery.add(Uiquery[j]);
            }
            // **************************************************************
            // create the select-from-group-by query that includes the Squery
            // this is the query that represents the set i
            SPJQuery setquery = new SPJQuery();
            // add the Squery to the from clause
            setquery.getFrom().add(new Variable("ST"+i), Squery);
            // create the group-by clause that includes all the keys of the
            // Squery
            Variable varSet = new Variable("ST"+i);
            for(int k=0,KS=setKeyAttr[i].length; k<KS; k++)
            { 	
            	Projection term = new Projection(varSet.clone(), setKeyAttr[i][k]);
                setquery.addGroupByTerm(term);
            }
            // add the attributes of Squery to the select clause of the setquery
            SelectClauseList s = setquery.getSelect();
            Projection atts;
            // add the key attributes of Squery
            for(int k=0,KS=setKeyAttr[i].length; k<KS; k++)
            {
        		atts = new Projection(varSet.clone(), setKeyAttr[i][k]);
                s.add(setKeyAttr[i][k], atts);
            }
            // add the non-key attributes of Squery as MAX(attribute)
            for(int f= 0; f<N; f++)
            {	
            	if (setUse[f][i] == false)  continue;
            	for(int k=0, FS=setFreeAttr[f][i].length; k<FS; k++)
            	{
            		atts = new Projection(varSet.clone(), setFreeAttr[f][i][k]);
            	    Function fmax = new Function("MAX");
            		fmax.addArg(atts);
            		s.add(setFreeAttr[f][i][k], fmax);
            	}
            }	
            setquery.setSelect(s);
            // each setquery is added to the select clause of the final query
            select.add(setNames[i], setquery);
        }
        
        query.setSelect(select);
        // the final query will be returned to the main method
        SelectClauseList pselect = pquery.getSelect();
        pselect.add(elTrgName, query);
    }
}
