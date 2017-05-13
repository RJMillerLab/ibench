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

import java.util.Vector;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

/**
 * A key constraint is a special egd rule that has the property that does not
 * have any left-side side conditions other than those referring to a key. and
 * the right side is only an EQ condition that specify what element has that
 * key.
 */
public class Key extends Egd implements Visitable, Cloneable
{
    private int _contextGens;

    public Key()
    {
        super();
        _contextGens = 0;
    }

    public Key clone()
    {
        Key key = (Key) super.clone();
        key._contextGens = _contextGens;
        return key;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Key))
            return false;
        if (!super.equals(o))
            return false;
        Key key = (Key) o;
        if (key._contextGens != _contextGens)
            return false;
        return true;
    }

    public void addContextLeftTerm(Variable var, Path path)
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
        _contextGens++;
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
        Path path2 = path.clone();
        Variable var2 = new Variable(var.getName() + "_2");
        _leftTerms.add(var, path);
        _leftTerms.add(var2, path2);
    }

    public void addRightTerm(Variable var, Path path)
    {
        throw new RuntimeException("Keys cannot have terms on the right hand side");
    }

    public FromClauseList getRightTerms()
    {
        return new FromClauseList();
    }

    public void addLeftConjunct(BooleanExpression cond)
    {
        throw new RuntimeException("Not used for Keys. Use the addKeyAttr instead");
    }

    public void addKeyAttr(Path path)
    {
        Path root = path.getRoot();
        if (!(root instanceof Variable))
            throw new RuntimeException("the key attr expression is not starting w/ a variable");
        // the variable must be from the left part
        int pos = super.getLeftTerms().getExprVarPosition((Variable) root);
        if (pos == -1)
            throw new RuntimeException("Left expression not in the left side terms");

        // So we are ok. We now make a copy of the path and make its first
        // variable to be suffixed with "_2"
        Path path2 = path.clone();
        Variable var = (Variable) path2.getRoot();
        var.setName(var.getName() + "_2");

        EQ eq = new EQ(path, path2);
        super.addLeftConjunct(eq);
    }

    /**
     * The right-side condition has to be one and only one. Thus this method is
     * de-activated
     */
    public void addRightConjunct(BooleanExpression cond)
    {
        throw new RuntimeException("Use the setEqElement instead");
    }

    public void setEqualElement(Path path)
    {
        Path root = path.getRoot();
        if (!(root instanceof Variable))
            throw new RuntimeException("Expr not starting with a variable");
        // the variable must be from the left part
        int pos = _leftTerms.getExprVarPosition((Variable) root);
        if (pos == -1)
            throw new RuntimeException("Variable " + root + " not found in the terms");

        Path path2 = path.clone();
        Variable var = (Variable) path2.getRoot();
        var.setName(var.getName() + "_2");

        EQ eq = new EQ(path, path2);
        super._rightCond = eq;
    }


    /**
     * Returns the Key in the form of a set of strings. The first is the element
     * that has the key and the others are the attributes.
     */
    public String[][] getHumanReadableRepresentation()
    {
        AND and = null;
        if (_leftCond instanceof EQ)
        {
            and = new AND();
            and.add(_leftCond);
        }
        else if (_leftCond instanceof AND)
        {
            and = (AND) _leftCond;
        }
        else throw new RuntimeException("Should not happen");

        String[][] retVal = new String[1 + and.size()][];

        Path p = (Path) ((EQ) _rightCond).getLeft();
        retVal[0] = stringisize(p, _leftTerms, null);

        // Read the right condition
        EQ eq = (EQ) _rightCond;
        if (eq == null)
            throw new RuntimeException("Cannot print an incomplete key");
        Variable cutoff = (Variable) eq.getLeft();

        for (int i = 0, imax = and.size(); i < imax; i++)
        {
            p = (Path) ((EQ) and.getComponent(i)).getLeft();
            retVal[i + 1] = stringisize(p, _leftTerms, cutoff);
        }
        return retVal;
    }

    /*
     * Returns the full path of an element in an expression
     */
    private String[] stringisize(Path path, FromClauseList from, Variable cutoff)
    {
        Vector<String> retValV = new Vector<String>();
        while (true)
        {
            if (path instanceof Projection)
            {
                Projection proj = (Projection) path;
                retValV.add(proj.getLabel());
                path = proj.getPrefix();
                continue;
            }
            else if (path instanceof Root)
                break;
            else if (path instanceof Variable)
            {
                // if this variable is the cutoff variable we stop
                if (((Variable) path).equals(cutoff))
                    break;

                // Otherwise we first find the variable expression
                path = (Path) from.getVarExpression((Variable) path);
                if (path == null)
                    throw new RuntimeException("Should not happen");
                continue;
            }
            else if (path instanceof Current)
                throw new RuntimeException("Should not happen II");
        }
        String[] retVal = new String[retValV.size()];
        for (int i = 0, imax = retValV.size(); i < imax; i++)
        {
            retVal[i] = retValV.elementAt(imax - i - 1);
        }
        return retVal;
    }
}