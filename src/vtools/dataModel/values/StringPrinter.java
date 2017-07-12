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

import vtools.visitor.VisitorImpl;

public class StringPrinter extends VisitorImpl
{
    public static StringPrinter StringPrinter = new StringPrinter();

    public Object visit(IntegerValue o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append(o.getValue() + "");
        return null;
    }

    public Object visit(StringValue o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        String value = (String) o.getValue();
        // you give \\\\ -> takes as parameter \ \ and hence looks for \
        value = value.replaceAll("\\\\", "\\\\\\\\");
        value = value.replaceAll("\\\'", "\\\\\'");
        value = value.replaceAll("\\\"", "\\\\\"");
        value = "\'" + value + "\'";
        buf.append(value + "");
        return null;
    }

    public Object visit(RcdValue o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        Integer oldTabsValue = (Integer) args[1];
        Integer plus1 = new Integer(oldTabsValue.intValue()  + 1);
        
        buf.append("[");
        for (int i=0, imax=o.size(); i< imax; i++)
        {
            String label = o.getFieldLabel(i);
            Value v = o.getFieldValue(i);
            buf.append("\n");
            for (int i2=0, imax2=oldTabsValue.intValue(); i2< imax2; i2++)
                buf.append("   ");
            buf.append(label + ":");
            args[1] = plus1;
            v.accept(this, args);
            if (i != (imax - 1))
                buf.append(",");
        }
        buf.append("]");
        args[1] = oldTabsValue;
        return null;
    }

    public Object visit(SetValue o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        
        buf.append("{");
        for (int i=0, imax=o.size(); i< imax; i++)
        {
            Value v = o.getMember(i);
            v.accept(this, args);
            if (i != (imax - 1))
                buf.append(",");
        }
        buf.append("}");
        return null;
    }

    public Object visit(NULL o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("NULL");
        return null;
    }

    public Object visit(FALSE o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("FALSE");
        return null;
    }

    public Object visit(TRUE o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("TRUE");
        return null;
    }
}
