package tresc.benchmark.schemaGen;

import java.util.Random;
import java.util.Vector;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.AND;
import vtools.dataModel.expression.EQ;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;

public class NestingScenarioGenerator extends AbstractScenarioGenerator
{
    private Random _generator;

    private final String _stamp = "NS";
    
    private int countTrgTbl = 0;

    public NestingScenarioGenerator()
    {		;		}

    public void generateScenario(MappingScenario scenario, Configuration configuration)
    {
    	init(configuration, scenario);
        // generate the generator based on the seed
        //long seed = configuration.getScenarioSeeds(Constants.ScenarioName.NESTING.ordinal());
        //_generator = (seed == 0) ? new Random() : new Random(seed);

    	int depth = Utils.getRandomNumberAroundSomething(_generator, nesting, nestingDeviation);

        for (int i = 0, imax = repetitions; i < imax; i++)
        {
            // First create the source element
            String randomName = Modules.nameFactory.getARandomName();
            String elName = randomName + "_" + _stamp + i + "FE";
            SMarkElement se = new SMarkElement(elName, new Set(), null, 0, 0);
            se.setHook(new String( _stamp + i + "FE"));
            source.addSubElement(se);

            // Now generate the target nested structure
            String randomName2 = Modules.nameFactory.getARandomName();
            String elName2 = randomName2 + "_" + _stamp + i + "NE0";
            SMarkElement te = new SMarkElement(elName2, new Set(), null, 0, 0);
            te.setHook(new String( _stamp + i + "NE0"));
            target.addSubElement(te);
            
            SPJQuery subquery = new SPJQuery();
            SelectClauseList pselect = pquery.getSelect();
            pselect.add(elName2, subquery);
            Vector<Projection> whereAttr = new Vector<Projection>(); 
            
            createSubElements(se, te, 0, depth, i, numOfElements, numOfElementsDeviation, keyWidth,
                keyWidthDeviation, 0, 1, subquery, whereAttr);
            
           pquery.setSelect(pselect);
        }

    }

    // At each level, there will be E elements. From these elements , K will make
    // a key atomic elements on which we nest (KE), and N will be nested
    // set elements (NE).Theoretically K + N = E
    // K>=1, N>=1, hence, E>=2. If E< K+N, then E becomes = K + N
    private void createSubElements(Element sourceParent, Element targetParent, int nestingLevel, int maxNesting,
            int repetition, int numOfElements, int numOfElementsDeviation, int keyWidth, int keyWidthDeviation,
            int atomicElNum, int nestElNum, SPJQuery query, Vector<Projection> whereAttr)
    {
        // first we decide the parameters we will have in this case.
        int K = Utils.getRandomNumberAroundSomething(_generator, keyWidth, keyWidthDeviation);
        K = (K < 1) ? 1 : K;
        int E = Utils.getRandomNumberAroundSomething(_generator, numOfElements, numOfElementsDeviation);
        E = (E < (K + 1)) ? (K + 1) : E;
        int N = E - K;
        
        // create the query 
        SelectClauseList sel = query.getSelect();
        // add the from clause
        query.getFrom().add(new Variable("S"+countTrgTbl), new Projection(Path.ROOT, sourceParent.getLabel()));
        
        // create the new vector that contains the attributes 
        // that will appear in the where clause of the subquery of the parent query
        Vector<Projection> newWhereAttr = new Vector<Projection>(); 
        for(int j=0, jmax=whereAttr.size(); j<jmax; j++)
        {
        	newWhereAttr.add(whereAttr.get(j).clone());
        }
        
        // First we create the key elements (aka the atomic elements)
        for (int i = 0; i < K; i++)
        {
            String randomName = Modules.nameFactory.getARandomName();
            String elName = randomName + "_" + _stamp + repetition + "AE" + atomicElNum;
            SMarkElement se = new SMarkElement(elName, Atomic.STRING, null, 0, 0);
            se.setHook(new String(_stamp + repetition + "AE" + atomicElNum));
            sourceParent.addSubElement(se);
            SMarkElement te = new SMarkElement(elName, Atomic.STRING, null, 0, 0);
            te.setHook(new String(_stamp + repetition + "AE" + atomicElNum));
            targetParent.addSubElement(te);
            atomicElNum++;
            
            // add the keys attributes to the select
            Projection att = new Projection(new Variable("S"+countTrgTbl), elName);
            sel.add(elName, att);
            // add the key attributes to the newWhereAttr to be used in the subquery
            newWhereAttr.add(att.clone());
        }

        // create the where clause
        AND andCond = new AND();
        for(int j=0, jmax=whereAttr.size(); j<jmax; j++)
        {
        	String attname = whereAttr.get(j).getLabel();
        	Projection att1 = new Projection(new Variable("S"+countTrgTbl), attname);
        	Projection att2 = whereAttr.get(j).clone();
        	andCond.add( new EQ(att1,att2));
        }
        if (whereAttr.size() != 0) query.setWhere(andCond);
     
        countTrgTbl ++;
        
        // and now the nested cases
        for (int i = 0; ((nestingLevel < maxNesting) && (i < N)); i++)
        {
            String randomName = Modules.nameFactory.getARandomName();
            String elName = randomName + "_" + _stamp + repetition + "NE" + nestElNum;
            SMarkElement newTargetParent = new SMarkElement(elName, new Set(), null, 0, 0);
            newTargetParent.setHook(new String(_stamp + repetition + "NE" + nestElNum));
            targetParent.addSubElement(newTargetParent);
            nestElNum++;
            // but since we have created nested SMarkElements (i.e., set of
            // records).We have to populate them with attributes . For that reason we
            // call recursively create the subquery representing the target table
            // and add it to the the select of the big query
            SPJQuery subquery = new SPJQuery();
            sel.add(elName, subquery);
            
            createSubElements(sourceParent, newTargetParent, nestingLevel + 1, maxNesting, repetition,
                numOfElements, numOfElementsDeviation, keyWidth, keyWidthDeviation, atomicElNum, nestElNum, subquery, newWhereAttr);
        }
        // add the select to the big query
        query.setSelect(sel);

    }



	@Override
	protected void genSourceRels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void genTargetRels() {
		// TODO Auto-generated method stub
		
	}

	
	
	@Override
	public ScenarioName getScenType() {
		return ScenarioName.NESTING;
	}

	@Override
	protected void genCorrespondences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void genMappings() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void genTransformations() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
