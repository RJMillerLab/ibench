package vtools.dataModel.types;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


/**
 * A String Atomic Type
 */
public class Str extends Atomic implements Visitable, Cloneable
{

    public boolean equals(Object o)
    {
        if (!(o instanceof Str))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public Str clone()
    {
        if (this == Type.STRING)
            return this;
        return (Str)super.clone();
    }
   
    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    public String toString () {
    	return "Atomic.STRING";
    }
    
}