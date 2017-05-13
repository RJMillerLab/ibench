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


public class Function extends ValueExpression implements Visitable, Cloneable
{
    String _name;

    Vector<ValueExpression> _args;

    public Function(String name)
    {
        _name = name;
        _args = new Vector<ValueExpression>();
    }

    public Function(String name, Vector<ValueExpression> args)

    {
        _name = name;
        _args = new Vector<ValueExpression>();
        for (int i = 0, imax = args.size(); i < imax; i++)
            _args.addElement(args.elementAt(i).clone());
    }

    public String getName()
    {
        return _name;
    }

    public void addArg(ValueExpression arg)
    {
        _args.add(arg);
    }

    public ValueExpression getArg(int i)
    {
        return (ValueExpression) _args.elementAt(i);
    }

    public void removeArg(int i)
    {
        if (i >= _args.size())
            throw new RuntimeException("Not that many attributes. Removal failed");
        _args.remove(i);
    }

    public int getNumOfArgs()
    {
        return _args.size();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Function))
            return false;
        if (!super.equals(o))
            return false;
        Function f = (Function) o;
        if (!f._name.equals(_name))
            return false;
        for (int i = 0, imax = _args.size(); i < imax; i++)
        {
            ValueExpression arg = _args.elementAt(i);
            if (!arg.equals(f.getArg(i)))
                return false;
        }
        return true;
    }

    public Function clone()
    {
        Function f = (Function) super.clone();
        f._name = new String(_name);
        f._args = new Vector<ValueExpression>();
        for (int i = 0, imax = _args.size(); i < imax; i++)
            f._args.add(_args.elementAt(i).clone());
        return f;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
