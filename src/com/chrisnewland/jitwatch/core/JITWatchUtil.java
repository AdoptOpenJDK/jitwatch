package com.chrisnewland.jitwatch.core;

public class JITWatchUtil
{
    public static String formatTimestamp(long stamp, boolean showMillis)
    {
        long stampCopy = stamp;

        long hourMillis = 3600000L;
        long minuteMillis = 60000L;
        long secondMillis = 1000L;

        long hours = (long) Math.floor(stampCopy / hourMillis);
        stampCopy -= hours * hourMillis;

        long minutes = (long) Math.floor(stampCopy / minuteMillis);
        stampCopy -= minutes * minuteMillis;

        long seconds = (long) Math.floor(stampCopy / secondMillis);
        stampCopy -= seconds * secondMillis;

        long millis = stampCopy;

        StringBuilder sb = new StringBuilder();

        // sb.append(stamp).append("=>");

        sb.append(pad(hours, 2)).append(":");
        sb.append(pad(minutes, 2)).append(":");
        sb.append(pad(seconds, 2));

        if (showMillis)
        {
            sb.append(".").append(pad(millis, 3));
        }

        return sb.toString();
    }

    public static String pad(long num, int width)
    {
        String numString = Long.toString(num);

        StringBuilder sb = new StringBuilder();

        int len = numString.length();

        if (len < width)
        {
            for (int i = 0; i < width - len; i++)
            {
                sb.append("0");
            }
        }

        sb.append(numString);

        return sb.toString();
    }
}
