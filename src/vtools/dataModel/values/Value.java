package vtools.dataModel.values;

import vtools.VObject;
import vtools.visitor.Visitable;

/* 
 * Is used to represent atomic or set value  ... 
 */
public abstract class Value extends VObject implements Visitable, Cloneable
{
    public static NULL NULL = new NULL();

    public static TRUE TRUE = new TRUE();

    public static FALSE FALSE = new FALSE();

    public Value clone()
    {
        return (Value) super.clone();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Value))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }
}
