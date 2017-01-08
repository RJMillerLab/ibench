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
package tresc.benchmark.dataGen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.vagabond.util.LoggerUtil;

import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import vtools.dataModel.schema.Schema;

public class TrampCSVGen extends ToXDataGenerator {

	private static final String XML_TO_CSV_XSLT_TEMPLATE_XML =
			"resource/xmlToCSV_XSLT_template.xml";

	Logger log = Logger.getLogger(TrampCSVGen.class);

	private String templateXSLT;

	static {
		System.setProperty("javax.xml.transform.TransformerFactory",
				"net.sf.saxon.TransformerFactoryImpl");
	}

	public TrampCSVGen(Configuration config) {
		super(config);
	}

	public TrampCSVGen(Schema schema, Configuration config) {
		super(schema, config);
	}

	@Override
	protected void initFromConfig() {
		super.initFromConfig();
	}

	private void readTemplate() throws IOException {
		StringBuffer result = new StringBuffer();
		BufferedReader in =
				new BufferedReader(new FileReader(XML_TO_CSV_XSLT_TEMPLATE_XML));

		while (in.ready()) {
			result.append(in.readLine() + "\n");
		}
		in.close();

		templateXSLT = result.toString();
	}

	@Override
	public void generateData() throws Exception {
		readTemplate();
		super.generateData();
		xsltToCsv();
	}

	private void xsltToCsv() {
		File instFile = new File(outputPath, instanceXMLFile);
		File outFile;
		
		// create one CSV file for each relation
		for (int i = 0; i < schema.size(); i++) {
			SMarkElement rootSetElt = (SMarkElement) schema.getSubElement(i);
			String relName = rootSetElt.getLabel();
			String xsltScript = templateXSLT.replace("$RELNAME$", relName);
			outFile = new File(outputPath, relName + ".csv");
			if (log.isDebugEnabled()) {log.debug("use XSLT script:\n" + xsltScript);};
			
			transform(xsltScript, instFile, outFile);
		}
	}

	private void transform(String script, File instFile, File outFile) {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer =
					tFactory.newTransformer(new StreamSource(new StringReader(
							script)));

			transformer.transform(new StreamSource(instFile), new StreamResult(
					outFile));
		}
		catch (Exception e) {
			LoggerUtil.logException(e, log);
		}
	}
}
