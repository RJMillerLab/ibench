package vtools.dataModel.values;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

/*
 *  
 */
public class StringValue extends AtomicValue implements Visitable, Cloneable
{
    String _value;

    public StringValue(String value)
    {
        if (value == null)
            throw new RuntimeException();
        _value = new String(value);
    }

    public String getValue()
    {
        return _value;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof StringValue))
            return false;
        if (!super.equals(o))
            return false;
        StringValue oi = (StringValue) o;
        if (!_value.equals(oi._value))
            return false;
        return true;
    }

    public StringValue clone()
    {
        StringValue strValue = (StringValue) super.clone();
        strValue._value = new String(_value);
        return strValue;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
