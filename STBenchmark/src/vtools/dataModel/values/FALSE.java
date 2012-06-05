package vtools.dataModel.values;

import vtools.dataModel.types.Type;
import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public class FALSE extends BooleanValue implements Visitable, Cloneable
{
    /*
     * For the programming purposes two nulls are equal (do not confuse this
     * with the case of comparison of two nulls in databases.
     */
    public boolean equals(Object o)
    {
        if (!(o instanceof FALSE))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public FALSE clone()
    {
        if (this == Value.FALSE)
            return this;
        return (FALSE) super.clone();
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
