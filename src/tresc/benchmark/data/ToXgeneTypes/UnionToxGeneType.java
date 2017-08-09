/**
 * 
 */
package tresc.benchmark.data.ToXgeneTypes;

import java.util.List;
import java.util.Random;

import toxgene.interfaces.ToXgeneCdataGenerator;

/**
 * @author lord_pretzel
 *
 */
public class UnionToxGeneType implements ToXgeneCdataGenerator {

	private ToXgeneCdataGenerator[] types;
	private Random rand;
	private int numDts;

	public UnionToxGeneType (List<ToXgeneCdataGenerator> l) {
		types = new ToXgeneCdataGenerator[l.size()];
		
		for(int i = 0; i < types.length; i++) {
			types[i] = l.get(i);
		}
		init();
	}
	
	public UnionToxGeneType (ToXgeneCdataGenerator[] types) {
		this.types = types;
		init();
	}

	private void init() {
		numDts = types.length;
		rand = new Random(0);
	}

	/* (non-Javadoc)
	 * @see toxgene.interfaces.ToXgeneCdataGenerator#setRandomSeed(int)
	 */
	@Override
	public void setRandomSeed(int seed) {
		rand.setSeed(seed);
		for(int i = 0; i < types.length; i++) {
			types[i].setRandomSeed(seed);
		}
	}

	/* (non-Javadoc)
	 * @see toxgene.interfaces.ToXgeneCdataGenerator#getCdata(int)
	 */
	@Override
	public String getCdata(int length) {
		int pos;
		
		pos = rand.nextInt(numDts);
		return types[pos].getCdata(length);
	}

}
