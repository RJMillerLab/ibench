package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public abstract class BooleanExpression extends Expression implements Visitable, Cloneable
{

    protected BooleanExpression()
    {
        super();
    }
    
    public boolean equals(Object o)
    {
        if (!(o instanceof BooleanExpression))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }
    
    public BooleanExpression clone()
    {
       return (BooleanExpression) super.clone();
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
