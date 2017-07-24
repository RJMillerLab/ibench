/**
 * 
 */
package tresc.benchmark.data.ToXgeneTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import toxgene.util.cdata.xmark.CSVDataType;
import tresc.benchmark.Configuration;

/**
 * @author lord_pretzel
 *
 */
public class DeviatedCSVDataType extends GenericDeviatedDataType {

	private CSVDataType csv;
	private Double[] newProbs;
	
	/**
	 * @param dt
	 */
	public DeviatedCSVDataType(Random r, CSVDataType dt, int deviation) {
		this.dt = dt;
		this.deviation = deviation;
		csv = dt;
		
		newProbs = Arrays.copyOf(csv.getProbabilities(), csv.getProbabilities().length);
		adjustProbablities(r);
	}

	/* (non-Javadoc)
	 * @see toxgene.interfaces.ToXgeneCdataGenerator#setRandomSeed(int)
	 */
	@Override
	public void setRandomSeed(int seed) {
		getDt().setRandomSeed(seed);
	}
	
	/* (non-Javadoc)
	 * @see tresc.benchmark.data.ToXgeneTypes.DeviatedToxGeneType#getCdata(int)
	 */
	@Override
	public String getCdata(int length) {
		List<String> vals = csv.getUniqueStrings();
		double r = csv.getRandy().nextDouble();
		
		for (int k = 0; k < newProbs.length; k++) {
			if (r <= newProbs[k])
				return vals.get(k);
		}
		
		return null;
	}

}
