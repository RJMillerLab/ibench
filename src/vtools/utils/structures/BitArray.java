package vtools.utils.structures;

public class BitArray
{

    protected int[] _spec;

    public BitArray(int size)
    {
        _spec = new int[size];
        for (int i = 0, imax = size; i < imax; i++)
            _spec[i] = 0;
    }

    public BitArray(int[] val)
    {
        int size = val.length;
        _spec = new int[size];
        for (int i = 0, imax = size; i < imax; i++)
            _spec[i] = val[i];
    }
    
    public int size()
    {
        return _spec.length;
    }

    public int[] toArray()
    {
        int[] ret = new int[_spec.length];
        for (int i = 0, imax = _spec.length; i < imax; i++)
            ret[i] = _spec[i];
        return ret;
    }

    public int toInt()
    {
        int value = 0;
        int power = 1;
        for (int i=0, imax=_spec.length; i< imax; i++)
        {
            value += _spec[i] * power;
            power *= 2;
        }
        return value;
    }
    public void set(int[] val)
    {
        if (val.length != _spec.length)
            throw new RuntimeException("Different size in the value");
        for (int i = 0, imax = _spec.length; i < imax; i++)
            _spec[i] = val[i];
    }

    /*
     * Sets all the bits to 1
     */
    public void setMax()
    {
        for (int i = 0, imax = _spec.length; i < imax; i++)
            _spec[i] = 1;
    }

    /*
     * Sets all the bits to 0
     */
    public void setZero()
    {
        for (int i = 0, imax = _spec.length; i < imax; i++)
            _spec[i] = 0;
    }

    public void increment()
    {
        int[] mask = new int[_spec.length];
        for (int i = 0, imax = mask.length; i < imax; i++)
            mask[i] = 0;
        increment(mask);
    }

    public void increment(int[] mask)
    {
        int kratoumeno = 0;
        for (int i = 0, imax = _spec.length; i < imax; i++)
        {
            if (mask[i] == 1)
                continue;
            int newVal = _spec[i] + 1 + kratoumeno;
            kratoumeno = (newVal > 1) ? 1 : 0;
            _spec[i] = (newVal == 2) ? 0 : 1;
            if (kratoumeno == 0)
                break;
        }
    }

    public void setBit(int pos, int val)
    {
        if ((val != 0) && (val != 1))
            throw new RuntimeException("bits can be 0 or 1 only");
        _spec[pos] = val;
    }

    public void shiftRight(boolean cyclic)
    {
        int lastBit = _spec[_spec.length - 1];
        for (int i = 0, imax = _spec.length - 1; i < imax; i++)
            _spec[i + 1] = _spec[i];
        if (cyclic)
            _spec[0] = lastBit;
        else _spec[0] = 0;
    }
    
    public String toString()
    {
        String str = new String();
        for (int i=_spec.length - 1; i >= 0; i--)
            str += _spec[i];
        return str;
    }
}
