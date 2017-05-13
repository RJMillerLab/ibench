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
package vtools;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

/**
 * A VObject node is the vLibrary top Class (something like the Object class in
 * the jdk). It serves the following purposes: It provides a hook. A hook is an
 * Object attribute that can be used by external programs for any purpose they
 * are willing. NOTE THAT the clone simply copies the hook, thus a clone call
 * needs to keep that in mind.
 */
public abstract class VObject implements Cloneable, Visitable
{
    protected Object _hook = null;

    /**
     * @returns the hook
     */
    public Object getHook()
    {
        return _hook;
    }

    /**
     * sets the hook to an Object
     */
    public void setHook(Object hook)
    {
        _hook = hook;
    }

    public Object clone()
    {
        VObject n;
        try
        {
            n = (VObject) super.clone();
            if (_hook instanceof String)
            {
                n._hook = new String((String) _hook);
            }
            else n._hook = _hook;
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException("This should not happen since we are cloneable");
        }
        return n;
    }

    public abstract Visitor getPrintVisitor();

    public boolean equals(Object o)
    {
        if (!(o instanceof VObject))
            return false;
        VObject n = (VObject) o;
        if (_hook == null)
        {
            if (n.getHook() != null)
                return false;
        }
        else
        {
            if (n.getHook() == null)
                return false;
            if (!(n.getHook().equals(_hook)))
                return false;
        }
        return true;
    }


    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        Object[] args = new Object[50];
        args[0] = buf;
        args[1] = new Integer(0);
        this.accept(getPrintVisitor(), args);
        return buf.toString();
    }
 
    public Object accept(Visitor visitor, Object[] args)
    {
        return visitor.dispatch(this, args);
    }
}
