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
package  vtools.statistics;

import org.apache.log4j.Logger;

public class Counter
{
	static Logger log = Logger.getLogger(Counter.class);

public static long _programTime = System.currentTimeMillis();

public static void reset()
{
    _programTime = System.currentTimeMillis();
}

public static long elapsedTime(long prevTime)
{
    return System.currentTimeMillis() - prevTime;
}

public static long dumpCheckPoint(long prevCheck, String msg)
{
    long mills = System.currentTimeMillis();
    if (prevCheck == -1)
        prevCheck = _programTime;
    //if (log.isDebugEnabled()) {log.debug((mills - prevCheck) + "ms " + msg);};
    long ms = (mills - prevCheck);
    long sec = ms / 1000;
    ms = ms % 1000;
    long min = sec / 60;
    sec = sec % 60;
    if (log.isDebugEnabled()) {log.debug(min + "min " + sec + "sec and " + ms + "ms " + msg);};
    return mills;
}

public static long getCurrentTime()
{
    return System.currentTimeMillis();
}
}