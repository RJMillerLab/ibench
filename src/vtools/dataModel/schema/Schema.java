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

import java.util.ArrayList;
import java.util.Vector;

import vtools.dataModel.expression.ForeignKey;
import vtools.dataModel.expression.Key;
import vtools.dataModel.expression.Rule;
import vtools.dataModel.types.Rcd;
import vtools.visitor.Visitor;

/* 
 * A schema is nothing more than a tree, thus, it is represented as a root element 
 * and that root element is a record in order to keep the tables or the elements that are at the first level, or 
 * whatever other information the schema needs to keep. It could have been represented as a Vector of elements 
 * but for convenience we made it element of type rcd. Note that if you subclass the class Element and you use
 * your kind of elements, the schema will never be of that kind. Schema is not an element .. it is something else. The fact that 
 * it subclasses the Element class is just for convenience. 
 */
public class Schema extends Element
{
    private Vector<Rule> _constraints;
    
    public Schema(String name)
    {
        super(name, new Rcd(), null);
        _constraints = new Vector<Rule>();
    }

    public Element getRootElement(int i)
    {
        return (Element) super.getSubElement(i);
    }

    public Element getRootElement(String name)
    {
        return (Element) super.getSubElement(name);
    }

    public void addRootElement(Element t)
    {
        if (t.getLabel().equals("*") || (t.getLabel() == null))
            throw new RuntimeException("Cannot have null label elements at the top of the schema");
        super.addSubElement(t);
    }

    public Element removeRootElement(String name)
    {
        return (Element) removeSubElement(name);
    }

    public Rule getConstraint(int i)
    {
        return _constraints.elementAt(i);
    }

    public void addConstraint(Rule constraint)
    {
        _constraints.add(constraint);
    }
    
    public int getConstrSize()
    {
        return _constraints.size();
    }
    
    public Rule getKeyConstraint(String relName) {
    	Rule constraint = null;
        for (int i = 0, imax = getConstrSize(); i < imax; i++) {
            constraint = getConstraint(i);
            if (constraint instanceof Key) {
                String[][] l = ((Key) constraint).getHumanReadableRepresentation();
                for (int j = 0, jmax = l.length; j < jmax; j++) {
                    String[] m = l[j];
                    if (m.equals(relName)) return constraint;
                }
            	
            }
        }
        
    	return constraint; 
    }
    
    
    // TODO: The above method seems incorrect. I create my own method so no original codes are broken.
    public Rule getMyKeyConstraint(String relName) {
    	Rule constraint = null;
        for (int i = 0, imax = getConstrSize(); i < imax; i++) {
            constraint = getConstraint(i);
            if (constraint instanceof Key) {
                String[][] l = ((Key) constraint).getHumanReadableRepresentation();
                for (int j = 0, jmax = l.length; j < jmax; j++) {
                    String m = l[j][0];
                    if (m.equals(relName)) return constraint;
                }
            	
            } else {
            	constraint = null;
            }
        }
        
    	return constraint; 
    }
    
    public ArrayList<Rule> getForeignKeyConstraints() {
    	ArrayList<Rule> constraints = new ArrayList<Rule>();
        for (int i = 0, imax = getConstrSize(); i < imax; i++) {
            Rule constraint = getConstraint(i);
            if (constraint instanceof ForeignKey) {
                constraints.add(constraint);
            }
        }
        
    	return constraints;
    }
    
    public Schema clone()
    {
        Schema s = (Schema) super.clone();
        Vector<Rule> c = new Vector<Rule>();
        for (int i = 0, imax = _constraints.size(); i < imax; i++)
        {
            c.add((_constraints.elementAt(i)).clone());
        }
        s._constraints = c;
        return s;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
    

}
