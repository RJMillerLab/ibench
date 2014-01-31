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
