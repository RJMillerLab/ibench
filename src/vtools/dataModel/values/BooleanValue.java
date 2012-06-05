package vtools.dataModel.values;

import vtools.visitor.Visitable;


/**
 * It represents a TRUE OR a FALSE
 */
public abstract class BooleanValue extends AtomicValue implements Visitable, Cloneable
{
    public BooleanValue clone()
    {
        return (BooleanValue) super.clone();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof BooleanValue))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }


}
