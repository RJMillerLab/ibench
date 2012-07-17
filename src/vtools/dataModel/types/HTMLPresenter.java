package vtools.dataModel.types;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Any;
import vtools.dataModel.types.Complex;
import vtools.dataModel.types.Group;
import vtools.dataModel.types.Int;
import vtools.dataModel.types.NameTypePair;
import vtools.dataModel.types.Rcd;
import vtools.dataModel.types.Set;
import vtools.dataModel.types.Str;
import vtools.dataModel.types.Structured;
import vtools.dataModel.types.Type;
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
