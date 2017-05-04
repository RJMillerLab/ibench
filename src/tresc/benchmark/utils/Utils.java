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
package tresc.benchmark.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

import org.vagabond.util.CollectionUtils;

public class Utils
{

	public static int getRandomUniformNumber (Random generator, int max) {
		return generator.nextInt(max);
	}
	
	public static int getRandomUniformNumber (Random generator, int center, int deviation) {
		if (deviation == 0)
			return center;
		
		return center + (generator.nextInt(2 * deviation)) - deviation;
	}
	
	public static int getRandomNumberAroundSomethingCapped(Random generator, int center, int deviation, int min, int max) {
		int result = getRandomNumberAroundSomething(generator, center, deviation);
		return (result < min ? min : (result > max ? max : result));
	}
	
    /*
     * Returns a random number (positive one) around some specific number given
     * as argument Note that the Random generator is provided so that this
     * function is used in many cases and by independent parts of the program
     * 
     */

    public static int getRandomNumberAroundSomething(Random generator, int center, int deviation)
    {
        if (center == 0)
            return 0;
        if (deviation == 0)
            return center;

        double val = generator.nextGaussian();

        // first we scale to account for the standard deviation we need
        // here we just use half of the average
        // val *= center * 0.5;
        val *= deviation;

        // Shift it to the center we want
        val += center;
        // make any possible negative value, positive
        val = (val < 0) ? -val : val;
        
        // and if 0 make it 1
        //val = (val == 0) ? 1 : val;

        // if less than 1 make it 1
        val = (val < 1) ? 1 : val;
        
        return (int) val;
    }
    
    public static Vector<String> getRandomWithoutReplacementSequence(Random generator, 
    		int desiredSize, String[] allElems) {
    		return getRandomWithoutReplacementSequence(generator, desiredSize,
    				CollectionUtils.makeVec(allElems));
    }
    
    // BORIS + PRG - Method getRandomWithoutReplacementSequence() - Sep 20 & 21, 2012
 	public static Vector<String> getRandomWithoutReplacementSequence(Random generator, int desiredSize, Vector<String> allElems) {

 		Vector<String> randomElems = new Vector<String> ();
 		HashSet<String> elemHashSet = new HashSet<String> ();
 		
 		// Build a hashSet varSet out of allElems
 		for(String anElem: allElems) {
 			elemHashSet.add(anElem);
 		}
 		
 		// Do we have enough elements to obtain a random sequence of desiredSize?
 		// If not, just use all elements for the sake of completion
 		// Ditto if we were asked to choose 0 elements
 		if ( (desiredSize <= 0) || (desiredSize >= elemHashSet.size())) {
 			randomElems = new Vector<String> (elemHashSet);
 			Collections.sort(randomElems);
 			return randomElems;
 		}
 		
 		// Randomly select variables until we have enough. As we go along, remove chosen variables to guarantee convergence.
 		// By design, we select random numbers following a uniform distribution
 		for(int i = 0; i < desiredSize; i++) {
 			
 			int pos = Utils.getRandomUniformNumber(generator, allElems.size());
 			String elem = allElems.get(pos);
 			randomElems.add(elem);
 			
 			// Ensure "Random Without Replacement" Strategy; thus remove all occurrences of elem
 			elemHashSet.remove(elem);
 			allElems.removeAll(Collections.singleton(elem));
 		}
 		
 		// The following sorting only makes sense when this method is invoked with strings representing variables, instead of attribute names
 		Collections.sort(randomElems);
 		return randomElems;		
 	}
    
 	/**
	 * Converts a string vector to an array of strings
	 * 
	 * @param vStr
	 *            A string vector
	 * @return An array of strings
	 * 
	 * @author mdangelo
	 */
	public static String[] convertVectorToStringArray(Vector<String> vStr) 
	{
		String[] ret = new String[vStr.size()];

		int j = 0;
		for (String str : vStr)
			ret[j++] = str;

		return ret;
	}
	
	
}
