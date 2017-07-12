/**
 * 
 */
package ibench.benchmark.dataGen.datatypes;

import toxgene.interfaces.ToXgeneCdataGenerator;

/**
 * @author lord_pretzel
 *
 */
public class SpecifiedMultDataType implements ToXgeneCdataGenerator {

	int seed;
	
	/* (non-Javadoc)
	 * @see toxgene.interfaces.ToXgeneCdataGenerator#setRandomSeed(int)
	 */
	@Override
	public void setRandomSeed(int seed) {
		this.seed = seed;
	}

	/* (non-Javadoc)
	 * @see toxgene.interfaces.ToXgeneCdataGenerator#getCdata(int)
	 */
	@Override
	public String getCdata(int length) {
		return null;
	}

}
