package vtools.utils.structures;

/**
 * An associative array that does not allow duplicates in its keys. 
 */
public class SetAssociativeArray extends AssociativeArray
{
    public SetAssociativeArray()
    {
        super();
        super._duplicatesAllowed = false;
    }

}
