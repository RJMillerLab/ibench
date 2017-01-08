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
package vtools.visitor;

import java.lang.reflect.Method;

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
                // if (log.isDebugEnabled()) {log.debug("Looking for " + this.getClass().getName()
                // + ".visit(" + currClass.getName()
                // + "," + objArrayClass.getName() + ")");};
                m = this.getClass().getMethod("visit", new Class[]
                {
                        currClass, objArrayClass
                });
            }
            catch (NoSuchMethodException e)
            {
                // if (log.isDebugEnabled()) {log.debug("Nothing for " + currClass.getName());};
                currClass = currClass.getSuperclass();
            }
        }

//        if (m == null)
//            throw new RuntimeException("This is impossible to happen "
//                + "given the existence of the visit for the Object");

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
