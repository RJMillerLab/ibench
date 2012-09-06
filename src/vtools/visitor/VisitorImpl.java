package vtools.visitor;

import java.lang.reflect.Method;

import vtools.dataModel.expression.Union;

public class VisitorImpl implements Visitor
{

    public Object visit(Object o, Object[] args)
    {
        throw new RuntimeException("No visit has been defined in Visitor " + this.getClass().getName()
            + " for class " + o.getClass().getName());
    }

    public Object dispatch(Object o, Object[] args)
    {
        Class currClass = o.getClass();
        Class objArrayClass = (new Object[30]).getClass();

        // Start traversing the class hierarchy until you find a class for which
        // there is a method visit with that class as an argument. Note that
        // this loop will stop for sure at some point since we have a visit
        // method for the Object class.
        Method m = null;
        while (m == null)
        {
            try
            {
                // log.debug("Looking for " + this.getClass().getName()
                // + ".visit(" + currClass.getName()
                // + "," + objArrayClass.getName() + ")");
                m = this.getClass().getMethod("visit", new Class[]
                {
                        currClass, objArrayClass
                });
            }
            catch (NoSuchMethodException e)
            {
                // log.debug("Nothing for " + currClass.getName());
                currClass = currClass.getSuperclass();
            }
        }

        if (m == null)
            throw new RuntimeException("This is impossible to happen "
                + "given the existence of the visit for the Object");

        // Now we try to invoke the method we found above.
        try
        {
            return m.invoke(this, o, args);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        throw new RuntimeException("This point should have never been reached. ");
    }
}
