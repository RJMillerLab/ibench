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
package tresc.benchmark.dataGen.toxgenewrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.util.LoggerUtil;

import toxgene.core.Engine;
import toxgene.core.ToXgeneErrorException;
import toxgene.interfaces.ToXgeneCdataGenerator;
import toxgene.interfaces.ToXgeneDocumentCollection;
import toxgene.interfaces.ToXgeneReporter;
import toxgene.interfaces.ToXgeneSession;
import toxgene.util.ToXgeneReporterImpl;
import toxgene.util.cdata.xmark.CSVDataType;
import toxgene.util.cdata.xmark.CSVHandler;
import vtools.dataModel.types.CustomDataType;
import vtools.dataModel.types.DataType;
import vtools.dataModel.types.DataTypeHandler;

public class ToXGeneWrapper {

	static Logger log = Logger.getLogger(ToXGeneWrapper.class);

	/* This is the ToXgene Engine */
	private Engine tgEngine;

	/*
	 * The ToXgeneReporter handles all messages sent by the engine during
	 * parsing and generation of documents. These messages include warnings,
	 * notification of errors, or simply progress report messages.
	 */
	private Log4jToxGeneReporter tgReporter;
//	private String toxGenPath;

	public ToXGeneWrapper(String toxGenePath) {
		setToXGenePath(toxGenePath);
	}
	
	public void setToXGenePath (String toxGenePath) {
//		this.toxGenPath = toxGenePath;
		System.setProperty("ToXgene_home", toxGenePath);
	}

	public void generate(String template, String outputPath) throws Exception {
		generate (new File(template), outputPath);
	}
	
	
	public void registerDT(DataType dt) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		// register with toxgene engine
		if (dt instanceof CustomDataType) {
			CSVDataType cdt = CSVHandler.getInst().createDT(new File(dt.getClassPath()), dt.getName());
			tgEngine.registerCDataGenerator(cdt.getAttributeName(), cdt);	
		}
		else {
			String jarFile = dt.getJarPath();
			String className = dt.getClassPath();
			Class<?> dtClass = null;
			ToXgeneCdataGenerator gen = null;
			
			// have to load from Jar file?
			if (jarFile != null) {
				URL jarURL = new File(jarFile).toURI().toURL();
				URLClassLoader loader = new URLClassLoader (new URL[] {jarURL}, this.getClass().getClassLoader());
				dtClass = Class.forName (className, true, loader);
			}
			else {
				dtClass = Class.forName(className);
			}
			
			gen = (ToXgeneCdataGenerator) dtClass.newInstance();
			
			tgEngine.registerCDataGenerator(dt.getName(), gen);
		}
	}
	
	public String generate(File template, String outputPath) throws Exception {
		String name = null;
		
		try {
			tgEngine = new Engine();
			tgEngine.setUseJarLoading(false);
//			tgEngine.setJarPath(toxGenePath);
//			boolean verbose = true; /*
//									 * useful for debugging templates
//									 */
//			boolean showWarnings = true;
			tgReporter = new Log4jToxGeneReporter(log);

			/*
			 * The ToXgeneSession specifies all parameters the engine needs for
			 * generating the documents.
			 */
			ToXgeneSession session = new ToXgeneSession();
			session.reporter = tgReporter;
			session.initialSeed = 123456;
			session.addNewLines = true;
			session.inputPath = "./";
			session.usePOM = false;
			session.pomBufferPath = ".";
			session.pomMemFracBuffer = (float) 0.5;
			session.pomBufferSize = 80000 * 1024;
			/* Initialize the engine */
			tgEngine.startSession(session);
			
			// register new CSVDataTypes
			List<CustomDataType> csvDTs = DataTypeHandler.getInst().getAllCustomTypes();
			for(CustomDataType dt: csvDTs) {
				registerDT(dt);
			}
			
			// also register standard UDTs
			List<DataType> udts = DataTypeHandler.getInst().getAllNonCSVDTs();
			for(DataType dt: udts) {
				registerDT(dt);
			}
			
			/*
			 * The progress() method sends a progress report message to the
			 * message handler.
			 */
			tgReporter.progress("Parsing template: ");
			tgEngine.parseTemplate(new FileInputStream(template));
			tgReporter.progress("Done !\n");

			/*
			 * The generateLists() method tells the engine to generate all
			 * temporary data declared in tox-list elements in the template.
			 * Calling this method is optional, ToXgene will materialize all
			 * temporary data if needed even if generateLists is not invoked.
			 */
			tgEngine.generateLists();
			name = generateCollections(outputPath);
		}
		catch (ToXgeneErrorException e1) {
			LoggerUtil.logException(e1, log);
			error(e1.getMessage());
			throw e1;
		}
		catch (FileNotFoundException e) {
			tgEngine.endSession();
			error("cannot open template file \"" + template + "\"");
			throw e;
		}
		catch (Exception e) {
			LoggerUtil.logException(e, log);
			throw e;
		}

		tgEngine.endSession();

//		int nWarnings = tgReporter.warnings();
//		if (nWarnings > 0) {
//			log.warn("There were " + nWarnings + " warning(s).");
//			tgReporter.printAllWarnings();
//		}
		
		return name;
	}

	//TODO don't need document collections
	/**
	 * Scans the collections declared in the template and generates the XML
	 * documents they specify on files.
	 */
	private String generateCollections(String outputPath) {
		Vector<?> collections = tgEngine.getToXgeneDocumentCollections();
		String lastName = null;
		
		if (collections.size() == 0) {
			tgReporter.warning("no document genes found");
			return null;
		}
		/* Iterate over all collections in the template */
		for (int i = 0; i < collections.size(); i++) {
			ToXgeneDocumentCollection tgColl =
					(ToXgeneDocumentCollection) collections.get(i);
			
			/*
			 * Test whether this collection has more than one document
			 */
			if (tgColl.getSize() > 1) {
				int start = tgColl.getStartingNumber();
				int documents = tgColl.getSize();
				DecimalFormat nf = new DecimalFormat("0;0");

				tgReporter.progress("Generating collection: "
						+ tgColl.getName());

				File current;
//				int count = 0, sum = 0;

				for (int j = start; j < start + documents; j++) {
					lastName = tgColl.getName() +  nf.format(j) + ".xml";
					current = new File(outputPath, lastName);
					
					try {
						/*
						 * Create a file for storing this document; note that
						 * any PrintStrem object would work here as far as
						 * ToXgene is concerned.
						 */
						PrintStream outStream =
								new PrintStream(new FileOutputStream(current));
						/*
						 * The materialize() method "prints" the document into
						 * the given PrintStream object.
						 */
						tgEngine.materialize(tgColl, outStream);
					}
					catch (Exception e) {
						/*
						 * The endSession() method tells ToXgene's engine to
						 * clean up, e.g., temporary files it may have created.
						 */
						LoggerUtil.logException(e, log);
						tgEngine.endSession();
						error("Couldn't create " + current);
					}

				}
				tgReporter.progress(" ...Done!\n");
			}
			else {
				/*
				 * In this case, the collection has a single document
				 */
				tgReporter.progress("Generating document \"" + tgColl.getName()
						+ ".xml\"");

				lastName = tgColl.getName() + ".xml";
				File file = new File(outputPath, lastName);
				try {
					PrintStream outStream =
							new PrintStream(new FileOutputStream(file));
					tgEngine.materialize(tgColl, outStream);

				}
				catch (Exception e) {
					tgEngine.endSession();
					throw new ToXgeneErrorException("Couldn't create " + file);
				}
				tgReporter.progress(" ...Done!\n");
			}
		}

		return lastName;
	}

	/**
	 * Prints an error message and aborts.
	 */
	public void error(String msg) {
		tgReporter.printAllWarnings();
		log.fatal("\n***** ERROR: " + msg);
	}

}
