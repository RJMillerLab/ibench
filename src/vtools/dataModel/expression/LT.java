package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public class LT extends ComparisonOperator implements Visitable, Cloneable
{

    public LT(ValueExpression left, ValueExpression right)
    {
        super(left, right);
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof LT))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public LT clone()
    {
        LT eq = (LT) super.clone();
        return eq;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    public String getSymbol()
    {
        return "<";
    }

    public int getCode()
    {
        return ComparisonOperator.LT;
    }
}
