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
