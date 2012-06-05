package vtools.dataModel.schema;

import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.NameTypePair;
import vtools.dataModel.types.Structured;
import vtools.dataModel.types.Type;
import vtools.visitor.Visitor;

/**
 * An element is a label-type pair that records the parent element it belongs.
 * It should be used for the nested relational model. The table and attribute
 * are specializations of this for the relational model only.
 */
public class Element extends NameTypePair implements Cloneable
{

    private Element _parent;

    public Element(String name, Type type, Element parent)
    {
        super(name, type);
        _parent = parent;
    }

    public Element getParent()
    {
        return _parent;
    }

    public void setParent(Element parent)
    {
        _parent = parent;
    }

    private void addSubElement(Element child, boolean virtually)
    {
        if ((_type instanceof Atomic) || (_type == Type.ANY))
            throw new RuntimeException("Subelements cannot be added under atomic or ANY types");
        else if (_type instanceof Structured)
        {
            Structured strType = (Structured) _type;
            strType.addField(child);
            if (!virtually)
                child.setParent(this);
        }
        else throw new RuntimeException("Should not happen!");
    }

    private void addSubElementAt(Element child, int pos, boolean virtually)
    {
        if ((_type instanceof Atomic) || (_type == Type.ANY))
            throw new RuntimeException("Subelements cannot be added under atomic or ANY types");
        else if (_type instanceof Structured)
        {
            Structured strType = (Structured) _type;
            strType.addField(child, pos);
            if (!virtually)
                child.setParent(this);
        }
        else throw new RuntimeException("Should not happen!");
    }

    public Element removeSubElement(String name)
    {
        if ((_type instanceof Atomic) || (_type == Type.ANY))
            throw new RuntimeException("Subelements cannot exist under atomic/ANY types");
        else if (_type instanceof Structured)
        {
            Structured rcd = (Structured) _type;
            return (Element) rcd.removeField(name);
        }
        else throw new RuntimeException("Should not happen!");
    }

    public void addVirtualSubElement(Element child)
    {
        addSubElement(child, true);
    }

    public void addVirtualSubElementAt(Element child, int pos)
    {
        addSubElementAt(child, pos, true);
    }

    public void addSubElement(Element child)
    {
        addSubElement(child, false);
    }

    public void addSubElementAt(Element child, int pos)
    {
        addSubElementAt(child, pos, false);
    }

    public Element getSubElement(int i)
    {
        if ((_type instanceof Atomic) || (_type == Type.ANY))
            throw new RuntimeException("Subelements cannot exist under atomic types");
        else if (_type instanceof Structured)
        {
            Structured strT = (Structured) _type;
            return (Element) strT.getField(i);
        }
        else throw new RuntimeException("Should not happen!");
    }

    public int size()
    {
        if ((_type instanceof Atomic) || (_type == Type.ANY))
            return 0;
        else if (_type instanceof Structured)
        {
            Structured strT = (Structured) _type;
            return strT.size();
        }
        else throw new RuntimeException("Unknown Type: Should not have happened!");
    }

    public Element clone()
    {
        Element el = (Element) super.clone();
        el._parent = null;
        // Inform the kids that I am the father :-)
        if (el._type instanceof Structured)
        {
            Structured ct = (Structured) el._type;
            for (int i = 0, imax = ct.size(); i < imax; i++)
            {
                if (ct.getField(i) instanceof Element)
                {
                    Element e = (Element) ct.getField(i);
                    e.setParent(el);
                }
            }
        }
        return el;
    }

    /**
     * Returns the element with the specific label.
     */
    public Element getSubElement(String labelName)
    {
        if ((_type instanceof Atomic) || (_type == Type.ANY))
            return null;
        int pos = getSubElementPosition(labelName);
        if (pos == -1)
            return null;
        return getSubElement(pos);
    }

    /**
     * Returns the position of an element in the list of sub-elements
     */
    public int getSubElementPosition(String name)
    {
        if ((_type instanceof Atomic) || (_type == Type.ANY))
            return -1;
        // To have subelement, Type must be Rcd or Set of Rcd
        for (int i = 0, imax = size(); i < imax; i++)
        {
            Element el = getSubElement(i);
            if (el.getLabel().equals(name))
                return i;
        }
        return -1;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Element))
            return false;
        if (!super.equals(o))
            return false;
        Element e = (Element) o;
        if (_parent == null)
        {
            if (e._parent != null)
                return false;
        }
        else
        {
            if (e._parent == null)
                return false;
            else if (!e._parent.equals(_parent))
                return false;
        }
        return true;
    }
}