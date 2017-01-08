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
package vtools.dataModel.expression;

import vtools.VObject;
import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class Rule extends VObject implements Visitable, Cloneable
{
    protected FromClauseList _leftTerms;

    protected FromClauseList _rightTerms;

    protected BooleanExpression _leftCond;

    protected BooleanExpression _rightCond;

    public Rule()
    {
        _leftTerms = new FromClauseList();
        _rightTerms = new FromClauseList();
        _leftCond = null;
        _rightCond = null;
    }

    public void addLeftConjunct(BooleanExpression cond)
    {
        _leftCond = addConjunct(_leftCond, cond);
    }

    public void addRightConjunct(BooleanExpression cond)
    {
        _rightCond = addConjunct(_rightCond, cond);
    }

    private BooleanExpression addConjunct(BooleanExpression oldCond, BooleanExpression newCond)
    {
        if (newCond == null)
            return oldCond;
        else if (oldCond == null)
            return newCond;
        else if (oldCond instanceof AND)
        {
            ((AND) oldCond).add(newCond);
            return oldCond;
        }
        else
        {
            if (newCond instanceof AND)
            {
                ((AND) newCond).add(oldCond);
                return newCond;
            }
            else
            {
                AND and = new AND();
                and.add(oldCond);
                and.add(newCond);
                return and;
            }
        }
    }

    public void addLeftTerm(Variable var, Path path)
    {
        // check that the variable name of the term already exists
        Path root = path.getRoot();
        if (root instanceof Variable)
        {
            Variable v = (Variable) root;
            int pos = _leftTerms.getExprVarPosition(v);
            if (pos == -1)
                throw new RuntimeException("Variable " + v + " does not exist in the existing terms");
        }
        _leftTerms.add(var, path);
    }

    public void addRightTerm(Variable var, Path path)
    {
        // check that the variable name of the term already exists
        Path root = path.getRoot();
        if (root instanceof Variable)
        {
            Variable v = (Variable) root;
            int pos = _rightTerms.getExprVarPosition(v);
            if (pos == -1)
                throw new RuntimeException("Variable " + v + " does not exist in the existing terms");
        }
        _rightTerms.add(var, path);
    }

    public FromClauseList getLeftTerms()
    {
        return _leftTerms;
    }

    public FromClauseList getRightTerms()
    {
        return _rightTerms;
    }

    public BooleanExpression getLeftConditions()
    {
        return _leftCond;
    }

    public BooleanExpression getRightConditions()
    {
        return _rightCond;
    }

    public Rule clone()
    {
        Rule rule = (Rule) super.clone();
        rule._leftTerms = _leftTerms.clone();
        rule._rightTerms = _rightTerms.clone();
        rule._leftCond = _leftCond.clone();
        rule._rightCond = _rightCond.clone();
        return rule;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Rule))
            return false;
        if (!super.equals(o))
            return false;
        Rule rule = (Rule) o;
        if (!rule._leftTerms.equals(_leftTerms))
            return false;
        if (!rule._rightTerms.equals(_rightTerms))
            return false;
        if (!rule._leftCond.equals(_leftCond))
            return false;
        if (!rule._rightCond.equals(_rightCond))
            return false;
        return true;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
