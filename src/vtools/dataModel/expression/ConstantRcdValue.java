package vtools.dataModel.expression;


import vtools.dataModel.values.RcdValue;
import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class ConstantRcdValue extends ValueExpression implements Visitable, Cloneable
{
    private RcdValue _value;

    public RcdValue getValue()
    {
        return _value;
    }

    public void setValue(RcdValue value)
    {
        _value = value;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof ConstantRcdValue))
            return false;
        if (!super.equals(o))
            return false;
        ConstantRcdValue crcd = (ConstantRcdValue) o;
        if (!_value.equals(crcd._value))
            return false;
        return true;
    }

    public ConstantRcdValue clone()
    {
        ConstantRcdValue crcd = (ConstantRcdValue) super.clone();
        crcd._value = (RcdValue) _value.clone();
        return crcd;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
