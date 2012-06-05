package vtools.dataModel.values;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public class TRUE extends BooleanValue implements Visitable, Cloneable
{

    /*
     * For the programming purposes two nulls are equal (do not confuse this
     * with the case of comparison of two nulls in databases.
     */
    public boolean equals(Object o)
    {
        if (!(o instanceof TRUE))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public TRUE clone()
    {
        if (this == Value.TRUE)
            return this;
        return (TRUE) super.clone();
    }


    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}
