package tresc.benchmark.schemaGen;

import java.util.Random;
import java.util.Vector;

import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Modules;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.ConstantAtomicValue;
import vtools.dataModel.expression.ForeignKey;
import vtools.dataModel.expression.Function;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.ValueExpression;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Rcd;
import vtools.dataModel.types.Set;
import vtools.dataModel.types.Type;
import vtools.dataModel.values.StringValue;
import vtools.utils.structures.EqClassManager;


/**
 * Used to perform the process of generation of the target schema in the GLAV
 * mappings scenario. This is a component of the GLAVScenarion Generator.
 * 
 */
public class GLAVScenarioTrgGenerator
{
    private static int FROM = 0;

    private static int TO = 1;

    // used to generate new ids for the skolem names we generate
    private static int _skolemNum = 0;

    public Object[] generateTargetSchema(Vector<SMarkElement> atomicElements, Configuration configuration,
            Random generator, EqClassManager eqClassMgr, int repetition)
    {
        SMarkElement[] constantElements = createConstantElements(atomicElements.size(), generator, eqClassMgr,
            repetition);
        SMarkElement[] combElements = createCombinatorialElements(atomicElements, configuration, generator,
            eqClassMgr, repetition);
        SMarkElement[] surrogateKeyElements = createSurrogateElements(atomicElements, configuration, generator,
            eqClassMgr, repetition);
        atomicElements = ignoreSomeElements(atomicElements, generator);
        Vector<SMarkElement>[] fragments = fragmentsAttributeSet(atomicElements, configuration, generator);
        
        //DEBUG
        /*for (int i = 0, imax = fragments.length; i < imax; i++)
        {
            log.debug(" fragment "+i+ " " + fragments[i]);
        }*/
        
        shuffle(fragments, generator);

        //DEBUG
        /*for (int i = 0, imax = fragments.length; i < imax; i++)
        {
            log.debug(" after Shuffle fragment "+i+ " " + fragments[i]);
        }*/
        
        distribute(constantElements, fragments, generator);
        
        //DEBUG
        /*for (int i = 0, imax = fragments.length; i < imax; i++)
        {
            log.debug(" after Distribute Constants fragment "+i+ " " + fragments[i]);
        }*/
        
        distribute(combElements, fragments, generator);
        distribute(surrogateKeyElements, fragments, generator);
        
        //DEBUG
        /*for (int i = 0, imax = fragments.length; i < imax; i++)
        {
            log.debug(" after Distribute fragment "+i+ " " + fragments[i]);
        }*/

        // and now nest each fragment
        SMarkElement[] rootSets = new SMarkElement[fragments.length];
        for (int i = 0, imax = fragments.length; i < imax; i++)
        {
            rootSets[i] = nest(fragments[i], configuration, generator, 1, repetition);
        }

        // and finally do the join conditions between the fragments
        Vector<ForeignKey> constraints = generateConstraints(rootSets, configuration, generator, eqClassMgr);

        Object[] retVal = new Object[2];
        retVal[0] = rootSets;
        retVal[1] = constraints;

        return retVal;
    }

    /**
     * Puts a set of elements in the fragments. It first puts 1 at each fragment
     * and if there are more, it starts distributing them randomly
     */
    private void distribute(SMarkElement[] elements, Vector<SMarkElement>[] fragments, Random generator)
    {

        int elemi = 0;
        // first we do 1 at each fragment, in a way that we start either from
        // the top or from the bottom.
        int incr = generator.nextInt(2);
        if (incr == 0)
        {
            // we go from top
            for (int i = 0, imax = fragments.length; ((elemi < elements.length) && (i < imax)); elemi++, i++)
            {
                int pos = generator.nextInt(fragments[i].size());
                fragments[i].insertElementAt(elements[elemi], pos);
            }
        }
        else
        {
            // we go from bottom
            for (int i = (fragments.length - 1); ((elemi < elements.length) && (i >= 0)); elemi++, i--)
            {
                int pos = generator.nextInt(fragments[i].size());
                fragments[i].insertElementAt(elements[elemi], pos);
            }
        }

        // now the rest are randomly put
        for (int i = elemi, imax = elements.length; i < imax; i++)
        {
            int fi = generator.nextInt(fragments.length);
            int pos = generator.nextInt(fragments[fi].size());
            fragments[fi].insertElementAt(elements[elemi], pos);
            elemi++;
        }
    }

    /**
     * Performs some shuffling to the attributes in the fragments. The shuffling
     * is as follows. For every fragment, 2 elements are sent to the one below,
     * and one is sent to the one above (below means greater index, above means
     * smaller index)
     */
    private void shuffle(Vector<SMarkElement>[] fragments, Random generator)
    {
        int F = fragments.length;
        SMarkElement[] forUp = new SMarkElement[F];
        SMarkElement[] forDown1 = new SMarkElement[F];
        SMarkElement[] forDown2 = new SMarkElement[F];
        // first select what we want to move
        for (int i = 0, imax = F; i < imax; i++)
        {
            if (i != 0)
            {
                int s = fragments[i].size();
                int random = generator.nextInt(s);
                forUp[i] = (SMarkElement) fragments[i].remove(random);
            }

            if (i != (F - 1))
            {
                int s = fragments[i].size();
                int random = generator.nextInt(s);
                forDown1[i] = (SMarkElement) fragments[i].remove(random);
                random = generator.nextInt(s - 1);
                forDown2[i] = (SMarkElement) fragments[i].remove(random);
            }
        }
        // and now do the moving
        for (int i = 0, imax = F; i < imax; i++)
        {
            if (i != 0)
            {
                fragments[i].add(forDown1[i - 1]);
                fragments[i].add(forDown2[i - 1]);
            }

            if (i != (F - 1))
            {
                fragments[i].add(forUp[i + 1]);
            }
        }
    }

    /**
     * Splits the attributes into a set of fragments
     */
    private Vector<SMarkElement>[] fragmentsAttributeSet(Vector<SMarkElement> atomicElements,
            Configuration configuration, Random generator)
    {
        int A = atomicElements.size();

        int fragm = configuration.getParam(Constants.ParameterName.JoinSize);
        int fragmDeviation = configuration.getDeviation(Constants.ParameterName.JoinSize);
        int F = Utils.getRandomNumberAroundSomething(generator, fragm, fragmDeviation);
        // at least one fragment
        F = (F < 1) ? 1 : F;
        // we do not want too many fragments so that each fragment has at least
        // 3 attributes.
        F = ((A / F) < 3) ? (A / 3) : F;
        // check again because due to the division it may get the value 0
        F = (F == 0) ? 1 : F;

        // we basically divide the atomic attributes to F sets (note that each
        // set has the same number of attributes.
        Vector<SMarkElement>[] fragments = (Vector<SMarkElement>[]) new Vector[F];
        int pos = 0;
        for (int i = 0, imax = fragments.length; i < imax; i++)
        {
            fragments[i] = new Vector<SMarkElement>();
            int maxAttrInFragment = A / F;
            maxAttrInFragment = (maxAttrInFragment < 1) ? 1 : maxAttrInFragment;
            for (int k = 0; (k < maxAttrInFragment) && (pos < A); pos++, k++)
                fragments[i].add(atomicElements.get(pos));
        }
        // if there are any left overs we just add them to the last fragments
        for (int fi = F - (A - pos); pos < A; pos++, fi = F - (A - pos))
            fragments[fi].addElement(atomicElements.get(pos));
        return fragments;
    }

    /**
     * It takes out a 5% of the attributes in order to have attributes that do
     * not make it to the source. It does nothing if the num of elements is
     * smaller than 4
     */
    private Vector<SMarkElement> ignoreSomeElements(Vector<SMarkElement> atomicElements, Random generator)
    {
        int A = atomicElements.size();
        if (A < 4)
            return atomicElements;
        int tmp = (int) (A * 0.05);
        tmp = ((tmp < 1) && (atomicElements.size() > 2)) ? 1 : tmp;

        for (int i = 0, imax = tmp; i < imax; i++)
        {
            int tempi = generator.nextInt(atomicElements.size());
            atomicElements.remove(tempi);
        }
        return atomicElements;
    }

    /**
     * Generates a number of elements that get a new fresh value every time they
     * are called. (Surrogate Keys)
     */
    private SMarkElement[] createSurrogateElements(Vector<SMarkElement> atomicElements,
            Configuration configuration, Random generator, EqClassManager eqClassMgr, int repetition)
    {
        // We generate by default 5% of the atomic elements as surrogate keys
        // (or 3) whichever is larger
        int S = atomicElements.size() * 5 / 100;
        S = (S < 3) ? 3 : S;
        S = (configuration.getScenarioRepetitions(Constants.ScenarioName.SURROGATEKEY.ordinal()) == 0) ? 0 : S;
        SMarkElement[] surrAttr = new SMarkElement[S];
        for (int si = 0; si < S; si++)
        {
            String humanName = Modules.nameFactory.getARandomName();
            String coding = GLAVScenarioGenerator._stamp + repetition + "Surr" + si;
            String name = humanName + "_" + coding;
            SMarkElement el = new SMarkElement(name, Type.STRING, null, -1, -1);
            surrAttr[si] = el;
            // create a function
            Function f = new Function("generate-id");
            f.addArg(Path.CURRENT);
            // create an eqClass that has a that function as a value
            eqClassMgr.classify(el);
            Object elClass = eqClassMgr.getEqClass(el);
            eqClassMgr.setEqClassHook(elClass, f);
        }
        return surrAttr;
    }

    /**
     * It generates elements that belong into an eqClass with a value being a
     * combination of elements (e.g. a concatenation function) of elements from
     * the source.
     */
    private SMarkElement[] createCombinatorialElements(Vector<SMarkElement> atomicElements,
            Configuration configuration, Random generator, EqClassManager eqClassMgr, int repetition)
    {
        int argParam = configuration.getParam(Constants.ParameterName.NumOfParamsInFunctions);
        int argParamDeviation = configuration.getDeviation(Constants.ParameterName.NumOfParamsInFunctions);

        // the number of constant attributes we are going to generate. By
        // default this is 5% of the atomic elements or 3, whichever is larger.

        int C = atomicElements.size() * 5 / 100;
        C = (C < 3) ? 3 : C;
        // if Value management case is 0 then no such elements are created
        C = (configuration.getScenarioRepetitions(Constants.ScenarioName.VALUEMANAGEMENT.ordinal()) == 0) ? 0 : C;
        SMarkElement[] combValueAttr = new SMarkElement[C];
        for (int ci = 0; ci < C; ci++)
        {
            String humanName = Modules.nameFactory.getARandomName();
            String coding = GLAVScenarioGenerator._stamp + repetition + "Comb" + ci;
            String name = humanName + "_" + coding;
            SMarkElement el = new SMarkElement(name, Type.STRING, null, -1, -1);
            el.setHook(el);
            combValueAttr[ci] = el;
            // create a function
            Function f = new Function("CONCAT");
            // create an eqClass that has a that function as a value}
            eqClassMgr.classify(el);
            Object elClass = eqClassMgr.getEqClass(el);
            eqClassMgr.setEqClassHook(elClass, f);

            // and now we build the function arguments (after randomly deciding
            // how many that will be)
            int A = Utils.getRandomNumberAroundSomething(generator, argParam, argParamDeviation);
            A = (A < 1) ? 1 : A;
            for (int ai = 0; ai < A; ai++)
            {
                // choose a random element and get its value.
                int eli = generator.nextInt(atomicElements.size());
                SMarkElement tmpEl = atomicElements.elementAt(eli);
                Object tmpElClass = eqClassMgr.getEqClass(tmpEl);
                ValueExpression exp = (ValueExpression) eqClassMgr.getEqClassHook(tmpElClass);
                f.addArg(exp);
            }
        }
        return combValueAttr;
    }

    /**
     * Generates and returns a table of SMarkElements that need to get a
     * constant value. This is done by classifying them to eqClasses that have a
     * hook (i.e., value) which is an constant value.
     */
    private SMarkElement[] createConstantElements(int numOftrgElements, Random generator,
            EqClassManager eqClassMgr, int repetition)
    {
        // the number of constant attributes we are going to generate will be 5%
        // of the atomic elements we have or 3 which ever is larger.
        int C = numOftrgElements * 5 / 100;
        C = (C < 3) ? 3 : C;
        SMarkElement[] constValueAttr = new SMarkElement[C];
        for (int ci = 0; ci < C; ci++)
        {
            String humanName = Modules.nameFactory.getARandomName();
            String coding = GLAVScenarioGenerator._stamp + repetition + "Const" + ci;
            String name = humanName + "_" + coding;
            SMarkElement el = new SMarkElement(name, Type.STRING, null, -1, -1);
            el.setHook(coding);
            
            //DEBUG
            //log.debug(" constant el "+el);
            
            // create a constant value
            String value = Modules.nameFactory.getARandomName();
            ConstantAtomicValue constValExpr = new ConstantAtomicValue(new StringValue(value));
            eqClassMgr.classify(el);
            Object elClass = eqClassMgr.getEqClass(el);
            eqClassMgr.setEqClassHook(elClass, constValExpr);
            constValueAttr[ci] = el;
        }
        return constValueAttr;
    }

    /**
     * Takes a list of attributes and generates the nested Sets
     */
    private SMarkElement nest(Vector<SMarkElement> elements, Configuration configuration, Random generator,
            int rootNum, int repetition)
    {
        // first we decide the depth D
        int depthParam = configuration.getParam(Constants.ParameterName.NestingDepth);
        int depthParamDeviation = configuration.getDeviation(Constants.ParameterName.NestingDepth);
        int D = Utils.getRandomNumberAroundSomething(generator, depthParam, depthParamDeviation);
        D = (D < 1) ? 1 : D;

        int A = elements.size();

        // log.debug("A=" + A + " D=" + D);

        int[][] counters = new int[D][D];

        for (int line = 0; line < D; line++)
            for (int col = 0; col < D; col++)
                counters[line][col] = 0;


        // this counter is used to keep track how many I have left to distribute
        int remain = A / 2;
        for (int line = 0; ((remain > 0) && (line < D)); line++)
        {
            // traverse this line and all those below
            for (int currLine = line; ((remain > 0) && (currLine >= 0)); currLine--)
            {
                for (int col = currLine; ((remain > 0)) && (col < D); col++)
                {
                    counters[currLine][col]++;
                    remain--;
                }
            }
        }

        // There is a chance that there are more attributes that remain to be
        // distributed. These are completely random. But keep in mind that now
        // the whole triangle of the counters table is filled in so we can go
        // for it easily.
        for (; remain > 0; remain--)
        {
            int line = generator.nextInt(D);
            int col = generator.nextInt(D - line) + line;
            counters[line][col]++;
        }

        SMarkElement[][] rootSets = new SMarkElement[D][D];
        int setNum = 0;

        for (int line = 0; line < D; line++)
            for (int col = 0; col < D; col++)
                if (counters[line][col] == 0)
                    rootSets[line][col] = null;
                else
                {
                    String name = Modules.nameFactory.getARandomName();
                    name = name + "_" + GLAVScenarioGenerator._stamp + repetition + "R" + +rootNum + "S" + setNum;
                    setNum++;
                    rootSets[line][col] = new SMarkElement(name, new Set(), null, -1, -1);
                }

        // This table is telling you which is the parent element of each set.
        // The entry in position X,Y is a pair of two integers that specify the
        // parent. First set everything to nothing-pointing
        int[][][] parentElement = new int[D][D][2];
        for (int line = 0; line < D; line++)
            for (int col = 0; col < D; col++)
            {
                parentElement[line][col][0] = -1;
                parentElement[line][col][1] = -1;
            }

        // add each attribute in the first line under the set on its left (this
        // will guarantee the depth)
        for (int col = 0; col < (D - 1); col++)
        {
            if (rootSets[0][col + 1] != null)
            {
                parentElement[0][col + 1][0] = 0;
                parentElement[0][col + 1][1] = col;
            }
        }

        // and now we add the rest of the sets under one of their sets on their
        // column on the left. (anyone)
        for (int line = 1; line < D; line++)
            for (int col = line; col < D; col++)
            {
                // if there is no set to hang, we do nothing
                SMarkElement childSet = rootSets[line][col];
                if (childSet == null)
                    continue;
                // on the left of the rootSets[line][col], i.e., on column
                // col-1, there are at most col-1 sets but the exact number
                // is not known so we count them
                int count = 0;
                for (int i = 0; i < D; i++)
                    if (rootSets[i][col - 1] != null)
                        count++;
                // select randomly one of those that exist (they are all at the
                // bottom cells of the column)
                int choosenSet = generator.nextInt(count);
                parentElement[line][col][0] = choosenSet;
                parentElement[line][col][1] = col - 1;
            }

        // We put the set elements under the corresponding parent as decided
        // above.
        for (int line = 0; line < D; line++)
            for (int col = line; col < D; col++)
                if (parentElement[line][col][0] == -1)
                    continue;
                else
                {
                    SMarkElement child = rootSets[line][col];
                    SMarkElement parent = rootSets[parentElement[line][col][0]][parentElement[line][col][1]];
                    parent.addSubElement(child);
                }

        // now we create a copy of the rootSets table
        SMarkElement[][] rootSetsCopy = new SMarkElement[D][D];
        for (int line = 0; line < D; line++)
            for (int col = 0; col < D; col++)
            {
                SMarkElement set = rootSets[line][col];
                if (set == null)
                    rootSetsCopy[line][col] = null;
                else
                {
                    String name = Modules.nameFactory.getARandomName();
                    name = name + "_" + GLAVScenarioGenerator._stamp + repetition + "R" + +rootNum + "S" + setNum;
                    setNum++;
                    rootSetsCopy[line][col] = new SMarkElement(name, new Set(), null, -1, -1);
                }
            }

        // And as we did before we create the parent-child relationship but this
        // time for the rootSetsCopy. We ignore the first line (the 0) because
        // for 0 we use the elements of the rootElements table (not of the copy)
        for (int line = 1; line < D; line++)
            for (int col = line; col < D; col++)
                if (parentElement[line][col][0] == -1)
                    continue;
                else
                {
                    SMarkElement child = rootSetsCopy[line][col];
                    int parentLine = parentElement[line][col][0];
                    int parentCol = parentElement[line][col][1];
                    SMarkElement parent = null;
                    if (parentLine == 0)
                        parent = rootSets[parentLine][parentCol];
                    else parent = rootSetsCopy[parentLine][parentCol];
                    parent.addSubElement(child);
                }

        // now the tree hierarchy has been created.
        // we measure the number of set attributes that exist.
        int numOfSetAttrs = 0;
        for (int line = 0; line < D; line++)
            for (int col = line; col < D; col++)
                if (rootSets[line][col] != null)
                    numOfSetAttrs++;
        for (int line = 1; line < D; line++)
            for (int col = line; col < D; col++)
                if (rootSetsCopy[line][col] != null)
                    numOfSetAttrs++;



        // decide how many attributes you are going to put in each set
        int N = elements.size() / numOfSetAttrs;
        fillWithAtomicAttributes(elements, N, 0, rootSets[0][0]);



        // for (int col = 0; col < D; col++)
        // {
        // for (int line = D - 1; line >= 0; line--)
        // {
        // if (rootSets[line][col] != null)
        // log.debug("\t" + rootSets[line][col].size());
        // else log.debug("X");
        // log.debug("");
        // }
        // }

        // StringBuffer buf = new StringBuffer();
        // rootSets[0][0].prettyPrint(buf, 0);
        // log.debug(buf.toString());

        // System.exit(0);
        // return null;
        return rootSets[0][0];
    }

    /**
     * performs a breath first traversal of the tree of sets and populates it
     * with the atomic elements. It returns the next element position to use.
     */
    private int fillWithAtomicAttributes(Vector<SMarkElement> elements, int numOfAtomicAttrs, int elementPos,
            SMarkElement currentSet)
    {
        int totalNumOfAtomicElements = elements.size();

        // make a copy of the set elements
        Vector<SMarkElement> tmpSets = new Vector<SMarkElement>();
        for (int i = 0, imax = currentSet.size(); i < imax; i++)
            tmpSets.addElement((SMarkElement) currentSet.getSubElement(i));

        // add the atomic elements that you need to add.
        for (int i = 0, imax = numOfAtomicAttrs; (i < imax) && (elementPos < totalNumOfAtomicElements); i++)
        {
            currentSet.addSubElementAt(elements.elementAt(elementPos), i);
            elementPos++;
        }

        // check if the atomic elements that remain are less than
        // numOfAtomicAttrs. If yes, it means that we have reached the end set
        // and thus we add those as well.
        int remained = elements.size() - elementPos;
        if (remained < numOfAtomicAttrs)
        {
            for (int i = 0, imax = remained; i < imax; i++)
            {
                currentSet.addSubElementAt(elements.elementAt(elementPos), numOfAtomicAttrs + i);
                elementPos++;
            }
            return elementPos;
        }

        // if we are not in the last set, we visit the child sets (if any) in
        // order to populate them as well
        for (int i = 0, imax = tmpSets.size(); i < imax; i++)
        {
            SMarkElement setEl = tmpSets.elementAt(i);
            elementPos = fillWithAtomicAttributes(elements, numOfAtomicAttrs, elementPos, setEl);
        }

        return elementPos;
    }

    /**
     * Generates the join constraints.
     */
    private Vector<ForeignKey> generateConstraints(SMarkElement[] rootSets, Configuration configuration,
            Random generator, EqClassManager eqClassMgr)
    {
        int F = rootSets.length;
        Vector<ForeignKey> constraints = new Vector<ForeignKey>(F - 1);

        // collect from each root set the set elements it has
        Vector<SMarkElement>[] setElements = new Vector[F];
        for (int i = 0, imax = F; i < imax; i++)
        {
            SMarkElement root = rootSets[i];
            Vector<SMarkElement> v = new Vector<SMarkElement>();
            collectSetElements(root, v);
            setElements[i] = v;
        }

        // decide the kind of join we will do
        int joinKind = configuration.getParam(Constants.ParameterName.JoinKind);
        if (joinKind == Constants.JoinKind.VARIABLE.ordinal())
        {
            int randomInt = generator.nextInt(2);
            joinKind = (randomInt == 0) ? Constants.JoinKind.CHAIN.ordinal() : Constants.JoinKind.STAR.ordinal();
        }
        // we keep all the atomic elements on which foreign keys are
        // defined so that we merge their eqClasses at the end
        SMarkElement[][][] allPairs = new SMarkElement[F][][];

        for (int i = 1; i < F; i++)
        {
            // decide how many attributes to be used in the join
            int joinWidth = configuration.getParam(Constants.ParameterName.NumOfJoinAttributes);
            int joinWidthDeviation = configuration.getDeviation(Constants.ParameterName.NumOfJoinAttributes);
            // select a join width. Note that the number above may be 0 in which
            // case we have a cartesian product.
            int numOfJoinAttr = Utils.getRandomNumberAroundSomething(generator, joinWidth, joinWidthDeviation);

            int fromFr = (joinKind == Constants.JoinKind.CHAIN.ordinal()) ? i : 0;
            int toFr = (joinKind == Constants.JoinKind.CHAIN.ordinal()) ? (i - 1) : i;

            // select a random set from the fragment fromFr
            int numberOfSetsInFr = setElements[fromFr].size();
            int setNum = generator.nextInt(numberOfSetsInFr);
            SMarkElement fromSetElement = setElements[fromFr].elementAt(setNum);
            // collect the atomic elements of the set element
            Vector<SMarkElement> fromAtomicSubElem = new Vector<SMarkElement>();
            collectAtomicElements(fromSetElement, fromAtomicSubElem, true);

            // select a random set from the fragment toFr
            numberOfSetsInFr = setElements[toFr].size();
            setNum = generator.nextInt(numberOfSetsInFr);
            SMarkElement toSetElement = setElements[toFr].elementAt(setNum);
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

            // now that we know the attributes we are going to create the FK
            // constraint
            Object[] o = new Object[1];
            o[0] = new Integer(0);
            ForeignKey fKeySrc = new ForeignKey();
            Vector<Variable> varsF = new Vector<Variable>();
            Vector<Path> exprsF = new Vector<Path>();
            Path vf = getPathTerms(fromSetElement, varsF, exprsF, o);
            for (int k = 0, kmax = varsF.size(); k < kmax; k++)
            {
                fKeySrc.addLeftTerm(varsF.elementAt(k), exprsF.elementAt(k));
            }
            Vector<Variable> varsT = new Vector<Variable>();
            Vector<Path> exprsT = new Vector<Path>();
            Path vt = getPathTerms(toSetElement, varsT, exprsT, o);
            for (int k = 0, kmax = varsT.size(); k < kmax; k++)
            {
                fKeySrc.addRightTerm(varsT.elementAt(k), exprsT.elementAt(k));
            }

            for (int j = 0; j < numOfJoinAttr; j++)
            {
                // get the path to this element from the closest set
                SMarkElement fromEl = pairs[j][FROM];
                Path pathFrom = getPathFromParentSet(fromEl, vf);
                SMarkElement toEl = pairs[j][TO];
                Path pathTo = getPathFromParentSet(toEl, vt);
                fKeySrc.addFKeyAttr(pathTo, pathFrom);
            }
            constraints.add(fKeySrc);
        }


        // we merge the eqClasses for all the pairs
        for (int i = 1; i < F; i++)
        {
            SMarkElement[][] pairs = allPairs[i];
            for (int j = 0, jmax = pairs.length; j < jmax; j++)
            {
                Object oclassF = eqClassMgr.getEqClass(pairs[j][FROM]);
                Object oclassT = eqClassMgr.getEqClass(pairs[j][TO]);
                // F becomes whatever T is unless F is of a function value
                Object value = eqClassMgr.getEqClassHook(oclassF);
                if (value instanceof Function)
                    eqClassMgr.mergeEqClass(oclassT, oclassF);
                else eqClassMgr.mergeEqClass(oclassF, oclassT);
            }
        }
        return constraints;

    }

    /**
     * Traverses a tree and collects its set elements
     */
    private void collectSetElements(SMarkElement currEl, Vector<SMarkElement> v)
    {
        Type type = currEl.getType();
        if (type instanceof Atomic)
            return;
        else if (type instanceof Rcd)
        {
            for (int i = 0, imax = currEl.size(); i < imax; i++)
            {
                SMarkElement child = (SMarkElement) currEl.getSubElement(i);
                collectSetElements(child, v);
            }
        }
        else if (type instanceof Set)
        {
            v.add(currEl);
            for (int i = 0, imax = currEl.size(); i < imax; i++)
            {
                SMarkElement child = (SMarkElement) currEl.getSubElement(i);
                collectSetElements(child, v);
            }
        }
        else
        {
            throw new RuntimeException("Should not happen 8234432894324");
        }
    }

    /**
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

    /**
     * Returns for each set the terms you put in the generators of a foreign
     * key.
     */
    private Path getPathTerms(SMarkElement curEl, Vector<Variable> vars, Vector<Path> exprs, Object[] varIndex)
    {
        Path parentPath = null;
        SMarkElement parent = (SMarkElement) curEl.getParent();
        if (parent == null)
            parentPath = Path.ROOT;
        else parentPath = getPathTerms(parent, vars, exprs, varIndex);

        Type t = curEl.getType();
        if (t instanceof Rcd)
        {
            Path retP = new Projection(parentPath, curEl.getLabel());
            return retP;
        }
        else if (t instanceof Atomic)
        {
            Path retP = new Projection(parentPath, curEl.getLabel());
            return retP;
        }
        else if (t instanceof Set)
        {
            Path retP = new Projection(parentPath, curEl.getLabel());
            Integer valInt = (Integer) varIndex[0];
            int index = valInt.intValue();
            Variable v = new Variable("X" + index);
            index++;
            valInt = new Integer(index);
            varIndex[0] = valInt;
            vars.add(v);
            exprs.add(retP);
            retP = v.clone();
            return retP;
        }
        else throw new RuntimeException("Should not happen 7823423942");
    }

    /**
     * Returns a path that starts from the first set type ancestor. The path is
     * a set of projections and starts either with the variable vf or with the
     * root path.
     */
    private Path getPathFromParentSet(SMarkElement curEl, Path vf)
    {
        Path parentPath = null;
        SMarkElement parent = (SMarkElement) curEl.getParent();
        if (parent == null)
            parentPath = Path.ROOT;
        else if (parent.getType() instanceof Set)
            parentPath = vf;
        else parentPath = getPathFromParentSet(parent, vf);

        Path retP = new Projection(parentPath, curEl.getLabel());
        return retP;
    }

}