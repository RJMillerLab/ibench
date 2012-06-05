package vtools.utils.structures;

import java.util.Vector;

/**
 * The role of an EqClassManager is to categorize objects into eqClasses. Note
 * that an object can be either into one equivalence class or to none. If an
 * object belongs to 2 classes then these two eqclasses are actually one.
 */
public class EqClassManager
{
    // Specify whether we will use exactEquality or equals equality when we are
    // comparing objects
    public static int EQCHECK = 1;

    public static int ABSCHECK = 2;

    private int _checkMethod;

    private Object[] _members;

    private Object[] _representatives;

    private Vector<Object> _eqClasses;

    private Vector<Object> _hooks;

    int _size;

    private static int _INCREASE_STEP = 50;

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0, imax = _size; i < imax; i++)
        {
            buf.append(i + ". ---------------------------------------------------------\n");
            buf.append(_members[i] + " Representative:");
            buf.append(_representatives[i] + " with value ");
            Object hook = getEqClassHook(_representatives[i]);
            if (hook == null)
                buf.append("XXX\n");
            else buf.append(hook + "\n");

        }
        // for (int i = 0, imax = _eqClasses.size(); i < imax; i++)
        // {
        // buf.append(i + ". ******************************************\n");
        // buf.append(_eqClasses.elementAt(i) + "\n [" + _hooks.elementAt(i) +
        // "]\n");
        // }
        return buf.toString();


    }

    public EqClassManager(int method)
    {
        if ((method != EQCHECK) && (method != ABSCHECK))
            throw new RuntimeException("Not supported check method");
        _checkMethod = method;
        _members = new Object[50];
        _representatives = new Object[50];
        _eqClasses = new Vector<Object>();
        _hooks = new Vector<Object>();
        _size = 0;
    }

    /**
     * Compares two objects and returns if they are the same or not based on the
     * check_method
     */
    private boolean areTheSame(Object o1, Object o2)
    {
        return (_checkMethod == ABSCHECK) ? (o1 == o2) : (o1.equals(o2));
    }

    /**
     * Returns the position of an eqClass hook in the list of eqClasses
     */
    private int getHookPosition(Object eqClass)
    {
        for (int i = 0, imax = _eqClasses.size(); i < imax; i++)
        {
            Object tmpEqClass = _eqClasses.elementAt(i);
            if (areTheSame(eqClass, tmpEqClass))
                return i;
        }
        return -1;
    }

    /**
     * Returns the hook of an eqClass or null if it does not exist
     */
    public Object getEqClassHook(Object eqClass)
    {
        if (getEqClassPosition(eqClass) == -1)
            throw new RuntimeException("Not such eqClass exists");

        int pos = getHookPosition(eqClass);
        if (pos == -1)
            return null;
        return _hooks.elementAt(pos);
    }

    /**
     * Sets the hook of a given eqClass to the one provided. Note that it
     * overwrites any previous version.
     */
    public void setEqClassHook(Object eqClass, Object hook)
    {
        if (getEqClassPosition(eqClass) == -1)
            throw new RuntimeException("Not such eqClass exists");

        int pos = getHookPosition(eqClass);
        if (pos == -1)
        {
            _hooks.add(hook);
            _eqClasses.add(eqClass);
        }
        else
        {
            _hooks.setElementAt(hook, pos);
        }
        return;
    }

    /**
     * Returns the representatives of the EqClasses
     */
    public Object[] getEqClasses()
    {
        Object[] reps = new Object[_size];
        int tmpSize = 0;
        for (int i = 0, imax = _size; i < imax; i++)
        {
            Object cand = _representatives[i];
            // check if the repr has already been collected
            boolean isNew = true;
            for (int k = 0, kmax = tmpSize; k < kmax; k++)
            {
                Object tmp = reps[k];
                if (areTheSame(tmp, cand))
                {
                    isNew = false;
                    break;
                }
                if (!isNew)
                    break;
            }
            // if we have seen the cand before, go to the next
            if (!isNew)
                break;
            // otherwise store it in the table of candidates
            reps[tmpSize] = cand;
            tmpSize++;
        }
        Object[] retVal = new Object[tmpSize];
        for (int i = 0, imax = tmpSize; i < imax; i++)
            retVal[i] = reps[i];
        return retVal;
    }

    /**
     * Removes the hook of an eqClass
     */
    public void removeEqClassHook(Object eqClass)
    {
        if (getEqClassPosition(eqClass) == -1)
            throw new RuntimeException("Not such eqClass exists");

        int pos = getHookPosition(eqClass);
        // if the eqClass has no hook, we have nothing to do
        if (pos == -1)
            return;
        _hooks.remove(pos);
        _eqClasses.remove(pos);
    }

    /**
     * Destroys a class and makes all its objects non-classable
     */
    public void destroyEqClass(Object eqClass)
    {
        if (getEqClassPosition(eqClass) == -1)
            throw new RuntimeException("Not such eqClass");

        // First remove the hook of the class you destroyed.
        removeEqClassHook(eqClass);

        // Now delete all the members
        Object[] newMembers = new Object[_size];
        Object[] newRepr = new Object[_size];
        int newSize = 0;
        for (int i = 0, imax = _size; i < imax; i++)
        {
            Object repr = _representatives[i];
            if (areTheSame(eqClass, repr))
                continue;

            newMembers[newSize] = _members[i];
            newRepr[newSize] = _representatives[i];
            newSize++;
        }
        _members = newMembers;
        _representatives = newRepr;
        _size = newSize;
    }

    /**
     * Adds a new classable member in the list of objects that belong to
     * classes.
     */
    private void add(Object member, Object representative)
    {
        // Check if we need to extend the table
        if (_size == _members.length)
        {
            int newMaxSize = _members.length + _INCREASE_STEP;
            Object[] newMembers = new Object[newMaxSize];
            Object[] newRep = new Object[newMaxSize];
            for (int i = 0, imax = _members.length; i < imax; i++)
            {
                newMembers[i] = _members[i];
                newRep[i] = _representatives[i];
            }
            _members = newMembers;
            _representatives = newRep;
        }

        // now we can simply add that new Object
        _members[_size] = member;
        _representatives[_size] = representative;
        _size++;
    }

    /**
     * Returns the position of a member in the list of members or -1 if it is
     * not there.
     */
    private int getMemberPosition(Object member)
    {
        if (member == null)
            throw new RuntimeException("Cannot find position of a null element");
        int pos = -1;
        for (int i = 0, imax = _size; i < imax; i++)
        {
            if (areTheSame(_members[i], member))
            {
                pos = i;
                break;
            }
        }
        return pos;
    }

    /**
     * Returns the position of an eqclass in the list of representatives or -1
     * if it is not there.
     */
    private int getEqClassPosition(Object eqClass)
    {
        if (eqClass == null)
            throw new RuntimeException("Cannot find position of a null class");
        int pos = -1;
        for (int i = 0, imax = _size; i < imax; i++)
        {
            if (areTheSame(_representatives[i], eqClass))
            {
                pos = i;
                break;
            }
        }
        return pos;
    }

    /**
     * Gives you the representative of the class the object belongs If the
     * object has not been classified, it returns null
     */
    public Object getEqClass(Object member)
    {
        int pos = getMemberPosition(member);
        if (pos == -1)
            return null;
        return _representatives[pos];
    }

    /**
     * Sets the provided object to a class that consists of only itself. If the
     * member is already a member of a class nothing is done.
     */
    public void classify(Object member)
    {
        int pos = getMemberPosition(member);
        if (pos != -1)
            return;
        add(member, member);
    }

    /**
     * Sets the object to be a member of the given class. Note that this is
     * different from the class merging, in the sense that the object stops
     * being a member of its previous class.
     */
    public void classify(Object member, Object newClass)
    {
        // If it is a new member simply add it with the default representative
        // which is itself
        int pos = getMemberPosition(member);
        if (pos == -1)
        {
            classify(member);
            pos = getMemberPosition(member);
        }


        // Check if the representative provided is already a classified member.
        int repPos = getMemberPosition(newClass);
        if (repPos == -1)
            throw new RuntimeException("The class representative was not found among the classified members");

        // check also that the provided class (i.e., representative) is already
        // a class
        int eqClassPos = getEqClassPosition(newClass);
        if (eqClassPos == -1)
            throw new RuntimeException("provided class/representative is not a representative");

        // detach the object from any eqClass that it may belong
        detach(member);

        // Now the member is basically a class by itself but is its class has a
        // hook, we need to remove it.
        removeEqClassHook(member);

        // Now we can simply assign him to a new class.
        _representatives[pos] = newClass;
    }

    /**
     * Detaches a member from its current class and makes it a class by itself.
     */
    public void detach(Object member)
    {
        int pos = getMemberPosition(member);
        Object currClass = getEqClass(member);
        int size = getEqClassSize(currClass);
        if (size == 0)
            throw new RuntimeException("Should not happen");
        if (size == 1)
            return; // nothing to do.
        // if the member is not a representative, then simply change his
        // representative to be himself
        if (!areTheSame(currClass, member))
        {
            _representatives[pos] = member;
            return;
        }

        // Reaching this point means that the member is a representative and we
        // need to find a replacement. And definitely there is another member in
        // the same class because the size of its class has been checked and has
        // been found to be more than 1
        int deputy = -1;
        for (int i = 0, imax = _size; i < imax; i++)
        {
            Object candidateClass = _representatives[i];
            if (!areTheSame(currClass, candidateClass))
                continue;
            // we found a candidate
            deputy = i;
            break;
        }
        // safety check
        if (deputy == -1)
            throw new RuntimeException("Should not happen");
        // go through all the objects and replace the newClass representative
        // with the new one.
        Object newRepresentative = _members[deputy];

        // Update the hook index
        int hookPos = getHookPosition(currClass);
        if (hookPos == -1)
            throw new RuntimeException("Should not happen 9822937");
        _eqClasses.setElementAt(newRepresentative, hookPos);

        // We update the representative of all the members to be the new one.
        for (int i = 0, imax = _size; i < imax; i++)
        {
            Object currRep = _representatives[i];
            boolean isOldRep = (_checkMethod == ABSCHECK) ? (currRep == currClass) : currRep.equals(currClass);
            if (!isOldRep)
                continue;
            _representatives[i] = newRepresentative;
        }

        // Finally just make sure that the member we want to make independent is
        // becoming actually independent.
        _representatives[pos] = _members[pos];
    }

    /**
     * Counts the number of elements that belong to a class.
     */
    private int getEqClassSize(Object currClass)
    {
        int size = 0;
        for (int i = 0, imax = _size; i < imax; i++)
        {
            Object tmpRep = _representatives[i];
            if (areTheSame(tmpRep, currClass))
                size++;
        }
        return size;
    }

    /**
     * Moves all the members of the first eqClass into the second
     */
    public void mergeEqClass(Object eqClass, Object intoEqClass)
    {
        int eqClassPos = getEqClassPosition(eqClass);
        if (eqClassPos == -1)
            throw new RuntimeException("eqClass provided not exists as an eqClass");

        int intoEqClassPos = getEqClassPosition(intoEqClass);
        if (intoEqClassPos == -1)
            throw new RuntimeException("intoEqClass provided not exists as an eqClass");

        // the eqClass will disappear so we need to remove the hook
        removeEqClassHook(eqClass);

        // go through the eqClass representatives and update
        for (int i = 0, imax = _size; i < imax; i++)
        {
            Object candRepr = _representatives[i];
            if (!areTheSame(candRepr, eqClass))
                continue;
            _representatives[i] = intoEqClass;
        }

    }

    /**
     * Merge the class of the first member into the class of the second argument
     * member
     */
    public void mergeMemberEqClass(Object member, Object intoMember)
    {
        Object fromEqClass = getEqClass(member);
        Object toEqClass = getEqClass(intoMember);
        mergeEqClass(fromEqClass, toEqClass);
    }

    /**
     * Returns the members of a class.
     */
    public Object[] getEqClassMembers(Object eqClass)
    {
        if (getEqClassPosition(eqClass) == -1)
            throw new RuntimeException("Not such eqClass");

        Object[] v = new Object[_size];
        int tmpSize = 0;
        for (int i = 0, imax = _size; i < imax; i++)
        {
            Object repr = _representatives[i];
            if (areTheSame(eqClass, repr))
            {
                v[tmpSize] = _members[i];
                tmpSize++;
            }
        }
        Object[] retVal = new Object[tmpSize];
        for (int i = 0, imax = retVal.length; i < imax; i++)
            retVal[i] = v[i];
        return retVal;
    }


}
