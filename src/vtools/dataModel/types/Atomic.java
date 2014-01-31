package vtools.dataModel.types;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public abstract class Atomic extends Type implements Visitable, Cloneable
{

    public boolean equals(Object o)
    {
        if (!(o instanceof Atomic))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }
    
    public Atomic clone()
    {
        return (Atomic)super.clone();
    }    

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
