package tresc.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.XmlOptions;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.PropertyWrapper;

import smark.support.MappingScenario;
import tresc.benchmark.Constants.OutputOption;
import tresc.benchmark.dataGen.ToXDataGenerator;
import tresc.benchmark.dataGen.toxgenewrap.ToXGeneWrapper;
import vtools.dataModel.expression.Expression;
import vtools.dataModel.expression.HTMLPresenter;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.schema.Schema;
import vtools.xml.XSDWriter;
import vtools.xml.XMLWriter;

public class STBenchmark {
	static Logger log = Logger.getLogger(STBenchmark.class);

	private Configuration _configuration;

	public STBenchmark() {
		_configuration = new Configuration();
	}
	
	public void parseArgs (String[] args) throws CmdLineException {
		CmdLineParser parser;
		
		log.debug("Command line args are: <" + LoggerUtil.arrayToString(args) + ">");
		parser = new CmdLineParser(_configuration);
		try {
			parser.parseArgument(args);
		}
		catch (CmdLineException e) {
			LoggerUtil.logException(e, log);
			parser.printUsage(System.err);
			throw e;
		}
	}

	public void parseConfigFile(String scenarioListFile) throws Exception {
		if (!new File(scenarioListFile).exists())
			throw new Exception(scenarioListFile + " does not exit");

		try {
			RandomAccessFile raf = new RandomAccessFile(scenarioListFile, "r");

			String line = "";
			while (line != null) {
				line = raf.readLine();

				if (line == null)
					break;
				if (line.startsWith("#"))
					continue;

				String trimmedLine = line.trim();
				if (trimmedLine.length() == 0)
					continue;

				run(trimmedLine);
			}

			raf.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("resource/log4jproperties.txt");

		STBenchmark benchmark = new STBenchmark();
		benchmark.parseArgs(args);
		benchmark.run(args);
	}

	private void printResults(MappingScenario scenario, String S, String T,
			String M, String S1) throws Exception {
		// System.out.println("Printing results !");
		File instDir = new File (Configuration.instancePathPrefix);
		if (!instDir.exists())
			instDir.mkdirs();
		File schemDir = new File(Configuration.schemaPathPrefix);
		if (!schemDir.exists())
			schemDir.mkdirs();
		
		XSDWriter schemaPrinter = new XSDWriter();
		XMLWriter schemaWriter = new XMLWriter();

		// print scenario on the screen
		StringBuffer buf = new StringBuffer();
		// scenario.prettyPrint(buf, 0);
		// System.out.println(buf);

		// print scenario on file
		if (_configuration.getOutputOption(OutputOption.XMLSchemas)) {
			StringBuffer sourceSchemaBuffer = new StringBuffer();
			schemaPrinter.print(sourceSchemaBuffer, scenario.getSource(), 0);
			
			StringBuffer targetSchemaBuffer = new StringBuffer();
			schemaPrinter.print(targetSchemaBuffer, scenario.getTarget(), 0);
			
			try {
				BufferedWriter bufWriter =
						new BufferedWriter(new FileWriter(new File(
								Configuration.schemaPathPrefix, S)));
				bufWriter.write(sourceSchemaBuffer.toString());
				bufWriter.close();

				bufWriter =
						new BufferedWriter(new FileWriter(new File(
								Configuration.schemaPathPrefix, T)));
				bufWriter.write(targetSchemaBuffer.toString());
				bufWriter.close();
			}
			catch (Exception e) {
				LoggerUtil.logException(e, log);
				throw e;
			}
		}
		
		if (_configuration.getOutputOption(OutputOption.TrampXML)) {
//			StringBuffer mappingScenarioXMLBuffer = new StringBuffer();
			
//			schemaWriter.print(mappingScenarioXMLBuffer, scenario, 0,
//					instDir.getAbsolutePath(), _configuration);

			try {
				File trampFile = new File(Configuration.schemaPathPrefix, S1);
//				if (trampFile.exists()) {
//					trampFile.delete();
//					trampFile.createNewFile();
//				}
				BufferedWriter bufWriter =
						new BufferedWriter(new FileWriter(trampFile));
				XmlOptions options = new XmlOptions();
				options.setSavePrettyPrint();
				scenario.getDoc().getDocument().save(bufWriter, options);
//				bufWriter.write(mappingScenarioXMLBuffer.toString());
				bufWriter.close();
			}
			catch (IOException e) {
				LoggerUtil.logException(e, log);
				throw e;
			}
		}

		if (_configuration.getOutputOption(OutputOption.HTMLSchemas)) {
			// print the src schema in an HTML file
			buf = new StringBuffer();
			Object[] o = new Object[10];
			o[0] = buf;
			o[1] = new Integer(0);
			Schema s = scenario.getSource();
			s.accept(vtools.dataModel.schema.HTMLPresenter.HTMLPresenter, o);
			// vtools.dataModel.schema.HTMLPresenter.HTMLPresenter.printInHtmlFile(buf,
			// "web/" + S + ".html");
			vtools.dataModel.schema.HTMLPresenter.HTMLPresenter
					.printInHtmlFile(buf, Configuration.schemaPathPrefix, S
							+ ".html");

			// print the trg schema in an HTML file
			buf = new StringBuffer();
			o = new Object[10];
			o[0] = buf;
			o[1] = new Integer(0);
			s = scenario.getTarget();
			s.accept(vtools.dataModel.schema.HTMLPresenter.HTMLPresenter, o);
			// vtools.dataModel.schema.HTMLPresenter.HTMLPresenter.printInHtmlFile(buf,
			// "web/" + T + ".html");
			vtools.dataModel.schema.HTMLPresenter.HTMLPresenter
					.printInHtmlFile(buf, Configuration.schemaPathPrefix, T
							+ ".html");
		}

		// and finally the transformation
		if (_configuration.getOutputOption(OutputOption.HTMLMapping)) {
			buf = new StringBuffer();
			Object[] o = new Object[10];
			o[0] = buf;
			o[1] = new Integer(0);
			SPJQuery spjq = scenario.getTransformation();
			SelectClauseList sel = spjq.getSelect();
			for (int i = 0, imax = sel.size(); i < imax; i++) {
				String name = sel.getTermName(i);
				Expression exp = sel.getTerm(i);
				buf.append("<p/><hr>insert into <b>" + name + "</b><br/>");
				exp.accept(vtools.dataModel.expression.HTMLPresenter.HTMLPresenter,
						o);
			}

			HTMLPresenter.HTMLPresenter.printInHtmlFile(buf,
					Configuration.schemaPathPrefix, M + ".html");
		}
	}

	public void run(String[] args) throws Exception {
		if (_configuration.configurationFile != null) {
			parseConfigFile(_configuration.configurationFile);
		}
		else if (_configuration.propertyFileName != null) {
			PropertyWrapper props = new PropertyWrapper(_configuration.propertyFileName);
			log.debug(props.toString());
			_configuration.readFromProperties(props);
			parseArgs(args);
			log.debug(_configuration.toString());
			runConfig();
		}
		else {
			runConfig();
		}
	}
	
	public void runConfig (Configuration conf) throws Exception {
		this._configuration = conf;
		runConfig();
	}
	
	public void runConfig() throws Exception {
		Modules.scenarioGenerator = new Generator(_configuration);
		MappingScenario scenario =
				Modules.scenarioGenerator.generateScenario(_configuration);
		// printResults(scenario, "S", "T", "M");
		log.info("---- GENERATED SCENARIO -----\n\n\n" + scenario.toString());

		printResults(scenario, _configuration.getSourceSchemaFile(),
				_configuration.getTargetSchemaFile(),
				_configuration.getMappingFileName(),
				_configuration.getSchemaFile());

		if (_configuration.getOutputOption(OutputOption.Data))
			Modules.scenarioGenerator.generateSourceData(scenario);
		if (_configuration.getOutputOption(OutputOption.ErrorsAndExplanations))
			Modules.explGen.genearteExpls(scenario, _configuration);
	}

	public void run(String configLine) throws Exception {
		_configuration = new Configuration(configLine);
		log.debug("Configuration is: " + _configuration.toString());
		runConfig();
	}

}
