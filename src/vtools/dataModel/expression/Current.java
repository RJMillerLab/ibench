package vtools.dataModel.expression;


import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class Current extends Path implements Visitable, Cloneable
{

    /*
     * For programming purposes, two current paths are equal, but in general in
     * a query semantics, we should not equate them.
     */
    public boolean equals(Object o)
    {
        if (!(o instanceof Current))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public Current clone()
    {
        return (Current) super.clone();
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}
