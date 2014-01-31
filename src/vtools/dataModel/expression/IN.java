package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class IN extends BooleanExpression implements Visitable, Cloneable
{
    ValueExpression _val;

    Query _q;

    public IN(ValueExpression val, Query q)
    {
        _val = val;
        _q = q;
    }

    public Query getRelation()
    {
        return _q;
    }

    public ValueExpression getExpression()
    {
        return _val;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof IN))
            return false;
        if (!super.equals(o))
            return false;
        IN newObj = (IN) o;
        if (!(_q.equals(newObj._q)))
            return false;
        if (!(_val.equals(newObj._val)))
            return false;
        return true;
    }

    public IN clone()
    {
        IN newObj = (IN) super.clone();
        newObj._q = _q.clone();
        newObj._val = (ValueExpression) _val.clone();
        return newObj;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
