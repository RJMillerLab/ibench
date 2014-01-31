package smark.support;

import vtools.dataModel.schema.Element;
import vtools.dataModel.types.Type;


/**
 * It is an element used to represent an attribute. It has a number of fields
 * that specify what the attribute is, how it was generated, etc. 
 */
public class SMarkElement extends Element
{
    // This is the fragment. All the set types take a number but actually since
    // we do not do nested mappings for the moment, we will have the fragments
    // to be the set numbers. In the future, this may change. Note that by
    // convention 0 corresponds to the root, so the numbering here starts from 1
    private int _fragment;

    // This by default should always be 0 unless we have cyclic joins in which
    // case it will be 1, 2, 3, etc.
    private int _fragmentAppearance;

    public SMarkElement(String name, Type type, Element parent, int fragment, int fragmentAppearance)
    {
        super(name, type, parent);
        _fragment = fragment;
        _fragmentAppearance = fragmentAppearance;
    }

    public void setFragmentAppearance(int appearance)
    {
        _fragmentAppearance = appearance;
    }
    
    public int getFragmentAppearance()
    {
        return _fragmentAppearance;
    }

    public int getFragment()
    {
        return _fragment;
    }

    public void setFragment(int fragment)
    {
        _fragment = fragment;
    }
    
    /**
     * Clones the object but keep in mind that the eqClass does not know that
     * the new object points to it. So take the right action when you call 
     * clone. That is, either make its eqClass to be null or inform the eqClass
     * that a new member was added.
     */
    public SMarkElement clone()
    {
        SMarkElement tmp = (SMarkElement) super.clone();
        tmp._fragment = _fragment;
        tmp._fragmentAppearance = _fragmentAppearance;
        return tmp;
    }
    
    public boolean equals(Object o)
    {
    	if(this==o)
    		return true;
    	else
    		return super.equals(o);
    }
    
}
