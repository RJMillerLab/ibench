package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public abstract class ValueExpression extends Expression implements Visitable, Cloneable
{
    public boolean equals(Object o)
    {
        if (!(o instanceof ValueExpression))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public ValueExpression clone()
    {
        return (ValueExpression) super.clone();
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}
