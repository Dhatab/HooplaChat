package com.hooplachat.TimeStamp;

import android.app.Application;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PostDate extends Application {

    private static final String currentDate = new SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(new Date());
    private static final String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

    public static String getPostDate(String date, String time, Context context) {

        if (currentDate.equals(date)){
            return currentTime;
        } else{
            return date;
        }
    }
}