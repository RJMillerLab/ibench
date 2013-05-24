package vtools.dataModel.values;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class NULL extends AtomicValue implements Visitable, Cloneable
{
    /*
     * For the programming purposes two nulls are equal (do not confuse this
     * with the case of comparison of two nulls in databases.
     */
    public boolean equals(Object o)
    {
        if (!(o instanceof NULL))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public NULL clone()
    {
        if (this == Value.NULL)
            return this;
        return (NULL) super.clone();
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}
