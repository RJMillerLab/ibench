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

import vtools.utils.structures.SetAssociativeArray;


public class FromClauseList extends SetAssociativeArray implements Cloneable
{

    public void add(Variable var, Expression expr)
    {
        super.add(var, expr);
    }

    public void add(Projection expr)
    {
        Variable var = new Variable(expr.getLabel());
        super.add(var, expr);
    }

    public void insertAt(Variable var, Expression expr, int position)
    {
        super.insertAt(var, expr, position);
    }

    public void insertAt(Projection expr, int position)
    {
        Variable var = new Variable(expr.getLabel());
        super.insertAt(var, expr, position);
    }

    public int getExprVarPosition(Variable var)
    {
        return getKeyPosition(var);
    }

    public Variable getExprVar(int i)
    {
        return (Variable) super.getKey(i);
    }

    public Expression getVarExpression(Variable var)
    {
        int pos = getExprVarPosition(var);
        if (pos == -1)
            return null;
        return getExpression(pos);
    }

    public void setExprVar(int i, Variable var)
    {
        super.setKeyAt(i, var);
    }

    public Expression getExpression(int i)
    {
        return (Expression) super.getValue(i);
    }

    public void setExpression(Expression expr, int i)
    {
        super.setValueAt(i, expr);
    }

    public FromClauseList clone()
    {
        FromClauseList from = new FromClauseList();
        for (int i = 0, imax = size(); i < imax; i++)
        {
            from.add(getExprVar(i), getExpression(i));
        }
        return from;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0, imax = size(); i < imax; i++)
        {
            String qStr = null;
            Expression expr = getExpression(i);
            if (expr instanceof Query)
                qStr = "(" + expr.toString() + ") AS";
            else qStr = expr.toString();
            buf.append(((i != 0) ? ", " : "") + qStr + " " + getExprVar(i).toString());
        }
        return buf.toString();
    }

}
