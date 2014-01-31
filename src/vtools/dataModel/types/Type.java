package vtools.dataModel.types;

import vtools.VObject;
import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public abstract class Type extends VObject implements Visitable, Cloneable
{
    public static Int INTEGER = new Int();

    public static Str STRING = new Str();

    public static Any ANY = new Any();


    public boolean equals(Object o)
    {
        if (!(o instanceof Type))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }
    
    public Type clone()
    {
        return (Type)super.clone();
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}