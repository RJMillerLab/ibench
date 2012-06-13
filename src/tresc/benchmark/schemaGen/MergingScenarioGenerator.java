package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.Arrays;
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
import tresc.benchmark.Constants.JoinKind;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.AND;
import vtools.dataModel.expression.EQ;
import vtools.dataModel.expression.ForeignKey;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;

public class MergingScenarioGenerator extends ScenarioGenerator
{

    private static int _currAttributeIndex = 0; // this determines the letter used for the attribute in the mapping

	private int numOfTables;
	private int numOfJoinAttributes;
	private JoinKind jk;
	private int[] numOfAttributes;

	private int factor;

	private String[] joinAttrs;
    
    public MergingScenarioGenerator()
    {		;		}

//    public void generateScenario(MappingScenario scenario, Configuration configuration)
//    {
//    	init(configuration, scenario);
//    	
//        // generate the generator based on the seed
//        //long seed = configuration.getScenarioSeeds(Constants.ScenarioName.MERGING.ordinal());
//        //_generator = (seed == 0) ? new Random() : new Random(seed);
//
//        // how many tables to have
//        // whether we do star or chain joins
//        // int joinKindDeviation =
//        // configuration.getDeviation(Constants.ParameterName.JoinKind);
//        // how many attributes to be used in the joins.
//        
//        for (int i = 0, imax = repetitions; i < imax; i++)
//        {
//        	initPartialMapping();
//            SPJQuery generatedQuery = new SPJQuery();
//            
//            SMarkElement[] srcRels = new SMarkElement[numOfAttributes.length];
//            SMarkElement tgtRel = createSubElements(source, target, numOfAttributes, numOfJoinAttributes, jk, i, pquery, generatedQuery, srcRels);
//            
//            setScenario(scenario, generatedQuery, tgtRel, srcRels);
//            log.debug(scenario);
//        }
//   }
//    
    @Override
    protected void initPartialMapping() {
    	super.initPartialMapping();
    	
        numOfTables = Utils.getRandomNumberAroundSomething(_generator, numOfSetElements,
            numOfSetElementsDeviation);
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
            numOfAttributes[k] = (tmpInt < (2 * numOfJoinAttributes + 1)) ? (2 * numOfJoinAttributes + 1)
                    : tmpInt;
        }
        
        factor = (jk == JoinKind.STAR) ? 1 : 2;
    }

    private Character getAttrSymbol(String attrName) {
    	if (attrInSymbolList(attrName))
    		return attrMap.get(attrName);
    	Character letter = _attributes.charAt(_currAttributeIndex++);
    	attrMap.put(attrName, letter);
    	return letter;
    }
    
    private boolean attrInSymbolList(String attrName) {
    	return attrMap.containsKey(attrName);
    }
    
    private void resetAttrSymbol() {
    	_currAttributeIndex = 0;
    	attrMap.clear();
    }

	private void setScenario(MappingScenario scenario, SPJQuery generatedQuery, SMarkElement tgtRel, SMarkElement[] srcRels) {
		SelectClauseList gselect = generatedQuery.getSelect();
		HashMap<String, List<Character>> sourceAttrs = new HashMap<String, List<Character>>();
		HashMap<String, List<Character>> targetAttrs = new HashMap<String, List<Character>>();
		
		String mKey = scenario.getNextMid();

		ArrayList<String> corrsList = new ArrayList<String>();
		
		ArrayList<Character> targetRelAttrs = new ArrayList<Character>();
		HashMap<String, Character> targetAttrSymbols = new HashMap<String , Character>();

		for (int j = 0; j < tgtRel.size(); j++) {
			Element attr = tgtRel.getSubElement(j);
			String attrName = attr.getLabel();
			Character symbol = getAttrSymbol(attrName);
			targetAttrSymbols.put(attrName, symbol);
			targetRelAttrs.add(symbol);
		}
    	String targetName = tgtRel.getLabel();
		targetAttrs.put(targetName, targetRelAttrs);

		SPJQuery e = (SPJQuery)(gselect.getTerm(0));
    	String[] whereArray = e.getWhere().toString().split("AND");
    	
    	// 2-way hashmap of the where clauses
    	HashMap<String, String> whereExprs0 = new HashMap<String, String>();
    	HashMap<String, String> whereExprs1 = new HashMap<String, String>();
    	
    	for (String wherecl : whereArray) {
    		String[] expr = wherecl.split("=");
    		String leftExpr = expr[0].replaceAll("^\\$(.*)/", "");
    		String rightExpr = expr[1].replaceAll("^\\$(.*)/", "");
    		whereExprs0.put(leftExpr, rightExpr);
    		whereExprs1.put(rightExpr, leftExpr);
    	}
    	
		String tKey = scenario.getNextTid();
		for (int i = 0; i < srcRels.length; i++) {
			List<Character> sourceRelAttrs = new ArrayList<Character>();
			String sourceName = srcRels[i].getLabel();

    		SMarkElement srcRel = srcRels[i];
        	for (int j = 0; j < srcRels[i].size(); j++) {
        		String attr = srcRel.getSubElement(j).getLabel();
        		
    			String sourceRelAttr = sourceName + "." + attr;
    			String targetRelAttr = targetName + "." + attr;
        		if (targetAttrSymbols.containsKey(attr)) {
        			sourceRelAttrs.add(getAttrSymbol(attr));
        		} else {
        			String refAttrName = null;
        			if (whereExprs0.containsKey(attr)) 
        				refAttrName = whereExprs0.get(attr);
        			else if (whereExprs1.containsKey(attr))
        				refAttrName = whereExprs1.get(attr);
        			else
        				refAttrName = attr;
        			targetRelAttr = targetName + "." + refAttrName;
        			sourceRelAttrs.add(getAttrSymbol(refAttrName));
        		}
    			String cKey = scenario.getNextCid();
    			String cVal = sourceRelAttr + "=" + targetRelAttr;
    			scenario.putCorrespondences(cKey, cVal);
    			corrsList.add(cKey);
        	}
        	sourceAttrs.put(sourceName, sourceRelAttrs);
    			
		}
		ArrayList<String> mList = new ArrayList<String>();
		mList.add(mKey);
		scenario.putTransformation2Mappings(tKey, mList);
		scenario.putTransformationCode(tKey, getQueryString(e, mKey));
		scenario.putTransformationRelName(tKey, targetName);
        	
		scenario.putMappings2Correspondences(mKey, corrsList);
        scenario.putMappings2Sources(mKey, sourceAttrs);
        scenario.putMappings2Targets(mKey, targetAttrs);

		resetAttrSymbol();
	}
	
	private String getQueryString(SPJQuery origQ, String mKey) {
		String retVal = origQ.toString();
		FromClauseList from = origQ.getFrom();
		for (int i = 0; i < from.size(); i++) {
			String key = from.getKey(i).toString();
			String relAlias = key.replace("$", "");
			retVal = retVal.replace(key+"/", relAlias+".");
			retVal = retVal.replace("${" + i + "}", mKey);
		}
		retVal = retVal.replaceAll("/", "");
		
		return retVal;
	}

    private SMarkElement createSubElements(Schema source, Schema target, int[] numOfAttributes,int numOfJoinAttributes, 
    		    JoinKind jk, int repetition, SPJQuery pquery, SPJQuery generatedQuery, SMarkElement[] sources)
    {
    	// the local query which will be added to the final pquery
    	SPJQuery query = new SPJQuery();
    	SelectClauseList sel = query.getSelect();
    	FromClauseList from = query.getFrom();
    	
        // create the target table
        String nameT = Modules.nameFactory.getARandomName() + "_" + getStamp() + repetition;
        SMarkElement elTrg = new SMarkElement(nameT, new Set(), null, 0, 0);
        elTrg.setHook(new String(getStamp() + repetition));
        target.addSubElement(elTrg);
    	
    	// auxiliary variable. When we are in a chain join we use double the
        // number of join attributes because we join with the next and the
        // previous table in the join chain.
   

        // first we create the source tables and their attributes 
        // that do not participate in the joins.
        SMarkElement[] tables = sources;
        //SMarkElement[] tables = new SMarkElement[numOfAttributes.length];
        for (int i = 0, imax = tables.length; i < imax; i++)
        {
            String namePrefix = Modules.nameFactory.getARandomName();
            String name = namePrefix + "_" + getStamp() + repetition + "Comp" + i;
            SMarkElement el = new SMarkElement(name, new Set(), null, 0, 0);
            el.setHook(new String(getStamp() + repetition + "Comp" + i));
            source.addSubElement(el);
            tables[i] = el;
            // add the table to the from clause
            from.add(new Variable("X"+i), new Projection(Path.ROOT,name));

            // create the non join attributes of the specific table/fragment/component
            int numOfNonJoinAttr = numOfAttributes[i] - (factor * numOfJoinAttributes);
            for (int k = 0, kmax = numOfNonJoinAttr; k < kmax; k++)
            {
                String tmpName = Modules.nameFactory.getARandomName();
                String attrName = tmpName + "_" + getStamp() + repetition + "Comp" + i + "Attr" + k;
                SMarkElement attrEl = new SMarkElement(attrName, Atomic.STRING, null, 0, 0);
                attrEl.setHook(new String(getStamp() + repetition + "Comp" + i + "Attr" + k));
                el.addSubElement(attrEl);
                // add the non-join attribute to the target table
                SMarkElement attrElT = new SMarkElement(attrName, Atomic.STRING, null, 0,0);
                attrElT.setHook(new String(getStamp() + repetition + "Comp" + i + "Attr" + k));
                elTrg.addSubElement(attrElT);
                // add the non-join attribute to the select clause
                Projection att = new Projection(new Variable("X"+i), attrName);
                sel.add(attrName, att);
            }
            
            generatedQuery.addSource(name);
        }

        // The way the algorithm works is by generating the following situation
        // where the left column describes the case of a star join
        // and the right one the case of a chain join.
        // 
        // XXXXComp0JoinAttr0Ref1______________XXXXComp0JoinAttr0
        // XXXXComp0JoinAttr1Ref1______________XXXXComp0JoinAttr1
        // XXXXComp0JoinAttr0Ref2________________________________
        // XXXXComp0JoinAttr0Ref2________________________________
        //
        //
        // XXXXComp1JoinAttr0__________________XXXXComp1JoinAttr0Ref0
        // XXXXComp1JoinAttr1__________________XXXXComp1JoinAttr1Ref0
        // ____________________________________XXXXComp1JoinAttr0
        // ____________________________________XXXXComp1JoinAttr1
        //                              
        //
        // XXXXComp2JoinAttr0__________________XXXXComp2JoinAttr0Ref1
        // XXXXComp2JoinAttr1__________________XXXXComp2JoinAttr1Ref1
        // ____________________________________XXXXComp1JoinAttr0
        // ____________________________________XXXXComp1JoinAttr1
        // .....
        //

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
                String attrName = joinAttrNames[i] + "_" + getStamp() + repetition + "Comp" + tbli + "JoinAttr" + i;
                SMarkElement attrEl = new SMarkElement(attrName, Atomic.STRING, null, 0,0);
                attrEl.setHook(new String(getStamp() + repetition + "Comp" + tbli + "JoinAttr" + i));
                tables[tbli].addSubElement(attrEl);
                // add the attribute to the target table
                SMarkElement attrElT = new SMarkElement(attrName, Atomic.STRING, null, 0,0);
                attrElT.setHook(new String(getStamp() + repetition + "Comp" + tbli + "JoinAttr" + i));
                elTrg.addSubElement(attrElT);
                // add the join attribute to the select clause
                Projection att = new Projection(new Variable("X"+tbli), attrName);
                sel.add(attrName, att);
            }
        }
        
        // and now we add the reference (Foreign key if you like the term)
        // attributes in the first table. the foreign keys will point to 
        // all the join attributes from the other tables.
        // create the join conditions in the where clause and the fkeys.
        int referencedTable = 0;
        AND andCond = new AND();
        andCond.toString();
        for (int tbli = 1, tblimax = numOfAttributes.length; ((tbli < tblimax) && (jk == JoinKind.STAR)); tbli++)
        {
        	referencedTable = tbli;
        	Variable varKey1 = new Variable("F");
            Variable varKey2 = new Variable("K");
            ForeignKey fKeySrc = new ForeignKey();
            fKeySrc.addLeftTerm(varKey1.clone(), new Projection(Path.ROOT,tables[0].getLabel()));
            fKeySrc.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,tables[referencedTable].getLabel()));
        	
        	for (int i = 0, imax = numOfJoinAttributes; i < imax; i++)
        	{
        		String attrName = joinAttrNames[i] + "_" + getStamp() + repetition + "Comp" + tbli + 
        		                    "JoinAttr" + i + "Ref" + referencedTable;
                SMarkElement attrEl = new SMarkElement(attrName, Atomic.STRING, null, 0,0);
                attrEl.setHook(new String(getStamp() + repetition + "Comp" + tbli + "JoinAttr" + i + 
                "Ref" + referencedTable));
                tables[0].addSubElement(attrEl);
                // we add attributes to the source foreign key constraint
                String referencedAttrName=joinAttrNames[i] + "_" + getStamp() + repetition + "Comp" + 
                                            referencedTable + "JoinAttr" + i;
                fKeySrc.addFKeyAttr(new Projection(varKey2.clone(),referencedAttrName), 
                                    new Projection(varKey1.clone(),attrName));
                // create the where clause; the join attributes and the reference attributes that
                // make the join condition
                Projection att1 = new Projection(new Variable("X"+0), attrName);
                Projection att2 = new Projection(new Variable("X"+referencedTable), referencedAttrName);
                andCond.add(new EQ(att1,att2));
        	}
        	source.addConstraint(fKeySrc);
        	source.addConstraint(fKeySrc); // this just makes it consistent with other cases so the printing does not miss one.
        }	

        // ********************************************************************
        // for the case of a CHAIN join, create the join attributes for all the
        // tables (apart from the last one of course since none is referencing
        // that).also, all the join attributes will be added to the target table.
        for (int tbli = 0, tblimax = numOfAttributes.length; ((tbli < (tblimax - 1)) && (jk == JoinKind.CHAIN)); tbli++)
        {
            for (int i = 0, imax = numOfJoinAttributes; i < imax; i++)
            {
                String attrName = joinAttrNames[i] + "_" + getStamp() + repetition + "Comp" + tbli + "JoinAttr" + i;
                SMarkElement attrEl = new SMarkElement(attrName, Atomic.STRING, null, 0,0);
                attrEl.setHook(new String(getStamp() + repetition + "Comp" + tbli + "JoinAttr" + i));
                tables[tbli].addSubElement(attrEl);
                // add the attribute to the target table
                SMarkElement attrElT = new SMarkElement(attrName, Atomic.STRING, null, 0,0);
                attrElT.setHook(new String(getStamp() + repetition + "Comp" + tbli + "JoinAttr" + i));
                elTrg.addSubElement(attrElT);
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
            referencedTable = tbli - 1;
            Variable varKey1 = new Variable("F");
            Variable varKey2 = new Variable("K");
            ForeignKey fKeySrc = new ForeignKey();
            fKeySrc.addLeftTerm(varKey1.clone(), new Projection(Path.ROOT,tables[tbli].getLabel()));
            fKeySrc.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,tables[referencedTable].getLabel()));
           
            for (int i = 0, imax = numOfJoinAttributes; i < imax; i++)
            {
                String attrName = joinAttrNames[i] + "_" + getStamp() + repetition + "Comp" + tbli + "JoinAttr" + i + 
                						"Ref" + referencedTable;
                SMarkElement attrEl = new SMarkElement(attrName, Atomic.STRING, null, 0, 0);
                attrEl.setHook(new String(getStamp() + repetition + "Comp" + tbli + "JoinAttr" + i + 
                                        "Ref" + referencedTable));
                tables[tbli].addSubElement(attrEl);
                // we add attributes to the source foreign key constraint
                String referencedAttrName=joinAttrNames[i] + "_" + getStamp() + repetition + "Comp" + referencedTable + "JoinAttr" + i;
                fKeySrc.addFKeyAttr(new Projection(varKey2.clone(),referencedAttrName), 
                                    new Projection(varKey1.clone(),attrName));               
                // create the where clause; the join attributes and the reference attributes that
                // make the join condition
                Projection att1 = new Projection(new Variable("X"+tbli), attrName);
                Projection att2 = new Projection(new Variable("X"+referencedTable), referencedAttrName);
                andCond.add(new EQ(att1,att2));
            }
            source.addConstraint(fKeySrc);
        	source.addConstraint(fKeySrc); // this just makes it consistent with other cases so the printing does not miss one.
       }

        query.setSelect(sel);
        query.setFrom(from);
        if(andCond.size() > 0)
        	query.setWhere(andCond);
        SelectClauseList pselect = pquery.getSelect();
        SelectClauseList gselect = generatedQuery.getSelect();
        pselect.add(nameT, query);
        gselect.add(nameT, query);
        pquery.setSelect(pselect);
        generatedQuery.setSelect(gselect);
        return elTrg;
    }

    // The way the algorithm works is by generating the following situation
    // where the left column describes the case of a star join
    // and the right one the case of a chain join.
    // 
    // XXXXComp0JoinAttr0Ref1______________XXXXComp0JoinAttr0
    // XXXXComp0JoinAttr1Ref1______________XXXXComp0JoinAttr1
    // XXXXComp0JoinAttr0Ref2________________________________
    // XXXXComp0JoinAttr0Ref2________________________________
    //
    //
    // XXXXComp1JoinAttr0__________________XXXXComp1JoinAttr0Ref0
    // XXXXComp1JoinAttr1__________________XXXXComp1JoinAttr1Ref0
    // ____________________________________XXXXComp1JoinAttr0
    // ____________________________________XXXXComp1JoinAttr1
    //                              
    //
    // XXXXComp2JoinAttr0__________________XXXXComp2JoinAttr0Ref1
    // XXXXComp2JoinAttr1__________________XXXXComp2JoinAttr1Ref1
    // ____________________________________XXXXComp1JoinAttr0
    // ____________________________________XXXXComp1JoinAttr1
    // .....
    //
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
			int numOfNonJoinAttr = numOfAttributes[i] - (factor * numOfJoinAttributes);
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
	
	@Override
	protected void genTargetRels() {
		String targetName = randomRelName(0);
		List<String> attrs = new ArrayList<String> ();
		
		if (jk == JoinKind.STAR) {
			for(int i = 0; i < numOfTables; i++) {
				int numAtt = numOfAttributes[i];
				if (i == 0)
					numAtt -= (numOfTables - 1) * numOfJoinAttributes;
				for(int j = 0; j < numAtt; j++)
					attrs.add(m.getAttrId(i, j, true));
			}
		}
		if (jk == JoinKind.CHAIN) {
			for(int i = 0; i < numOfTables; i++) {
				int numAtt = numOfAttributes[i];
				if (i != 0)
					numAtt -= numOfJoinAttributes;
				for(int j = 0; j < numAtt; j++)
					attrs.add(m.getAttrId(i, j, true));				
			}
		}
		
		fac.addRelation(getRelHook(0), targetName, attrs.toArray(new String[] {}), false);
	}
	
	@Override
	protected void genMappings() throws Exception {
		MappingType m1 = fac.addMapping(m.getCorrs());
		String[][] vars = new String[numOfTables][];
		String[] targetVars = new String[m.getNumRelAttr(0, false)];
		int offset;
		
		// add foreach atoms for the the source fragments
		offset = m.getNumRelAttr(0, true);
		vars[0] = fac.getFreshVars(0, offset);
		fac.addForeachAtom(m1, 0, vars[0]);
		
		// each table get fresh vars for its free and join attributes
		// the fk vars are takes from the join attributes they reference
		for(int i = 1; i < numOfTables; i++) {
			int numFreshVars = numOfAttributes[i] - numOfJoinAttributes;
			String[] freeVars = fac.getFreshVars(offset, numFreshVars);
			String[] fkVars = null;
			
			// get vars for the referenced attributes from the first table 
			if (jk == JoinKind.STAR) {
				int from = numOfAttributes[0] - ((numOfTables - i) * numOfJoinAttributes);
				fkVars = Arrays.copyOfRange(vars[0], from, from + numOfJoinAttributes);
			}
			// get vars for the referenced attributes from the previous table
			if (jk == JoinKind.CHAIN) {
				fkVars = Arrays.copyOfRange(vars[i - 1], numFreshVars, 
						numFreshVars + numOfJoinAttributes);
			}
			vars[i] = CollectionUtils.concat(freeVars, fkVars);
			
			fac.addForeachAtom(m1, i, vars[i]);
		}
		
		// generate an array of vars for the target
		// first we add vars for the free attributes of all table
		// then we add the join attribute vars
		offset = 0;
		for(int i = 0; i < numOfTables; i++) {
			int numVars = vars[i].length;
			
			if (jk == JoinKind.STAR) {
				if (i == 0)
					numVars -= (numOfTables - 1) * numOfJoinAttributes;
				else
					numVars -= numOfJoinAttributes;
			}
			if (jk == JoinKind.CHAIN) {
				if (i > 0 && i != numOfTables - 1)
					numVars -= numOfJoinAttributes * 2;
				if (i == numOfTables)
					numVars -= numOfJoinAttributes;
			}
			
			System.arraycopy(vars[i], 0, targetVars, offset, numVars);
			offset += numVars;
		}
		
		// star join, add join attribute vars from first table
		if (jk == JoinKind.STAR) {
			int start = numOfAttributes[0] - ((numOfTables - 1) * numOfJoinAttributes);
			for(int i = 1; i < numOfTables; i++) {
				System.arraycopy(vars[0], start, targetVars, offset, numOfJoinAttributes);
				offset += numOfJoinAttributes;
				start += numOfJoinAttributes;
			}
		}
		// chain join, take join attribute vars from each table
		if (jk == JoinKind.CHAIN) {
			for(int i = 1; i < numOfTables; i++) {
				int start = numOfAttributes[i] - numOfJoinAttributes;
				System.arraycopy(vars[i], start, targetVars, offset, numOfJoinAttributes);
				offset += numOfJoinAttributes;
			}
		}
		
		fac.addExistsAtom(m1, 0, targetVars);
	}
	
	@Override
	protected void genTransformations() throws Exception {
		SPJQuery q;
		String creates = m.getRelName(0, false);
		String mapping = m.getMapIds()[0];
		
		q = genQueries();
		fac.addTransformation(q.toTrampStringOneMap(mapping), mapping, creates);
	}
	
	private SPJQuery genQueries() {
	  	String nameT = m.getRelName(0, false);
		SPJQuery query = new SPJQuery();
    	SelectClauseList sel = query.getSelect();
    	FromClauseList from = query.getFrom();
    	
       // first we create the source tables and their attributes 
        // that do not participate in the joins.
        for (int i = 0, imax = numOfTables; i < imax; i++) {
            // add the table to the from clause
            from.add(new Variable("X"+i), new Projection(Path.ROOT, 
            		m.getRelName(i, true)));

            // create the non join attributes of the specific table/fragment/component
            int numOfNonJoinAttr = numOfAttributes[i] - (factor * numOfJoinAttributes);
            
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

        query.setSelect(sel);
        query.setFrom(from);
        if(andCond.size() > 0)
        	query.setWhere(andCond);
        SelectClauseList pselect = pquery.getSelect();
        pselect.add(nameT, query);
        pquery.setSelect(pselect);
        
        return query;
	}

	@Override
	protected void genCorrespondences() {
		int tOffset = 0;
		if (jk == JoinKind.STAR) {
			for(int i = 0; i < numOfTables; i++) {
				int numAtt = numOfAttributes[i];
				if (i == 0)
					numAtt -= (numOfTables - 1) * numOfJoinAttributes;
				for(int j = 0; j < numAtt; j++)
					addCorr(i, j, 0, tOffset++);
			}
		}
		if (jk == JoinKind.CHAIN) {
			for(int i = 0; i < numOfTables; i++) {
				int numAtt = numOfAttributes[i];
				if (i != 0)
					numAtt -= numOfJoinAttributes;
				for(int j = 0; j < numAtt; j++)
					addCorr(i, j, 0, tOffset++);				
			}
		}
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.MERGING;
	}
}
