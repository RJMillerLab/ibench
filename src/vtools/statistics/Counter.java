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