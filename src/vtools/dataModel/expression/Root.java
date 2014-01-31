package vtools.dataModel.expression;


import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class Root extends Path implements Visitable, Cloneable
{
    public boolean equals(Object o)
    {
        if (!(o instanceof Root))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public Root clone()
    {
        return (Root) super.clone();
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}
