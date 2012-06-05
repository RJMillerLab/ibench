package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public class GT extends ComparisonOperator implements Visitable, Cloneable
{

    public GT(ValueExpression left, ValueExpression right)
    {
        super(left, right);
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof GT))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public GT clone()
    {
        GT eq = (GT) super.clone();
        return eq;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    public String getSymbol()
    {
        return ">";
    }

    public int getCode()
    {
        return ComparisonOperator.GT;
    }
}
