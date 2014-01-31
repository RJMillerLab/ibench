package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class Projection extends Path implements Visitable, Cloneable
{
    private Path _prefix;

    private String _label;

    public Projection(Path prefix, String label)
    {
        if (prefix == null)
            throw new RuntimeException("Prefix cannot be null in a Projection expression");
        if (label == null)
            throw new RuntimeException("Label cannot be null in a Projection expression");
        _prefix = prefix;
        _label = label;
    }

    public String getLabel()
    {
        return _label;
    }

    public void setLabel(String label)
    {
        _label = label;
    }

    public Path getPrefix()
    {
        return _prefix;
    }

    public Projection clone()
    {
        Projection a = (Projection) super.clone();
        a._prefix = _prefix.clone();
        a._label = new String(_label);
        return a;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Projection))
            return false;
        if (!super.equals(o))
            return false;
        Projection proj = (Projection) o;
        if (!proj._label.equals(_label))
            return false;
        if (!proj._prefix.equals(_prefix))
            return false;
        return true;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}
