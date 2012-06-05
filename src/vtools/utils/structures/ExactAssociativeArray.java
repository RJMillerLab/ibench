package vtools.utils.structures;

import java.util.Vector;

/**
 * It is an Associative array with the only difference is that it bases its
 * comparisons on exact equality (i.e., ==) instead of the equals(Object)
 * function
 */
public class ExactAssociativeArray extends AssociativeArray
{
    protected boolean vequals(Object o1, Object o2)
    {
        return o1 == o2;
    }


    protected int vindexOf(Vector<Object> v, Object key, int k)
    {
        for (int i = k, imax = v.size(); i < imax; i++)
        {
            if (v.elementAt(i) == key)
                return i;
        }
        return -1;
    }

    protected int vindexOf(Vector<Object> v, Object key)
    {
        return vindexOf(v, key, 0);
    }

}
