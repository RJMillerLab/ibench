/**
 * 
 */
package tresc.benchmark.dataGen.toxgenewrap;

import java.util.Vector;

import org.apache.log4j.Logger;

import toxgene.interfaces.ToXgeneReporter;

/**
 * @author lord_pretzel
 *
 */
public class Log4jToxGeneReporter implements ToXgeneReporter {
	
	private Logger log;
	private Vector<String> warnings;
	
	public Log4jToxGeneReporter (Logger log) {
		this.log = log;
	}
	
	
	/* (non-Javadoc)
	 * @see toxgene.interfaces.ToXgeneReporter#explain(java.lang.String)
	 */
	@Override
	public void explain(String arg0) {
		log.debug(arg0);
	}

	/* (non-Javadoc)
	 * @see toxgene.interfaces.ToXgeneReporter#progress(java.lang.String)
	 */
	@Override
	public void progress(String arg0) {
		log.info(arg0);
	}

	/* (non-Javadoc)
	 * @see toxgene.interfaces.ToXgeneReporter#warning(java.lang.String)
	 */
	@Override
	public void warning(String arg0) {
		log.warn(arg0);
	}

	public int warnings(){
		return warnings.size();
	}

	public void printAllWarnings(){
		for (int i=0; i<warnings.size(); i++){
			log.warn(warnings.get(i));
		}
		warnings.clear();
	}
}
