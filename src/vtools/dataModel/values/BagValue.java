package vtools.dataModel.values;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;


/**
 * It represents a bag of (constant) values (This is not fully implemented yet)
 */
public class BagValue extends GroupValue implements Visitable, Cloneable
{

    protected boolean canBeAdded(Value v)
    {
        return true;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
    
    public boolean equals(Object o)
    {
        if (!(o instanceof BagValue))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

    public BagValue clone()
    {
        BagValue bagVal = (BagValue) super.clone();
        return bagVal;
    }

}
