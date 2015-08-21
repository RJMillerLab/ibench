package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.List;

import org.vagabond.benchmark.model.TrampModelFactory.FuncParamType;
import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.MappingType;

import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.ConstantAtomicValue;
import vtools.dataModel.expression.Function;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Query;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.values.IntegerValue;

public class ValueManagementScenarioGenerator extends AbstractScenarioGenerator
{
	//private int numOfSubElements;
	private int X;
	private int Y;
	private int[] splits;
	private int[] merges;
	private int stLen;
	
    public ValueManagementScenarioGenerator()
    {
        ;
    }


    /*public void generateScenario(MappingScenario scenario, Configuration configuration)
    {
    	init(configuration, scenario);
        
        for (int i = 0, imax = repetitions; i < imax; i++)
        {
            createSubElements(source, target, numOfSubElements, i, numOfParams, numOfParamsDeviation, pquery);
        }

    }*/

    @Override
    protected void initPartialMapping () {
    	super.initPartialMapping();
        //numOfSubElements = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
        X = ((numOfElements) / (numOfParams + 1)) + 1;
        Y = numOfElements - X;
        // make sure we have at least one case
        Y = (Y == 0) ? 1 : Y;
        
        stLen = configuration.getMaxStringLength();
    }
    
    // Assume that we have E elements in each table (which means that we also want E
    // elements to create in the target table). From them (the source), let X be
    // those that will be slit in the target (to A pieces). The remaining E-X elements in the
    // source table will be combined to generate elements in the target. If A is
    // the number of arguments, the E-X elements will generate (E-X)/A elements in
    // the target. But the target table has also E elenments which means that
    // (X*A)+(E-X)/A=E. Solving this equation gives us that X = E/(A+1)
    // In the following code, because I am playing with integers, and after all
    // everything is approximate, I take X to be (E/(A+1))+1
    /*private void createSubElements(Element sourceParent, Element targetParent, int numOfElements, int repetition,
            int numOfArgs, int numOfArgsDeviation, SPJQuery pquery)
    {
        // first create the name of the two tables
        String randomName = Modules.nameFactory.getARandomName() + "_" + getStamp() + repetition;
        SMarkElement srcTbl = new SMarkElement(randomName, new Set(), null, 0, 0);
        srcTbl.setHook(new String(getStamp() + repetition));
        sourceParent.addSubElement(srcTbl);
        String randomNameTrg = randomName;
        SMarkElement trgTbl = new SMarkElement(randomName + "Target", new Set(), null, 0, 0);
        trgTbl.setHook(new String(getStamp() + repetition));
        targetParent.addSubElement(trgTbl);
        
        // create the intermediate query
        SPJQuery query = new SPJQuery();
        // create the From Clause of the query
        query.getFrom().add(new Variable("X"), new Projection(Path.ROOT, randomName));
        
        // create the elements that are going to split in the target
        int X = ((numOfElements) / (numOfArgs + 1)) + 1;
        int fcount = 0;
        SelectClauseList select = query.getSelect();
        for (int i = 0, imax = X; i < imax; i++)
        {
            randomName = Modules.nameFactory.getARandomName() + "_" + getStamp() + repetition + "AE" + i;
            SMarkElement srcElem = new SMarkElement(randomName, Atomic.STRING, null, 0, 0);
            srcElem.setHook(new String(getStamp() + repetition + "AE" + i));
            srcTbl.addSubElement(srcElem);
            // decide in how many pieces you will split it
            int pieces = Utils.getRandomNumberAroundSomething(_generator, numOfArgs, numOfArgsDeviation);
            // and add that many pieces in the target table (the name will be
            // the same as the name in the src table, but suffixed with
            // Part1, Part2, Part3, etc.
            for (int k = 0; k < pieces; k++)
            {
                SMarkElement trgElem = new SMarkElement(randomName + "Part" + k, Atomic.STRING, null, 0, 0);
                trgElem.setHook(new String(getStamp() + repetition + "AE" + i + "Part" + k));
                trgTbl.addSubElement(trgElem);
                // add the attributes to the select clause of the query
                Function f = new Function("F"+fcount);
                fcount++;
                Projection att = new Projection(new Variable("X"),randomName);
                f.addArg(att);
                select.add(randomName + "Part" + k, f);
            }
        }

        // Now we do the other way around. We generate the elements that will be
        // merged in the target
        int Y = numOfElements - X;
        // make sure we have at least one case
        Y = (Y == 0) ? 1 : Y;
        for (int i = 0, imax = Y; i < imax; i++)
        {
            randomName = Modules.nameFactory.getARandomName() + "_" + getStamp() + repetition + "AE" + (i + X);
            SMarkElement trgElem = new SMarkElement(randomName, Atomic.STRING, null, 0, 0);
            trgElem.setHook(new String( getStamp() + repetition + "AE" + (i + X)));
            trgTbl.addSubElement(trgElem);
            // decide from how many pieces you will compose it
            int pieces = Utils.getRandomNumberAroundSomething(_generator, numOfArgs, numOfArgsDeviation);
            // and add that many pieces in the target table (the name will be
            // the same as the name in the src table, but suffixed with
            // Part1, Part2, Part3, etc.
            Function f = new Function("Concat");
            for (int k = 0; k < pieces; k++)
            {
                SMarkElement srcElem = new SMarkElement(randomName + "Part" + k, Atomic.STRING, null, 0, 0);
                srcElem.setHook(new String(getStamp() + repetition + "AE" + (i + X)+ "Part" + k));
                srcTbl.addSubElement(srcElem);
                // add the attributes to the select clause of the query
                Projection att = new Projection(new Variable("X"), randomName + "Part" + k);
                f.addArg(att);
            }
            select.add(randomName, f);
        }
        
        // add the subquery to the final transformation query
        query.setSelect(select);
        SelectClauseList pselect = pquery.getSelect();
        pselect.add(randomNameTrg, query);
        pquery.setSelect(pselect);
    }*/



	@Override
	protected void genSourceRels() {
		String srcName = randomRelName(0);
		List<String> attrs = new ArrayList<String> ();
		splits = new int[X];
		merges = new int[Y];
		
		// create attributes to be split and determine in how many pieces to split
		for(int i = 0; i < X; i++) {
			attrs.add(randomAttrName(0, i));
			splits[i] = Utils.getRandomNumberAroundSomething(_generator, numOfParams, 
					numOfParamsDeviation);
		}
		
		int offset = X;
		for(int i = 0; i < Y; i++) {
			merges[i] = Utils.getRandomNumberAroundSomething(_generator, numOfParams, 
					numOfParamsDeviation);
			for(int j = 0; j < merges[i]; j++)
				attrs.add(randomAttrName(0, offset++));
		}
		
		fac.addRelation(getRelHook(0), srcName, attrs.toArray(new String[] {}), true);
	}


	@Override
	protected void genTargetRels() {
		String trgName = randomRelName(0);
		int numAttr = CollectionUtils.sum(splits) + Y;
		String[] attrs = new String[numAttr];
		String[] sAttrs = m.getAttrIds(0, true);
		
		int sOffset = 0, tOffset = 0;
		
		for(int i = 0; i < X; i++) {
			for(int j = 0; j < splits[i]; j++)
				attrs[tOffset++] = sAttrs[sOffset] + "_part_" + j;
			sOffset++;
		}
		
		for(int i = 0; i < Y; i++)
			attrs[tOffset++] = randomAttrName(0, tOffset);
		
		fac.addRelation(getRelHook(0), trgName, attrs, false);
	}

	@Override
	protected void genMappings() throws Exception {
		MappingType m1 = fac.addMapping(m.getCorrs());
		int offset, offsetS = 0;
		
		int numAttr = m.getNumRelAttr(0, true);
		fac.addForeachAtom(m1, 0, fac.getFreshVars(0, numAttr));
		
		/* create exists atom */
		fac.addEmptyExistsAtom(m1, 0);
		
		/* add expressions for splits */
		offset = 0;
		for(int i = 0; i < X; i++) {
			int subStrLen = (stLen < splits[offsetS]) ? 1 : stLen / splits[offsetS];
        	int stOffset = 0;
        	
			for(int j = 0; j < splits[i]; j++, offset++) {
				fac.addFuncToExistsAtom(m1, 0, "extract", 
						CollectionUtils.concat(fac.getFreshVars(offset, 1), 
								stOffset +"", subStrLen +""),
								new FuncParamType[] {FuncParamType.Var, 
										FuncParamType.Const, 
										FuncParamType.Const});
				 stOffset += subStrLen;
	             if (stOffset + subStrLen > stLen)
	             	stOffset = 0;
			}
		}
		
		/* add expressions for merge */
		for(int i = 0; i < Y; i++) {
			fac.addFuncToExistsAtom(m1, 0, "concat", fac.getFreshVars(offset, merges[i]));
			offset += merges[i];
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

//		fac.addTransformation(q.toTrampString(m.getMapIds()), m.getMapIds(), 
//				creates);
	}
	
	private SPJQuery genQueries() {
//		String targetName = m.getRelName(0, false);
		String sourceName = m.getRelName(0, true);
		String[] sAttrs = m.getAttrIds(0, true);
		String[] tAttrs = m.getAttrIds(0, false);
		int stLen = configuration.getMaxStringLength();
		
	     // create the intermediate query
        SPJQuery query = new SPJQuery();
        // create the From Clause of the query
        query.getFrom().add(new Variable("X"), new Projection(Path.ROOT, sourceName));
        
        // create the elements that are going to split in the target
        SelectClauseList select = query.getSelect();
        int offsetS = 0, offsetT = 0;
        
        for (; offsetS < X; offsetS++)
        {
        	int subStrLen = (stLen < splits[offsetS]) ? 1 : stLen / splits[offsetS];
        	int stOffset = 0;
        	
            for (int k = 0; k < splits[offsetS]; k++) {              
            	// add the attributes to the select clause of the query
                Function f = new Function("extract");
                Projection att = new Projection(new Variable("X"),sAttrs[offsetS]);
                f.addArg(att);
                f.addArg(new ConstantAtomicValue(new IntegerValue(stOffset)));
                f.addArg(new ConstantAtomicValue(new IntegerValue(subStrLen)));
                
                select.add(tAttrs[offsetT++], f);
                
                stOffset += subStrLen;
                if (stOffset + subStrLen > stLen)
                	stOffset = 0;
            }
        }

        // Now we do the other way around. We generate the elements that will be
        // merged in the target
        for (int i = 0, imax = Y; i < imax; i++)
        {
            // and add that many pieces in the target table (the name will be
            // the same as the name in the src table, but suffixed with
            // Part1, Part2, Part3, etc.
            Function f = new Function("Concat");
            for (int k = 0; k < merges[i]; k++)
            {
                // add the attributes to the select clause of the query
                Projection att = new Projection(new Variable("X"), sAttrs[offsetS++]);
                f.addArg(att);
            }
            select.add(tAttrs[offsetT++], f);
        }
        
        // add the subquery to the final transformation query
        query.setSelect(select);
//        SelectClauseList pselect = pquery.getSelect();
//        pselect.add(targetName, query);
//        pquery.setSelect(pselect);
        
        return query;
	}


	@Override
	protected void genCorrespondences() {
		//TODO
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.VALUEMANAGEMENT;
	}
}
