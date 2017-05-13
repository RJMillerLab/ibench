/*
 *
 * Copyright 2016 Big Data Curation Lab, University of Toronto,
 * 		   	  	  	   				 Patricia Arocena,
 *   								 Boris Glavic,
 *  								 Renee J. Miller
 *
 * This software also contains code derived from STBenchmark as described in
 * with the permission of the authors:
 *
 * Bogdan Alexe, Wang-Chiew Tan, Yannis Velegrakis
 *
 * This code was originally described in:
 *
 * STBenchmark: Towards a Benchmark for Mapping Systems
 * Alexe, Bogdan and Tan, Wang-Chiew and Velegrakis, Yannis
 * PVLDB: Proceedings of the VLDB Endowment archive
 * 2008, vol. 1, no. 1, pp. 230-244
 *
 * The copyright of the ToxGene (included as a jar file: toxgene.jar) belongs to
 * Denilson Barbosa. The iBench distribution contains this jar file with the
 * permission of the author of ToxGene
 * (http://www.cs.toronto.edu/tox/toxgene/index.html)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
