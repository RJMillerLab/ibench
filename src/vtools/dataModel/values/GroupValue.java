package vtools.dataModel.values;

import java.util.Vector;

import vtools.visitor.Visitable;


/**
 * It represents a set or a bag of values
 */
public abstract class GroupValue extends Value implements Cloneable, Visitable
{

    protected Vector<Value> _members;

    public GroupValue()
    {
        _members = new Vector<Value>();
    }

    public void addMember(Value value)
    {
        if (canBeAdded(value))
            _members.add(value);
    }

    protected abstract boolean canBeAdded(Value v);

    public Value getMember(int position)
    {
        return (Value) _members.elementAt(position);
    }

    public int size()
    {
        return _members.size();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof GroupValue))
            return false;
        if (!super.equals(o))
            return false;
        GroupValue grpVal = (GroupValue) o;
        if (_members.size() != grpVal._members.size())
            return false;
        for (int i = 0, imax = _members.size(); i < imax; i++)
        {
            if (!((Value) _members.elementAt(i)).equals(grpVal._members.elementAt(i)))
                return false;
        }
        return true;
    }

    public GroupValue clone()
    {
        GroupValue grpVal = (GroupValue) super.clone();
        Vector<Value> members = new Vector<Value>();
        for (int i = 0, imax = _members.size(); i < imax; i++)
        {
            members.add((Value) _members.elementAt(i).clone());
        }
        grpVal._members = members;
        return grpVal;
    }
}
