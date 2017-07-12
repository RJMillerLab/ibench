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
package vtools.utils.structures;

import java.util.Vector;

/**
 * Has pairs of <key, value> that are objects, thus, they can be whatever Note
 * that for the objects you use, if they are complex, i.e., not Strings, you
 * will have to define your own equals method.
 */
public abstract class AssociativeArray
{
    protected boolean _duplicatesAllowed = true;

    Vector<Object> _keys;

    Vector<Object> _values;

    public AssociativeArray()
    {
        _keys = new Vector<Object>();
        _values = new Vector<Object>();
    }

    public int size()
    {
        return _keys.size();
    }

    /**
     * Adds the specific entry in the provided position and shifts the rest by 1
     */
    public void insertAt(Object key, Object value, int position)
    {
        if (!_duplicatesAllowed)
        {
            int i = vindexOf(_keys, key);
            if (i != -1)
                throw new RuntimeException("Key " + key + " already exists ");
        }
        _keys.insertElementAt(key, position);
        _values.insertElementAt(value, position);

    }

    protected int vindexOf(Vector<Object> v, Object key)
    {
        return v.indexOf(key);
    }

    public void add(Object key, Object value)
    {
        if (!_duplicatesAllowed)
        {
            int i = vindexOf(_keys, key);
            if (i != -1)
                throw new RuntimeException("Key " + key + " already exists ");
        }
        _keys.add(key);
        _values.add(value);
    }

    public Object getKey(int i)
    {
        return _keys.elementAt(i);
    }

    public Object getValue(int i)
    {
        return _values.elementAt(i);
    }

    public int getKeyPosition(Object key)
    {
        for (int i = 0, imax = _keys.size(); i < imax; i++)
        {
            if (vequals(key, _keys.elementAt(i)))
                return i;
        }
        return -1;
    }

    protected boolean vequals(Object o1, Object o2)
    {
        return o1.equals(o2);
    }

    /**
     * Returns the value of the first occurence of the key key starting checking
     * at position k
     */
    public Object getValue(Object key, int k)
    {
        int i = vindexOf(_keys, key, k);
        if (i == -1)
            return null;
        return getValue(i);
    }

    protected int vindexOf(Vector<Object> v, Object key, int k)
    {
        return v.indexOf(key, k);
    }

    public Object getValue(Object key)
    {
        int i = vindexOf(_keys, key);
        if (i == -1)
            return null;
        return getValue(i);
    }

    public void setKeyAt(int i, Object key)
    {
        _keys.set(i, key);
    }

    public void setValueAt(int i, Object value)
    {
        _values.set(i, value);
    }

    public void setValueOf(Object key, Object value)
    {
        int i = vindexOf(_keys, key);
        if (i == -1)
            throw new RuntimeException("The key " + key + " does not exists in the Assoc. Array");
        setValueAt(i, value);
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0, imax = _keys.size(); i < imax; i++)
        {
            buf.append(i + ". " + _keys.elementAt(i) + " : ");
            buf.append(_values.elementAt(i));
        }
        return buf.toString();
    }

}
