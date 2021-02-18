package com.realapps.chat.utils;

import android.content.Context;

import com.realapps.chat.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Prashant Sharma on 3/19/2018.
 */

public class DateTimeUtils {

    //Get Message Destruction Time by Burn Time ID (Array)
    public static String getMessageDestructionTimeByBurnTime(Context mContext, int burnTime) {
        List<String> destructionTime = Arrays.asList(mContext.getResources().getStringArray(R.array.burn_time));

        if (burnTime < 0)
            burnTime = 0;
        int value = Integer.parseInt(destructionTime.get(burnTime));
        Calendar nextDate = getMessageDestructionDateTimeByBurnTimeAndValue(burnTime, value);
        return getCurrentDateTimeString(nextDate.getTime());

    }
    public static long getReamingTime(String date) {
        Date currentTime = getDateTimeFromString(getCurrentDateTime(), "yyyy-MM-dd HH:mm:ss");
        Date destructTime = getDateTimeFromString(date, "yyyy-MM-dd HH:mm:ss");
        return destructTime.getTime() - currentTime.getTime();
    }
    //Get Exact Burn DateTime Time Stamp by Burn Id and Value.
    public static Calendar getMessageDestructionDateTimeByBurnTimeAndValue(int id, int value) {
        Calendar nextDate = Calendar.getInstance();
        nextDate.setTimeInMillis(System.currentTimeMillis());

        if (id >= 0 && id <= 12)
            nextDate.add(Calendar.SECOND, value);
        else if (id >= 13 && id <= 27)
            nextDate.add(Calendar.MINUTE, value);
        else if (id >= 28 && id <= 38)
            nextDate.add(Calendar.HOUR, value);
        else
            nextDate.add(Calendar.HOUR, value * 24);
        return nextDate;
    }

    public static String getCurrentTimeMilliseconds() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static String getCurrentDateTime() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);
        return df.format(Calendar.getInstance().getTime());
    }

    public static String getCurrentDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
        return df.format(Calendar.getInstance().getTime());
    }

    public static String getCurrentDateOnly(String date, String format) {
        Date MsgDate = getDateTimeFromString(date, "yyyy-MM-dd HH:mm:ss");
        return formatDateTime(MsgDate, format);
    }

    public static String getSimplifiedDateTime(String date) {
        if (date.length() < 10) {
            return date;
        }
        String displayDate = "";
        Date MsgDate = getDateTimeFromString(date, "yyyy-MM-dd HH:mm:ss");
        Date nowDate;
        long msgTimeInMillisecond = MsgDate.getTime();
        Calendar msgCalender = Calendar.getInstance();
        msgCalender.setTimeInMillis(msgTimeInMillisecond);
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            nowDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            long diff = nowDate.getTime() - MsgDate.getTime();
            if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) > 1) {
                displayDate = formatDateTime(MsgDate, "yyyy-MM-dd hh:mm a");
            } else if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) == 0) {
                if (calendar.get(Calendar.DATE) - msgCalender.get(Calendar.DATE) == 1) {
                    displayDate = formatDateTime(MsgDate, "yyyy-MM-dd hh:mm a");
                } else {
                    StringTokenizer tk = new StringTokenizer(date);
                    String date1 = tk.nextToken();
                    String time = tk.nextToken();
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ROOT);
                    SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a", Locale.ROOT);
                    Date dt;
                    dt = sdf.parse(time);
                    displayDate = sdfs.format(dt);
                }
            } else if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) == 1) {
                displayDate = formatDateTime(MsgDate, "yyyy-MM-dd hh:mm a");
            } else {
                displayDate = formatDateTime(MsgDate, "yyyy-MM-dd hh:mm a");
            }
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return displayDate;
    }

    public static Date getDateTimeFromString(String dateString, String currentDateFormat) {
        DateFormat formatter = new SimpleDateFormat(currentDateFormat, Locale.ROOT);
        Date date = null;
        try {
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String formatDateTime(Date dateTime, String format) {
        //"dd MMM yyyy" HH:mm aa
        DateFormat formatter = new SimpleDateFormat(format, Locale.ROOT);
        return formatter.format(dateTime);
    }
    public static String getCurrentDateTimeString() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(Calendar.getInstance().getTime());
    }

    public static String getCurrentDateTimeString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(date);
    }


    public static String localDateTimeToUTC(String dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ROOT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date gmt = new Date(sdf.format(formatDateTimeGmt(dateTime)));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(gmt);
        calendar.add(Calendar.MILLISECOND, getRandomMiliseconds());
        gmt = calendar.getTime();
        SimpleDateFormat sdfSec = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);
        return sdfSec.format(gmt);
    }

    private static int getRandomMiliseconds() {
        Random random = new Random();
        return random.nextInt(899) + 100;
    }

    public static String UTCDateTimeToLocalTime(String date) {
        Date newDate = formatDateTimeGmt(date);
        SimpleDateFormat sdfSec = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);
        String timeZone = Calendar.getInstance().getTimeZone().getID();
        Date local = new Date(newDate.getTime() + TimeZone.getTimeZone(timeZone).getOffset(newDate.getTime()));
        return sdfSec.format(local);
    }

    public static Date formatDateTimeGmt(String dateTime) {

        if (!dateTime.contains(".")) {
            dateTime = dateTime + "." + getRandomMiliseconds();
        }
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);
        Date newDate = null;
        try {
            newDate = formatter.parse(dateTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newDate;
    }
}
