package tresc.benchmark.schemaGen;

import java.util.Random;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.Function;
import vtools.dataModel.expression.Key;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.expression.Key;
import vtools.dataModel.types.Set;

public class AddAttributeScenarioGenerator extends ScenarioGenerator
{
    private Random _generator;

    private final String _stamp = "AA";
    private int skolemCounter = 0;

    public AddAttributeScenarioGenerator()
    {
        ;
    }

    public void generateScenario(MappingScenario scenario, Configuration configuration)
    {
        _generator = configuration.getRandomGenerator();

        Schema source = scenario.getSource();
        Schema target = scenario.getTarget();
        SPJQuery pquery = scenario.getTransformation();

        // first let's read the parameters
        int repetitions = configuration.getScenarioRepetitions(Constants.ScenarioName.ADDATTRIBUTE.ordinal());
        // How many elements to have in each table
        int numOfElements = configuration.getParam(Constants.ParameterName.NumOfSubElements);
        int numOfElementsDeviation = configuration.getDeviation(Constants.ParameterName.NumOfSubElements);
        int numNewAttr = configuration.getParam(Constants.ParameterName.NumOfNewAttributes);
        int typeOfSkolem = configuration.getParam(Constants.ParameterName.SkolemKind);

        for (int i = 0, imax = repetitions; i < imax; i++)
        {
            // decide how many attributes will the source table have
            int numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
                numOfElementsDeviation);
            
            createSubElements(source, target, numOfSrcTblAttr, numNewAttr, typeOfSkolem, i, pquery);
        }
    }

    /**
     * This is the main function. It generates a table in the source, a number
     * of tables in the target and a respective number of queries.
     */
    private void createSubElements(Schema source, Schema target, int numOfSrcTblAttr,
            int numNewAttr, int typeOfSkolem, int repetition, SPJQuery pquery)
    {
    	// determine how the skolem term will be generated
    	Boolean randomSkolem;
    	Boolean useKey;
    	
    	// the configuration file allows three values for skValue
    	// 		0 - use all attributes in source for skolem generation
    	//		1 - use a random subset of attributes in the source 
    	//		2 - generate a key for the source, and use only that for skolem generation
    	// if any other values are used in the configuration file, use case 0 (use all attributes)
    	switch(typeOfSkolem)
    	{
    		case 0:
    			randomSkolem = false;
    			useKey = false;
    			break;
    		case 1:
    			randomSkolem = true;
    			useKey = false;
    			break;
    		case 2:
    			randomSkolem = false;
    			useKey = true;
    			break;
    		default:
    			randomSkolem = false;
    			useKey = false;
    			break;
    	}
    	
    	String coding = _stamp + repetition;
    	int curTbl = repetition;
    	
    	// First create the source table
        String sourceRelName = Modules.nameFactory.getARandomName();
        sourceRelName = sourceRelName + "_" + coding;
        SMarkElement srcRel = new SMarkElement(sourceRelName, new Set(), null, 0, 0);
        srcRel.setHook(new String(coding));
        source.addSubElement(srcRel);
        
        // create the target table
        String targetRelName = Modules.nameFactory.getARandomName();
        targetRelName = targetRelName + "_" + coding;
        SMarkElement tgtRel = new SMarkElement(targetRelName, new Set(), null, 0, 0);
        tgtRel.setHook(new String(coding));
        target.addSubElement(tgtRel);
    	
        // generate random key name even though it may not be used to avoid variable may not have been initialized errors
        String randomName = Modules.nameFactory.getARandomName();
        String keyName = randomName + "_" + _stamp + repetition + "KE0";
        
    	if(useKey)
        {
        	// create key for source table
	        Key srcKey = new Key();
	        srcKey.addLeftTerm(new Variable("X"), new Projection(Path.ROOT,source.getSubElement(curTbl).getLabel()));
	        srcKey.setEqualElement(new Variable("X"));
	        
	        // create key for target table
	        Key tgtKey = new Key();
	        tgtKey.addLeftTerm(new Variable("Y"), new Projection(Path.ROOT,target.getSubElement(curTbl).getLabel()));
	        tgtKey.setEqualElement(new Variable("Y"));
	        
	        // create the actual key and add it to the source schema
            SMarkElement es = new SMarkElement(keyName, Atomic.STRING, null, 0, 0);
            es.setHook(new String(_stamp + repetition + "KE0"));
            source.getSubElement(curTbl).addSubElement(es);
            // add the key attribute to the source key
            srcKey.addKeyAttr(new Projection(new Variable("X"),keyName));
                
            // add the key to the target schema
            SMarkElement et = new SMarkElement(keyName, Atomic.STRING, null, 0, 0);
            et.setHook(new String(_stamp + repetition + "KE0"));
            target.getSubElement(curTbl).addSubElement(et);
            // add the key attribute to the target key
            tgtKey.addKeyAttr(new Projection(new Variable("Y"),keyName));
            
            // add constraints to the source and target
            source.addConstraint(srcKey);
            target.addConstraint(tgtKey);
            
            // since we added a key to the table, we add one less free element to the source and target
            numOfSrcTblAttr--;
        } 
    	
        // Populate the source with elements. The array attNames, keeps the
        // coding of these elements
        String[] attNames = new String[numOfSrcTblAttr];
        for (int i = 0; i < numOfSrcTblAttr; i++)
        {
            String namePrefix = Modules.nameFactory.getARandomName();
            coding = _stamp + repetition + "A" + i;
            String srcAttName = namePrefix + "_" + coding;
            SMarkElement el = new SMarkElement(srcAttName, Atomic.STRING, null, 0, 0);
            el.setHook(new String(coding));
            srcRel.addSubElement(el);
            attNames[i] = srcAttName;
        }
        
        // create the query for the target table
        SPJQuery q = new SPJQuery();
        q.getFrom().add(new Variable("X"), new Projection(Path.ROOT, sourceRelName));
        
        // populate this table with the same element created above for the source
        SelectClauseList sel = q.getSelect();
        
        // go through all the attributes put in the source table and pop them into the target
        for (int i = 0, imax = attNames.length; i < imax; i++)
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
        
        // now we need to add a fixed number of attributes to the target
        coding = _stamp + repetition + "NewAtt";
        
        // by default use all elements in the table as arguments for skolem generation
        int numArgsForSkolem = numOfSrcTblAttr;
        
        for (int j = 0; j < numNewAttr; j++)
        {
	        String newAttName = Modules.nameFactory.getARandomName() + "_" + coding;
	        SMarkElement newAttElement = new SMarkElement(newAttName, Atomic.STRING, null, 0, 0);
	        newAttElement.setHook(new String(coding));
	        
	        // here we take the correct table (a subelement which is a set) from the target relation and add a new attribute to it
	        target.getSubElement(curTbl).addSubElement(newAttElement);
	        
	        // add to the first partial query a skolem function to generate
            // the join attribute in the first target table
            SelectClauseList sel0 = q.getSelect();
            String skolemName = "SK" + String.valueOf(skolemCounter);
            skolemCounter++;
            Function f0 = new Function(skolemName);
            
            // if we are using a key in the original relation then we base the skolem on just that key
            if (useKey)
            {
            	Projection att = new Projection(new Variable("X"), keyName);
            	f0.addArg(att);
            }
            
            else
            {
            	// if configuration specifies that we need to randomly decide how many arguments the skolem will take, generate a random number
                if (randomSkolem)
                	numArgsForSkolem = Utils.getRandomNumberAroundSomething(_generator, numOfSrcTblAttr/2, numOfSrcTblAttr/2);
           
                // ensure that we are still within the bounds of the number of source attributes
                if(numArgsForSkolem > numOfSrcTblAttr)
                	numArgsForSkolem = numOfSrcTblAttr;
                
                // add all the source attributes as arguments for the skolem function
	            for (int k = 0; k < numArgsForSkolem; k++)
	            {
	                Projection att = new Projection(new Variable("X"), attNames[k]);
	                f0.addArg(att);
	            }
            }
            
            sel0.add(newAttName, f0);
            q.setSelect(sel0);
        }
        
        // add the partial queries to the parent query
        // to form the whole transformation
        SelectClauseList pselect = pquery.getSelect();
        String tblTrgName = tgtRel.getLabel();
        pselect.add(tblTrgName, q);
            
        pquery.setSelect(pselect);
    }
}
