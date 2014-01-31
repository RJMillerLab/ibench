package smark.support;

import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;

/**
 * A SMapProjection is a projection that keeps also the SMarkElement. Its label
 * is the label of the SMarkElement.
 */
public class SMarkProjection extends Projection
{
    private SMarkElement _element;

    public SMarkProjection(Path path, SMarkElement element)
    {
        super(path, "foo");
        _element = element;
        setLabel(element.getLabel());
    }

    public SMarkProjection(Path path, String projection)
    {
        super(path, "lola");
        throw new RuntimeException("This constructor should not be used for SMarkProjections");
    }

    public SMarkElement getElement()
    {
        return _element;
    }

    public void set_element(SMarkElement _element)
    {
        this._element = _element;
    }

    public SMarkProjection clone()
    {
        SMarkProjection sm = (SMarkProjection) super.clone();
        sm._element = _element;
        return sm;
    }



}
