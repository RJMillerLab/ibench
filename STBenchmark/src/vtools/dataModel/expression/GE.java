package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public class GE extends ComparisonOperator implements Visitable, Cloneable
{

    public GE(ValueExpression left, ValueExpression right)
    {
        super(left, right);
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof GE))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public GE clone()
    {
        GE eq = (GE) super.clone();
        return eq;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    public String getSymbol()
    {
        return ">=";
    }

    public int getCode()
    {
        return ComparisonOperator.GE;
    }
}
