package vtools.dataModel.types;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

/**
 * Represents a set of records. These records are presumably attributes.
 */
public class Set extends Group implements Visitable, Cloneable
{
    public Set()
    {
        super();
    }
    
    public Set clone()
    {
        return (Set) super.clone();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Set))
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