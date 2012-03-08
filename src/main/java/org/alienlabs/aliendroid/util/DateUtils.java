package org.alienlabs.aliendroid.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

	public static int calcularDiferencaDias(Date data1, Date data2) {
		Calendar calendarData1 = Calendar.getInstance();
		calendarData1.setTime(data1);
		Long dateStamp1 = (calendarData1.getTimeInMillis() - (calendarData1.getTimeInMillis() % (1000 * 60 * 60 * 24))) / (1000 * 60 * 60 * 24);
		Calendar calendarData2 = Calendar.getInstance();
		calendarData2.setTime(data2);
		Long dateStamp2 = (calendarData2.getTimeInMillis() - (calendarData2.getTimeInMillis() % (1000 * 60 * 60 * 24))) / (1000 * 60 * 60 * 24);
		Long diff = dateStamp1 - dateStamp2;
		return Math.abs(diff.intValue());
	}

	public static String format(Date data, String formato) {
		if (data != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(formato);
			return dateFormat.format(data);
		} else {
			return "";
		}
	}

	public static Date format(String strData, String formato) {
		if (strData != null) {
			SimpleDateFormat df = new SimpleDateFormat(formato);
			try {
				return (Date) df.parse(strData);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}
