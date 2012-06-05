package vtools.dataModel.expression;

import vtools.dataModel.values.AtomicValue;
import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class ConstantAtomicValue extends ValueExpression implements Visitable, Cloneable
{
    private AtomicValue _value;

    public ConstantAtomicValue(AtomicValue value)
    {
        if (value == null)
            throw new RuntimeException("Need a real atomic value");
        _value = value;
    }

    public AtomicValue getValue()
    {
        return _value;
    }

    public void setValue(AtomicValue value)
    {
        _value = value;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof ConstantAtomicValue))
            return false;
        if (!super.equals(o))
            return false;
        ConstantAtomicValue cat = (ConstantAtomicValue) o;
        if (!_value.equals(cat._value))
            return false;
        return true;
    }

    public ConstantAtomicValue clone()
    {
        ConstantAtomicValue catValue = (ConstantAtomicValue) super.clone();
        catValue._value = _value.clone();
        return catValue;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
