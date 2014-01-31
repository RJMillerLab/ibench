package vtools.dataModel.types;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

/**
 * Represents an array of something. This something is presumably attributes.
 */
public class Rcd extends Complex implements Visitable, Cloneable
{
    public Rcd()
    {
        super();
    }
    
    public Rcd clone()
    {
        return (Rcd) super.clone();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Rcd))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }
    
    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}