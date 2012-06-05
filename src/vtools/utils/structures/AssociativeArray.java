package vtools.utils.structures;

import java.util.Vector;

/**
 * Has pairs of <key, value> that are objects, thus, they can be whatever Note
 * that for the objects you use, if they are complex, i.e., not Strings, you
 * will have to define your own equals method.
 */
public abstract class AssociativeArray
{
    protected boolean _duplicatesAllowed = true;

    Vector<Object> _keys;

    Vector<Object> _values;

    public AssociativeArray()
    {
        _keys = new Vector<Object>();
        _values = new Vector<Object>();
    }

    public int size()
    {
        return _keys.size();
    }

    /**
     * Adds the specific entry in the provided position and shifts the rest by 1
     */
    public void insertAt(Object key, Object value, int position)
    {
        if (!_duplicatesAllowed)
        {
            int i = vindexOf(_keys, key);
            if (i != -1)
                throw new RuntimeException("Key " + key + " already exists ");
        }
        _keys.insertElementAt(key, position);
        _values.insertElementAt(value, position);

    }

    protected int vindexOf(Vector<Object> v, Object key)
    {
        return v.indexOf(key);
    }

    public void add(Object key, Object value)
    {
        if (!_duplicatesAllowed)
        {
            int i = vindexOf(_keys, key);
            if (i != -1)
                throw new RuntimeException("Key " + key + " already exists ");
        }
        _keys.add(key);
        _values.add(value);
    }

    public Object getKey(int i)
    {
        return _keys.elementAt(i);
    }

    public Object getValue(int i)
    {
        return _values.elementAt(i);
    }

    public int getKeyPosition(Object key)
    {
        for (int i = 0, imax = _keys.size(); i < imax; i++)
        {
            if (vequals(key, _keys.elementAt(i)))
                return i;
        }
        return -1;
    }

    protected boolean vequals(Object o1, Object o2)
    {
        return o1.equals(o2);
    }

    /**
     * Returns the value of the first occurence of the key key starting checking
     * at position k
     */
    public Object getValue(Object key, int k)
    {
        int i = vindexOf(_keys, key, k);
        if (i == -1)
            return null;
        return getValue(i);
    }

    protected int vindexOf(Vector<Object> v, Object key, int k)
    {
        return v.indexOf(key, k);
    }

    public Object getValue(Object key)
    {
        int i = vindexOf(_keys, key);
        if (i == -1)
            return null;
        return getValue(i);
    }

    public void setKeyAt(int i, Object key)
    {
        _keys.set(i, key);
    }

    public void setValueAt(int i, Object value)
    {
        _values.set(i, value);
    }

    public void setValueOf(Object key, Object value)
    {
        int i = vindexOf(_keys, key);
        if (i == -1)
            throw new RuntimeException("The key " + key + " does not exists in the Assoc. Array");
        setValueAt(i, value);
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0, imax = _keys.size(); i < imax; i++)
        {
            buf.append(i + ". " + _keys.elementAt(i) + " : ");
            buf.append(_values.elementAt(i));
        }
        return buf.toString();
    }

}
