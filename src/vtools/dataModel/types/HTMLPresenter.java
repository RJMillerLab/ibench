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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import vtools.visitor.VisitorImpl;

public class HTMLPresenter extends VisitorImpl
{
    public static vtools.dataModel.types.HTMLPresenter HTMLPresenter = new vtools.dataModel.types.HTMLPresenter();

    /**
     * Prints the buffer (which should be in HTML) in a file which will serve
     * for the browser by putting before and after the HTML headers
     */
    public void printInHtmlFile(StringBuffer buf, String file)
    {

        StringBuffer fbuf = new StringBuffer();
        printHTMLHeader(fbuf);
        fbuf.append(buf);
        printHTMLFooter(fbuf);
        try
        {
            BufferedWriter bufWriter = new BufferedWriter(new FileWriter(new File(file)));
            bufWriter.write(fbuf.toString());
            bufWriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }


    private void printHTMLHeader(StringBuffer buf)
    {
        buf.append("<html><head><meta http-equiv=\"Content-Language\" content=\"en-us\"><meta http-equiv=\"Content-Type\" content=\"text/html\">");
        buf.append("<style>\n<!--\nbody, p, tbody\n{\nfont-weight: normal;\nfont-family: Verdana, Arial, Helvetica, sans-serif;\nfont-size: 10pt;\n}\n-->\n</style>\n</head><body>");
    }

    private void printHTMLFooter(StringBuffer buf)
    {
        buf.append("</body></html>");
    }

    public Object visit(NameTypePair o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        Integer tabsInteger = (Integer) args[1];
        int tabs = tabsInteger.intValue();
        for (int ij = 0; ij < tabs; ij++)
            buf.append("   ");
        Type t = o.getType();
        if (t instanceof Set)
            buf.append("<font color=\"blue\">");
        buf.append("<b>" + o.getLabel() + "</b>");
        if (t instanceof Set)
            buf.append("</font>");
        args[1] = new Integer(tabs + 1);
        t.accept(this, args);
        args[1] = tabsInteger;
        return null;
    }

    public Object visit(Structured o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        Integer tabsInteger = (Integer) args[1];
        int tabs = tabsInteger.intValue();
        buf.append("<b>[</b><br/>");
        for (int i = 0, imax = o.size(); i < imax; i++)
        {
            NameTypePair pair = o.getField(i);
            args[1] = new Integer(tabs + 1);
            pair.accept(this, args);
            buf.append((i != (imax - 1)) ? ",<br/>" : "");
        }
        buf.append("<b>]</b>");
        args[1] = tabsInteger;
        return null;
    }

    public Object visit(Str o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("<font color=\"green\"><i>String</i></font>");
        return null;
    }

    public Object visit(Int o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("<font color=\"green\"><i>Integer</i></font>");
        return null;
    }

    public Object visit(Complex o, Object[] args)
    {
        visit((Structured) o, args);
        return null;
    }

    public Object visit(Group o, Object[] args)
    {
        visit((Structured) o, args);
        return null;
    }

    public Object visit(Rcd o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("<font color=\"green\"><i>Rcd </i></font>");
        visit((Complex) o, args);
        return null;
    }

    public Object visit(Set o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("<font color=\"green\"><i> Set </i></font>");
        visit((Group) o, args);
        return null;
    }

    public Object visit(Any o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("<i>ANY</i>");
        return null;
    }


}
