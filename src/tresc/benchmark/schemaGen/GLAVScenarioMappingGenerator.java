/*
 *
 * Copyright 2016 Big Data Curation Lab, University of Toronto,
 * 		   	  	  	   				 Patricia Arocena,
 *   								 Boris Glavic,
 *  								 Renee J. Miller
 *
 * This software also contains code derived from STBenchmark as described in
 * with the permission of the authors:
 *
 * Bogdan Alexe, Wang-Chiew Tan, Yannis Velegrakis
 *
 * This code was originally described in:
 *
 * STBenchmark: Towards a Benchmark for Mapping Systems
 * Alexe, Bogdan and Tan, Wang-Chiew and Velegrakis, Yannis
 * PVLDB: Proceedings of the VLDB Endowment archive
 * 2008, vol. 1, no. 1, pp. 230-244
 *
 * The copyright of the ToxGene (included as a jar file: toxgene.jar) belongs to
 * Denilson Barbosa. The iBench distribution contains this jar file with the
 * permission of the author of ToxGene
 * (http://www.cs.toronto.edu/tox/toxgene/index.html)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package tresc.benchmark.schemaGen;

import java.util.Vector;

import smark.support.SMarkElement;
import vtools.dataModel.expression.AND;
import vtools.dataModel.expression.ComparisonOperator;
import vtools.dataModel.expression.ConstantAtomicValue;
import vtools.dataModel.expression.Current;
import vtools.dataModel.expression.EQ;
import vtools.dataModel.expression.Expression;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.Function;
import vtools.dataModel.expression.NOT;
import vtools.dataModel.expression.OR;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Root;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.ValueExpression;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Rcd;
import vtools.dataModel.types.Set;
import vtools.dataModel.types.Type;
import vtools.utils.structures.EqClassManager;
import vtools.utils.structures.ExactAssociativeArray;

public class GLAVScenarioMappingGenerator
{
    public Vector<SPJQuery> generateMapping(SMarkElement[] rootSets, SPJQuery basis, EqClassManager eqClassMgr)
    {
        Vector<SPJQuery> retQueries = new Vector<SPJQuery>();        
        
        // An associative array that keeps for every set its name
        ExactAssociativeArray setNames = new ExactAssociativeArray();

        // An associative array that keeps for every set a Vector with all its
        // atomic attributes
        ExactAssociativeArray setAttributes = new ExactAssociativeArray();

        // give names to the sets and find their atomic attributes
        for (int i = 0, imax = rootSets.length; i < imax; i++)
            initiateSets(rootSets[i], null, setNames, setAttributes);

        Vector<SMarkElement> parentSetStack = new Vector<SMarkElement>();
        for (int i = 0, imax = rootSets.length; i < imax; i++)
        {
            SPJQuery query = new SPJQuery();
            generateMappingQuery(rootSets[i], query, parentSetStack, basis, "", eqClassMgr, setNames,
                setAttributes);
            retQueries.add(query);
        }
        return retQueries;
    }

    /**
     * Traverses the current element and based on its type, it takes the
     * appropriate action. If it is an atomic value, it adds the right things in
     * the select clause, if it is a Rcd it only enhances the current path, and
     * if it is a set, then it creates a subquery, makes the right joins with
     * the parents and then calls its children to populate the select clause.
     */
    private void generateMappingQuery(SMarkElement currElement, SPJQuery query,
            Vector<SMarkElement> parentSetStack, SPJQuery basis, String currAtomicPath, EqClassManager eqClassMgr,
            ExactAssociativeArray setNames, ExactAssociativeArray setAttributes)
    {
        Type t = currElement.getType();
        if (t instanceof Atomic)
        {
            // Get the set in which we are located (the last entry in the stack
            SMarkElement setElement = parentSetStack.elementAt(parentSetStack.size() - 1);
            // Read the suffix we need to use for that element
            String suffix = (String) setNames.getValue(setElement);
            // read the value the atomic element should get
            Object eqClass = eqClassMgr.getEqClass(currElement);
            Expression eqClassValue = (Expression) eqClassMgr.getEqClassHook(eqClass);
            // clone because we will modify it
            Expression elValue = eqClassValue.clone();
            // and suffix its variables
            suffixVariables(elValue, suffix);
            // now create the AS part
            String as = currAtomicPath + currElement.getLabel();
            SelectClauseList select = query.getSelect();
            select.add(as, elValue);
        }
        else if (t instanceof Rcd)
        {
            for (int i = 0, imax = currElement.size(); i < imax; i++)
            {
                SMarkElement child = (SMarkElement) currElement.getSubElement(i);
                String newAtomicPath = currAtomicPath + currElement.getLabel() + ".";
                generateMappingQuery(child, query, parentSetStack, basis, newAtomicPath, eqClassMgr, setNames,
                    setAttributes);
            }
        }
        else if (t instanceof Set)
        {
            // now create the AS part
            String as = currAtomicPath + currElement.getLabel();
            // since it is a set, we are going to create a subquery in the
            // select clause, thus, we clone the basis
            SPJQuery subQuery = basis.clone();
            // find the suffix we need to use for this subQuery and suffix it
            // and add it in the select clause of the current query
            String suffix = (String) setNames.getValue(currElement);
            suffixVariables(subQuery, suffix);
            query.getSelect().add(as, subQuery);

            // Now we need to put the conditions that will make this query
            // consistent with the parent sets.
            createJoinsWithParentSets(subQuery, suffix, parentSetStack, eqClassMgr, setNames, setAttributes);

            // put the set element in the stack for to be known for the
            // subsequent (children)
            parentSetStack.add(currElement);
            // now visit the children
            for (int i = 0, imax = currElement.size(); i < imax; i++)
            {
                SMarkElement child = (SMarkElement) currElement.getSubElement(i);
                generateMappingQuery(child, subQuery, parentSetStack, basis, "", eqClassMgr, setNames,
                    setAttributes);
            }
            // before returning, take out the current set element from the
            // stack. It will be the last element
            parentSetStack.remove(parentSetStack.size() - 1);
        }
    }

    /**
     * Creates a set of join conditions with the parent sets and adds them into
     * the where clause of the provided query. The reason is to perform the
     * right nesting
     */
    private void createJoinsWithParentSets(SPJQuery query, String localSuffix,
            Vector<SMarkElement> parentSetStack, EqClassManager eqClassMgr, ExactAssociativeArray setNames,
            ExactAssociativeArray setAttributes)
    {
        for (int i = 0, imax = parentSetStack.size(); i < imax; i++)
        {
            SMarkElement parentSet = parentSetStack.elementAt(i);
            String parentSetSuffix = (String) setNames.getValue(parentSet);
            Vector<SMarkElement> parentElementAttrs = (Vector<SMarkElement>) setAttributes.getValue(parentSet);
            for (int ji = 0, jimax = parentElementAttrs.size(); ji < jimax; ji++)
            {
                SMarkElement parElAttr = parentElementAttrs.elementAt(ji);
                Object oclass = eqClassMgr.getEqClass(parElAttr);
                ValueExpression attrValue = (ValueExpression) eqClassMgr.getEqClassHook(oclass);
                ValueExpression parAttrExpr = attrValue.clone();
                suffixVariables(parAttrExpr, parentSetSuffix);
                ValueExpression curAttrExpr = attrValue.clone();
                suffixVariables(curAttrExpr, localSuffix);
                EQ eq = new EQ(curAttrExpr, parAttrExpr);
                query.addAndInWhere(eq);
            }
        }
    }

    /**
     * Traverses the schema and collects all the set elements into the names
     * associative array and also all the atomic elements in the respective
     * vector of the set
     */
    private void initiateSets(SMarkElement currEl, SMarkElement currSet, ExactAssociativeArray names,
            ExactAssociativeArray attributes)
    {
        Type type = currEl.getType();
        if (type instanceof Atomic)
        {
            Vector<SMarkElement> attrVector = (Vector<SMarkElement>) attributes.getValue(currSet);
            attrVector.add(currEl);
            return;
        }
        else if (type instanceof Rcd)
        {
            for (int i = 0, imax = currEl.size(); i < imax; i++)
            {
                SMarkElement child = (SMarkElement) currEl.getSubElement(i);
                initiateSets(child, currSet, names, attributes);
            }
            return;
        }
        else if (type instanceof Set)
        {
            int si = names.size();
            String sname = "t" + si;
            names.add(currEl, sname);
            attributes.add(currEl, new Vector<SMarkElement>());
            for (int i = 0, imax = currEl.size(); i < imax; i++)
            {
                SMarkElement child = (SMarkElement) currEl.getSubElement(i);
                initiateSets(child, currEl, names, attributes);
            }
            return;
        }
        else
        {
            throw new RuntimeException("Should not happen 76876287628768787");
        }

    }


    /**
     * Finds the variables in the provided expression and suffixes them with the
     * provided string
     */
    private void suffixVariables(Object exp, String suffix)
    {
        if (exp == null)
            return;

        if (exp instanceof SPJQuery)
        {
            SPJQuery q = (SPJQuery) exp;
            suffixVariables(q.getSelect(), suffix);
            suffixVariables(q.getFrom(), suffix);
            suffixVariables(q.getWhere(), suffix);
        }
        else if (exp instanceof SelectClauseList)
        {
            SelectClauseList select = (SelectClauseList) exp;
            for (int i = 0, imax = select.size(); i < imax; i++)
                suffixVariables(select.getTerm(i), suffix);
        }
        else if (exp instanceof FromClauseList)
        {
            FromClauseList from = (FromClauseList) exp;
            for (int i = 0, imax = from.size(); i < imax; i++)
            {
                suffixVariables(from.getExpression(i), suffix);
                suffixVariables(from.getExprVar(i), suffix);
            }
        }
        else if (exp instanceof Function)
        {
            Function f = (Function) exp;
            for (int i = 0, imax = f.getNumOfArgs(); i < imax; i++)
            {
                Expression arg = f.getArg(i);
                suffixVariables(arg, suffix);
            }
        }
        else if (exp instanceof ConstantAtomicValue)
            return;
        else if (exp instanceof AND)
        {
            AND op = (AND) exp;
            for (int i = 0, imax = op.size(); i < imax; i++)
                suffixVariables(op.getComponent(i), suffix);
        }
        else if (exp instanceof OR)
        {
            OR op = (OR) exp;
            for (int i = 0, imax = op.size(); i < imax; i++)
                suffixVariables(op.getComponent(i), suffix);
        }
        else if (exp instanceof NOT)
        {
            NOT op = (NOT) exp;
            for (int i = 0, imax = op.size(); i < imax; i++)
                suffixVariables(op.getExpr(), suffix);
        }
        else if (exp instanceof Projection)
        {
            Projection proj = (Projection) exp;
            suffixVariables(proj.getPrefix(), suffix);
        }
        else if (exp instanceof ComparisonOperator)
        {
            ComparisonOperator comp = (ComparisonOperator) exp;
            suffixVariables(comp.getLeft(), suffix);
            suffixVariables(comp.getRight(), suffix);
        }
        else if (exp instanceof Root)
            return;
        else if (exp instanceof Current)
            return;
        else if (exp instanceof Variable)
        {
            Variable v = (Variable) exp;
            v.setName(v.getName() + suffix);
        }
        else throw new RuntimeException("Code 66677: Do not know how to handle " + exp.getClass().getName());
    }
}
