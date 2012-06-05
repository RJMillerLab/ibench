package vtools.dataModel.expression;


import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

/**
 * 
 */
public abstract class Query extends Expression implements Visitable, Cloneable
{
    public boolean equals(Object o)
    {
        if (!(o instanceof Query))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }
    
    public Query clone()
    {
        return (Query) super.clone();
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
    
}
