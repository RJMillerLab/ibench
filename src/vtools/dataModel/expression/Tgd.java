package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class Tgd extends Rule implements Visitable, Cloneable
{
    public Tgd clone()
    {
        return (Tgd) super.clone();
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Tgd))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }
}
