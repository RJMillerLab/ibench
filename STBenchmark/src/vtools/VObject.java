package vtools;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

/**
 * A VObject node is the vLibrary top Class (something like the Object class in
 * the jdk). It serves the following purposes: It provides a hook. A hook is an
 * Object attribute that can be used by external programs for any purpose they
 * are willing. NOTE THAT the clone simply copies the hook, thus a clone call
 * needs to keep that in mind.
 */
public abstract class VObject implements Cloneable, Visitable
{
    protected Object _hook = null;

    /**
     * @returns the hook
     */
    public Object getHook()
    {
        return _hook;
    }

    /**
     * sets the hook to an Object
     */
    public void setHook(Object hook)
    {
        _hook = hook;
    }

    public Object clone()
    {
        VObject n;
        try
        {
            n = (VObject) super.clone();
            if (_hook instanceof String)
            {
                n._hook = new String((String) _hook);
            }
            else n._hook = _hook;
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException("This should not happen since we are cloneable");
        }
        return n;
    }

    public abstract Visitor getPrintVisitor();

    public boolean equals(Object o)
    {
        if (!(o instanceof VObject))
            return false;
        VObject n = (VObject) o;
        if (_hook == null)
        {
            if (n.getHook() != null)
                return false;
        }
        else
        {
            if (n.getHook() == null)
                return false;
            if (!(n.getHook().equals(_hook)))
                return false;
        }
        return true;
    }


    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        Object[] args = new Object[50];
        args[0] = buf;
        args[1] = new Integer(0);
        this.accept(getPrintVisitor(), args);
        return buf.toString();
    }

    public Object accept(Visitor visitor, Object[] args)
    {
        return visitor.dispatch(this, args);
    }
}
