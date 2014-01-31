package vtools.dataModel.values;

import vtools.utils.structures.SetAssociativeArray;
import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


/**
 * It represents a record (not in the schema terms but in the value) For
 * instance, a tuple is a RcdValue
 */
public class RcdValue extends Value implements Visitable, Cloneable
{
    private SetAssociativeArray _fields;

    public RcdValue()
    {
        _fields = new SetAssociativeArray();
    }

    public void addField(String name, Value value)
    {
        _fields.add(name, value);
    }

    public Value getFieldValue(String name)
    {
        return (Value) _fields.getValue(name);
    }

    public Value getFieldValue(int position)
    {
        return (Value) _fields.getValue(position);
    }

    public int size()
    {
        return _fields.size();
    }

    public String getFieldLabel(int position)
    {
        return (String) _fields.getKey(position);
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof RcdValue))
            return false;
        if (!super.equals(o))
            return false;
        RcdValue rcdv = (RcdValue) o;
        if (_fields.size() != rcdv._fields.size())
            return false;
        for (int i = 0, imax = _fields.size(); i < imax; i++)
        {
            if (!(_fields.getKey(i).equals(rcdv._fields.getKey(i))))
                return false;
            if (!(_fields.getValue(i).equals(rcdv._fields.getValue(i))))
                return false;
        }
        return true;
    }

    public RcdValue clone()
    {
        RcdValue rcdVal = (RcdValue) super.clone();
        SetAssociativeArray rcd = new SetAssociativeArray();
        for (int i = 0, imax = _fields.size(); i < imax; i++)
        {
            rcd.add(new String((String) _fields.getKey(i)), ((Value) _fields.getValue(i)).clone());
        }
        rcdVal._fields = rcd;
        return rcdVal;
    }
}
