package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class Variable extends Path implements Visitable, Cloneable
{
    private String _name;

    public Variable(String name)
    {
        if (name == null)
            throw new RuntimeException("variable name cannot be null");
        _name = name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getName()
    {
        return _name;
    }

    public Variable clone()
    {
        Variable tbl = (Variable) super.clone();
        tbl._name = new String(_name);
        return tbl;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Variable))
            return false;
        if (!super.equals(o))
            return false;
        if (!_name.equals(((Variable) o)._name))
            return false;
        return true;

    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
