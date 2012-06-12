package tresc.benchmark.test.toxgene;

/*
 * Sample.java implements a sample front-end to the ToxGene engine
 * @author Denilson Barbosa
 * @version 1.0
 * @date February 2005
 */

import java.util.Vector;
import java.text.DecimalFormat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.PrintStream;

import toxgene.core.Engine;
import toxgene.core.ToXgeneErrorException;
import toxgene.interfaces.ToXgeneDocumentCollection;
import toxgene.interfaces.ToXgeneSession;
import toxgene.util.ToXgeneReporterImpl;

public class ToxGeneTest {

	/* This is the ToXgene Engine */
	private static Engine tgEngine;

	/*
	 * The ToXgeneReporter handles all messages sent by the engine during
	 * parsing and generation of documents. These messages include warnings,
	 * notification of errors, or simply progress report messages.
	 */
	private static ToXgeneReporterImpl tgReporter;

	public static void main(String argv[]) {
		if (argv.length == 0) {
			System.out.println("Usage: Sample <template file>\n");
			System.exit(0);
		}
		System.setProperty("ToXgene_home", "lib");
		String template = argv[0];
		try {
			tgEngine = new Engine();
			boolean verbose = true; /*
									 * useful for debugging templates
									 */
			boolean showWarnings = true;
			tgReporter = new ToXgeneReporterImpl(verbose, showWarnings);

			/*
       */
			if (System.getProperty("ToXgene_home") == null) {
				System.out.println("\n***** WARNING: "
						+ "ToXgene_home property is not set. "
						+ "ToXgene will attempt to load\n"
						+ "toxgene.jar/config/cdata.xml assuming"
						+ "toxgene.jar is in the current "
						+ "directory.\n\nUse java "
						+ "-DToXgene_home=<path>... " + "to override this.");
			}
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
			session.pomBufferSize = 8 * 1024;
			
			/* Initialize the engine */
			tgEngine.startSession(session);

			File temp = new File(template);
			
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
			generateCollections();
		}
		catch (ToXgeneErrorException e1) {
			error(e1.getMessage());
		}
		catch (FileNotFoundException e) {
			tgEngine.endSession();
			error("cannot open template file \"" + template + "\"");
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		tgEngine.endSession();

		int nWarnings = tgReporter.warnings();
		if (nWarnings > 0) {
			System.out.println("There were " + nWarnings + " warning(s).");
			tgReporter.printAllWarnings();
		}
		System.exit(0);
	}

	/**
	 * Scans the collections declared in the template and generates the XML
	 * documents they specify on files.
	 */
	private static void generateCollections() {
		Vector collections = tgEngine.getToXgeneDocumentCollections();
		String outputPath = "./"; /*
								 * Path where to put the documents
								 */

		if (collections.size() == 0) {
			tgReporter.warning("no document genes found");
			return;
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

				String current;
				int count = 0, sum = 0;

				for (int j = start; j < start + documents; j++) {
					current =
							outputPath + tgColl.getName() + nf.format(j)
									+ ".xml";
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

				String file = outputPath + tgColl.getName() + ".xml";
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

	}

	/**
	 * Prints an error message and aborts.
	 */
	public static void error(String msg) {
		tgReporter.printAllWarnings();
		System.out.println("\n***** ERROR: " + msg);
		System.exit(1);
	}
}
