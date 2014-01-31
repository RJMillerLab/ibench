package vtools.dataModel.types;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

/**
 * Represents Sets Lists and Bags
 */
public abstract class Group extends Structured implements Visitable, Cloneable
{
    public boolean equals(Object o)
    {
        if (!(o instanceof Group))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }
    
    public Group clone()
    {
        return (Group) super.clone();
    } 
    
    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}