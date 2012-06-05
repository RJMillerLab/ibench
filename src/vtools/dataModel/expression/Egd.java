package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class Egd extends Rule implements Visitable, Cloneable
{
    public Egd clone()
    {
        return (Egd) super.clone();
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Egd))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }
}
