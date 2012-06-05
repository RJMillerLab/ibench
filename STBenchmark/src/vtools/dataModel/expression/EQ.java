package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public class EQ extends ComparisonOperator implements Visitable, Cloneable
{

    public EQ(ValueExpression left, ValueExpression right)
    {
        super(left, right);
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof EQ))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public EQ clone()
    {
        EQ eq = (EQ) super.clone();
        return eq;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    public String getSymbol()
    {
        return "=";
    }

    public int getCode()
    {
        return ComparisonOperator.EQ;
    }
}
