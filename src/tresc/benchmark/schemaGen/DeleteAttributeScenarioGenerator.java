package tresc.benchmark.schemaGen;

import java.util.Random;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.Function;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;

public class DeleteAttributeScenarioGenerator extends AbstractScenarioGenerator
{

    public DeleteAttributeScenarioGenerator()
    {
        ;
    }

    public void generateScenario(MappingScenario scenario, Configuration configuration)
    {
    	init(configuration, scenario);
        SPJQuery pquery = scenario.getTransformation();

        for (int i = 0, imax = repetitions; i < imax; i++)
        {
            // decide how many attributes will the source table have
            int numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
                numOfElementsDeviation);
            
            // if trying to delete more attributes than those that exist, then just delete one element
            if (numDelAttr > numOfSrcTblAttr)
            	numDelAttr = 1;
            
            // if going to delete all elements that were in the source table then make sure that there is at least one extra element in the source table
            if (numDelAttr == numOfSrcTblAttr)
            	numOfSrcTblAttr++;
            
            createSubElements(source, target, numOfSrcTblAttr, numDelAttr, i, pquery);
        }
    }

    /**
     * This is the main function. It generates a table in the source, a number
     * of tables in the target and a respective number of queries.
     */
    private void createSubElements(Schema source, Schema target, int numOfSrcTblAttr,
            int numDelAttr, int repetition, SPJQuery pquery)
    {
    	String coding = getStamp() + repetition;
    	int curTbl = repetition;
    	
        // First create the source table
        String sourceRelName = Modules.nameFactory.getARandomName();
        sourceRelName = sourceRelName + "_" + coding;
        SMarkElement srcRel = new SMarkElement(sourceRelName, new Set(), null, 0, 0);
        srcRel.setHook(new String(coding));
        source.addSubElement(srcRel);
        // and populate that table with elements. The array attNames, keeps the
        // coding of these elements
        String[] attNames = new String[numOfSrcTblAttr];
        for (int i = 0; i < numOfSrcTblAttr; i++)
        {
            String namePrefix = Modules.nameFactory.getARandomName();
            coding = getStamp() + repetition + "A" + i;
            String srcAttName = namePrefix + "_" + coding;
            SMarkElement el = new SMarkElement(srcAttName, Atomic.STRING, null, 0, 0);
            el.setHook(new String(coding));
            srcRel.addSubElement(el);
            attNames[i] = srcAttName;
        }

        // create the target table
        String targetRelName = Modules.nameFactory.getARandomName();
        targetRelName = targetRelName + "_" + coding;
        SMarkElement tgtRel = new SMarkElement(targetRelName, new Set(), null, 0, 0);
        tgtRel.setHook(new String(coding));
        target.addSubElement(tgtRel);
        
        // create the query for the target table
        SPJQuery q = new SPJQuery();
        q.getFrom().add(new Variable("X"), new Projection(Path.ROOT, sourceRelName));
        
        // populate this table with the same element created above for the source
        SelectClauseList sel = q.getSelect();
        
        // go through all the attributes put in the source table and pop them into the target
        for (int i = 0, imax = attNames.length - numDelAttr; i < imax; i++)
        {
        	String tgtAttrName = attNames[i];
        	SMarkElement tgtAtomicElt = new SMarkElement(tgtAttrName, Atomic.STRING, null, 0, 0);
        	String hook = tgtAttrName.substring(tgtAttrName.indexOf("_"));
        	tgtAtomicElt.setHook(hook);
        	tgtRel.addSubElement(tgtAtomicElt);
        	
        	// since we added an attr in the target, we add an entry in the
            // respective select clause
            Projection att = new Projection(new Variable("X"), tgtAttrName);
            sel.add(tgtAttrName, att);
        }
        
        // add the partial queries to the parent query
        // to form the whole transformation
        SelectClauseList pselect = pquery.getSelect();
        String tblTrgName = tgtRel.getLabel();
        pselect.add(tblTrgName, q);
            
        pquery.setSelect(pselect);
    }

    @Override
    protected void chooseSourceRels() {
    	
    }
    
    @Override
    protected void chooseTargetRels () {
    	
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
	
	@Override
	public ScenarioName getScenType() {
		return ScenarioName.DELATTRIBUTE;
	}


}
