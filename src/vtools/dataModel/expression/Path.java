package vtools.dataModel.expression;


import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public abstract class Path extends ValueExpression implements Visitable, Cloneable
{
    public static Current CURRENT = new Current();

    public static Root ROOT = new Root();

    public boolean equals(Object o)
    {
        if (!(o instanceof Path))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public Path getRoot()
    {
        if (!(this instanceof Projection))
            return this;
        return ((Projection) this).getPrefix().getRoot();
    }


    public Path clone()
    {
        return (Path) super.clone();
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
