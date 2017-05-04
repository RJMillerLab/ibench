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

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public abstract class ComparisonOperator extends BooleanExpression implements Visitable, Cloneable
{
    private ValueExpression _left;

    private ValueExpression _right;

    public static int LT = 1;

    public static int LE = 2;

    public static int EQ = 3;

    public static int GE = 4;

    public static int GT = 5;

    public abstract String getSymbol();

    public abstract int getCode();

    protected ComparisonOperator(ValueExpression left, ValueExpression right)
    {
        _left = left;
        _right = right;
    }

    public ValueExpression getLeft()
    {
        return _left;
    }

    public ValueExpression getRight()
    {
        return _right;
    }

    public void setLeft(ValueExpression left)
    {
        _left = left;
    }

    public void setRight(ValueExpression right)
    {
        _right = right;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof ComparisonOperator))
            return false;
        if (!super.equals(o))
            return false;
        ComparisonOperator eq = (ComparisonOperator) o;
        if (!(eq._right.equals(_right)))
            return false;
        if (!(eq._left.equals(_left)))
            return false;
        return true;
    }

    public ComparisonOperator clone()
    {
        ComparisonOperator eq = (ComparisonOperator) super.clone();
        eq._left = _left.clone();
        eq._right = _right.clone();
        return eq;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
