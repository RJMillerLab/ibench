package vtools.dataModel.types;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


/**
 * An Integer Type
 */
public class Int extends Atomic implements Visitable, Cloneable
{
    public boolean equals(Object o)
    {
        if (!(o instanceof Int))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public Int clone()
    {
        if (this == Type.INTEGER)
            return this;
        return (Int) super.clone();
    }
 
    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}