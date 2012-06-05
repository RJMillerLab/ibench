package tresc.benchmark.utils;

import java.util.Random;

public class Utils
{

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
}
