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
package io.github.future0923.debug.tools.base.hutool.core.date;

import java.util.Calendar;

/**
 * 日期各个部分的枚举<br>
 * 与Calendar相应值对应
 *
 * @author Looly
 *
 */
public enum DateField {

	/**
	 * 世纪
	 *
	 * @see Calendar#ERA
	 */
	ERA(Calendar.ERA),
	/**
	 * 年
	 *
	 * @see Calendar#YEAR
	 */
	YEAR(Calendar.YEAR),
	/**
	 * 月
	 *
	 * @see Calendar#MONTH
	 */
	MONTH(Calendar.MONTH),
	/**
	 * 一年中第几周
	 *
	 * @see Calendar#WEEK_OF_YEAR
	 */
	WEEK_OF_YEAR(Calendar.WEEK_OF_YEAR),
	/**
	 * 一月中第几周
	 *
	 * @see Calendar#WEEK_OF_MONTH
	 */
	WEEK_OF_MONTH(Calendar.WEEK_OF_MONTH),
	/**
	 * 一月中的第几天
	 *
	 * @see Calendar#DAY_OF_MONTH
	 */
	DAY_OF_MONTH(Calendar.DAY_OF_MONTH),
	/**
	 * 一年中的第几天
	 *
	 * @see Calendar#DAY_OF_YEAR
	 */
	DAY_OF_YEAR(Calendar.DAY_OF_YEAR),
	/**
	 * 周几，1表示周日，2表示周一
	 *
	 * @see Calendar#DAY_OF_WEEK
	 */
	DAY_OF_WEEK(Calendar.DAY_OF_WEEK),
	/**
	 * 天所在的周是这个月的第几周
	 *
	 * @see Calendar#DAY_OF_WEEK_IN_MONTH
	 */
	DAY_OF_WEEK_IN_MONTH(Calendar.DAY_OF_WEEK_IN_MONTH),
	/**
	 * 上午或者下午
	 *
	 * @see Calendar#AM_PM
	 */
	AM_PM(Calendar.AM_PM),
	/**
	 * 小时，用于12小时制
	 *
	 * @see Calendar#HOUR
	 */
	HOUR(Calendar.HOUR),
	/**
	 * 小时，用于24小时制
	 *
	 * @see Calendar#HOUR
	 */
	HOUR_OF_DAY(Calendar.HOUR_OF_DAY),
	/**
	 * 分钟
	 *
	 * @see Calendar#MINUTE
	 */
	MINUTE(Calendar.MINUTE),
	/**
	 * 秒
	 *
	 * @see Calendar#SECOND
	 */
	SECOND(Calendar.SECOND),
	/**
	 * 毫秒
	 *
	 * @see Calendar#MILLISECOND
	 */
	MILLISECOND(Calendar.MILLISECOND);

	// ---------------------------------------------------------------
	private final int value;

	DateField(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	/**
	 * 将 {@link Calendar}相关值转换为DatePart枚举对象<br>
	 *
	 * @param calendarPartIntValue Calendar中关于Week的int值
	 * @return DateField
	 */
	public static DateField of(int calendarPartIntValue) {
		switch (calendarPartIntValue) {
		case Calendar.ERA:
			return ERA;
		case Calendar.YEAR:
			return YEAR;
		case Calendar.MONTH:
			return MONTH;
		case Calendar.WEEK_OF_YEAR:
			return WEEK_OF_YEAR;
		case Calendar.WEEK_OF_MONTH:
			return WEEK_OF_MONTH;
		case Calendar.DAY_OF_MONTH:
			return DAY_OF_MONTH;
		case Calendar.DAY_OF_YEAR:
			return DAY_OF_YEAR;
		case Calendar.DAY_OF_WEEK:
			return DAY_OF_WEEK;
		case Calendar.DAY_OF_WEEK_IN_MONTH:
			return DAY_OF_WEEK_IN_MONTH;
		case Calendar.AM_PM:
			return AM_PM;
		case Calendar.HOUR:
			return HOUR;
		case Calendar.HOUR_OF_DAY:
			return HOUR_OF_DAY;
		case Calendar.MINUTE:
			return MINUTE;
		case Calendar.SECOND:
			return SECOND;
		case Calendar.MILLISECOND:
			return MILLISECOND;
		default:
			return null;
		}
	}
}
