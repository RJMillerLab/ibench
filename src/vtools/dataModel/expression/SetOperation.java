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

import org.vagabond.benchmark.model.IdGen;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public abstract class SetOperation extends Query implements Visitable, Cloneable, Trampable
{

    public static int UNION = 0;

    public static int INTERSECT = 0;

    public static int DIFFERENCE = 0;


    protected Vector<Query> _v;

    protected SetOperation()
    {
        _v = new Vector<Query>();
    }

    public SetOperation(Vector<Query> components)
    {
        _v = components;
    }

    public abstract String getSymbol();

    public abstract int getCode();

    public String getStoredCode() {
       	StringBuffer result = new StringBuffer();
    	for(int i = 0; i < _v.size(); i++) {
    		Query q = _v.get(i);
    		result.append(q.getStoredCode());
    		if (i != _v.size() - 1)
    			result.append("\n" + getSymbol() + "\n");
    	}
    	return result.toString();
    }
    
    public void set(Query rel, int n)
    {
        _v.setElementAt(rel, n);
    }

    public void add(Query rel)
    {
        _v.add(rel);
    }

    public Query getComponent(int pos)
    {
        return (Query) _v.get(pos);
    }
    
    public Vector<Query> getComponents () {
    	return _v;
    }

    public int size()
    {
        return _v.size();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof SetOperation))
            return false;
        if (!super.equals(o))
            return false;
        SetOperation op = (SetOperation) o;
        if (_v.size() != op._v.size())
            return false;
        for (int i = 0, imax = _v.size(); i < imax; i++)
        {
            if (!(_v.elementAt(i).equals(op._v.elementAt(i))))
                return false;
        }
        return true;
    }

    public SetOperation clone()
    {
        SetOperation soq = (SetOperation) super.clone();
        soq._v = new Vector<Query>();
        for (int i = 0, imax = _v.size(); i < imax; i++)
            _v.add(_v.elementAt(i).clone());
        return soq;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
    
//    @Override
//    public String toTrampString(IdGen idGen) throws Exception {
//    	StringBuffer result = new StringBuffer();
//    	for(int i = 0; i < _v.size(); i++) {
//    		Query q = _v.get(i);
//    		result.append(q.toTrampString(idGen));
//    		if (i != _v.size() - 1)
//    			result.append("\n" + getSymbol() + "\n");
//    	}
//    	return result.toString();
//    }
//    
//	@Override
//	public String toTrampStringOneMap(String mapping) throws Exception {
//		String result = toTrampString();
//		int maxId = findMaxId(result);
//		
//		for(int i = 0; i < maxId; i++) {
//			result = result.replace("${" + i + "}", mapping);
//		}
//		return result;
//	}
//	
	@Override
	public int getNumberOfLeafs () {
		int result = 0;
		
		for(Query q: _v)
			result += q.getNumberOfLeafs();
		
		return result;
	}
}
