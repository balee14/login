package com.chatbot.adjustment.web.common.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

public class DateUtils {

	/**
	 * 현재 시간을 해당 포맷에 맞게 가져온다.
	 * @return
	 */
	public static String getCurrentDateTime(String pattern) {
		Calendar cal = Calendar.getInstance();

		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.format(cal.getTime());
	}

	/**
	 * 오늘의 날짜를 20130101 식으로 가져온다.
	 * @return
	 */
	public static String getTodayDate() {
		Calendar cal = Calendar.getInstance();

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		return format.format(cal.getTime());
	}

	/**
	 * 오늘의 년월을 201301 식으로 가져온다.
	 * @return
	 */
	public static String getTodayMonth() {
		Calendar cal = Calendar.getInstance();

		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		return format.format(cal.getTime());
	}

	/**
	 *
	 * @return
	 */
	public static Calendar getTodayCal() {
		Calendar cal = Calendar.getInstance();
		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH)+1;
		int d =  cal.get(Calendar.DAY_OF_MONTH);
		cal.set(y, m-1, d);

		return cal;
	}

	/**
	 *
	 * @return
	 */
	public static Calendar getCal(int year, int month) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month-1);
		/*int y = cal.get(year);
		int m = cal.get(month)+1;
		int d =  cal.get(Calendar.DAY_OF_MONTH);
		cal.set(y, m-1, d);*/

		return cal;
	}

	/**
	 * 이달의 첫째날짜
	 * @return
	 */
	public static String getFirstDay(Calendar cal) {

		String year = String.valueOf(cal.get(Calendar.YEAR));
		String month = String.format("%02d", cal.get(Calendar.MONTH)+1);

		return year + month + String.format("%02d", cal.getMinimum(Calendar.DAY_OF_MONTH));
	}

	/**
	 * 이달의 마지막날짜
	 * @return
	 */
	public static String getLastDay(Calendar cal) {

		String year = String.valueOf(cal.get(Calendar.YEAR));
		String month = String.format("%02d", cal.get(Calendar.MONTH)+1);

		return year + month + String.valueOf(cal.getActualMaximum(Calendar.DAY_OF_MONTH));
	}


	/**
	 * 현재 날짜 기준으로 특정 날짜 가져오기
	 * @param dateType
	 * @param interval
	 * @return
	 */
	public static String getDateTime(int dateType, int interval, String pattern) {
		Calendar cal = getTodayCal();

		cal.add(dateType, interval);

		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.format(cal.getTime());
	}


	public static String getDateTimeAdd(String date, String pattern, int interval, ChronoUnit unit) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);
		localDateTime.plus(interval, unit);

		return localDateTime.format(formatter);
	}


}
