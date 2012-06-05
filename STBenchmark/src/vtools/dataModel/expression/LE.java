package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public class LE extends ComparisonOperator implements Visitable, Cloneable
{

    public LE(ValueExpression left, ValueExpression right)
    {
        super(left, right);
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof LE))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public LE clone()
    {
        LE eq = (LE) super.clone();
        return eq;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    public String getSymbol()
    {
        return "<=";
    }


    public int getCode()
    {
        return ComparisonOperator.LE;
    }
}
