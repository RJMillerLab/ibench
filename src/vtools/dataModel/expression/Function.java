package vtools.dataModel.expression;

import java.util.Vector;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public class Function extends ValueExpression implements Visitable, Cloneable
{
    String _name;

    Vector<ValueExpression> _args;

    public Function(String name)
    {
        _name = name;
        _args = new Vector<ValueExpression>();
    }

    public Function(String name, Vector<ValueExpression> args)

    {
        _name = name;
        _args = new Vector<ValueExpression>();
        for (int i = 0, imax = args.size(); i < imax; i++)
            _args.addElement(args.elementAt(i).clone());
    }

    public String getName()
    {
        return _name;
    }

    public void addArg(ValueExpression arg)
    {
        _args.add(arg);
    }

    public ValueExpression getArg(int i)
    {
        return (ValueExpression) _args.elementAt(i);
    }

    public void removeArg(int i)
    {
        if (i >= _args.size())
            throw new RuntimeException("Not that many attributes. Removal failed");
        _args.remove(i);
    }

    public int getNumOfArgs()
    {
        return _args.size();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Function))
            return false;
        if (!super.equals(o))
            return false;
        Function f = (Function) o;
        if (!f._name.equals(_name))
            return false;
        for (int i = 0, imax = _args.size(); i < imax; i++)
        {
            ValueExpression arg = _args.elementAt(i);
            if (!arg.equals(f.getArg(i)))
                return false;
        }
        return true;
    }

    public Function clone()
    {
        Function f = (Function) super.clone();
        f._name = new String(_name);
        f._args = new Vector<ValueExpression>();
        for (int i = 0, imax = _args.size(); i < imax; i++)
            f._args.add(_args.elementAt(i).clone());
        return f;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
