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

public class Projection extends Path implements Visitable, Cloneable
{
    private Path _prefix;

    private String _label;

    public Projection(Path prefix, String label)
    {
        if (prefix == null)
            throw new RuntimeException("Prefix cannot be null in a Projection expression");
        if (label == null)
            throw new RuntimeException("Label cannot be null in a Projection expression");
        _prefix = prefix;
        _label = label;
    }

    public String getLabel()
    {
        return _label;
    }

    public void setLabel(String label)
    {
        _label = label;
    }

    public Path getPrefix()
    {
        return _prefix;
    }

    public Projection clone()
    {
        Projection a = (Projection) super.clone();
        a._prefix = _prefix.clone();
        a._label = new String(_label);
        return a;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Projection))
            return false;
        if (!super.equals(o))
            return false;
        Projection proj = (Projection) o;
        if (!proj._label.equals(_label))
            return false;
        if (!proj._prefix.equals(_prefix))
            return false;
        return true;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}
