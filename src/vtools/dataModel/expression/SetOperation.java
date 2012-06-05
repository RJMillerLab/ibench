package vtools.dataModel.expression;

import java.util.Vector;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public abstract class SetOperation extends Query implements Visitable, Cloneable
{

    public static int UNION = 0;

    public static int INTERSECT = 0;

    public static int DIFFERENCE = 0;


    protected Vector<Query> _v;

    protected SetOperation()
    {
        _v = new Vector<Query>();
    }

    public SetOperation(Vector<Query> components)
    {
        _v = components;
    }

    public abstract String getSymbol();

    public abstract int getCode();

    public void set(Query rel, int n)
    {
        _v.setElementAt(rel, n);
    }

    public void add(Query rel)
    {
        _v.add(rel);
    }

    public Query getComponent(int pos)
    {
        return (Query) _v.get(pos);
    }

    public int size()
    {
        return _v.size();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof SetOperation))
            return false;
        if (!super.equals(o))
            return false;
        SetOperation op = (SetOperation) o;
        if (_v.size() != op._v.size())
            return false;
        for (int i = 0, imax = _v.size(); i < imax; i++)
        {
            if (!(_v.elementAt(i).equals(op._v.elementAt(i))))
                return false;
        }
        return true;
    }

    public SetOperation clone()
    {
        SetOperation soq = (SetOperation) super.clone();
        soq._v = new Vector<Query>();
        for (int i = 0, imax = _v.size(); i < imax; i++)
            _v.add(_v.elementAt(i).clone());
        return soq;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
