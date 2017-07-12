/**
 * 
 */
package tresc.benchmark;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * @author lord_pretzel
 *
 */
public class Log4jLevelOptionHandler extends OptionHandler<Level> {

	static Logger log = Logger.getLogger(Log4jLevelOptionHandler.class);
			
	
	/**
	 * @param parser
	 * @param option
	 * @param setter
	 */
	public Log4jLevelOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Level> setter) {
		super(parser, option, setter);
	}

	/* (non-Javadoc)
	 * @see org.kohsuke.args4j.spi.OptionHandler#parseArguments(org.kohsuke.args4j.spi.Parameters)
	 */
	@Override
	public int parseArguments(Parameters params) throws CmdLineException {
		if (params.size() != 1)
			throw new CmdLineException ("an option of type log level should have one parameter [trace,debug,info,warn,error,fatal,off]");
		Level result = Level.toLevel(params.getParameter(0).toLowerCase());
		setter.addValue(result);
		log.debug("set log level " + result);
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.kohsuke.args4j.spi.OptionHandler#getDefaultMetaVariable()
	 */
	@Override
	public String getDefaultMetaVariable() {
		// TODO Auto-generated method stub
		return null;
	}

}
