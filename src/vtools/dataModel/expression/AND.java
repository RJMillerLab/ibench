package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public class AND extends LogicOperator implements Visitable, Cloneable
{

    public AND()
    {
        super();
    }

    public void set(BooleanExpression rel, int n)
    {
        _v.setElementAt(rel, n);
    }

    public void add(BooleanExpression rel)
    {
        if (rel instanceof AND)
        {
            for (int i = 0, imax = ((AND) rel).size(); i < imax; i++)
                _v.add(((AND) rel).getComponent(i));
        }
        else _v.add(rel);
    }

    public BooleanExpression getComponent(int pos)
    {
        return (BooleanExpression) _v.get(pos);
    }

    public AND clone()
    {
        return (AND) super.clone();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof AND))
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
        return "AND";
    }

    public int getCode()
    {
        return LogicOperator.AND;
    }
}
