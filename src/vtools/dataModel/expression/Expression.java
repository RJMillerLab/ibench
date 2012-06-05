package vtools.dataModel.expression;

import vtools.VObject;
import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public abstract class Expression extends VObject implements Visitable, Cloneable
{
    public boolean equals(Object o)
    {
        if (!(o instanceof Expression))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public Expression clone()
    {
        return (Expression) super.clone();
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}
