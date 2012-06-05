package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public abstract class ComparisonOperator extends BooleanExpression implements Visitable, Cloneable
{
    private ValueExpression _left;

    private ValueExpression _right;

    public static int LT = 1;

    public static int LE = 2;

    public static int EQ = 3;

    public static int GE = 4;

    public static int GT = 5;

    public abstract String getSymbol();

    public abstract int getCode();

    protected ComparisonOperator(ValueExpression left, ValueExpression right)
    {
        _left = left;
        _right = right;
    }

    public ValueExpression getLeft()
    {
        return _left;
    }

    public ValueExpression getRight()
    {
        return _right;
    }

    public void setLeft(ValueExpression left)
    {
        _left = left;
    }

    public void setRight(ValueExpression right)
    {
        _right = right;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof ComparisonOperator))
            return false;
        if (!super.equals(o))
            return false;
        ComparisonOperator eq = (ComparisonOperator) o;
        if (!(eq._right.equals(_right)))
            return false;
        if (!(eq._left.equals(_left)))
            return false;
        return true;
    }

    public ComparisonOperator clone()
    {
        ComparisonOperator eq = (ComparisonOperator) super.clone();
        eq._left = _left.clone();
        eq._right = _right.clone();
        return eq;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
