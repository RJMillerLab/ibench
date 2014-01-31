package vtools.dataModel.expression;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import vtools.visitor.VisitorImpl;

public class HTMLPresenter extends VisitorImpl
{
    public static HTMLPresenter HTMLPresenter = new HTMLPresenter();

    public Object visit(ValueExpression valExpr, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append(valExpr.toString());
        return null;
    }

    public Object visit(SetOperation setOp, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];

        buf.append("\n<table border=\"0\" cellspacing=\"0\" cellpadding=\"3\">\n");
        buf.append("<tr bgcolor=\"green\"><td><font size=\"0\"></font></td><td></td><td><font size=\"0\"></font></td></tr>\n");

        // print the first element of the set operator
        buf.append("<tr><td bgcolor=\"green\"></td><td>");
        setOp.getComponent(0).accept(this, args);
        buf.append("</td><td bgcolor=\"green\"></td></tr>\n");

        // and now print the rest
        for (int i = 1, imax = setOp.size(); i < imax; i++)
        {
            buf.append("<tr><td bgcolor=\"green\"></td><td>");
            buf.append("<b>" + setOp.getSymbol() + "</b>");
            buf.append("</td><td bgcolor=\"green\"></td></tr>\n");
            // and now the component
            buf.append("<tr><td bgcolor=\"green\"></td><td>");
            setOp.getComponent(i).accept(this, args);
            buf.append("</td><td bgcolor=\"green\"></td></tr>\n");
        }

        // finally close the table
        buf.append("<tr bgcolor=\"green\"><td></td><td></td><td></td></tr>\n");
        buf.append("</table>\n");

        return null;
    }

    // each query is printed in a table
    public Object visit(SPJQuery query, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];

        buf.append("\n<table border=\"0\" cellspacing=\"0\" cellpadding=\"3\">\n");

        buf.append("<tr bgcolor=\"black\"><td><font size=\"0\"></font></td><td></td><td></td><td></td></tr>\n");
        // create a line for the select clause
        buf.append("<tr><td bgcolor=\"black\"></td><td valign=\"top\" align=\"left\"><b>select</b></td><td>");
        // inside the line there is a table with 2 lines and one column for each
        // term. The first column simply has the select string
        SelectClauseList select = query.getSelect();
        buf.append("\n<table><tr>");

        for (int i = 0, imax = select.size(); i < imax; i++)
        {
            String asString = select.getTermName(i);
            buf.append("<td valign=\"top\" align=\"center\" bgcolor=\"#FF9900\"><b>" + asString + "</b></td>");
        }
        buf.append("</tr>\n<tr>");
        for (int i = 0, imax = select.size(); i < imax; i++)
        {
            Expression exp = select.getTerm(i);
            buf.append("<td valign=\"top\" align=\"center\" bgcolor=\"#FFCC99\">");
            if (exp instanceof SPJQuery)
            {
                ((SPJQuery) exp).accept(this, args);
            }
            else buf.append(exp.toString());
            buf.append("</td>");

        }
        buf.append("</tr></table>");

        // and now close the line of the select
        buf.append("</td><td bgcolor=\"black\"></td></tr>\n");

        // create a line for the from clause
        FromClauseList from = query.getFrom();
        if (from.size() != 0)
        {
            buf.append("<tr><td bgcolor=\"black\"></td><td valign=\"top\" align=\"left\"><b>from</b></td><td nowrap>");
            buf.append("\n<table border=\"0\" cellspacing=\"0\" cellpadding=\"3\"><tr>\n");
            for (int i = 0, imax = from.size(); i < imax; i++)
            {
                Expression setExpression = from.getExpression(i);
                buf.append("<td>");
                setExpression.accept(this, args);
                buf.append("</td>");
                Variable var = from.getExprVar(i);
                buf.append("<td><b>&nbsp;</b></td><td><i>" + var.getName() + "</i></td>");
            }
            buf.append("</tr></table>");
            buf.append("</td><td bgcolor=\"black\"></td></tr>\n");
        }

        // create a line for the where
        BooleanExpression where = query.getWhere();
        if (where != null)
        {
            buf.append("<tr><td bgcolor=\"black\"></td><td valign=\"top\" align=\"left\"><b>where</b></td><td>");
            buf.append(where.toString());
            buf.append("</td><td bgcolor=\"black\"></td></tr>\n");
        }

        // create a line for the groupBy
        int groupBySize = query.getGroupBySize();
        if (groupBySize != 0)
        {
            buf.append("<tr><td  bgcolor=\"black\"></td><td align=\"left\"><b>groupBy</b></td><td>");
            buf.append("(");
            for (int i = 0; i < groupBySize; i++)
            {
                buf.append((i == 0) ? "" : ", ");
                Projection p = query.getGroupByTerm(i);
                p.accept(this, args);
            }
            buf.append(")");
            buf.append("</td><td bgcolor=\"black\"></td></tr>\n");
        }

        buf.append("<tr bgcolor=\"black\"><td></td><td></td><td></td><td><font size=\"0\"></font></td></tr>\n");
        buf.append("</table>\n");

        return null;
    }

    /**
     * Prints the buffer (which should be in HTML) in a file which will serve
     * for the browser by putting before and after the HTML headers
     */
    public void printInHtmlFile(StringBuffer buf, String pathPrefix, String file)
    {

        StringBuffer fbuf = new StringBuffer();
        printHTMLHeader(fbuf);
        fbuf.append(buf);
        printHTMLFooter(fbuf);
        try
        {
            BufferedWriter bufWriter = new BufferedWriter(new FileWriter(new File(pathPrefix, file)));
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
}
