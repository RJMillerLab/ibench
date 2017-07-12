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
package vtools.dataModel.values;

import vtools.utils.structures.SetAssociativeArray;
import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


/**
 * It represents a record (not in the schema terms but in the value) For
 * instance, a tuple is a RcdValue
 */
public class RcdValue extends Value implements Visitable, Cloneable
{
    private SetAssociativeArray _fields;

    public RcdValue()
    {
        _fields = new SetAssociativeArray();
    }

    public void addField(String name, Value value)
    {
        _fields.add(name, value);
    }

    public Value getFieldValue(String name)
    {
        return (Value) _fields.getValue(name);
    }

    public Value getFieldValue(int position)
    {
        return (Value) _fields.getValue(position);
    }

    public int size()
    {
        return _fields.size();
    }

    public String getFieldLabel(int position)
    {
        return (String) _fields.getKey(position);
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof RcdValue))
            return false;
        if (!super.equals(o))
            return false;
        RcdValue rcdv = (RcdValue) o;
        if (_fields.size() != rcdv._fields.size())
            return false;
        for (int i = 0, imax = _fields.size(); i < imax; i++)
        {
            if (!(_fields.getKey(i).equals(rcdv._fields.getKey(i))))
                return false;
            if (!(_fields.getValue(i).equals(rcdv._fields.getValue(i))))
                return false;
        }
        return true;
    }

    public RcdValue clone()
    {
        RcdValue rcdVal = (RcdValue) super.clone();
        SetAssociativeArray rcd = new SetAssociativeArray();
        for (int i = 0, imax = _fields.size(); i < imax; i++)
        {
            rcd.add(new String((String) _fields.getKey(i)), ((Value) _fields.getValue(i)).clone());
        }
        rcdVal._fields = rcd;
        return rcdVal;
    }
}
