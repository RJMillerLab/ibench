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

import java.util.Vector;

import vtools.visitor.Visitable;


/**
 * It represents a set or a bag of values
 */
public abstract class GroupValue extends Value implements Cloneable, Visitable
{

    protected Vector<Value> _members;

    public GroupValue()
    {
        _members = new Vector<Value>();
    }

    public void addMember(Value value)
    {
        if (canBeAdded(value))
            _members.add(value);
    }

    protected abstract boolean canBeAdded(Value v);

    public Value getMember(int position)
    {
        return (Value) _members.elementAt(position);
    }

    public int size()
    {
        return _members.size();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof GroupValue))
            return false;
        if (!super.equals(o))
            return false;
        GroupValue grpVal = (GroupValue) o;
        if (_members.size() != grpVal._members.size())
            return false;
        for (int i = 0, imax = _members.size(); i < imax; i++)
        {
            if (!((Value) _members.elementAt(i)).equals(grpVal._members.elementAt(i)))
                return false;
        }
        return true;
    }

    public GroupValue clone()
    {
        GroupValue grpVal = (GroupValue) super.clone();
        Vector<Value> members = new Vector<Value>();
        for (int i = 0, imax = _members.size(); i < imax; i++)
        {
            members.add((Value) _members.elementAt(i).clone());
        }
        grpVal._members = members;
        return grpVal;
    }
}
