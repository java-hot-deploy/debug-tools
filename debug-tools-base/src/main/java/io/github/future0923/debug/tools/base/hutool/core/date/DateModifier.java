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

import io.github.future0923.debug.tools.base.hutool.core.util.ArrayUtil;

import java.util.Calendar;

/**
 * 日期修改器<br>
 * 用于实现自定义某个日期字段的调整，包括：
 *
 * <pre>
 * 1. 获取指定字段的起始时间
 * 2. 获取指定字段的四舍五入时间
 * 3. 获取指定字段的结束时间
 * </pre>
 *
 * @author looly
 */
public class DateModifier {

	/**
	 * 忽略的计算的字段
	 */
	private static final int[] IGNORE_FIELDS = new int[]{ //
			Calendar.HOUR_OF_DAY, // 与HOUR同名
			Calendar.AM_PM, // 此字段单独处理，不参与计算起始和结束
			Calendar.DAY_OF_WEEK_IN_MONTH, // 不参与计算
			Calendar.DAY_OF_YEAR, // DAY_OF_MONTH体现
			Calendar.WEEK_OF_MONTH, // 特殊处理
			Calendar.WEEK_OF_YEAR // WEEK_OF_MONTH体现
	};

	/**
	 * 修改日期
	 *
	 * @param calendar   {@link Calendar}
	 * @param dateField  日期字段，即保留到哪个日期字段
	 * @param modifyType 修改类型，包括舍去、四舍五入、进一等
	 * @return 修改后的{@link Calendar}
	 */
	public static Calendar modify(Calendar calendar, int dateField, ModifyType modifyType) {
		return modify(calendar, dateField, modifyType, false);
	}

	/**
	 * 修改日期，取起始值或者结束值<br>
	 * 可选是否归零毫秒。
	 *
	 * <p>
	 * 在{@link ModifyType#TRUNCATE}模式下，毫秒始终要归零,
	 * 但是在{@link ModifyType#CEILING}和{@link ModifyType#ROUND}模式下，
	 * 有时候由于毫秒部分必须为0（如MySQL数据库中），因此在此加上选项。
	 * </p>
	 *
	 * @param calendar            {@link Calendar}
	 * @param dateField           日期字段，即保留到哪个日期字段
	 * @param modifyType          修改类型，包括舍去、四舍五入、进一等
	 * @param truncateMillisecond 是否归零毫秒
	 * @return 修改后的{@link Calendar}
	 * @since 5.7.5
	 */
	public static Calendar modify(Calendar calendar, int dateField, ModifyType modifyType, boolean truncateMillisecond) {
		// AM_PM上下午特殊处理
		if (Calendar.AM_PM == dateField) {
			boolean isAM = DateUtil.isAM(calendar);
			switch (modifyType) {
				case TRUNCATE:
					calendar.set(Calendar.HOUR_OF_DAY, isAM ? 0 : 12);
					break;
				case CEILING:
					calendar.set(Calendar.HOUR_OF_DAY, isAM ? 11 : 23);
					break;
				case ROUND:
					int min = isAM ? 0 : 12;
					int max = isAM ? 11 : 23;
					int href = (max - min) / 2 + 1;
					int value = calendar.get(Calendar.HOUR_OF_DAY);
					calendar.set(Calendar.HOUR_OF_DAY, (value < href) ? min : max);
					break;
			}
			// 处理下一级别字段
			return modify(calendar, dateField + 1, modifyType);
		}

		final int endField = truncateMillisecond ? Calendar.SECOND : Calendar.MILLISECOND;
		// 循环处理各级字段，精确到毫秒字段
		for (int i = dateField + 1; i <= endField; i++) {
			if (ArrayUtil.contains(IGNORE_FIELDS, i)) {
				// 忽略无关字段（WEEK_OF_MONTH）始终不做修改
				continue;
			}

			// 在计算本周的起始和结束日时，月相关的字段忽略。
			if (Calendar.WEEK_OF_MONTH == dateField || Calendar.WEEK_OF_YEAR == dateField) {
				if (Calendar.DAY_OF_MONTH == i) {
					continue;
				}
			} else {
				// 其它情况忽略周相关字段计算
				if (Calendar.DAY_OF_WEEK == i) {
					continue;
				}
			}

			modifyField(calendar, i, modifyType);
		}

		if (truncateMillisecond) {
			calendar.set(Calendar.MILLISECOND, 0);
		}

		return calendar;
	}

	// -------------------------------------------------------------------------------------------------- Private method start

	/**
	 * 修改日期字段值
	 *
	 * @param calendar   {@link Calendar}
	 * @param field      字段，见{@link Calendar}
	 * @param modifyType {@link ModifyType}
	 */
	private static void modifyField(Calendar calendar, int field, ModifyType modifyType) {
		if (Calendar.HOUR == field) {
			// 修正小时。HOUR为12小时制，上午的结束时间为12:00，此处改为HOUR_OF_DAY: 23:59
			field = Calendar.HOUR_OF_DAY;
		}

		switch (modifyType) {
			case TRUNCATE:
				calendar.set(field, DateUtil.getBeginValue(calendar, field));
				break;
			case CEILING:
				calendar.set(field, DateUtil.getEndValue(calendar, field));
				break;
			case ROUND:
				int min = DateUtil.getBeginValue(calendar, field);
				int max = DateUtil.getEndValue(calendar, field);
				int href;
				if (Calendar.DAY_OF_WEEK == field) {
					// 星期特殊处理，假设周一是第一天，中间的为周四
					href = (min + 3) % 7;
				} else {
					href = (max - min) / 2 + 1;
				}
				int value = calendar.get(field);
				calendar.set(field, (value < href) ? min : max);
				break;
		}
		// Console.log("# {} -> {}", DateField.of(field), calendar.get(field));
	}
	// -------------------------------------------------------------------------------------------------- Private method end

	/**
	 * 修改类型
	 *
	 * @author looly
	 */
	public enum ModifyType {
		/**
		 * 取指定日期短的起始值.
		 */
		TRUNCATE,

		/**
		 * 指定日期属性按照四舍五入处理
		 */
		ROUND,

		/**
		 * 指定日期属性按照进一法处理
		 */
		CEILING
	}
}
