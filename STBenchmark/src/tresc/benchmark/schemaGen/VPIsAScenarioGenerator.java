package tresc.benchmark.schemaGen;

import java.util.Random;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Modules;
import tresc.benchmark.Constants.JoinKind;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.ForeignKey;
import vtools.dataModel.expression.Function;
import vtools.dataModel.expression.Key;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;

// very similar to merging scenario generator, with source and target schemas swapped
public class VPIsAScenarioGenerator extends ScenarioGenerator
{
    private Random _generator;

    private final String _stamp = "VP";

    public VPIsAScenarioGenerator()
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
        int repetitions = configuration.getScenarioRepetitions(Constants.ScenarioName.VERTPARTITIONISA.ordinal());
        // How many elements to have in each table
        int numOfElements = configuration.getParam(Constants.ParameterName.NumOfSubElements);
        int numOfElementsDeviation = configuration.getDeviation(Constants.ParameterName.NumOfSubElements);
        // how many tables to have
        int numOfFragments = configuration.getParam(Constants.ParameterName.JoinSize);
        int numOfFragmentsDeviation = configuration.getDeviation(Constants.ParameterName.JoinSize);
        // whether we do star of chain joins
        int joinKind = configuration.getParam(Constants.ParameterName.JoinKind);

        for (int i = 0, imax = repetitions; i < imax; i++)
        {
            // decide how many attributes will the source table have
            int numOfSrcTblAttr = Utils.getRandomNumberAroundSomething(_generator, numOfElements,
                numOfElementsDeviation);

            // number of tables we will use in the target
            int numOfTgtTables = Utils.getRandomNumberAroundSomething(_generator, numOfFragments,
                numOfFragmentsDeviation);

            // decide the kind of join we will follow.
            JoinKind jk = JoinKind.values()[joinKind];
            if (jk == JoinKind.VARIABLE)
            {
                int tmp = Utils.getRandomNumberAroundSomething(_generator, 0, 1);
                if (tmp < 0)
                    jk = JoinKind.STAR;
                else jk = JoinKind.CHAIN;
            }
            createSubElements(source, target, numOfSrcTblAttr, numOfTgtTables, jk, i, pquery);
        }

    }

    /**
     * This is the main function. It generates a table in the source, a number
     * of tables in the target and a respective number of queries.
     */
    private void createSubElements(Schema source, Schema target, int numOfSrcTblAttr, int numOfTgtTables,
            JoinKind jk, int repetition, SPJQuery pquery)
    {   
        // since we add a key to the tables, we add one less free element to the source and target
        numOfSrcTblAttr--;
            
        // First create the source table
        String sourceRelName = Modules.nameFactory.getARandomName();
        String coding = _stamp + repetition;
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
            coding = _stamp + repetition + "A" + i;
            String srcAttName = namePrefix + "_" + coding;
            SMarkElement el = new SMarkElement(srcAttName, Atomic.STRING, null, 0, 0);
            el.setHook(new String(coding));
            srcRel.addSubElement(el);
            attNames[i] = srcAttName;
        }

        // create key for source table
        Key srcKey = new Key();
        srcKey.addLeftTerm(new Variable("X"), new Projection(Path.ROOT,srcRel.getLabel()));
        srcKey.setEqualElement(new Variable("X"));
        
        // create the actual key and add it to the source schema
        String randomName = Modules.nameFactory.getARandomName();
        String keyName = randomName + "_" + _stamp + repetition + "KE0";
        SMarkElement es = new SMarkElement(keyName, Atomic.STRING, null, 0, 0);
        es.setHook(new String(_stamp + repetition + "KE0"));
        srcRel.addSubElement(es);
        // add the key attribute to the source key
        srcKey.addKeyAttr(new Projection(new Variable("X"),keyName));
        
        // add constraint
        source.addConstraint(srcKey);

        // create the set of the partial (intermediate) queries
        // each query populates a target table. We also create the target tables
        SMarkElement[] trgTables = new SMarkElement[numOfTgtTables];
        SPJQuery[] queries = new SPJQuery[numOfTgtTables];
        for (int i = 0; i < numOfTgtTables; i++)
        {
            SPJQuery q = new SPJQuery();
            q.getFrom().add(new Variable("X"), new Projection(Path.ROOT, sourceRelName));
            queries[i] = q;

            String targetRelNamePrefix = Modules.nameFactory.getARandomName();
            coding = _stamp + repetition + "TT" + i;
            String targetRelName = targetRelNamePrefix + "_" + coding;
            SMarkElement tgtRel = new SMarkElement(targetRelName, new Set(), null, 0, 0);
            tgtRel.setHook(new String(coding));
            target.addSubElement(tgtRel);
            trgTables[i] = tgtRel;
        }

        // we distribute the source atomic elements among the target relations
        // we add all the atomic elements in the partial queries
        // int attsPerTargetRel = (int) Math.ceil((float) numOfSrcTblAttr /
        // numOfTgtTables);
        int attsPerTargetRel = numOfSrcTblAttr / numOfTgtTables;
        int attrPos = 0;
        for (int ti = 0; ti < numOfTgtTables; ti++)
        {
            SelectClauseList sel = queries[ti].getSelect();
            SMarkElement tgtRel = trgTables[ti];
            for (int i = 0, imax = attsPerTargetRel; i < imax; i++)
            {
                String trgAttrName = attNames[attrPos];
                attrPos++;
                SMarkElement tgtAtomicElt = new SMarkElement(trgAttrName, Atomic.STRING, null, 0, 0);
                String hook = trgAttrName.substring(trgAttrName.indexOf("_"));
                tgtAtomicElt.setHook(hook);
                tgtRel.addSubElement(tgtAtomicElt);

                // since we added an attr in the target, we add an entry in the
                // respective select clause
                Projection att = new Projection(new Variable("X"), trgAttrName);
                sel.add(trgAttrName, att);
            }
        }

        // it may be the case that some elements are left over due to not
        // perfect division between integers. We add them all in the last
        // fragment
        for (int i = attrPos, imax = attNames.length; i < imax; i++)
        {
            String trgAttrName = attNames[i];
            SMarkElement tgtAtomicElt = new SMarkElement(trgAttrName, Atomic.STRING, null, 0, 0);
            String hook = trgAttrName.substring(trgAttrName.indexOf("_"));
            tgtAtomicElt.setHook(hook);
            trgTables[numOfTgtTables - 1].addSubElement(tgtAtomicElt);

            // since we added an attr in the target, we add an entry in the
            // respective select clause
            Projection att = new Projection(new Variable("X"), trgAttrName);
            queries[numOfTgtTables - 1].getSelect().add(trgAttrName, att);
        }

        // now we generate the join attributes in the target tables
        if (jk == JoinKind.STAR)
        {
            /*coding = _stamp + repetition + "JoinAtt";
            String joinAttName = Modules.nameFactory.getARandomName() + "_" + coding;
            String joinAttNameRef = joinAttName + "Ref";

            SMarkElement joinAttElement = new SMarkElement(joinAttName, Atomic.STRING, null, 0, 0);
            joinAttElement.setHook(new String(coding));
            target.getSubElement(0).addSubElement(joinAttElement);
            // add to the first partial query a skolem function to generate
            // the join attribute in the first target table
            SelectClauseList sel0 = queries[0].getSelect();
            Function f0 = new Function("SK");
            for (int k = 0; k < numOfSrcTblAttr; k++)
            {
                Projection att = new Projection(new Variable("X"), attNames[k]);
                f0.addArg(att);
            }
            sel0.add(joinAttName, f0);
            queries[0].setSelect(sel0);*/
        	
        	// create key for target fragment
            Key tgtKey = new Key();
            tgtKey.addLeftTerm(new Variable("Y"), new Projection(Path.ROOT,target.getSubElement(0).getLabel()));
            tgtKey.setEqualElement(new Variable("Y"));
            
            // add the key to the target schema
            SMarkElement et = new SMarkElement(keyName, Atomic.STRING, null, 0, 0);
            et.setHook(new String(_stamp + repetition + "KE0"));
            target.getSubElement(0).addSubElement(et);
            // add the key attribute to the target key
            tgtKey.addKeyAttr(new Projection(new Variable("Y"),keyName));
            
            // add constraint
            //target.addConstraint(tgtKey);
            
            SelectClauseList sel0 = queries[0].getSelect();
            Projection att0 = new Projection(new Variable("X"), keyName);
            sel0.add(keyName, att0);
            queries[0].setSelect(sel0);
            
            for (int i = 1; i < numOfTgtTables; i++)
            {
                SMarkElement keyElement = new SMarkElement(keyName, Atomic.STRING, null, 0, 0);
                keyElement.setHook(new String(coding + "Ref"));
                target.getSubElement(i).addSubElement(keyElement);
                // we create the target constraint(i.e. foreign key both ways )
                Variable varKey1 = new Variable("F");
                Variable varKey2 = new Variable("K");
                ForeignKey fKeySrc1 = new ForeignKey();
                fKeySrc1.addLeftTerm(varKey1.clone(),
                    new Projection(Path.ROOT, target.getSubElement(0).getLabel()));
                fKeySrc1.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,
                    target.getSubElement(i).getLabel()));
                fKeySrc1.addFKeyAttr(new Projection(varKey2.clone(), keyName), new Projection(
                    varKey1.clone(), keyName));
                target.addConstraint(fKeySrc1);
                ForeignKey fKeySrc2 = new ForeignKey();
                fKeySrc2.addLeftTerm(varKey1.clone(),
                    new Projection(Path.ROOT, target.getSubElement(i).getLabel()));
                fKeySrc2.addRightTerm(varKey2.clone(), new Projection(Path.ROOT,
                    target.getSubElement(0).getLabel()));
                fKeySrc2.addFKeyAttr(new Projection(varKey2.clone(), keyName), new Projection(varKey1.clone(),
                		keyName));
                target.addConstraint(fKeySrc2);
                // add to the each partial query a skolem function to generate
                // the join
                // reference attribute in all the other target tables
                SelectClauseList seli = queries[i].getSelect();
                Projection att = new Projection(new Variable("X"), keyName);
                seli.add(keyName, att);
                queries[i].setSelect(seli);
            }
        }

        // add the partial queries to the parent query
        // to form the whole transformation
        SelectClauseList pselect = pquery.getSelect();
        for (int i = 0; i < numOfTgtTables; i++)
        {
            String tblTrgName = trgTables[i].getLabel();
            pselect.add(tblTrgName, queries[i]);
        }
        pquery.setSelect(pselect);
    }
}
