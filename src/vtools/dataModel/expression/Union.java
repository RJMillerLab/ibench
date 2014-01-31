package vtools.dataModel.expression;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;



public class Union extends SetOperation implements Visitable, Cloneable
{

    public Union()
    {
        super();
    }

    public String getSymbol()
    {
        return "UNION";
    }

    public int getCode()
    {
        return SetOperation.UNION;
    }

    public Union clone()
    {
        return (Union) super.clone();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Union))
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
