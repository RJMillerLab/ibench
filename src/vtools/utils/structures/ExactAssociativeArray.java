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

import java.util.Vector;

/**
 * It is an Associative array with the only difference is that it bases its
 * comparisons on exact equality (i.e., ==) instead of the equals(Object)
 * function
 */
public class ExactAssociativeArray extends AssociativeArray
{
    protected boolean vequals(Object o1, Object o2)
    {
        return o1 == o2;
    }


    protected int vindexOf(Vector<Object> v, Object key, int k)
    {
        for (int i = k, imax = v.size(); i < imax; i++)
        {
            if (v.elementAt(i) == key)
                return i;
        }
        return -1;
    }

    protected int vindexOf(Vector<Object> v, Object key)
    {
        return vindexOf(v, key, 0);
    }

}
