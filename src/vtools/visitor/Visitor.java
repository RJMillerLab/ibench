package vtools.visitor;

/**
 * Any Visitor should have this function, but this is in the
 * VisitorImplementation anyway, so no need to have it.
 */

public interface Visitor
{
    public Object dispatch(Object o, Object[] args);
}