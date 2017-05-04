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


public class SelectClauseList extends SetAssociativeArray implements Cloneable
{
	private static int _functionNumber = 0;

    public void add(String name, Expression expr)
    {
        super.add(name, expr);
    }
    
    public void add(Projection expr)
    {
        super.add(expr.getLabel(), expr);
    }
 
    public void insertAt (String name, Expression  expr, int position)
    {
    	super.insertAt(name, expr, position);
    }
    
    public void insertAt(Projection expr, int position)
    {
        super.insertAt(expr.getLabel(), expr, position);
    }
    
    public Expression getTerm(int i)
    {
        return (Expression) super.getValue(i);
    }

    public Expression getTerm(String label)
    {
        return (Expression) super.getValue(label);
    }
    
    public String getTermName(int i)
    {
        return (String) super.getKey(i);
    }

    public void setTermName(int i, String name)
    {
       super.setKeyAt(i, name);
    }
   
    public SelectClauseList clone()
    {
        SelectClauseList sel =  new SelectClauseList();
        for (int i = 0, imax = size(); i < imax; i++)
        {
            sel.add(getTermName(i), getTerm(i));
        }
        return sel;
    }
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0, imax = size(); i < imax; i++)
        {
            buf.append(((i != 0) ? ", " : ""));
            Object tmpo = getValue(i);   
            if (tmpo instanceof Projection)
            	buf.append(((Projection)tmpo).toString());
            else if (tmpo instanceof Query)
            	buf.append("(" + ((Query)tmpo).toString()+")");
            else if (tmpo instanceof Function)
            	buf.append(((Function)tmpo).toString().substring(0,2)+_functionNumber++); // only return the first 2 chars
        }
        return buf.toString();
    }
    
}
