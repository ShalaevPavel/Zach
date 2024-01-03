package org.example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateParser {

    public static String getMonthFromDateString(String dateString) {
        try {
            // Parse the date string
            SimpleDateFormat parser = new SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH);
            Date date = parser.parse(dateString);

            // Format the date to get the month name
            SimpleDateFormat formatter = new SimpleDateFormat("MMMM", Locale.ENGLISH);
            return formatter.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Invalid date";
        }
    }

    public static void main(String[] args) {
        String inputDate = "2020.12.12";
        String month = getMonthFromDateString(inputDate);
        System.out.println("The month is: " + month);
    }
}

