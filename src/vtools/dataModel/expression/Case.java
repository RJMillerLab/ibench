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

public class Case extends ValueExpression implements Visitable, Cloneable
{
    private Expression _caseExpr;

    private Vector<BooleanExpression> _whenExpr;

    private Vector<Expression> _thenExpr;

    private Expression _elseExpr;

    public Case(Expression caseExpr, Expression defaultExpr)
    {
        _caseExpr = caseExpr;
        _whenExpr = new Vector<BooleanExpression>();
        _thenExpr = new Vector<Expression>();
        _elseExpr = defaultExpr;
    }

    public Expression getCaseExpr()
    {
        return _caseExpr;
    }

    public void setCaseExpr(Expression expr)
    {
        _caseExpr = expr;
    }

    public Expression getDefaultExpr()
    {
        return _elseExpr;
    }

    public void setDefaultExpr(Expression expr)
    {
        _elseExpr = expr;
    }

    public Expression getThenExpr(int i)
    {
        return _thenExpr.elementAt(i);
    }

    public void setThenExpr(Expression expr, int i)
    {
        _thenExpr.setElementAt(expr, i);
    }

    public BooleanExpression getWhenExpr(int i)
    {
        return _whenExpr.elementAt(i);
    }

    public void setWhenExpr(BooleanExpression expr, int i)
    {
        _whenExpr.setElementAt(expr, i);
    }

    public void addConditionAction(BooleanExpression when, Expression then)
    {
        _whenExpr.add(when);
        _thenExpr.add(then);
    }

    public int size()
    {
        return _whenExpr.size();
    }

    public Case clone()
    {
        Case cr = (Case) super.clone();
        cr._caseExpr = _caseExpr.clone();
        cr._elseExpr = _elseExpr.clone();
        cr._whenExpr = new Vector<BooleanExpression>(_whenExpr.size());
        for (int i = 0, imax = _whenExpr.size(); i < imax; i++)
            cr._whenExpr.add(_whenExpr.elementAt(i).clone());
        cr._thenExpr = new Vector<Expression>(_thenExpr.size());
        for (int i = 0, imax = _thenExpr.size(); i < imax; i++)
            cr._thenExpr.add(_thenExpr.elementAt(i).clone());
        return cr;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Case))
            return false;
        if (!super.equals(o))
            return false;
        Case cr = (Case) o;
        if (!cr._caseExpr.equals(_caseExpr))
            return false;
        if (!cr._elseExpr.equals(_elseExpr))
            return false;
        if (_whenExpr.size() != cr._whenExpr.size())
            return false;
        for (int i = 0, imax = _whenExpr.size(); i < imax; i++)
        {
            BooleanExpression arg = _whenExpr.elementAt(i);
            if (!arg.equals(cr.getWhenExpr(i)))
                return false;
        }
        if (_thenExpr.size() != cr._thenExpr.size())
            return false;
        for (int i = 0, imax = _thenExpr.size(); i < imax; i++)
        {
            Expression arg = _thenExpr.elementAt(i);
            if (!arg.equals(cr.getThenExpr(i)))
                return false;
        }
        return true;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}
