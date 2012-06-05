package vtools.dataModel.values;

import vtools.visitor.Visitable;


/**
 * It represents an integer or a string value (or NULL)
 */
public abstract class AtomicValue extends Value implements Visitable, Cloneable
{
    public AtomicValue clone()
    {
        return (AtomicValue) super.clone();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof AtomicValue))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

}
