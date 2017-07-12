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
package vtools.dataModel.types;

import java.util.Vector;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class Structured extends Type implements Visitable, Cloneable
{
    protected Vector<NameTypePair> _attributes;

    public int size()
    {
        return _attributes.size();
    }

    public Structured()
    {
        _attributes = new Vector<NameTypePair>();
    }


    public void addField(NameTypePair attr)
    {
        _attributes.add(attr);
    }

    public void addField(NameTypePair attr, int pos)
    {
        _attributes.add(pos, attr);
    }

    public NameTypePair getField(String l)
    {
        for (int i = 0, imax = size(); i < imax; i++)
        {
            NameTypePair tmp = getField(i);
            if (tmp.getLabel().equals(l))
                return tmp;
        }
        return null;
    }

    public NameTypePair getField(int i)
    {
        return (NameTypePair) _attributes.elementAt(i);
    }

    public int getFieldPosition(String name)
    {
        int pos = -1;
        for (int i = 0, imax = _attributes.size(); i < imax; i++)
        {
            NameTypePair t = (NameTypePair) _attributes.elementAt(i);
            if (!t.getLabel().equals(name))
                continue;
            pos = i;
        }
        return pos;
    }

    public NameTypePair removeField(String name)
    {
        int pos = getFieldPosition(name);
        if (pos != -1)
            return (NameTypePair) _attributes.remove(pos);
        return null;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Structured))
            return false;
        if (!super.equals(o))
            return false;
        Structured stru = (Structured) o;
        if (_attributes.size() != stru._attributes.size())
            return false;
        for (int i = 0, imax = _attributes.size(); i < imax; i++)
        {
            if (!(_attributes.elementAt(i).equals(stru._attributes.elementAt(i))))
                return false;
        }
        return true;
    }

    public Structured clone()
    {
        Structured stru = (Structured) super.clone();
        Vector<NameTypePair> nv = new Vector<NameTypePair>();
        for (int i = 0, imax = _attributes.size(); i < imax; i++)
        {
            nv.add((_attributes.elementAt(i)).clone());
        }
        stru._attributes = nv;
        return stru;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}
