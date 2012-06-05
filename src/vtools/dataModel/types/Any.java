package vtools.dataModel.types;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class Any extends Type implements Visitable, Cloneable
{

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
    
    public Any clone()
    {
        if (this == Type.ANY)
            return this;
        Any any = (Any) super.clone();
        return any;
    }
    
    public boolean equals(Object o)
    {
        if (!(o instanceof Any))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

}
