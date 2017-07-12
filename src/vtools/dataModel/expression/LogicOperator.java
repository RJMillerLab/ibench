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

public abstract class LogicOperator extends BooleanExpression implements Visitable, Cloneable
{

    public static int AND = 1;
    public static int OR = 1;
    public static int NOT = 1;
    
    protected Vector<BooleanExpression> _v;

    protected LogicOperator()
    {
        _v = new Vector<BooleanExpression>();
    }

    public abstract int getCode();
    public abstract String getSymbol();
    
    public BooleanExpression remove(int i)
    {
        return (BooleanExpression)_v.remove(i);
    }
   
    public void add(BooleanExpression expr)
    {
        _v.add(expr);
    }
   
    protected BooleanExpression getComponent(int i)
    {
        return _v.get(i);
    }
    
    public int size()
    {
        return _v.size();
    }

    public LogicOperator clone()
    {
        LogicOperator op = (LogicOperator) super.clone();
        Vector<BooleanExpression> v = new Vector<BooleanExpression>();
        for (int i = 0, imax = _v.size(); i < imax; i++)
        {
            v.add(_v.elementAt(i).clone());
        }
        op._v = v;
        return op;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof LogicOperator))
            return false;
        if (!super.equals(o))
            return false;
        LogicOperator op = (LogicOperator) o;
        if (_v.size() != op._v.size())
            return false;
        for (int i = 0, imax = _v.size(); i < imax; i++)
        {
            if (!(_v.elementAt(i).equals(op._v.elementAt(i))))
                return false;
        }
        return true;
    }
    
    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
