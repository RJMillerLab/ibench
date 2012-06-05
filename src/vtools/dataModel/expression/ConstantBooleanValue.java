package vtools.dataModel.expression;


import vtools.dataModel.values.BooleanValue;
import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class ConstantBooleanValue extends BooleanExpression implements Visitable, Cloneable
{
    private BooleanValue _value;

    public BooleanValue getValue()
    {
        return _value;
    }

    public void setValue(BooleanValue value)
    {
        _value = value;
    }

    public ConstantBooleanValue clone()
    {
        ConstantBooleanValue cv = (ConstantBooleanValue) super.clone();
        cv._value = _value.clone();
        return cv;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof ConstantBooleanValue))
            return false;
        if (!super.equals(o))
            return false;
        ConstantBooleanValue cv = (ConstantBooleanValue) o;
        if (!(_value.equals(cv._value)))
            return false;
        return true;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
