package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class OR extends LogicOperator implements Visitable, Cloneable
{
    public OR()
    {
        super();
    }

    public void set(BooleanExpression rel, int n)
    {
        _v.setElementAt(rel, n);
    }

    public void add(BooleanExpression rel)
    {
        _v.add(rel);
    }

    public BooleanExpression getComponent(int pos)
    {
        return (BooleanExpression) _v.get(pos);
    }

    public OR clone()
    {
        return (OR) super.clone();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof OR))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    public String getSymbol()
    {
        return "OR";
    }

    public int getCode()
    {
        return LogicOperator.OR;
    }
}
