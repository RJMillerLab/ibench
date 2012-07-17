package vtools.dataModel.expression;

import java.util.Vector;
import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


public class Intersect extends SetOperation implements Visitable, Cloneable
{
    public Intersect()
    {
        super();
    }

    public String getSymbol()
    {
        return "INTERSECT";
    }

    public int getCode()
    {

        return SetOperation.INTERSECT;
    }

    public Intersect clone()
    {
        return (Intersect) super.clone();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Intersect))
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
