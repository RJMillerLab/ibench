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
package vtools.dataModel.schema;

import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.NameTypePair;
import vtools.dataModel.types.Structured;
import vtools.dataModel.types.Type;
import vtools.visitor.Visitor;

/**
 * An element is a label-type pair that records the parent element it belongs.
 * It should be used for the nested relational model. The table and attribute
 * are specializations of this for the relational model only.
 */
public class Element extends NameTypePair implements Cloneable
{

    private Element _parent;

    public Element(String name, Type type, Element parent)
    {
        super(name, type);
        _parent = parent;
    }

    public Element getParent()
    {
        return _parent;
    }

    public void setParent(Element parent)
    {
        _parent = parent;
    }

    private void addSubElement(Element child, boolean virtually)
    {
        if ((_type instanceof Atomic) || (_type == Type.ANY))
            throw new RuntimeException("Subelements cannot be added under atomic or ANY types");
        else if (_type instanceof Structured)
        {
            Structured strType = (Structured) _type;
            strType.addField(child);
            if (!virtually)
                child.setParent(this);
        }
        else throw new RuntimeException("Should not happen!");
    }

    private void addSubElementAt(Element child, int pos, boolean virtually)
    {
        if ((_type instanceof Atomic) || (_type == Type.ANY))
            throw new RuntimeException("Subelements cannot be added under atomic or ANY types");
        else if (_type instanceof Structured)
        {
            Structured strType = (Structured) _type;
            strType.addField(child, pos);
            if (!virtually)
                child.setParent(this);
        }
        else throw new RuntimeException("Should not happen!");
    }

    public Element removeSubElement(String name)
    {
        if ((_type instanceof Atomic) || (_type == Type.ANY))
            throw new RuntimeException("Subelements cannot exist under atomic/ANY types");
        else if (_type instanceof Structured)
        {
            Structured rcd = (Structured) _type;
            return (Element) rcd.removeField(name);
        }
        else throw new RuntimeException("Should not happen!");
    }

    public void addVirtualSubElement(Element child)
    {
        addSubElement(child, true);
    }

    public void addVirtualSubElementAt(Element child, int pos)
    {
        addSubElementAt(child, pos, true);
    }

    public void addSubElement(Element child)
    {
        addSubElement(child, false);
    }

    public void addSubElementAt(Element child, int pos)
    {
        addSubElementAt(child, pos, false);
    }

    public Element getSubElement(int i)
    {
        if ((_type instanceof Atomic) || (_type == Type.ANY))
            throw new RuntimeException("Subelements cannot exist under atomic types");
        else if (_type instanceof Structured)
        {
            Structured strT = (Structured) _type;
            return (Element) strT.getField(i);
        }
        else throw new RuntimeException("Should not happen!");
    }

    public int size()
    {
        if ((_type instanceof Atomic) || (_type == Type.ANY))
            return 0;
        else if (_type instanceof Structured)
        {
            Structured strT = (Structured) _type;
            return strT.size();
        }
        else throw new RuntimeException("Unknown Type: Should not have happened!");
    }

    public Element clone()
    {
        Element el = (Element) super.clone();
        el._parent = null;
        // Inform the kids that I am the father :-)
        if (el._type instanceof Structured)
        {
            Structured ct = (Structured) el._type;
            for (int i = 0, imax = ct.size(); i < imax; i++)
            {
                if (ct.getField(i) instanceof Element)
                {
                    Element e = (Element) ct.getField(i);
                    e.setParent(el);
                }
            }
        }
        return el;
    }

    /**
     * Returns the element with the specific label.
     */
    public Element getSubElement(String labelName)
    {
        if ((_type instanceof Atomic) || (_type == Type.ANY))
            return null;
        int pos = getSubElementPosition(labelName);
        if (pos == -1)
            return null;
        return getSubElement(pos);
    }

    /**
     * Returns the position of an element in the list of sub-elements
     */
    public int getSubElementPosition(String name)
    {
        if ((_type instanceof Atomic) || (_type == Type.ANY))
            return -1;
        // To have subelement, Type must be Rcd or Set of Rcd
        for (int i = 0, imax = size(); i < imax; i++)
        {
            Element el = getSubElement(i);
            if (el.getLabel().equals(name))
                return i;
        }
        return -1;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Element))
            return false;
        if (!super.equals(o))
            return false;
        Element e = (Element) o;
        if (_parent == null)
        {
            if (e._parent != null)
                return false;
        }
        else
        {
            if (e._parent == null)
                return false;
            else if (!e._parent.equals(_parent))
                return false;
        }
        return true;
    }
}