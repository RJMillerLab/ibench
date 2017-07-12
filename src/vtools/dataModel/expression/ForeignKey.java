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
 * A foreign key constraint is a special tgd rule that has the property that
 * does not have any left-side side conditions and the right side is a
 * conjunction of terms that involve a variable from the left part and a
 * variable from the right part.
 */
public class ForeignKey extends Tgd implements Visitable, Cloneable
{
    public ForeignKey clone()
    {
        return (ForeignKey) super.clone();
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof ForeignKey))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public void addLeftConjunct(BooleanExpression cond)
    {
        throw new RuntimeException("FK cannot have conditions on the left hand side");
    }

    public void addRightConjunct(BooleanExpression cond)
    {
        throw new RuntimeException("Cannot be used. Use addFKeyAttribute instead");
    }

    public void addFKeyAttr(Path keyPath, Path fKeyPath)
    {
        Path keyPathR = keyPath.getRoot();
        if (!(keyPathR instanceof Variable))
            throw new RuntimeException("Keypath not starting w/ a variable");
        // the variable must be from the right part
        int pos = super.getRightTerms().getExprVarPosition((Variable) keyPathR);
        if (pos == -1)
            throw new RuntimeException("Keypath starting with unexisting variable");

        Path fKeyPathR = fKeyPath.getRoot();
        if (!(fKeyPathR instanceof Variable))
            throw new RuntimeException("FKeypath not starting w/ a variable");
        // the variable must be from the left part
        pos = super.getLeftTerms().getExprVarPosition((Variable) fKeyPathR);
        if (pos == -1)
            throw new RuntimeException("FKeypath starting with unexisting variable");

        // everything is ok.
        EQ eq = new EQ(keyPath, fKeyPath);
        super.addRightConjunct(eq);
    }

    /**
     * Returns the FK in the form of a set of strings of the form xx/x/xx/xx/xx
     * --> xxx/xx/xx/xxxx/xxx
     */
    public String[][][] getHumanReadableRepresentation()
    {
        BooleanExpression exp = getRightConditions();
        if (exp == null)
            throw new RuntimeException("FK with no conditions found");
        Path[][] attrs = null;
        if (exp instanceof EQ)
        {
            attrs = new Path[1][2];
            attrs[0][0] = (Path) ((EQ) exp).getLeft();
            attrs[0][1] = (Path) ((EQ) exp).getRight();
        }
        else if (exp instanceof AND)
        {
            AND and = (AND) exp;
            attrs = new Path[and.size()][2];
            for (int i = 0, imax = and.size(); i < imax; i++)
            {
                attrs[i][0] = (Path) ((EQ) and.getComponent(i)).getLeft();
                attrs[i][1] = (Path) ((EQ) and.getComponent(i)).getRight();
            }
        }
        else throw new RuntimeException("should not happen");

        String[][][] retVal = new String[attrs.length][2][];
        for (int i = 0, imax = attrs.length; i < imax; i++)
        {
            String[] keyAttr = stringisize(attrs[i][1], getLeftTerms());
            String[] fkAttr = stringisize(attrs[i][0], getRightTerms());
            retVal[i][0] = fkAttr;
            retVal[i][1] = keyAttr;
        }
        return retVal;
    }

    /*
     * Returns the full path of an element in an expression
     */
    private String[] stringisize(Path path, FromClauseList from)
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
                // replace the variable with the new expression and continue
                path = (Path) from.getVarExpression((Variable) path);
                if (path == null)
                    throw new RuntimeException("Should not happen  345345435");
                continue;
            }
            else if (path instanceof Current)
                throw new RuntimeException("Should not happen 34543543");
        }
        String[] retVal = new String[retValV.size()];
        for (int i = 0, imax = retValV.size(); i < imax; i++)
        {
            retVal[i] = retValV.elementAt(imax - i - 1);
        }
        return retVal;
    }
}