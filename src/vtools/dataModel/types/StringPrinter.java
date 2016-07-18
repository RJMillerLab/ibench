package vtools.dataModel.types;

import vtools.visitor.VisitorImpl;

public class StringPrinter extends VisitorImpl
{
    public static StringPrinter StringPrinter = new StringPrinter();

    public Object visit(NameTypePair o, Object[] args)
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

    public Object visit(Structured o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        Integer tabsInteger = (Integer) args[1];
        int tabs = tabsInteger.intValue();
        buf.append("[\n");
        for (int i = 0, imax = o.size(); i < imax; i++)
        {
            NameTypePair pair = o.getField(i);
            args[1] = new Integer(tabs + 1);
            pair.accept(this, args);
            buf.append((i != (imax - 1)) ? ",\n" : "");
        }
        buf.append("]");
        args[1] = tabsInteger;
        return null;
    }

    public Object visit(Str o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("String");
        return null;
    }

    public Object visit(Int o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("Integer");
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
        buf.append("Rcd ");
        visit((Complex) o, args);
        return null;
    }

    public Object visit(Set o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("Set ");
        visit((Group) o, args);
        return null;
    }

    public Object visit(Any o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("ANY");
        return null;
    }
    
    public Object visit(DataType o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("DataType ");
        return null;
    }
}
