package vtools.dataModel.expression;


import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class EXISTS extends BooleanExpression implements Visitable, Cloneable
{
    private Query _q;

    public EXISTS(Query q)
    {
        _q = q;
    }

    public Query getRelation()
    {
        return _q;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof EXISTS))
            return false;
        if (!super.equals(o))
            return false;
        EXISTS newObj = (EXISTS) o;
        if (!newObj._q.equals(_q))
            return false;
        return true;
    }

    public EXISTS clone()
    {
        EXISTS newObj = (EXISTS) super.clone();
        newObj._q = (Query) _q.clone();
        return newObj;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
