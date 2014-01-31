package vtools.dataModel.expression;


import vtools.dataModel.values.GroupValue;
import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class ConstantSetValue extends ValueExpression implements Visitable, Cloneable
{
    private GroupValue _value;

    public GroupValue getValue()
    {
        return _value;
    }

    public void setValue(GroupValue value)
    {
        _value = value;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof ConstantSetValue))
            return false;
        if (!super.equals(o))
            return false;
        ConstantSetValue cset = (ConstantSetValue) o;
        if (!_value.equals(cset._value))
            return false;
        return true;
    }

    public ConstantSetValue clone()
    {
        ConstantSetValue csetValue = (ConstantSetValue) super.clone();
        csetValue._value = _value.clone();
        return csetValue;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
