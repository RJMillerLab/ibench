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


public class SetDifference extends SetOperation implements Visitable, Cloneable
{
    public SetDifference()
    {
        super();
    }

    public SetDifference(Query left, Query right)
    {
        super();
        super.add(left);
        super.add(right);
    }

    public void add(Query rel)
    {
        throw new RuntimeException("Cannot use this for set-difference. Use setLeft or setRight instead.");
    }

    public void setLeft(Query q)
    {
        if (_v.size() == 0)
            _v.add(q);
        else _v.set(0, q);
    }

    public void setRight(Query q)
    {
        if (_v.size() == 0)
        {
            _v.add(null);
            _v.add(q);
        }
        else if (_v.size() == 1)
            _v.add(q);
        else _v.set(0, q);
    }

    public Query getLeft()
    {
        if (_v.size() < 1)
            return null;
        return getComponent(0);
    }

    public Query getRight()
    {
        if (_v.size() < 2)
            return null;
        return getComponent(1);
    }

    public String getSymbol()
    {
        return "MINUS";
    }

    public int getCode()
    {
        return SetOperation.DIFFERENCE;
    }

    public SetDifference clone()
    {
        return (SetDifference) super.clone();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof SetDifference))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}
