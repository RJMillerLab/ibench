package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public class SetDifference extends SetOperation implements Visitable, Cloneable
{
    public SetDifference()
    {
        super();
    }

    public SetDifference(Query left, Query right)
    {
        super();
        super.add(left);
        super.add(right);
    }

    public void add(Query rel)
    {
        throw new RuntimeException("Cannot use this for set-difference. Use setLeft or setRight instead.");
    }

    public void setLeft(Query q)
    {
        if (_v.size() == 0)
            _v.add(q);
        else _v.set(0, q);
    }

    public void setRight(Query q)
    {
        if (_v.size() == 0)
        {
            _v.add(null);
            _v.add(q);
        }
        else if (_v.size() == 1)
            _v.add(q);
        else _v.set(0, q);
    }

    public Query getLeft()
    {
        if (_v.size() < 1)
            return null;
        return getComponent(0);
    }

    public Query getRight()
    {
        if (_v.size() < 2)
            return null;
        return getComponent(1);
    }

    public String getSymbol()
    {
        return "MINUS";
    }

    public int getCode()
    {
        return SetOperation.DIFFERENCE;
    }

    public SetDifference clone()
    {
        return (SetDifference) super.clone();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof SetDifference))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}
