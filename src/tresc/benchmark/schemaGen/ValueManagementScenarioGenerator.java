package tresc.benchmark.schemaGen;

import java.util.Random;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;

import vtools.dataModel.expression.Function;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;

public class ValueManagementScenarioGenerator extends ScenarioGenerator
{
    private Random _generator;

    private final String _stamp = "VM";

    public ValueManagementScenarioGenerator()
    {
        ;
    }


    public void generateScenario(MappingScenario scenario, Configuration configuration)
    {
        // generate the generator based on the seed
        //long seed = configuration.getScenarioSeeds(Constants.ScenarioName.VALUEMANAGEMENT.ordinal());
        //_generator = (seed == 0) ? new Random() : new Random(seed);

        _generator=configuration.getRandomGenerator();
        
        Schema source = scenario.getSource();
        Schema target = scenario.getTarget();
        SPJQuery pquery = scenario.getTransformation();

        int repetitions = configuration.getScenarioRepetitions(Constants.ScenarioName.VALUEMANAGEMENT.ordinal());
        int numOfElements = configuration.getParam(Constants.ParameterName.NumOfSubElements);
        int numOfElementsDeviation = configuration.getDeviation(Constants.ParameterName.NumOfSubElements);
        int numOfArguments = configuration.getParam(Constants.ParameterName.NumOfParamsInFunctions);
        int numOfArgumentsDeviation = configuration.getDeviation(Constants.ParameterName.NumOfParamsInFunctions);
        for (int i = 0, imax = repetitions; i < imax; i++)
        {
            int numOfSubElements = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
                numOfElementsDeviation);
            createSubElements(source, target, numOfSubElements, i, numOfArguments, numOfArgumentsDeviation, pquery);
        }

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
    private void createSubElements(Element sourceParent, Element targetParent, int numOfElements, int repetition,
            int numOfArgs, int numOfArgsDeviation, SPJQuery pquery)
    {
        // first create the name of the two tables
        String randomName = Modules.nameFactory.getARandomName() + "_" + _stamp + repetition;
        SMarkElement srcTbl = new SMarkElement(randomName, new Set(), null, 0, 0);
        srcTbl.setHook(new String(_stamp + repetition));
        sourceParent.addSubElement(srcTbl);
        String randomNameTrg = randomName;
        SMarkElement trgTbl = new SMarkElement(randomName, new Set(), null, 0, 0);
        trgTbl.setHook(new String(_stamp + repetition));
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
            randomName = Modules.nameFactory.getARandomName() + "_" + _stamp + repetition + "AE" + i;
            SMarkElement srcElem = new SMarkElement(randomName, Atomic.STRING, null, 0, 0);
            srcElem.setHook(new String(_stamp + repetition + "AE" + i));
            srcTbl.addSubElement(srcElem);
            // decide in how many pieces you will split it
            int pieces = Utils.getRandomNumberAroundSomething(_generator, numOfArgs, numOfArgsDeviation);
            // and add that many pieces in the target table (the name will be
            // the same as the name in the src table, but suffixed with
            // Part1, Part2, Part3, etc.
            for (int k = 0; k < pieces; k++)
            {
                SMarkElement trgElem = new SMarkElement(randomName + "Part" + k, Atomic.STRING, null, 0, 0);
                trgElem.setHook(new String(_stamp + repetition + "AE" + i + "Part" + k));
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
            randomName = Modules.nameFactory.getARandomName() + "_" + _stamp + repetition + "AE" + (i + X);
            SMarkElement trgElem = new SMarkElement(randomName, Atomic.STRING, null, 0, 0);
            trgElem.setHook(new String( _stamp + repetition + "AE" + (i + X)));
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
                srcElem.setHook(new String(_stamp + repetition + "AE" + (i + X)+ "Part" + k));
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
    }
}
