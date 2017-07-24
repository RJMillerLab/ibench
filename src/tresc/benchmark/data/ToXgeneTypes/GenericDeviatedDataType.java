/**
 * 
 */
package tresc.benchmark.data.ToXgeneTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import toxgene.interfaces.ToXgeneCdataGenerator;
import tresc.benchmark.Configuration;

/**
 * @author lord_pretzel
 *
 */
public class GenericDeviatedDataType extends DeviatedToxGeneType {

	protected static int SAMPLE_SIZE = 500;
	
	private String[] valSample;
	private Double[] newProbs;
	private Random rand;
	
	/**
	 * @param dt
	 * @param deviation
	 */
	public GenericDeviatedDataType(Random r, ToXgeneCdataGenerator dt, int deviation) {
		super(dt, deviation);
		rand = new Random();
		valSample = new String[SAMPLE_SIZE];
		newProbs = new Double[SAMPLE_SIZE];
		sampleValues();
		adjustProbablities(r);
	}
	
	public GenericDeviatedDataType () {
		super();
	}
	
	/* (non-Javadoc)
	 * @see toxgene.interfaces.ToXgeneCdataGenerator#setRandomSeed(int)
	 */
	@Override
	public void setRandomSeed(int seed) {
		rand.setSeed(seed);
	}

	/**
	 * 
	 */
	private void sampleValues() {
		dt.setRandomSeed(0);
		for (int i = 0; i < SAMPLE_SIZE; i++) {
			valSample[i] = dt.getCdata(1);
			newProbs[i] = 1.0 / SAMPLE_SIZE;
		}
	}



	/* (non-Javadoc)
	 * @see tresc.benchmark.data.ToXgeneTypes.DeviatedToxGeneType#getCdata(int)
	 */
	@Override
	public String getCdata(int length) {
		double r = rand.nextDouble();
		
		for (int k = 0; k < newProbs.length; k++) {
			if (r <= newProbs[k])
				return valSample[k];
		}
		
		return null;
	}

	/**
	 * change probabilities based on deviation
	 */
	protected void adjustProbablities(Random r) {
		List<Integer> dec = new ArrayList<Integer> ();
		List<Integer> inc = new ArrayList<Integer> ();
		
		for(int i = 0; i < newProbs.length; i++) {
			if (r.nextBoolean()) {
				dec.add(i);
			}
			else {
				inc.add(i);
			}
		}
		
		// make sure both are not empty
		if (inc.isEmpty()) {
			int pos = r.nextInt(dec.size());
			int val = dec.get(pos);
			dec.remove(pos);
			inc.add(val);
		}
		
		if (dec.isEmpty()) {
			int pos = r.nextInt(inc.size());
			int val = inc.get(pos);
			inc.remove(pos);
			dec.add(val);
		}
		
		int decCount = deviation / 2;
		int done = 0;
		
		for(int i = 0; i < decCount; i++) {
			int pos = r.nextInt(dec.size());
			int el = dec.get(pos);
			if (newProbs[el] > 0.0) {
				newProbs[i] -= 0.01; 
				done--;
			}
		}
		
		for(int i = 0; i < decCount; i++) {
			int pos = r.nextInt(inc.size());
			int el = inc.get(pos);
			if (newProbs[el] < 1.0) {
				newProbs[i] += 0.01;
				done++;
			}
		}
		
		// were to able change as much as requested
		if (done != 0) {
			int pos = 0;
			while(done < 0) {
				double cur = newProbs[pos];
				double need = -0.01 * done;
				int pot = (int) Math.floor(Math.min(1.0 - cur, need) / 0.01);
				newProbs[pos] += 0.01 * pot;
				done += pot;
				pos++;
			}
			while(done > 0) {
				double cur = newProbs[pos];
				double need = 0.01 * done;
				int pot = (int) Math.floor(Math.min(cur - need, cur) / 0.01);
				newProbs[pos] -= 0.01 * pot;
				done -= pot;
				pos++;				
			}
		}
		
	}

	
}
