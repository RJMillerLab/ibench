package vtools.dataModel.types;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public abstract class Complex extends Structured implements Visitable, Cloneable
{
    public boolean equals(Object o)
    {
        if (!(o instanceof Complex))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public Complex clone()
    {
        return (Complex) super.clone();
    }
    
    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}