package vtools.dataModel.types;

import java.util.Vector;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class Structured extends Type implements Visitable, Cloneable
{
    protected Vector<NameTypePair> _attributes;

    public int size()
    {
        return _attributes.size();
    }

    public Structured()
    {
        _attributes = new Vector<NameTypePair>();
    }


    public void addField(NameTypePair attr)
    {
        _attributes.add(attr);
    }

    public void addField(NameTypePair attr, int pos)
    {
        _attributes.add(pos, attr);
    }

    public NameTypePair getField(String l)
    {
        for (int i = 0, imax = size(); i < imax; i++)
        {
            NameTypePair tmp = getField(i);
            if (tmp.getLabel().equals(l))
                return tmp;
        }
        return null;
    }

    public NameTypePair getField(int i)
    {
        return (NameTypePair) _attributes.elementAt(i);
    }

    public int getFieldPosition(String name)
    {
        int pos = -1;
        for (int i = 0, imax = _attributes.size(); i < imax; i++)
        {
            NameTypePair t = (NameTypePair) _attributes.elementAt(i);
            if (!t.getLabel().equals(name))
                continue;
            pos = i;
        }
        return pos;
    }

    public NameTypePair removeField(String name)
    {
        int pos = getFieldPosition(name);
        if (pos != -1)
            return (NameTypePair) _attributes.remove(pos);
        return null;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Structured))
            return false;
        if (!super.equals(o))
            return false;
        Structured stru = (Structured) o;
        if (_attributes.size() != stru._attributes.size())
            return false;
        for (int i = 0, imax = _attributes.size(); i < imax; i++)
        {
            if (!(_attributes.elementAt(i).equals(stru._attributes.elementAt(i))))
                return false;
        }
        return true;
    }

    public Structured clone()
    {
        Structured stru = (Structured) super.clone();
        Vector<NameTypePair> nv = new Vector<NameTypePair>();
        for (int i = 0, imax = _attributes.size(); i < imax; i++)
        {
            nv.add((_attributes.elementAt(i)).clone());
        }
        stru._attributes = nv;
        return stru;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

}
