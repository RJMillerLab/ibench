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

import vtools.dataModel.expression.ForeignKey;
import vtools.dataModel.expression.Key;
import vtools.dataModel.expression.Rule;

public class StringPrinter extends vtools.dataModel.types.StringPrinter
{
    public static StringPrinter StringPrinter = new StringPrinter();

    public Object visit(Element o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        Integer tabsInteger = (Integer) args[1];
        int tabs = tabsInteger.intValue();
        for (int ij = 0; ij < tabs; ij++)
            buf.append("   ");
        buf.append(o.getLabel() + ":");
        args[1] = new Integer(tabs + 1);
        o.getType().accept(this, args);
        args[1] = tabsInteger;
        return null;
    }

    public Object visit(Schema o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        Element e = (Element) o;
        visit(e, args);
        buf.append("\nConstraints:\n");
        for (int i = 0, imax = o.getConstrSize(); i < imax; i++)
        {
            Rule constraint = o.getConstraint(i);
            if (constraint instanceof Key)
            {
                buf.append("// Key: ");
                String[][] l = ((Key) constraint).getHumanReadableRepresentation();
                for (int j = 0, jmax = l.length; j < jmax; j++)
                {
                    String[] m = l[j];
                    for (int k = 0, kmax = m.length; k < kmax; k++)
                    {
                        if (j != 0)
                        {
                            if (k != 0)
                                buf.append(".");
                            else buf.append(", ");
                        }
                        else if (k != 0)
                            buf.append(".");
                        buf.append(m[k]);
                    }
                    if (j == 0)
                        buf.append("[");
                }
                buf.append("]\n");
            }
            else if (constraint instanceof ForeignKey)
            {
                buf.append("// FKey: \n");
                String[][][] l = ((ForeignKey) constraint).getHumanReadableRepresentation();
                for (int j = 0, jmax = l.length; j < jmax; j++)
                {
                    String[] keyAttr = l[j][0];
                    String[] fkAttr = l[j][1];
                    buf.append("//           ");
                    for (int k = 0, kmax = fkAttr.length; k < kmax; k++)
                    {
                        if (k != 0)
                            buf.append(".");
                        buf.append(fkAttr[k]);
                    }
                    buf.append(" --> ");
                    for (int k = 0, kmax = keyAttr.length; k < kmax; k++)
                    {
                        if (k != 0)
                            buf.append(".");
                        buf.append(keyAttr[k]);
                    }
                    buf.append("\n");
                }
            }
            buf.append(constraint.toString());
            buf.append(";\n");
        }
        return null;
    }
}
