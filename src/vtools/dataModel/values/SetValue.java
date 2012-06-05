package vtools.dataModel.values;

import vtools.visitor.Visitor;


/**
 * It represents a set of values (atomic, complex or atomic).
 */
public class SetValue extends GroupValue implements Cloneable
{

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    protected boolean canBeAdded(Value v)
    {
        for (int i = 0, imax = size(); i < imax; i++)
        {
            if (v.equals(getMember(i)))
                return false;
        }
        return true;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof SetValue))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public SetValue clone()
    {
        SetValue setVal = (SetValue) super.clone();
        return setVal;
    }
}
