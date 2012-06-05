package tresc.benchmark.schemaGen;

import java.util.Random;
import java.util.Vector;

import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.AND;
import vtools.dataModel.expression.EQ;
import vtools.dataModel.expression.ForeignKey;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Rcd;
import vtools.dataModel.types.Set;
import vtools.dataModel.types.Type;
import vtools.utils.structures.EqClassManager;

/**
 * This class is a module/part of the GLAVScenarioGenerator. It should not be
 * created independently.
 */
public class GLAVScenarioSrcGenerator
{
    private final int FROM = 0;

    private final int TO = 1;

    // counter used for naming the set elements
    private int _si = 0;

    // counter used for naming the atomic elements
    private int _ai = 0;

    /*
     * It returns (i) An array of elements that are actually the root set
     * elements (ii) a Vector of all the constraints we created (iii) The query
     * we created which will serve as a basis for the mapping query (iv) The
     * list of all the atomic elements we have created
     */
    public Object[] generateSourceSchema(Configuration configuration, Random generator, EqClassManager eqClassMgr,
            int repetition)
    {
        // Decide how many tables to have
        int numOfFragmentsParam = configuration.getParam(Constants.ParameterName.JoinSize);
        int numOfFragmentsDeviation = configuration.getDeviation(Constants.ParameterName.JoinSize);
        int F = Utils.getRandomNumberAroundSomething(generator, numOfFragmentsParam, numOfFragmentsDeviation);
        F = (F < 1) ? 1 : F;
        // decide if we need self join
        int numOfSelfJoins = configuration.getScenarioRepetitions(Constants.ScenarioName.SELFJOINS.ordinal());
        numOfSelfJoins = (numOfSelfJoins != 0) ? 1 : 0;
        // a list with the root set elements we generate and the atomic elements
        // of each such root set
        SMarkElement[] rootSets = new SMarkElement[F];
        Vector<SMarkElement>[] atomicElements = new Vector[F];
        int[] numOfSetElements = new int[F + numOfSelfJoins];
        // a list with all the set elements
        Vector<SMarkElement> setElementsVector = new Vector<SMarkElement>();

        // create the fragments
        for (int f = 0; f < F; f++)
        {
            // Decide how deep we need to go for this root set
            int nestingParam = configuration.getParam(Constants.ParameterName.NestingDepth);
            int nestingDeviation = configuration.getDeviation(Constants.ParameterName.NestingDepth);
            int maxNestingdepth = Utils.getRandomNumberAroundSomething(generator, nestingParam, nestingDeviation);
            // now create the element
            String randomName = Modules.nameFactory.getARandomName();
            String hook = GLAVScenarioGenerator._stamp + repetition + "S" + _si;
            String name = randomName + "_" + hook;
            // create the root set element
            SMarkElement setElement = new SMarkElement(name, new Set(), null, f, 0);
            setElement.setHook(hook);
            // create the variable for its eqClass
            Variable var = new Variable("X" + _si);
            eqClassMgr.classify(setElement);
            Object oclass = eqClassMgr.getEqClass(setElement);
            eqClassMgr.setEqClassHook(oclass, var);
            rootSets[f] = setElement;
            _si++;

            populateSetElement(setElement, configuration, 0, maxNestingdepth, repetition, generator, f, 0,
                eqClassMgr);
            generateAtomicElementsEqClassValues(setElement, var, eqClassMgr);
            Vector<SMarkElement> atomicElementsVector = new Vector<SMarkElement>();
            collectAtomicElements(setElement, atomicElementsVector, false);
            atomicElements[f] = atomicElementsVector;
        }

        // partial query that will be used in the final mapping query
        SPJQuery query = new SPJQuery();
        FromClauseList from = query.getFrom();
        // create the from clause list and keep track of
        // the number of set elements in each fragment
        for (int i = 0, imax = rootSets.length; i < imax; i++)
        {
            generateFromClause(rootSets[i], Path.ROOT, from, setElementsVector, eqClassMgr);
            numOfSetElements[i] = from.size();
        }

        // generate the source constraints and add them in the where clause of
        // query
        Vector<ForeignKey> constraints = new Vector<ForeignKey>();
        generateConstraints(rootSets, configuration, generator, numOfSetElements, setElementsVector, eqClassMgr,
            query, constraints);

        // Now we need to take the appropriate steps in the case of self join
        Vector<SMarkElement> selfJoinAtomicElements = new Vector<SMarkElement>();
        if (numOfSelfJoins != 0)
        {
            // we clone the first fragment (i.e. we make a self join on the
            // first fragment) and we assign each of its elements to its own
            // eqclass
            SMarkElement selfJoinEl = rootSets[0].clone();
            selfJoinHouseKeeping(selfJoinEl, Path.ROOT, eqClassMgr);

            // collect its atomic elements that we will need to append to the
            // list of elements we will return
            collectAtomicElements(selfJoinEl, selfJoinAtomicElements, false);

            // now we create a join between the fragment 0 and the selfJoin
            // fragment. We decided to make the selfJoin only on 1 attribute
            // for simplicity. The FROM fragment is the 0 fragment and TO
            // fragment is the selfJoin Fragment.

            // select a random set from the fragment FROM fragment
            int numberOfSetsInFr = numOfSetElements[0];
            int randomSetNum = generator.nextInt(numberOfSetsInFr);
            SMarkElement fromSetElement = setElementsVector.elementAt(randomSetNum);
            // collect the atomic elements of the set element and select
            // randomly one of them.
            Vector<SMarkElement> fromAtomicSubElem = new Vector<SMarkElement>();
            collectAtomicElements(fromSetElement, fromAtomicSubElem, true);
            int rf = generator.nextInt(fromAtomicSubElem.size());
            SMarkElement fromAtomicElement = fromAtomicSubElem.remove(rf);

            // collect the set elements and select a random set from the
            // fragment TO fragment (i.e. the self join)
            randomSetNum = generator.nextInt(numberOfSetsInFr);
            Vector<SMarkElement> selfJoinSetElements = new Vector<SMarkElement>();
            generateFromClause(selfJoinEl, Path.ROOT, from, selfJoinSetElements, eqClassMgr);
            SMarkElement toSetElement = selfJoinSetElements.elementAt(randomSetNum);
            // collect the atomic elements of the set element and select
            // randomly one of them.
            Vector<SMarkElement> toAtomicSubElem = new Vector<SMarkElement>();
            collectAtomicElements(toSetElement, toAtomicSubElem, true);
            int rt = generator.nextInt(toAtomicSubElem.size());
            SMarkElement toAtomicElement = toAtomicSubElem.remove(rt);

            // System.out.println(fromAtomicElement + " "
            // +
            // eqClassMgr.getEqClassHook(eqClassMgr.getEqClass(fromAtomicElement))
            // + "\n" + fromSetElement
            // + "\n");
            // System.out.println(toAtomicElement + " "
            // +
            // eqClassMgr.getEqClassHook(eqClassMgr.getEqClass(toAtomicElement))
            // + "\n" + toSetElement + "\n");

            // and now we need to create a foreign key from the
            // fromAtomicElement (part of the fromSetElement) to the
            // toAtomicElement (part of the toSetElement)
            ForeignKey fKeySrc = new ForeignKey();
            Vector<Variable> varsF = new Vector<Variable>();
            Vector<Path> exprsF = new Vector<Path>();
            getPathTerms(fromSetElement, varsF, exprsF, from, eqClassMgr);
            for (int k = varsF.size() - 1; k >= 0; k--)
            {
                fKeySrc.addLeftTerm(varsF.elementAt(k), exprsF.elementAt(k));
            }
            Vector<Variable> varsT = new Vector<Variable>();
            Vector<Path> exprsT = new Vector<Path>();
            getPathTerms(toSetElement, varsT, exprsT, from, eqClassMgr);
            for (int k = varsT.size() - 1; k >= 0; k--)
            {
                fKeySrc.addRightTerm(varsT.elementAt(k), exprsT.elementAt(k));
            }
            Projection pathFrom = (Projection)getPathExprFromSet(fromAtomicElement, eqClassMgr);
            Projection pathTo = (Projection) getPathExprFromSet(toAtomicElement, eqClassMgr);
            fKeySrc.addFKeyAttr(pathTo, pathFrom);
            constraints.add(fKeySrc);

            // add the join condition to the where clause
            AND where = (AND) query.getWhere();
            where.add(new EQ(pathFrom.clone(), pathTo.clone()));
            query.setWhere(where);

            // we merge the eqClasses for the self join elements
            Object oclassF = eqClassMgr.getEqClass(fromAtomicElement);
            Object oclassT = eqClassMgr.getEqClass(toAtomicElement);
            eqClassMgr.mergeEqClass(oclassF, oclassT);
        }

        // put all the atomic elements together into one big Vector
        Vector<SMarkElement> allAtomicElements = new Vector<SMarkElement>();
        for (int i = 0; i < F; i++)
        {
            Vector<SMarkElement> v = atomicElements[i];
            for (int j = 0, jmax = v.size(); j < jmax; j++)
            {
                allAtomicElements.add(v.elementAt(j));
            }
        }
        for (int j = 0, jmax = selfJoinAtomicElements.size(); j < jmax; j++)
        {
            allAtomicElements.add(selfJoinAtomicElements.elementAt(j));
        }

        // return whatever you have generated
        Object[] retVal = new Object[4];
        retVal[0] = rootSets;
        retVal[1] = constraints;
        retVal[2] = query;
        retVal[3] = allAtomicElements;
        return retVal;
    }

    /*
     * Generates the join constraints. Create the where clause of the query,
     * i.e. it contains the attributes evolved in the join constraints.
     */
    private void generateConstraints(SMarkElement[] rootSets, Configuration configuration, Random generator,
            int[] numOfSetElements, Vector<SMarkElement> setElementsVector, EqClassManager eqClassMgr,
            SPJQuery query, Vector<ForeignKey> constraints)
    {
        int F = rootSets.length;
        // decide the kind of join we will do
        int joinKind = configuration.getParam(Constants.ParameterName.JoinKind);
        if (joinKind == Constants.JoinKind.VARIABLE.ordinal())
        {
            int randomInt = generator.nextInt(2);
            joinKind = (randomInt == 0) ? Constants.JoinKind.CHAIN.ordinal() : Constants.JoinKind.STAR.ordinal();
        }
        // we keep all the atomic elements on which foreign keys are
        // defined s.t. we merge their eqClasses at the end
        SMarkElement[][][] allPairs = new SMarkElement[F][][];

        FromClauseList from = query.getFrom();
        AND where = new AND();

        for (int i = 1; i < F; i++)
        {
            // read params for how many attributes to be used in the join
            int joinWidth = configuration.getParam(Constants.ParameterName.NumOfJoinAttributes);
            int joinWidthDeviation = configuration.getDeviation(Constants.ParameterName.NumOfJoinAttributes);
            // select a join width. Note that the number above may be 0 in which
            // case we have a cartezian product.
            int numOfJoinAttr = Utils.getRandomNumberAroundSomething(generator, joinWidth, joinWidthDeviation);

            int fromFr = (joinKind == Constants.JoinKind.CHAIN.ordinal()) ? i : 0;
            int toFr = (joinKind == Constants.JoinKind.CHAIN.ordinal()) ? (i - 1) : i;

            // select a random set from the fragment fromFr
            int numberOfSetsInFr = numOfSetElements[fromFr] - ((fromFr == 0) ? 0 : numOfSetElements[fromFr - 1]);
            int randomSetNum = generator.nextInt(numberOfSetsInFr);
            int setNum = ((fromFr == 0) ? 0 : numOfSetElements[fromFr - 1]) + randomSetNum;
            SMarkElement fromSetElement = setElementsVector.elementAt(setNum);
            // collect the atomic elements of the set element
            Vector<SMarkElement> fromAtomicSubElem = new Vector<SMarkElement>();
            collectAtomicElements(fromSetElement, fromAtomicSubElem, true);

            // select a random set from the fragment toFr
            numberOfSetsInFr = numOfSetElements[toFr] - ((toFr == 0) ? 0 : numOfSetElements[toFr - 1]);
            randomSetNum = generator.nextInt(numberOfSetsInFr);
            setNum = ((toFr == 0) ? 0 : numOfSetElements[toFr - 1]) + randomSetNum;
            SMarkElement toSetElement = setElementsVector.elementAt(setNum);
            // collect the atomic elements of the set element
            Vector<SMarkElement> toAtomicSubElem = new Vector<SMarkElement>();
            collectAtomicElements(toSetElement, toAtomicSubElem, true);

            numOfJoinAttr = (numOfJoinAttr > fromAtomicSubElem.size()) ? fromAtomicSubElem.size() : numOfJoinAttr;
            numOfJoinAttr = (numOfJoinAttr > toAtomicSubElem.size()) ? toAtomicSubElem.size() : numOfJoinAttr;

            // select randomly numOfJoinAttr from each vector of atomic elements
            SMarkElement[][] pairs = new SMarkElement[numOfJoinAttr][2];
            for (int j = 0; j < numOfJoinAttr; j++)
            {
                int rf = generator.nextInt(fromAtomicSubElem.size());
                pairs[j][FROM] = fromAtomicSubElem.remove(rf);
                int rt = generator.nextInt(toAtomicSubElem.size());
                pairs[j][TO] = toAtomicSubElem.remove(rt);
            }
            allPairs[i] = pairs;

            ForeignKey fKeySrc = new ForeignKey();
            Vector<Variable> varsF = new Vector<Variable>();
            Vector<Path> exprsF = new Vector<Path>();
            getPathTerms(fromSetElement, varsF, exprsF, from, eqClassMgr);
            for (int k = varsF.size() - 1; k >= 0; k--)
            {
                fKeySrc.addLeftTerm(varsF.elementAt(k), exprsF.elementAt(k));
            }
            Vector<Variable> varsT = new Vector<Variable>();
            Vector<Path> exprsT = new Vector<Path>();
            getPathTerms(toSetElement, varsT, exprsT, from, eqClassMgr);
            for (int k = varsT.size() - 1; k >= 0; k--)
            {
                fKeySrc.addRightTerm(varsT.elementAt(k), exprsT.elementAt(k));
            }
            for (int j = 0; j < numOfJoinAttr; j++)
            {
                Object oclass = eqClassMgr.getEqClass(pairs[j][FROM]);
                Projection pathFrom = (Projection) eqClassMgr.getEqClassHook(oclass);
                oclass = eqClassMgr.getEqClass(pairs[j][TO]);
                Projection pathTo = (Projection) eqClassMgr.getEqClassHook(oclass);
                fKeySrc.addFKeyAttr(pathTo, pathFrom);
                // add the join condition to the where clause
                where.add(new EQ(pathFrom.clone(), pathTo.clone()));
            }
            constraints.add(fKeySrc);
        }
        query.setWhere(where);

        // we merge the eqClasses for all the pairs
        for (int i = 1; i < F; i++)
        {
            SMarkElement[][] pairs = allPairs[i];
            for (int j = 0, jmax = pairs.length; j < jmax; j++)
            {
                Object oclassF = eqClassMgr.getEqClass(pairs[j][FROM]);
                Object oclassT = eqClassMgr.getEqClass(pairs[j][TO]);
                eqClassMgr.mergeEqClass(oclassF, oclassT);
            }
        }
    }

    /*
     * Traverses the contents of an element, makes its contents classable and
     * assigns to them the right eqClass value.
     */
    private void selfJoinHouseKeeping(SMarkElement curEl, Path parentPath, EqClassManager eqClassMgr)
    {
        Type type = curEl.getType();
        if (type instanceof Atomic)
        {
            Path valuePath = parentPath.clone();
            Projection attr = new Projection(valuePath, curEl.getLabel());
            if (eqClassMgr.getEqClass(curEl) != null)
                throw new RuntimeException("Should not happen 98342394");
            eqClassMgr.classify(curEl);
            Object oclass = eqClassMgr.getEqClass(curEl);
            eqClassMgr.setEqClassHook(oclass, attr);
        }
        else if (type instanceof Rcd)
        {
            Path valuePath = parentPath.clone();
            Projection attr = new Projection(valuePath, curEl.getLabel());
            if (eqClassMgr.getEqClass(curEl) != null)
                throw new RuntimeException("Should not happen 98342394");
            eqClassMgr.classify(curEl);
            Object oclass = eqClassMgr.getEqClass(curEl);
            eqClassMgr.setEqClassHook(oclass, attr);
            // we call its children now
            for (int i = 0, imax = curEl.size(); i < imax; i++)
            {
                SMarkElement child = (SMarkElement) curEl.getSubElement(i);
                selfJoinHouseKeeping(child, attr, eqClassMgr);
            }
        }
        else if (type instanceof Set)
        {
            // create a new variable for its eqClass
            Variable var = new Variable("X" + _si);
            _si++;
            // classify the current element
            if (eqClassMgr.getEqClass(curEl) != null)
                throw new RuntimeException("Should not happen 98342394");
            eqClassMgr.classify(curEl);
            Object oclass = eqClassMgr.getEqClass(curEl);
            eqClassMgr.setEqClassHook(oclass, var);

            // and since it is a set visit its children
            for (int i = 0, imax = curEl.size(); i < imax; i++)
            {
                SMarkElement child = (SMarkElement) curEl.getSubElement(i);
                selfJoinHouseKeeping(child, var, eqClassMgr);
            }
        }
        else
        {
            throw new RuntimeException("It should not happen 7687678687");
        }
    }

    /*
     * Returns for each set the terms you put in the generators of a foreign
     * key.
     */
    private void getPathTerms(SMarkElement curEl, Vector<Variable> vars, Vector<Path> exprs, FromClauseList from,
            EqClassManager eqClassMgr)
    {
        Object oclass = eqClassMgr.getEqClass(curEl);
        Variable var = (Variable) eqClassMgr.getEqClassHook(oclass);
        vars.add(var.clone());
        int pos = from.getExprVarPosition(var);
        Path expr = (Path) from.getExpression(pos);
        exprs.add(expr.clone());

        SMarkElement parent = (SMarkElement) curEl.getParent();
        if (parent == null)
        {
            ;
        }
        else
        {
            getPathTerms(parent, vars, exprs, from, eqClassMgr);
        }
    }

    /*
     * Returns for an atomic element its path expression from the first parent
     * set
     */
    private Path getPathExprFromSet(SMarkElement curEl, EqClassManager eqClassMgr)
    {
        if (curEl == null)
            return Path.ROOT;
        
        Type t = curEl.getType();
        if ((t instanceof Atomic) || (t instanceof Rcd))
        {
            Path p = getPathExprFromSet((SMarkElement) curEl.getParent(), eqClassMgr);
            return new Projection(p, curEl.getLabel());
        }
        else if (t instanceof Set)
        {
            Object oclass = eqClassMgr.getEqClass(curEl);
            Variable var = (Variable) eqClassMgr.getEqClassHook(oclass);
            return var.clone();
        }
        else throw new RuntimeException("Should not happen 782343784");
    }

    /*
     * Classifies the atomic elements and assigns the value of each eqClass to
     * be the appropriate path expression
     */
    private void generateAtomicElementsEqClassValues(SMarkElement curEl, Path curPath, EqClassManager eqClassMgr)
    {
        Type type = curEl.getType();
        if (type instanceof Atomic)
        {
            Projection attr = new Projection(curPath.clone(), curEl.getLabel());
            eqClassMgr.classify(curEl);
            Object oclass = eqClassMgr.getEqClass(curEl);
            eqClassMgr.setEqClassHook(oclass, attr);
        }
        else if (type instanceof Rcd)
        {
            Projection attr = new Projection(curPath.clone(), curEl.getLabel());
            eqClassMgr.classify(curEl);
            Object oclass = eqClassMgr.getEqClass(curEl);
            eqClassMgr.setEqClassHook(oclass, attr);
            for (int i = 0, imax = curEl.size(); i < imax; i++)
            {
                SMarkElement child = (SMarkElement) curEl.getSubElement(i);
                generateAtomicElementsEqClassValues(child, attr, eqClassMgr);
            }
        }
        else if (type instanceof Set)
        {
            Object oclass = eqClassMgr.getEqClass(curEl);
            Variable var = (Variable) eqClassMgr.getEqClassHook(oclass);
            for (int i = 0, imax = curEl.size(); i < imax; i++)
            {
                SMarkElement child = (SMarkElement) curEl.getSubElement(i);
                generateAtomicElementsEqClassValues(child, var, eqClassMgr);
            }
        }
        else
        {
            throw new RuntimeException("It should not happen");
        }

    }

    /*
     * Collects into a vector all the atomic elements of an element. The
     * variable stopOnSets determines whether we take the atomic elements of the
     * whole subtree or not
     */
    private void collectAtomicElements(SMarkElement currEl, Vector<SMarkElement> v, boolean stopOnSets)
    {
        Type type = currEl.getType();
        if (type instanceof Atomic)
        {
            v.add(currEl);
        }
        else if (type instanceof Rcd)
        {
            for (int i = 0, imax = currEl.size(); i < imax; i++)
            {
                SMarkElement child = (SMarkElement) currEl.getSubElement(i);
                Type type2 = child.getType();
                if (stopOnSets && (type2 instanceof Set))
                    continue;
                collectAtomicElements(child, v, stopOnSets);
            }
        }
        else if (type instanceof Set)
        {
            for (int i = 0, imax = currEl.size(); i < imax; i++)
            {
                SMarkElement child = (SMarkElement) currEl.getSubElement(i);
                Type type2 = child.getType();
                if (stopOnSets && (type2 instanceof Set))
                    continue;
                collectAtomicElements(child, v, stopOnSets);
            }
        }
        else
        {
            throw new RuntimeException("Should not happen");
        }
    }

    /*
     * Generates the From clause entries for a part of the schema. Also, creates
     * a vector with the set elements that are found from the curEl in the whole
     * fragment.
     */
    private void generateFromClause(SMarkElement curEl, Path parentPath, FromClauseList from,
            Vector<SMarkElement> setElementsVector, EqClassManager eqClassMgr)
    {
        Type type = curEl.getType();
        if (type instanceof Set)
        {
            Object oclass = eqClassMgr.getEqClass(curEl);
            Variable var = (Variable) eqClassMgr.getEqClassHook(oclass);
            Projection expr = new Projection(parentPath.clone(), curEl.getLabel());
            from.add(var, expr);
            // add the set element in the vector s.t. we know for each
            // position in the From Clause what element corresponds to
            setElementsVector.add(curEl);

            for (int i = 0, imax = curEl.size(); i < imax; i++)
            {
                SMarkElement child = (SMarkElement) curEl.getSubElement(i);
                generateFromClause(child, var, from, setElementsVector, eqClassMgr);
            }
        }
        else if (type instanceof Atomic)
        {
            ;
        }
        else if (type instanceof Rcd)
        {
            Object oclass = eqClassMgr.getEqClass(curEl);
            Path path = (Path) eqClassMgr.getEqClassHook(oclass);
            for (int i = 0, imax = curEl.size(); i < imax; i++)
            {
                SMarkElement child = (SMarkElement) curEl.getSubElement(i);
                generateFromClause(child, path, from, setElementsVector, eqClassMgr);
            }
        }
        else
        {
            throw new RuntimeException("It should not happen");
        }

    }

    /**
     * Generates the sub elements of an element and for the set sub elements it
     * calls recursively itself until the max depth has been reached.
     */
    private void populateSetElement(SMarkElement parentElement, Configuration configuration, int nestingDepth,
            int maxNestingdepth, int repetition, Random generator, int fragment, int fragmentAppearance,
            EqClassManager eqClassMgr)
    {
        // first decide how many attributes you will create.
        int numOfElements = configuration.getParam(Constants.ParameterName.NumOfSubElements);
        int numOfElementsDeviation = configuration.getDeviation(Constants.ParameterName.NumOfSubElements);
        int N = Utils.getRandomNumberAroundSomething(generator, numOfElements, numOfElementsDeviation);
        N = (N < 1) ? 1 : N;
        // number of set elements
        int numOfSetElements = configuration.getParam(Constants.ParameterName.JoinSize);
        int numOfSetElementsDeviation = configuration.getDeviation(Constants.ParameterName.JoinSize);
        int S = Utils.getRandomNumberAroundSomething(generator, numOfSetElements, numOfSetElementsDeviation);
        if (nestingDepth < maxNestingdepth)
        {
            if (S > N)
                S = N;
            if (S == 0) // if we need to go deeper, but nSetElts
                // came out 0, we set it to 1
                S = 1;
        }
        else S = 0;

        // number of atomic elements
        int A = N - S;
        // just a safety to have at least one atomic element in the max depth
        // if ((nestingDepth == maxNestingdepth) && (A == 0))
        // A = 1;
        // we should have at least one atomic attribute.
        A = (A == 0) ? 1 : A;

        // generate A atomic elements
        for (int i = 0, imax = A; i < imax; i++)
        {
            String randomName = Modules.nameFactory.getARandomName();
            String hook = GLAVScenarioGenerator._stamp + repetition + "A" + _ai;
            String name = randomName + "_" + hook;
            SMarkElement atomicElement = new SMarkElement(name, Atomic.STRING, null, fragment, fragmentAppearance);
            atomicElement.setHook(hook);
            parentElement.addSubElement(atomicElement);
            _ai++;
        }

        // generate the set elements.
        for (int i = 0, imax = S; i < imax; i++)
        {
            String randomName = Modules.nameFactory.getARandomName();
            String hook = GLAVScenarioGenerator._stamp + repetition + "S" + _si;
            String name = randomName + "_" + hook;
            SMarkElement setElement = new SMarkElement(name, new Set(), null, fragment, fragmentAppearance);
            setElement.setHook(hook);
            parentElement.addSubElement(setElement);
            // create the variable for its eq class
            Variable var = new Variable("X" + _si);
            eqClassMgr.classify(setElement);
            Object oclass = eqClassMgr.getEqClass(setElement);
            eqClassMgr.setEqClassHook(oclass, var);
            _si++;

            populateSetElement(setElement, configuration, nestingDepth + 1, maxNestingdepth, repetition,
                generator, fragment, fragmentAppearance, eqClassMgr);
        }
    }

}
