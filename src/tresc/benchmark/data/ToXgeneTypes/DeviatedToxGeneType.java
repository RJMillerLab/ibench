/**
 * 
 */
package tresc.benchmark.data.ToXgeneTypes;

import toxgene.interfaces.ToXgeneCdataGenerator;

/**
 * @author lord_pretzel
 *
 */
public abstract class DeviatedToxGeneType implements ToXgeneCdataGenerator {

	
	protected ToXgeneCdataGenerator dt;
	protected int deviation;
	
	public DeviatedToxGeneType (ToXgeneCdataGenerator dt, int deviation) {
		this.setDt(dt);
		this.setDeviation(deviation);
	}
	
	/**
	 * 
	 */
	public DeviatedToxGeneType() {
	}

	/* (non-Javadoc)
	 * @see toxgene.interfaces.ToXgeneCdataGenerator#setRandomSeed(int)
	 */
	@Override
	public void setRandomSeed(int seed) {
		getDt().setRandomSeed(seed);
	}

	/* (non-Javadoc)
	 * @see toxgene.interfaces.ToXgeneCdataGenerator#getCdata(int)
	 */
	@Override
	public abstract String getCdata(int length);

	public ToXgeneCdataGenerator getDt() {
		return dt;
	}

	public void setDt(ToXgeneCdataGenerator dt) {
		this.dt = dt;
	}

	public int getDeviation() {
		return deviation;
	}

	public void setDeviation(int deviation) {
		this.deviation = deviation;
	}
	

}
