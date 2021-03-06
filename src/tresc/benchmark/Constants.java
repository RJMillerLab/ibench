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
package tresc.benchmark;

import java.util.HashMap;
import java.util.Map;

import org.vagabond.benchmark.explgen.CorrespondenceGen;
import org.vagabond.benchmark.explgen.ExplanationGenerator;
import org.vagabond.benchmark.explgen.SourceCopyGen;
import org.vagabond.benchmark.explgen.SourceJoinGen;
import org.vagabond.benchmark.explgen.SourceSkeletonGen;
import org.vagabond.benchmark.explgen.SuperfluousMappingGen;
import org.vagabond.benchmark.explgen.TargetSkeletonGen;

import tresc.benchmark.dataGen.DataGenerator;
import tresc.benchmark.dataGen.ToXDataGenerator;
import tresc.benchmark.dataGen.ToXScriptOnlyDataGenerator;
import tresc.benchmark.dataGen.TrampCSVGen;
import tresc.benchmark.dataGen.TrampXMLinlineGen;


//MN added new sceanrio newVP - 23 June 2014
//PRG RENAMED CLASS - Before was newVP, Now is VPIsAAuthorityScenarioGenerator - 16 Oct 2014
//PRG ADD Parameter to control the complexity of the VP Authority Scenario - 24 FEB 2015
//PRG ADD Parameter to control the output of Clio/MapMerge files - 24 FEB 2015

public class Constants
{
    public enum ParameterName {
        NumOfSubElements,
        NestingDepth,
        JoinSize,
        JoinKind,
        NumOfJoinAttributes,
        NumOfParamsInFunctions,
        NumOfNewAttributes,
        NumOfAttributesToDelete,
        SkolemKind,
        ReuseSourcePerc,	// reuse previously generated schema elements
        ReuseTargetPerc,
        NoReuseScenPerc,	// percentage of basic scenarios where we do not do a reuse
        SourceSkolemPerc,	// associates skolems with source  
        SourceFDPerc,	// associate FDs with source
        PrimaryKeyFDs,
        SourceSkolemNumAttr,		// attributes and use them in mappings
        PrimaryKeySize,
        //mehrnaz: I've added six parameters in order to implement random inclusion dependency generation
        SourceInclusionDependencyPerc,
        SourceInclusionDependencyFKPerc,
        TargetInclusionDependencyPerc,
        TargetInclusionDependencyFKPerc,
        SourceCircularInclusionDependency,
        SourceCircularFK,
        TargetCircularInclusionDependency,
        TargetCircularFK,
        // PRG ADD Parameter to control the complexity of the VP Authority Scenario - 24 FEB 2015
        VPAuthorityComplexity,
        PropagateDTsToTarget,
        PropagateDTsChanceOfDeviation,
        PropagateDTsDegreeOfDeviation
    };

    public static final Map<ParameterName, Integer> defaultParameterValues
			= new HashMap<ParameterName, Integer> ();
    
    static {
    	defaultParameterValues.put(ParameterName.NumOfSubElements, 5);
    	defaultParameterValues.put(ParameterName.NestingDepth, 1);
    	defaultParameterValues.put(ParameterName.JoinSize, 2);
    	defaultParameterValues.put(ParameterName.JoinKind, 0);
    	defaultParameterValues.put(ParameterName.NumOfJoinAttributes, 1);
    	defaultParameterValues.put(ParameterName.NumOfParamsInFunctions, 2);
    	defaultParameterValues.put(ParameterName.NumOfNewAttributes, 2);
    	defaultParameterValues.put(ParameterName.NumOfAttributesToDelete, 1);
    	defaultParameterValues.put(ParameterName.SkolemKind, 0);
    	defaultParameterValues.put(ParameterName.ReuseSourcePerc, 0);
    	defaultParameterValues.put(ParameterName.ReuseTargetPerc, 0);
    	defaultParameterValues.put(ParameterName.SourceSkolemPerc, 0);
    	defaultParameterValues.put(ParameterName.SourceFDPerc, 0);
    	defaultParameterValues.put(ParameterName.PrimaryKeyFDs, 1);
    	defaultParameterValues.put(ParameterName.SourceSkolemNumAttr, 0);
    	defaultParameterValues.put(ParameterName.NoReuseScenPerc, 100);
    	defaultParameterValues.put(ParameterName.PrimaryKeySize, 1);
    	
    	defaultParameterValues.put(ParameterName.SourceInclusionDependencyPerc, 0);
    	defaultParameterValues.put(ParameterName.SourceInclusionDependencyFKPerc, 0);
    	defaultParameterValues.put(ParameterName.TargetInclusionDependencyPerc, 0);
    	defaultParameterValues.put(ParameterName.TargetInclusionDependencyFKPerc, 0);
    	
    	defaultParameterValues.put(ParameterName.SourceCircularInclusionDependency, 1);
    	defaultParameterValues.put(ParameterName.SourceCircularFK, 1);
    	defaultParameterValues.put(ParameterName.TargetCircularInclusionDependency, 1);
    	defaultParameterValues.put(ParameterName.TargetCircularFK, 1);
    	
    	// PRG ADD Parameter to control the complexity of the VP Authority Scenario - 24 FEB 2015
    	defaultParameterValues.put(ParameterName.VPAuthorityComplexity, 2);
    	defaultParameterValues.put(ParameterName.PropagateDTsToTarget, 0);
    	defaultParameterValues.put(ParameterName.PropagateDTsChanceOfDeviation, 0);
    	defaultParameterValues.put(ParameterName.PropagateDTsDegreeOfDeviation, 0);        
    }
    
    public static final Map<ParameterName, Integer> defaultParameterDeviation
			= new HashMap<ParameterName, Integer> ();
    
    static {
    	defaultParameterDeviation.put(ParameterName.NumOfSubElements, 0);
    	defaultParameterDeviation.put(ParameterName.NestingDepth, 0);
    	defaultParameterDeviation.put(ParameterName.JoinSize, 0);
    	defaultParameterDeviation.put(ParameterName.JoinKind, 0);
    	defaultParameterDeviation.put(ParameterName.NumOfJoinAttributes, 0);
    	defaultParameterDeviation.put(ParameterName.NumOfParamsInFunctions, 0);
    	defaultParameterDeviation.put(ParameterName.NumOfNewAttributes, 0);
    	defaultParameterDeviation.put(ParameterName.NumOfAttributesToDelete, 0);
    	defaultParameterDeviation.put(ParameterName.SkolemKind, 0);
    	defaultParameterDeviation.put(ParameterName.ReuseSourcePerc, 0);
    	defaultParameterDeviation.put(ParameterName.ReuseTargetPerc, 0);
    	defaultParameterDeviation.put(ParameterName.SourceSkolemPerc, 0);
    	defaultParameterDeviation.put(ParameterName.SourceFDPerc, 0);
    	defaultParameterDeviation.put(ParameterName.SourceSkolemNumAttr, 0);
    	defaultParameterDeviation.put(ParameterName.NoReuseScenPerc, 0);
    	defaultParameterDeviation.put(ParameterName.PrimaryKeySize, 0);
    	defaultParameterDeviation.put(ParameterName.PrimaryKeyFDs, 0);
    	
    	defaultParameterDeviation.put(ParameterName.SourceInclusionDependencyPerc, 0);
    	defaultParameterDeviation.put(ParameterName.SourceInclusionDependencyFKPerc, 0);
    	defaultParameterDeviation.put(ParameterName.TargetInclusionDependencyPerc, 0);
    	defaultParameterDeviation.put(ParameterName.TargetInclusionDependencyFKPerc, 0);
    	
    	defaultParameterDeviation.put(ParameterName.SourceCircularInclusionDependency, 0);
    	defaultParameterDeviation.put(ParameterName.SourceCircularFK, 0);
    	defaultParameterDeviation.put(ParameterName.TargetCircularInclusionDependency, 0);
    	defaultParameterDeviation.put(ParameterName.TargetCircularFK, 0);
    	
    	// PRG ADD Parameter to control the complexity of the VP Authority Scenario - 24 FEB 2015
    	defaultParameterDeviation.put(ParameterName.VPAuthorityComplexity, 0);
    	defaultParameterDeviation.put(ParameterName.PropagateDTsToTarget, 0);
    	
    	defaultParameterDeviation.put(ParameterName.PropagateDTsChanceOfDeviation, 0);
    	defaultParameterDeviation.put(ParameterName.PropagateDTsDegreeOfDeviation, 0);  
    }
    
    public enum JoinKind {
        STAR,
        CHAIN,
        VARIABLE;
    };
    
    public enum SkolemKind {
    	KEY,
    	ALL,
        RANDOM,
        EXCHANGED,
        VARIABLE;
    };

    public enum ScenarioName {
        COPY,
        VALUEGEN,
        HORIZPARTITION,
        SURROGATEKEY,
        MERGING,
        VALUEMANAGEMENT,
        FUSION,
        FLATTENING,
        NESTING,
        SELFJOINS,
        VERTPARTITION,
        GLAV,
        ADDATTRIBUTE,
        DELATTRIBUTE,
        ADDDELATTRIBUTE,
        VERTPARTITIONISA,
        VERTPARTITIONHASA,
        VERTPARTITIONNTOM,
        MERGEADD,
        //MN added new scenario - 23 June 2014, 30 June 2014
        VERTPARTITIONISAAUTHORITY,
        LOADEXISTING
    };
    
    public static final Map<ScenarioName,String> nameForScenarios 
    		= new HashMap<ScenarioName, String> ();
    
    static {
    	nameForScenarios.put(ScenarioName.COPY, "_CP");
    	nameForScenarios.put(ScenarioName.VALUEGEN, "_VG");
    	nameForScenarios.put(ScenarioName.HORIZPARTITION, "_HP");
    	nameForScenarios.put(ScenarioName.SURROGATEKEY, "_SU");
    	nameForScenarios.put(ScenarioName.MERGING, "_ME");
    	nameForScenarios.put(ScenarioName.VALUEMANAGEMENT, "_AV");
    	nameForScenarios.put(ScenarioName.FUSION, "_OF");
    	nameForScenarios.put(ScenarioName.FLATTENING, "_FL");
    	nameForScenarios.put(ScenarioName.NESTING, "_NE");
    	nameForScenarios.put(ScenarioName.SELFJOINS, "_SJ");
    	nameForScenarios.put(ScenarioName.VERTPARTITION, "_VP");
    	nameForScenarios.put(ScenarioName.GLAV, "_GL");
    	nameForScenarios.put(ScenarioName.VERTPARTITIONISA, "_VI");
    	nameForScenarios.put(ScenarioName.VERTPARTITIONHASA, "_VH");
    	nameForScenarios.put(ScenarioName.VERTPARTITIONNTOM, "_VNM");
    	nameForScenarios.put(ScenarioName.ADDATTRIBUTE, "_AD");
    	nameForScenarios.put(ScenarioName.DELATTRIBUTE, "_DL");
    	nameForScenarios.put(ScenarioName.ADDDELATTRIBUTE, "_ADL");
    	nameForScenarios.put(ScenarioName.MERGEADD, "_MA");
    	//MN added new scenario - 23 June 2014, 30 June 2014
    	nameForScenarios.put(ScenarioName.VERTPARTITIONISAAUTHORITY, "_VA");
    	nameForScenarios.put(ScenarioName.LOADEXISTING, "_LE");
    }
    
    public enum OutputOption {
    	HTMLSchemas,
    	Data,
    	XMLSchemas,
    	HTMLMapping,
    	TrampXML,
    	ErrorsAndExplanations,
    	//PRG ADD Parameter to control the output of Clio/MapMerge files - 24 FEB 2015
    	Clio,
    	EnableTargetData
    }
    
    public static final Map<OutputOption, Boolean> defaultOutputOptionValues
    		= new HashMap<OutputOption, Boolean> ();
    
    static {
    	defaultOutputOptionValues.put(OutputOption.HTMLSchemas, Boolean.FALSE);
    	defaultOutputOptionValues.put(OutputOption.HTMLMapping, Boolean.FALSE);
    	defaultOutputOptionValues.put(OutputOption.XMLSchemas, Boolean.FALSE);
    	defaultOutputOptionValues.put(OutputOption.Data, Boolean.TRUE);
    	defaultOutputOptionValues.put(OutputOption.TrampXML, Boolean.TRUE);
    	defaultOutputOptionValues.put(OutputOption.ErrorsAndExplanations, Boolean.FALSE);
    	//PRG ADD Parameter to control the output of Clio/MapMerge files - 24 FEB 2015
    	defaultOutputOptionValues.put(OutputOption.Clio, Boolean.FALSE);
    	defaultOutputOptionValues.put(OutputOption.EnableTargetData, Boolean.FALSE);
    }
    
  
    public static final Map<DESErrorType, Integer> defaultGroundTruthValues
    		= new HashMap<DESErrorType, Integer> ();
    		
    static {
    	defaultGroundTruthValues.put(DESErrorType.SuperfluousMapping, 1);
    	defaultGroundTruthValues.put(DESErrorType.CorrespondenceError, 0);
    	defaultGroundTruthValues.put(DESErrorType.SourceCopyError, 0);
    	defaultGroundTruthValues.put(DESErrorType.SourceJoinValueError, 0);
    	defaultGroundTruthValues.put(DESErrorType.SourceSkeletonError, 0);
    	defaultGroundTruthValues.put(DESErrorType.TargetSkeletonError, 0);
    }
    
    public static final Map<DESErrorType, Integer> defaultGroundTruthDeviation
	= new HashMap<DESErrorType, Integer> ();
	
	static {
		defaultGroundTruthDeviation.put(DESErrorType.SuperfluousMapping, 0);
		defaultGroundTruthDeviation.put(DESErrorType.CorrespondenceError, 0);
		defaultGroundTruthDeviation.put(DESErrorType.SourceCopyError, 0);
		defaultGroundTruthDeviation.put(DESErrorType.SourceJoinValueError, 0);
		defaultGroundTruthDeviation.put(DESErrorType.SourceSkeletonError, 0);
		defaultGroundTruthDeviation.put(DESErrorType.TargetSkeletonError, 0);	
	}
    
    public enum DataGenType {
    	ToXGeneScriptOnly,
    	ToXGene,
    	TrampCSV,
    	TrampXMLInline
    }
    
    public static final Map<DataGenType, Class<? extends DataGenerator>> dataGens 
    		= new HashMap<DataGenType, Class<? extends DataGenerator>> ();
    
    static {
    	dataGens.put(DataGenType.ToXGeneScriptOnly, ToXScriptOnlyDataGenerator.class);
    	dataGens.put(DataGenType.ToXGene, ToXDataGenerator.class);
    	dataGens.put(DataGenType.TrampCSV, TrampCSVGen.class);
    	dataGens.put(DataGenType.TrampXMLInline, TrampXMLinlineGen.class); //TODO
    }
    
    public enum TrampXMLOutputSwitch {
    	Correspondences,
    	Transformations,
    	Data,
    	ConnectionInfo,
    	FDs
    }
    
    public static final Map<TrampXMLOutputSwitch, Boolean> trampXmlOutDefaults
    		= new HashMap<TrampXMLOutputSwitch, Boolean> ();
    
    static {
    	trampXmlOutDefaults.put(TrampXMLOutputSwitch.Correspondences, Boolean.TRUE);
    	trampXmlOutDefaults.put(TrampXMLOutputSwitch.Transformations, Boolean.TRUE);
    	trampXmlOutDefaults.put(TrampXMLOutputSwitch.Data, Boolean.TRUE);
    	trampXmlOutDefaults.put(TrampXMLOutputSwitch.ConnectionInfo, Boolean.TRUE);
    	trampXmlOutDefaults.put(TrampXMLOutputSwitch.FDs, Boolean.FALSE);
    }
    
    public enum MappingLanguageType {
    	FOtgds,
    	SOtgds
    }
    
    public enum QueryTranslatorType {
    	Postgres,
    	Perm
    }
    
    public enum DESErrorType {
    	SuperfluousMapping,
    	SourceCopyError,
    	SourceJoinValueError,
    	SourceSkeletonError,
    	TargetSkeletonError,
    	CorrespondenceError
    }
    
    public static final Map<DESErrorType, ExplanationGenerator> errorGenerators
    		= new HashMap<DESErrorType, ExplanationGenerator> ();
    
    static {
    	errorGenerators.put(DESErrorType.SourceCopyError, new SourceCopyGen());
    	errorGenerators.put(DESErrorType.SourceJoinValueError, new SourceJoinGen());
    	errorGenerators.put(DESErrorType.SuperfluousMapping, new SuperfluousMappingGen());
    	errorGenerators.put(DESErrorType.SourceSkeletonError, new SourceSkeletonGen());
    	errorGenerators.put(DESErrorType.TargetSkeletonError, new TargetSkeletonGen());
    	errorGenerators.put(DESErrorType.CorrespondenceError, new CorrespondenceGen());
    }
    
    public enum DBOption {
    	User,
    	URL,
    	Password,
    	Port,
    	DBName
    }
    
    public static final Map<DBOption, String> dbOptionDefaults = 
    		new HashMap<DBOption, String> ();
    
    static {
    	dbOptionDefaults.put(DBOption.DBName, "tramptest");
    	dbOptionDefaults.put(DBOption.Password, "");
    	dbOptionDefaults.put(DBOption.URL, "127.0.0.1");
    	dbOptionDefaults.put(DBOption.User, "postgres");
    	dbOptionDefaults.put(DBOption.Port, "5432");
    }
    
    
    
}