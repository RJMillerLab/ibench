package vtools.visitor;


/** 
 * 
 * make all the classes that you need to be visited to implement this interface and 
 * also put the following as one of the methods of the class:
 *   
 *   public Object accept(Visitor visitor, Object[] args)
 *   {
 *       return visitor.dispatch(this, args);
 *   }
 */

public interface Visitable
{
    public Object accept(Visitor visitor, Object[] args);
} 
