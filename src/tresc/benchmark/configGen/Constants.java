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
package tresc.benchmark.configGen;

import java.util.HashMap;
import java.util.Map;

//PRG ADDED ParameterName.PrimaryKeyFDs to linearize.config.Constants - Sep 17, 2012
//PRG CHANGED maxParameterValues for ParameterName.PrimaryKeyFDs to use PrimaryKeyFDOptions.values().length - Sep 22, 2012
//(this forces ConfigGenerator to randomly choose from only 2 possible values, 0 or 1). Also increase maxParameterValue by one for
//SourceSkolemPerc and SourceFDPerc to force ConfigGenerator to randomly choose up to 50% inclusive.

//PRG FIXING ALL PARAMETERS TO ENSURE PROPER NUMBERS (scaling is done in ConfigGenerator) - Oct 6, 2012
//NOTE: maxParameterValues and minParameters values are used in the scaling formula; do not interpret them outside the scaling scope!!!!
//What we want is to randomly generate configurations with the following numbers:
//(a) ConfigOptions.NumOfSubElements between 2 and 10 with +- deviation of 5 elements. 
//  Given MAX=9 and MIN= 2, the scaling formula randomly generates a uniform number between 0 and the max number (exclusive) 
//  and then adds the MIN. In this case, the formula generates a number between 2 and 10 to be used as NumOfSubelements. 
//  The deviation is set to be always 5.
//
//(b) ConfigOptions.JoinSize between 2 and 4 with +- deviation of 1 element
//  Scaling done using MAX=3 and MIN=2.
//
//(c) ConfigOptions.PrimaryKey between 1 and 3 elements with +- deviation of 1 element
//  Scaling done using MAX=3 and MIN=1.
//
//(d) ConfigOptions.NumOfJoinAttributes between 1 and 3 with +- deviation of 1 element.
//  Scaling done using MAX=3 and MIN=1. 
//

public class Constants {
	public enum ParameterName {
        NumOfSubElements,
        NumOfNewAttributes,
        NumofAttributestoDelete,
        JoinSize, 
        NumOfParamsInFunctions,
        PrimaryKeySize,
    	NumOfJoinAttributes,
    	JoinKind, 
    	SkolemKind,
    	//MN added new parameters - 21 April 2014
    	ReuseSourcePerc,
        ReuseTargetPerc,
        NoReuseScenPerc,
        SourceInclusionDependencyPerc,
        SourceInclusionDependencyFKPerc,
        TargetInclusionDependencyPerc,
        TargetInclusionDependencyFKPerc,
        SourceCircularInclusionDependency,
        SourceCircularFK,
        TargetCircularInclusionDependency,
        TargetCircularFK,
        //MN end of adding new parameters - 21 April 2014
        SourceSkolemPerc, 
        SourceFDPerc,
        PrimaryKeyFDs
    };
    
    public static final Map<ParameterName, Integer> maxParameterValues
	= new HashMap<ParameterName, Integer> ();

    static {
    	maxParameterValues.put(ParameterName.NumOfSubElements, 9);
    	maxParameterValues.put(ParameterName.JoinSize, 3);
    	maxParameterValues.put(ParameterName.NumOfParamsInFunctions, 3);
    	maxParameterValues.put(ParameterName.NumOfNewAttributes, 5);
    	maxParameterValues.put(ParameterName.NumofAttributestoDelete, 5);
    	maxParameterValues.put(ParameterName.NumOfJoinAttributes, 3);
    	maxParameterValues.put(ParameterName.PrimaryKeySize, 3);
    	maxParameterValues.put(ParameterName.JoinKind, JoinKind.values().length);
    	maxParameterValues.put(ParameterName.SkolemKind, SkolemKind.values().length);
    	//MN added new max parameter values - 21 April 2014
    	maxParameterValues.put(ParameterName.ReuseSourcePerc, 100);
    	maxParameterValues.put(ParameterName.ReuseTargetPerc, 100);
    	maxParameterValues.put(ParameterName.NoReuseScenPerc, 100);
    	////MN I need to check this max value with Patricia - 21 April 2014
    	maxParameterValues.put(ParameterName.SourceInclusionDependencyPerc, 100);
    	maxParameterValues.put(ParameterName.SourceInclusionDependencyFKPerc, 100);
    	////MN I need to check this max value with Patricia - 21 April 2014
    	maxParameterValues.put(ParameterName.SourceCircularFK, 1);
    	maxParameterValues.put(ParameterName.SourceCircularInclusionDependency, 1);
    	////MN I need to check this max value with Patricia - 21 April 2014
    	maxParameterValues.put(ParameterName.TargetInclusionDependencyPerc, 100);
    	maxParameterValues.put(ParameterName.TargetInclusionDependencyFKPerc, 100);
    	////MN I need to check this max value with Patricia - 21 April 2014
    	maxParameterValues.put(ParameterName.TargetCircularFK, 1);
    	maxParameterValues.put(ParameterName.TargetCircularInclusionDependency, 1);
    	//MN end of adding new max parameters - 21 April 2014
    	maxParameterValues.put(ParameterName.SourceSkolemPerc, 51);
    	maxParameterValues.put(ParameterName.SourceFDPerc, 51);
    	maxParameterValues.put(ParameterName.PrimaryKeyFDs, PrimaryKeyFDOptions.values().length);
    }
    
    public static final Map<ParameterName, Integer> minParameterValues
	= new HashMap<ParameterName, Integer> ();

    static {
    	minParameterValues.put(ParameterName.NumOfSubElements, 2);
    	minParameterValues.put(ParameterName.JoinSize, 2);
    	minParameterValues.put(ParameterName.NumOfParamsInFunctions, 1);
    	minParameterValues.put(ParameterName.NumOfNewAttributes, 1);
    	minParameterValues.put(ParameterName.NumofAttributestoDelete, 1);
    	minParameterValues.put(ParameterName.NumOfJoinAttributes, 1);
    	minParameterValues.put(ParameterName.PrimaryKeySize, 1);
    	minParameterValues.put(ParameterName.JoinKind, 0);
    	minParameterValues.put(ParameterName.SkolemKind, 0);
    	//MN added new min parameter values - 21 April 2014
    	minParameterValues.put(ParameterName.ReuseSourcePerc, 0);
    	minParameterValues.put(ParameterName.ReuseTargetPerc, 0);
    	minParameterValues.put(ParameterName.NoReuseScenPerc, 0);
    	minParameterValues.put(ParameterName.SourceInclusionDependencyPerc, 0);
    	minParameterValues.put(ParameterName.SourceInclusionDependencyFKPerc, 0);
    	minParameterValues.put(ParameterName.SourceCircularInclusionDependency, 0);
    	minParameterValues.put(ParameterName.SourceCircularFK, 0);
    	minParameterValues.put(ParameterName.TargetInclusionDependencyPerc, 0);
    	minParameterValues.put(ParameterName.TargetInclusionDependencyFKPerc, 0);
    	minParameterValues.put(ParameterName.TargetCircularInclusionDependency, 0);
    	minParameterValues.put(ParameterName.TargetCircularFK, 0);
    	//MN end of adding new min paramter values - 21 April 2014
    	minParameterValues.put(ParameterName.SourceSkolemPerc, 0);
    	minParameterValues.put(ParameterName.SourceFDPerc, 0);
    	minParameterValues.put(ParameterName.PrimaryKeyFDs, 0);
    }
    
    public static final Map<ParameterName, Integer> parameterDeviations
	= new HashMap<ParameterName, Integer> ();

    static {
    	parameterDeviations.put(ParameterName.NumOfSubElements, 5);
    	parameterDeviations.put(ParameterName.JoinSize, 1);
    	parameterDeviations.put(ParameterName.NumOfParamsInFunctions, 2);
    	parameterDeviations.put(ParameterName.NumOfNewAttributes, 1);
    	parameterDeviations.put(ParameterName.NumofAttributestoDelete, 1);
    	parameterDeviations.put(ParameterName.NumOfJoinAttributes, 1);
    	parameterDeviations.put(ParameterName.PrimaryKeySize, 1);
    	parameterDeviations.put(ParameterName.JoinKind, 0);
    	parameterDeviations.put(ParameterName.SkolemKind, 0);
    	//MN added new parameter deviations - 21 April 2014 (I need to check that with Patricia)
    	parameterDeviations.put(ParameterName.ReuseSourcePerc, 0);
    	parameterDeviations.put(ParameterName.ReuseTargetPerc, 0);
    	parameterDeviations.put(ParameterName.NoReuseScenPerc, 0);
    	parameterDeviations.put(ParameterName.SourceInclusionDependencyPerc, 0);
    	parameterDeviations.put(ParameterName.SourceInclusionDependencyFKPerc, 0);
    	parameterDeviations.put(ParameterName.SourceCircularInclusionDependency, 0);
    	parameterDeviations.put(ParameterName.SourceCircularFK, 0);
    	parameterDeviations.put(ParameterName.TargetInclusionDependencyPerc, 0);
    	parameterDeviations.put(ParameterName.TargetInclusionDependencyFKPerc, 0);
    	parameterDeviations.put(ParameterName.TargetCircularInclusionDependency, 0);
    	parameterDeviations.put(ParameterName.TargetCircularFK, 0);
    	//MN end of adding new parameter deviations - 21 April 2014
    	parameterDeviations.put(ParameterName.SourceSkolemPerc, 0);
    	parameterDeviations.put(ParameterName.SourceFDPerc, 0);
    	parameterDeviations.put(ParameterName.PrimaryKeyFDs, 0);
    }
    
    public enum PrimaryKeyFDOptions {
    	NoPKFDs,
    	YesPKFDs
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
        HORIZPARTITION,
        SURROGATEKEY,
        MERGING,
        //MN we need to remove self join and fusion - check that with Patricia - 21 April 2014
        //FUSION,
        //SELFJOINS,
        //MN we need to remove the above-mentioned scenarios - 21 April 2014
        VERTPARTITION,
        ADDATTRIBUTE,
        DELATTRIBUTE,
        ADDDELATTRIBUTE,
        VERTPARTITIONISA,
        VERTPARTITIONHASA,
        VERTPARTITIONNTOM,
        MERGEADD,
        // PRG ADD 16 Oct 2014
        VERTPARTITIONISAAUTHORITY
    };
    
    public enum DisabledScenarios {
        FLATTENING,
        GLAV,
        NESTING,
        VALUEGEN,
        VALUEMANAGEMENT,
        //MN added two disabled scenarios - 21 April 2014 (check that with Patricia)
        //MN mappings of OF do not work properly in Clio (skeleton graph) - 14 May 2014
        FUSION,
        SELFJOINS,
        //MN end of adding disabled scenarios - 21 April 2014
    	// VERTPARTITIONHASA,
    	// MERGEADD
    };
    
    public enum OutputOption {
    	HTMLSchemas,
    	Data,
    	XMLSchemas,
    	HTMLMapping,
    	TrampXML,
    	ErrorsAndExplanations
    }
    
    public static final Map<OutputOption, Boolean> defaultOutputOptions
	= new HashMap<OutputOption, Boolean> ();

    static {
    	defaultOutputOptions.put(OutputOption.HTMLSchemas, Boolean.FALSE);
    	defaultOutputOptions.put(OutputOption.Data, Boolean.FALSE);
    	defaultOutputOptions.put(OutputOption.XMLSchemas, Boolean.FALSE);
    	defaultOutputOptions.put(OutputOption.HTMLMapping, Boolean.FALSE);
    	defaultOutputOptions.put(OutputOption.TrampXML, Boolean.TRUE);
    	defaultOutputOptions.put(OutputOption.ErrorsAndExplanations, Boolean.FALSE);
    }
    
    public enum TrampXMLOutputSwitch {
    	Correspondences,
    	Transformations,
    	Data,
    	ConnectionInfo,
    	FDs
    }
    
    public static final Map<TrampXMLOutputSwitch, Boolean> defaultTrampXMLOutput
	= new HashMap<TrampXMLOutputSwitch, Boolean> ();

    static {
    	defaultTrampXMLOutput.put(TrampXMLOutputSwitch.Correspondences, Boolean.FALSE);
    	defaultTrampXMLOutput.put(TrampXMLOutputSwitch.Transformations, Boolean.FALSE);
    	defaultTrampXMLOutput.put(TrampXMLOutputSwitch.Data, Boolean.FALSE);
    	defaultTrampXMLOutput.put(TrampXMLOutputSwitch.ConnectionInfo, Boolean.FALSE);
    	defaultTrampXMLOutput.put(TrampXMLOutputSwitch.FDs, Boolean.TRUE);
    }
}
