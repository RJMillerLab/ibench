package vtools.dataModel.types;

import vtools.VObject;
import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

/**
 * It is a pair of a label and a type
 */
public abstract class NameTypePair extends VObject implements Visitable, Cloneable
{
    protected String _label;

    protected Type _type;

    public NameTypePair(String name, Type type)
    {
        _label = name;
        _type = type;
    }

    public String getLabel()
    {
        return _label;
    }

    public Type getType()
    {
        return _type;
    }

    public void setType(Type type)
    {
        _type = type;
    }

    public NameTypePair clone()
    {
        NameTypePair ntp = (NameTypePair) super.clone();
        ntp._label = new String(_label);
        ntp._type = (Type) _type.clone();
        return ntp;
    }

    public boolean equals(Object o)
    {    	
    	
        if (!(o instanceof NameTypePair))
            return false;
        if (! super.equals(o))
            return false;
        NameTypePair ltpo = (NameTypePair) o;
        if (!_label.equals(ltpo._label))
            return false;
        if (!_type.equals(ltpo._type))
            return false;
        return true;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}
