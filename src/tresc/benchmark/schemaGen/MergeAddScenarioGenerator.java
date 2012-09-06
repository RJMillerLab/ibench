package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SKFunction;

import tresc.benchmark.Constants.JoinKind;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Constants.SkolemKind;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.AND;
import vtools.dataModel.expression.EQ;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Query;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;

// PRG FIXED Chain Join Problems - August 30, 2012
// PRG PRG FIXED Foreach Source Expression by correcting fromIndex - Sep 4, 2012 
// PRG FIXED BUG - ArrayCopy was always invoked with Null Destiny Array - Sep 5, 2012

public class MergeAddScenarioGenerator extends AbstractScenarioGenerator {
	
	// PRG ADD August 30, 2012
	static Logger log = Logger.getLogger(MergeAddScenarioGenerator.class);
	
	public static final int MAX_NUM_TRIES = 10;
	
	private int numOfTables;
	private int numOfJoinAttributes;
	private JoinKind jk;
	private int[] numOfAttributes;
	private String[] joinAttrs;
	private SkolemKind sk;
	
    public MergeAddScenarioGenerator()
    {		;		}
    
    @Override
    protected void initPartialMapping() {
    	super.initPartialMapping();
    	
        numOfTables = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements,
            numOfSetElementsDeviation);
        
        numOfTables = (numOfTables > 1) ? numOfTables : 2;
        
        numOfJoinAttributes = Utils.getRandomNumberAroundSomething(_generator, keyWidth,
            keyWidthDeviation);
        jk = JoinKind.values()[joinKind];
        if (jk == JoinKind.VARIABLE)
        {
            int tmp = Utils.getRandomNumberAroundSomething(_generator, 0, 1);
            if (tmp < 0)
                jk = JoinKind.STAR;
            else jk = JoinKind.CHAIN;
        }
        numOfAttributes = new int[numOfTables];
        for (int k = 0, kmax = numOfAttributes.length; k < kmax; k++)
        {
            int tmpInt = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
                numOfElementsDeviation);
            // make sure that we have enough attribute for the join + at least on free one
            tmpInt = (tmpInt <= getNumJoinAttrs(k)) ? getNumJoinAttrs(k) + 1 : tmpInt; 
            numOfAttributes[k] = tmpInt;
        }
        
        sk = SkolemKind.values()[typeOfSkolem];
    }
    
    /**
     * Find source rels that have enough attributes and either have no key or
     * have a key on the last numOfJoinAttributes attrs (except for the first
     * one in a STAR join)
     */
    @Override
	protected void chooseSourceRels() {
		if (jk == JoinKind.STAR)
			chooseStarSourceRels();
		else if (jk == JoinKind.CHAIN)
			chooseChainSourceRels();
		
	}

    /**
     * 
     */
    private void chooseChainSourceRels() {
		//TODO
	}

    /**
     * 
     */
	private void chooseStarSourceRels() {
		//TODO
	}
	
	@Override
	protected void genSourceRels() throws Exception {
		String[] sourceNames = new String[numOfTables];
		String[][] attrs  = new String[numOfTables][];
		joinAttrs = new String[numOfJoinAttributes];
		// create join attr names
		for(int i = 0; i < numOfJoinAttributes; i++)
			joinAttrs[i] = randomAttrName(0, i);
		
		// create numOfTables in the source to be denormalized
		for(int i = 0; i < numOfTables; i++) {
			sourceNames[i] = randomRelName(i);
			int numOfNonJoinAttr = numOfAttributes[i] - getNumJoinAttrs(i);
			attrs[i] = new String[numOfAttributes[i]];
		
			for(int j = 0; j < numOfNonJoinAttr; j++)
				attrs[i][j] = randomAttrName(i, j);
		}
		
		if (jk == JoinKind.STAR)
			createStarJoinAttrs(attrs);
		if (jk == JoinKind.CHAIN)
			createChainJoinAttrs(attrs);
		
		// create tables 
		for(int i = 0; i < numOfTables; i++)
			fac.addRelation(getRelHook(i), sourceNames[i], attrs[i], true);
		
		// create FK and key constraints
		if (jk == JoinKind.STAR)
			createStarConstraints(attrs);
		if (jk == JoinKind.CHAIN)
			createChainConstraints(attrs);
	}

	private int getNumJoinAttrs(int i) {
		if (jk == JoinKind.STAR) {
			if (i == 0)
				return numOfJoinAttributes * (numOfTables - 1);
			return numOfJoinAttributes;
		}
		if (jk == JoinKind.CHAIN) {
			if (i == 0 || i == numOfTables - 1)
				return numOfJoinAttributes;
			return 2 * numOfJoinAttributes;
		}
		return -1;
	}
	
	private int getNumNormalAttrs(int i) {
		return numOfAttributes[i] - getNumJoinAttrs(i);
	}
	
	private int getTotalNumNormalAttrs() {
		int totalNormalAttributes = 0;
    	for (int k = 0; k < numOfTables; k++)
    		totalNormalAttributes += getNumNormalAttrs(k);
    	
    	return totalNormalAttributes;
	}

	private void createChainConstraints(String[][] attrs) throws Exception {
		// create primary keys
		for(int i = 0; i < numOfTables - 1; i++) {
			String relName = m.getRelName(i, true);
			fac.addPrimaryKey(relName, getJoinAttrs(i), true);
		}
		// join every table with the previous one
		for(int i = 1; i < numOfTables; i++) {
			String[] fAttr, tAttr;
			fAttr = getJoinRefs(i);
			tAttr = getJoinAttrs(i - 1);
			addFK(i, fAttr, i - 1, tAttr, true);
		}
	}

	private void createStarConstraints(String[][] attrs) throws Exception {
		// create primary keys
		for(int i = 1; i < numOfTables; i++) {
			String relName = m.getRelName(i, true);
			fac.addPrimaryKey(relName, getJoinAttrs(i), true);
		}
		// create fks from every table to the first table
		for(int i = 1; i < numOfTables; i++) {
			String[] fAttr, tAttr;
			tAttr = getJoinAttrs(i);
			fAttr = getJoinRefs(i);
			addFK(0, fAttr, i, tAttr, true);
		}
	}
	
	private String[] getJoinRefs(int i) {
		String[] result = new String[numOfJoinAttributes];
		for(int j = 0; j < numOfJoinAttributes; j++)
			result[j] = getJoinRef(i, j);
		
		return result;
	}

	private String[] getJoinAttrs (int i) {
		String[] result = new String[numOfJoinAttributes];
		for(int j = 0; j < numOfJoinAttributes; j++)
			result[j] = getJoinAttr(i, j);
		
		return result;
	}

	private void createStarJoinAttrs(String[][] attrs) {
		// create join attrs in all except the first table (center of star)
		for(int i = 1; i < numOfTables; i++) {
			int offset = numOfAttributes[i] - (numOfJoinAttributes);
			for(int j = 0; j < numOfJoinAttributes; j++)
				attrs[i][offset + j] = getJoinAttr(i, j);
		}
		
		// create fk attributes in first table
		int offset = numOfAttributes[0] - ((numOfTables - 1) * numOfJoinAttributes);
		for(int i = 1; i < numOfTables; i++) {
			for(int j = 0; j < numOfJoinAttributes; j++)
				attrs[0][offset + j] = getJoinRef(i, j);
			offset += numOfJoinAttributes;
		}
	}
 

	private void createChainJoinAttrs(String[][] attrs) {
		// create join attributes in all tables except the last one
		for(int i = 0; i < numOfTables - 1; i++) {
			int fac = i == 0 ? 1 : 2;
			int offset = numOfAttributes[i] - (numOfJoinAttributes * fac);
			for(int j = 0; j < numOfJoinAttributes; j++)
				attrs[i][offset + j] = getJoinAttr(i, j);
		}
		
		// create fk attributes in all tables except first
		for (int i = 1; i < numOfTables; i++) {
			int offset = numOfAttributes[i] - numOfJoinAttributes;
			for(int j = 0; j < numOfJoinAttributes; j++)
				attrs[i][offset + j] = getJoinRef( i, j);
		}
	}
	
	private String getJoinRef(int i, int j) {
		return joinAttrs[j] + "comp" + i + "_joinref_" + j;
	}

	private String getJoinAttr(int i, int j) {
		return joinAttrs[j] + "comp" + i +  "_joinattr_" + j;
	}
	
	/**
	 * Find a table that has at least numOfTables * (numOfJoinAttributes + 1)
	 * attributes and either no key or the key is on the last 
	 * numOfJoin Attributes * (numOfTables - 1) attributes.  
	 * @throws Exception 
	 */
	@Override
	protected void chooseTargetRels() throws Exception { //TODO more flexible to adapt numOfJoinAttributes
		RelationType r = null;
		int tries = 0;
		int minAttrs = numOfTables * (numOfJoinAttributes + 1);
		int numTJoinAttrs = getTargetNumJoinAttrs();
		boolean ok = false;
		int numNormalAttr = 0;
		int[] joinAttPos = null;
		
		while(tries < MAX_NUM_TRIES && !ok) {
			r = getRandomRel(false, minAttrs);
			
			if (r == null)
				break;
			
			numNormalAttr = r.sizeOfAttrArray() - numTJoinAttrs;
			joinAttPos = CollectionUtils.createSequence(numNormalAttr, 
					numTJoinAttrs);
			
			if (r.isSetPrimaryKey()) {
				int[] pkPos = model.getPKPos(r.getName(), false);
				
				// PK has to be on the numTJoinAttrs last attributes
				if (pkPos.length ==  numTJoinAttrs 
						&& Arrays.equals(pkPos, joinAttPos))
					ok = true;
			}
			else
				ok = true;
		}
		
		// didn't find suiting rel? Create it
		if (!ok)
			genTargetRels();
		// add keys and distribute the normal attributes of rel
		else {
			m.addTargetRel(r);
			
			if (!r.isSetPrimaryKey())
				fac.addPrimaryKey(r.getName(), joinAttPos, false);
			
			// adapt number of attribute per source rel
			int numPerSrcRel = numNormalAttr / numOfTables;
			for(int i = 0; i < numOfTables; i++)
				numOfAttributes[i] = numPerSrcRel; 
			numOfAttributes[numOfTables - 1] = numNormalAttr % numOfTables;
		}
	}
	
	@Override
	protected void genTargetRels() throws Exception {
		String targetName = randomRelName(0);
		List<String> attrs = new ArrayList<String> ();
		int numNormalAttrs;
		
		// first copy normal attributes
		for(int i = 0; i < numOfTables; i++) {
			int numAtt = getNumNormalAttrs(i);
			for(int j = 0; j < numAtt; j++)
				attrs.add(m.getAttrId(i, j, true));
		}
		
		numNormalAttrs = attrs.size();
		
		// then copy join attributes
		if (jk == JoinKind.STAR) {
			for(int i = 1; i < numOfTables; i++) {
				int offset = getNumNormalAttrs(i);
				for(int j = 0; j < numOfJoinAttributes; j++)
					attrs.add(m.getAttrId(i, j + offset, true));		
			}
		}
		if (jk == JoinKind.CHAIN) {
			for(int i = 0; i < numOfTables - 1; i++) {
				int offset = getNumNormalAttrs(i);
				for(int j = 0; j < numOfJoinAttributes; j++)
					attrs.add(m.getAttrId(i, j + offset, true));				
			}
		}
		
		// calculate the total number of attributes (so we know the position of the new attributes)
		int numTotalAttrs = numOfJoinAttributes;
		for(int j = 0; j < numOfTables; j++)
			numTotalAttrs += getNumNormalAttrs(j);
		
		// create random names for the added attrs
		for (int i = numTotalAttrs; i < numTotalAttrs + numNewAttr; i++)
			attrs.add(randomAttrName(0, i));
		
		fac.addRelation(getRelHook(0), targetName, attrs.toArray(new String[] {}), false);
		
		// add PK on join attributes
		fac.addPrimaryKey(targetName, CollectionUtils.createSequence(numNormalAttrs, getTargetNumJoinAttrs()), false);
	}
	
	protected int getTargetNumJoinAttrs () {
		return numOfJoinAttributes * (numOfTables - 1);
	}
	
	@Override
	protected void genMappings() throws Exception {
		MappingType m1 = fac.addMapping(m.getCorrs());
		String[][] vars = new String[numOfTables][];
		String[] targetVars;
		int offset;
		
		// depending on whether they are first or second order tgds we need to allocate less space for the vars
		// (since in the actual mapping, SOtgds will use an SKFunction atom instead of a Var atom)
		switch (mapLang)
		{
			case FOtgds:
				targetVars = new String[m.getNumRelAttr(0, false)];
			case SOtgds:
				targetVars = new String[m.getNumRelAttr(0, false) - numNewAttr];
			default:
				targetVars = new String[m.getNumRelAttr(0, false) - numNewAttr];
		}
		
		// add foreach atoms for the the source fragments
		offset = m.getNumRelAttr(0, true);
		vars[0] = fac.getFreshVars(0, offset);
		fac.addForeachAtom(m1, 0, vars[0]);
		
		// each table get fresh vars for its free and join attributes
		// the fk vars are takes from the join attributes they reference
		log.debug("Relation 0 is of size " + numOfAttributes[0]);
		for(int i = 1; i < numOfTables; i++) {
			int numFreshVars = numOfAttributes[i] - numOfJoinAttributes;
			String[] freeVars = fac.getFreshVars(offset, numFreshVars);
			offset += numFreshVars;
			String[] fkVars = null;
			
			// get vars for the referenced attributes from the first table 
			if (jk == JoinKind.STAR) {
				int from = numOfAttributes[0] - ((numOfTables - i) * numOfJoinAttributes);
				fkVars = Arrays.copyOfRange(vars[0], from, from + numOfJoinAttributes);
			}
			// get vars for the referenced attributes from the previous table
			if (jk == JoinKind.CHAIN) {
				log.debug("Relation i is " + i  + " of size " + numOfAttributes[i] + ", " + numFreshVars + " , " + numOfJoinAttributes);
				// PRG FIXED CHAIN JOIN PROBLEMS - August 30, 2012
				// fkVars = Arrays.copyOfRange(vars[i - 1], numFreshVars, 
				//		numFreshVars + numOfJoinAttributes);
				// PRG FIXED Foreach Source Expression by correcting fromIndex - Sep 4, 2012
				// int fromIndex = vars[i-1].length - numOfJoinAttributes;
				int fromIndex = vars[i-1].length - getNumJoinAttrs(i-1); 
				int toIndex = (numOfJoinAttributes == 1 ? fromIndex + 1 : fromIndex + numOfJoinAttributes);
				log.debug("Copying Chain Join Variables from  " + fromIndex  + " to " + toIndex);
				fkVars = Arrays.copyOfRange(vars[i - 1], fromIndex, toIndex);
			}
			vars[i] = CollectionUtils.concat(freeVars, fkVars);
			
			fac.addForeachAtom(m1, i, vars[i]);
			
			log.debug("For Each Clause is: " + m1.getForeach().toString());
		}
		
		// generate an array of vars for the target
		// first we add vars for the free attributes of all table
		// then we add the join attribute vars
		offset = 0;
		for(int i = 0; i < numOfTables; i++) {
			int numVars = vars[i].length;
			numVars = getNumNormalAttrs(i);
			
			System.arraycopy(vars[i], 0, targetVars, offset, numVars);
			offset += numVars;
		}
		
		int totalVars = offset;
		
		// star join, add join attribute vars from first table
		if (jk == JoinKind.STAR) {
			int start = getNumNormalAttrs(0);
			for(int i = 1; i < numOfTables; i++) {
				System.arraycopy(vars[0], start, targetVars, offset, numOfJoinAttributes);
				offset += numOfJoinAttributes;
				start += numOfJoinAttributes;
			}
		}
		// chain join, take join attribute vars from each table
		if (jk == JoinKind.CHAIN) {
			for(int i = 0; i < numOfTables - 1; i++) {
				int start = getNumNormalAttrs(i);
				System.arraycopy(vars[i], start, targetVars, offset, numOfJoinAttributes);
				offset += numOfJoinAttributes;
			}
		}
		
		switch (mapLang) 
		{
			// target tables gets fresh vars for the new attrs
			case FOtgds:
				// add the new variables to the targetVar array
				System.arraycopy(fac.getFreshVars(offset, numNewAttr), 0, targetVars, offset, numOfJoinAttributes);
				fac.addExistsAtom(m1, 0, targetVars);
				break;
			// target gets all the src variables + skolem terms for the new attrs
			case SOtgds:
				fac.addEmptyExistsAtom(m1, 0);
				fac.addVarsToExistsAtom(m1, 0, targetVars);
				SkolemKind sk1 = sk;
				if(sk == SkolemKind.VARIABLE)
					sk1 = SkolemKind.values()[_generator.nextInt(3)];
				generateSKs(m1, sk1, totalVars, vars, targetVars);
				break;
		}	
	}
	
	private void generateSKs(MappingType m1, SkolemKind sk, int totalVars, String[][] vars, String[] targetVars) 
	{	
		log.debug("Method generateSKs with totalVars " + totalVars);
		for (int i = 0; i < numNewAttr; i++)
		{
			// in KEY mode we use the join attributes as the skolem arguments
			if (sk == SkolemKind.KEY)
			{
				log.debug("--- SKOLEM MODE = KEY ---");

				// PRG FIXED BUG - ArrayCopy was always invoked with Null Destiny Array - Sep 5, 2012
				// Initially we thought about allocating a max number of elements, as determined by the length of targetVars
				// But then we decided to compute the exact number as follows: numOfJoinAttributes*(numOfTables - 1)
				// This formula seems to work for both STAR and CHAIN Joins
				// String[] argVars = null;
				// int offset = totalVars;
				String[] argVars = new String[numOfJoinAttributes*(numOfTables - 1)];
				int offset = 0;

				// star join, add join attribute vars from first table
				if (jk == JoinKind.STAR) {
					int start = getNumNormalAttrs(0);
					for(int j = 1; j < numOfTables; j++) {
						System.arraycopy(vars[0], start, argVars, offset, numOfJoinAttributes);
						offset += numOfJoinAttributes;
						start += numOfJoinAttributes;
					}
				}

				// chain join, take join attribute vars from each table
				if (jk == JoinKind.CHAIN) {
					for(int j = 0; j < numOfTables - 1; j++) {
						int start = getNumNormalAttrs(j);
						log.debug("Relation j is " + j  + " of size " + numOfAttributes[j] + ", Num Join Attrs: " + numOfJoinAttributes);
						log.debug("Copying Chain Join Variables from  " + start  + " to " + numOfJoinAttributes);
						System.arraycopy(vars[j], start, argVars, offset, numOfJoinAttributes);
						offset += numOfJoinAttributes;
					}
				}
				
				fac.addSKToExistsAtom(m1, 0, argVars);
			}

			else if (sk == SkolemKind.RANDOM)
			{
				log.debug("--- SKOLEM MODE = RANDOM ---");

				int numArgsForSkolem = Utils.getRandomNumberAroundSomething(_generator, totalVars/2, totalVars/2);
				
				log.debug("number of arguments: " + numArgsForSkolem);

				// ensure that we are still within bounds
				numArgsForSkolem = (numArgsForSkolem >= totalVars) ? totalVars : numArgsForSkolem;

				// generate the random vars to be arguments for the skolem
				int tries = 0;
				Vector<String> randomVars = new Vector<String> ();
				for (int k=0; k < numArgsForSkolem; k++)
				{
					// use max tries to avoid infinite loops
					if (tries == MAX_NUM_TRIES)
						if (randomVars.size() != 0 && k == numArgsForSkolem-1)
							break;
						else
							tries--;
					
					int pos = Utils.getRandomNumberAroundSomething(_generator, totalVars/2, totalVars/2);
					pos = (pos >= totalVars) ? totalVars-1 : pos;

					// if we haven't already added this variable as an argument, add it
					if(randomVars.indexOf(fac.getFreshVars(pos, 1)[0]) == -1)
						randomVars.add(fac.getFreshVars(pos, 1)[0]);
					else
						k--;
					
					tries++;
				}

				Collections.sort(randomVars);

				log.debug("arguments: " + randomVars.toString());

				fac.addSKToExistsAtom(m1, 0, Utils.convertVectorToStringArray(randomVars));
			}

			else if (sk == SkolemKind.ALL)
			{
				log.debug("--- SKOLEM MODE = RANDOM ---");

				List<String> tgtVars = Arrays.asList(targetVars);
				log.debug("arguments: ");
				log.debug(tgtVars.toString());

				fac.addSKToExistsAtom(m1, 0,targetVars);
			}
		}
	}
	
	@Override
	protected void genTransformations() throws Exception {
		Query q;
		String creates = m.getRelName(0, false);
		String mapping = m.getMapIds()[0];
		
		q = genQueries();
		q.storeCode(q.toTrampStringOneMap(mapping));
		q = addQueryOrUnion(creates, q);
		
		fac.addTransformation(q.getStoredCode(), m.getMapIds(), creates);
	}
	
	private SPJQuery genQueries() throws Exception {
		SPJQuery query = new SPJQuery();
    	SelectClauseList sel = query.getSelect();
    	FromClauseList from = query.getFrom();
    	
       // first we create the source tables and their attributes 
        // that do not participate in the joins.
        for (int i = 0, imax = numOfTables; i < imax; i++) {
            // add the table to the from clause
            from.add(new Variable("X"+i), new Projection(Path.ROOT,  m.getRelName(i, true)));

            // create the non join attributes of the specific table/fragment/component
            int numOfNonJoinAttr = numOfAttributes[i] - getNumJoinAttrs(i);
            
            for (int k = 0, kmax = numOfNonJoinAttr; k < kmax; k++) {
            	String attrName = m.getAttrId(i, k, true);
                // add the non-join attribute to the select clause
                Projection att = new Projection(new Variable("X"+i), attrName);
                sel.add(attrName, att);
            }
        }

        // first create the join SMarkElements names
        String joinAttrNames[] = new String[numOfJoinAttributes];
        for (int i = 0, imax = numOfJoinAttributes; i < imax; i++)
            joinAttrNames[i] = Modules.nameFactory.getARandomName();
        //*******************************************************************
        // for the case of a STAR join, create the join attributes for all the
        // tables (apart from the first one) 
        // also, all the join attributes will be added to the target table
        // and to the select clause of the local query
        for (int tbli = 1, tblimax = numOfAttributes.length; ((tbli < tblimax) && (jk == JoinKind.STAR)); tbli++)
        {
            for (int i = 0, imax = numOfJoinAttributes; i < imax; i++)
            {
            	String attrName = getJoinAttr(tbli, i);
                // add the join attribute to the select clause
                Projection att = new Projection(new Variable("X"+tbli), attrName);
                sel.add(attrName, att);
            }
        }
        
        // and now we add the reference (Foreign key if you like the term)
        // attributes in the first table. the foreign keys will point to 
        // all the join attributes from the other tables.
        // create the join conditions in the where clause and the fkeys.
        AND andCond = new AND();
        andCond.toString();
        for (int tbli = 1, tblimax = numOfAttributes.length; ((tbli < tblimax) && (jk == JoinKind.STAR)); tbli++)
        {    	
        	for (int i = 0, imax = numOfJoinAttributes; i < imax; i++)
        	{
        		String attrName = getJoinRef(tbli, i);
        		String referencedAttrName = getJoinAttr(tbli, i);
        		
                // create the where clause; the join attributes and the reference attributes that
                // make the join condition
                Projection att1 = new Projection(new Variable("X"+0), attrName);
                Projection att2 = new Projection(new Variable("X"+tbli), referencedAttrName);
                andCond.add(new EQ(att1,att2));
        	}
        }	

        // ********************************************************************
        // for the case of a CHAIN join, create the join attributes for all the
        // tables (apart from the last one of course since none is referencing
        // that).also, all the join attributes will be added to the target table.
        for (int tbli = 0, tblimax = numOfAttributes.length; ((tbli < (tblimax - 1)) && (jk == JoinKind.CHAIN)); tbli++)
        {
            for (int i = 0, imax = numOfJoinAttributes; i < imax; i++)
            {
            	String attrName = getJoinAttr(tbli, i);
            	
                // add the join attribute to the select clause
                Projection att = new Projection(new Variable("X"+tbli), attrName);
                sel.add(attrName, att);
            }
        }

        // and now we add the reference (Foreign key if you like the term)
        // attributes. The only difference between the CHAIN and the STAR join
        // is that in the former case they reference the previous table, while
        // in the STAR they all reference the first table
        // in the case of CHAIN join, the foreign keys will not be added to the target table
        for (int tbli = 1, tblimax = numOfAttributes.length; ((tbli < tblimax) && (jk == JoinKind.CHAIN)); tbli++)
        {     
            for (int i = 0, imax = numOfJoinAttributes; i < imax; i++)
            {
            	String attrName = getJoinRef(tbli, i);
        		String referencedAttrName = getJoinAttr(tbli - 1, i);
        		int referencedTable = tbli - 1;
                // create the where clause; the join attributes and the reference attributes that
                // make the join condition
                Projection att1 = new Projection(new Variable("X"+tbli), attrName);
                Projection att2 = new Projection(new Variable("X"+referencedTable), referencedAttrName);
                andCond.add(new EQ(att1,att2));
            }
       }
        
        // retrieve skolems for the new attributes from what was generated in genMappings - this is basically just a way of cloning the existing skolem
        for(int i = 1 ; i <= numNewAttr; i++) {
        	int attPos = i + getTotalNumNormalAttrs() + numOfJoinAttributes;
        	String attName = m.getAttrIds(0, false)[attPos];
        	MappingType m1 = m.getMaps().get(0);
        	SKFunction sk = m.getSkolemFromAtom(m1, false, 0, attPos);

        	vtools.dataModel.expression.SKFunction stSK = 
        			new vtools.dataModel.expression.SKFunction(sk.getSkname());

        	// this works because the keys are always the first attributes 
        	for(int j = 0; j < sk.getVarArray().length; j++) {			
        		String sAttName = m.getAttrId(0, j, false);
        		Projection att = new Projection(new Variable("X"), sAttName);
        		stSK.addArg(att);
        	}

        	sel.add(attName, stSK);
        }

        query.setSelect(sel);
        query.setFrom(from);
        if(andCond.size() > 0)
        	query.setWhere(andCond);
        
        return query;
	}

	@Override
	protected void genCorrespondences() {
		int tOffset = 0;
		
		// create correspondences for free attributes
		for(int i = 0; i < numOfTables; i++) {
			int numAtt = getNumNormalAttrs(i);
			for(int j = 0; j < numAtt; j++)
				addCorr(i, j, 0, tOffset++);
		}
		
		// create correspondences for join attributes
		if (jk == JoinKind.STAR) {
			for(int i = 1; i < numOfTables; i++) {
				int offset = getNumNormalAttrs(i);
				for(int j = 0; j < numOfJoinAttributes; j++)
					addCorr(i, j + offset, 0, tOffset++);
			}
		}
		if (jk == JoinKind.CHAIN) {
			for(int i = 0; i < numOfTables - 1; i++) {
				int offset = getNumNormalAttrs(i);
				for(int j = 0; j < numOfJoinAttributes; j++)
					addCorr(i, j + offset, 0, tOffset++);
			}
		}
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.MERGEADD;
	}
}
