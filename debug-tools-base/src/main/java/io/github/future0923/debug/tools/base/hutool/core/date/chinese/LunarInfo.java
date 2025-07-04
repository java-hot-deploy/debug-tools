/*
 * Copyright (C) 2024-2025 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.future0923.debug.tools.base.hutool.core.date.chinese;

import java.time.LocalDate;

/**
 * 阴历（农历）信息
 *
 * @author looly
 * @since 5.4.1
 */
public class LunarInfo {

	/**
	 * 1900年
	 */
	public static final int BASE_YEAR = 1900;
	/**
	 * 1900-01-31，农历正月初一
	 */
	public static final long BASE_DAY = LocalDate.of(BASE_YEAR, 1, 31).toEpochDay();

	/**
	 * 此表来自：<a href="https://github.com/jjonline/calendar.js/blob/master/calendar.js">https://github.com/jjonline/calendar.js/blob/master/calendar.js</a>
	 * 农历表示：
	 * 1.  表示当年有无闰年，有的话，为闰月的月份，没有的话，为0。
	 * 2-4.为除了闰月外的正常月份是大月还是小月，1为30天，0为29天。
	 * 5.  表示闰月是大月还是小月，仅当存在闰月的情况下有意义。
	 */
	private static final long[] LUNAR_CODE = new long[]{
			0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,//1900-1909
			0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,//1910-1919
			0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,//1920-1929
			0x06566, 0x0d4a0, 0x0ea50, 0x16a95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,//1930-1939
			0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,//1940-1949
			0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,//1950-1959
			0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,//1960-1969
			0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,//1970-1979
			0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,//1980-1989
			0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x05ac0, 0x0ab60, 0x096d5, 0x092e0,//1990-1999
			0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,//2000-2009
			0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,//2010-2019
			0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,//2020-2029
			0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,//2030-2039
			0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,//2040-2049
			0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,//2050-2059
			0x092e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,//2060-2069
			0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,//2070-2079
			0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,//2080-2089
			0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252,//2090-2099
	};

	// 支持的最大年限
	public static final int MAX_YEAR = BASE_YEAR + LUNAR_CODE.length - 1;

	/**
	 * 传回农历 y年的总天数
	 *
	 * @param y 年
	 * @return 总天数
	 */
	public static int yearDays(int y) {
		int i, sum = 348;
		for (i = 0x8000; i > 0x8; i >>= 1) {
			if ((getCode(y) & i) != 0) {
				sum += 1;
			}
		}
		return (sum + leapDays(y));
	}

	/**
	 * 传回农历 y年闰月的天数，如果本年无闰月，返回0，区分大小月
	 *
	 * @param y 农历年
	 * @return 闰月的天数
	 */
	public static int leapDays(int y) {
		if (leapMonth(y) != 0) {
			return (getCode(y) & 0x10000) != 0 ? 30 : 29;
		}

		return 0;
	}

	/**
	 * 传回农历 y年m月的总天数，区分大小月
	 *
	 * @param y 年
	 * @param m 月
	 * @return 总天数
	 */
	public static int monthDays(int y, int m) {
		return (getCode(y) & (0x10000 >> m)) == 0 ? 29 : 30;
	}

	/**
	 * 传回农历 y年闰哪个月 1-12 , 没闰传回 0<br>
	 * 此方法会返回润N月中的N，如二月、闰二月都返回2
	 *
	 * @param y 年
	 * @return 润的月, 没闰传回 0
	 */
	public static int leapMonth(int y) {
		return (int) (getCode(y) & 0xf);
	}

	/**
	 * 获取对应年的农历信息
	 *
	 * @param year 年
	 * @return 农历信息
	 */
	private static long getCode(int year) {
		return LUNAR_CODE[year - BASE_YEAR];
	}
}
