package vtools.dataModel.values;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public class IntegerValue extends AtomicValue implements Visitable, Cloneable
{
    int _value; 

    public IntegerValue(int value)
    {
        _value = value;
    }

    public int getValue()
    {
        return _value;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
    
    public IntegerValue clone()
    {
        IntegerValue v = (IntegerValue) super.clone();
        v._value = _value;
        return v;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof IntegerValue))
            return false;
        if (!super.equals(o))
            return false;
        IntegerValue oi = (IntegerValue) o;
        if (_value != oi._value)
            return false;
        return true;

    }

}
