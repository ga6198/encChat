package com.example.p2pchat.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    static public String dateStringWithoutTime(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            Date currentDateWithoutTime = sdf.parse(sdf.format(new Date()));
            Date chatDateWithoutTime = sdf.parse(sdf.format(date));

            if(currentDateWithoutTime.equals(chatDateWithoutTime)){
                return "Today";
            }
            else{
                return sdf.format(chatDateWithoutTime);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    static public String dateStringWithTime(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm a");
        SimpleDateFormat todaySdf = new SimpleDateFormat("h:mm a");
        try {
            Date currentDateWithoutTime = sdf.parse(sdf.format(new Date()));
            Date chatDateWithoutTime = sdf.parse(sdf.format(date));

            if(currentDateWithoutTime.equals(chatDateWithoutTime)){
                return todaySdf.format(date);
            }
            else{
                return sdf.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
