package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public class NOT extends LogicOperator implements Visitable, Cloneable
{

    public NOT(BooleanExpression expr)
    {
        super();
        _v.setElementAt(expr, 0);
    }

    public BooleanExpression getExpr()
    {
        return _v.get(0);
    }

    public NOT clone()
    {
        return (NOT) super.clone();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof NOT))
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
        return "NOT";
    }

    public int getCode()
    {
        return LogicOperator.NOT;
    }
}
