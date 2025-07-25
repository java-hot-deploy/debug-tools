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
package io.github.future0923.debug.tools.base.hutool.json.serialize;

import io.github.future0923.debug.tools.base.hutool.core.lang.Assert;
import io.github.future0923.debug.tools.base.hutool.json.JSON;
import io.github.future0923.debug.tools.base.hutool.json.JSONException;
import io.github.future0923.debug.tools.base.hutool.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.TemporalAccessor;

/**
 * {@link TemporalAccessor}的JSON自定义序列化实现
 *
 * @author looly
 * @since 5.7.22
 */
public class TemporalAccessorSerializer implements JSONObjectSerializer<TemporalAccessor>, JSONDeserializer<TemporalAccessor> {

	private static final String YEAR_KEY = "year";
	private static final String MONTH_KEY = "month";
	private static final String DAY_KEY = "day";
	private static final String HOUR_KEY = "hour";
	private static final String MINUTE_KEY = "minute";
	private static final String SECOND_KEY = "second";
	private static final String NANO_KEY = "nano";

	private final Class<? extends TemporalAccessor> temporalAccessorClass;

	public TemporalAccessorSerializer(Class<? extends TemporalAccessor> temporalAccessorClass) {
		this.temporalAccessorClass = temporalAccessorClass;
	}

	@Override
	public void serialize(JSONObject json, TemporalAccessor bean) {
		if (bean instanceof LocalDate) {
			final LocalDate localDate = (LocalDate) bean;
			json.set(YEAR_KEY, localDate.getYear());
			json.set(MONTH_KEY, localDate.getMonthValue());
			json.set(DAY_KEY, localDate.getDayOfMonth());
		} else if (bean instanceof LocalDateTime) {
			final LocalDateTime localDateTime = (LocalDateTime) bean;
			json.set(YEAR_KEY, localDateTime.getYear());
			json.set(MONTH_KEY, localDateTime.getMonthValue());
			json.set(DAY_KEY, localDateTime.getDayOfMonth());
			json.set(HOUR_KEY, localDateTime.getHour());
			json.set(MINUTE_KEY, localDateTime.getMinute());
			json.set(SECOND_KEY, localDateTime.getSecond());
			json.set(NANO_KEY, localDateTime.getNano());
		} else if (bean instanceof LocalTime) {
			final LocalTime localTime = (LocalTime) bean;
			json.set(HOUR_KEY, localTime.getHour());
			json.set(MINUTE_KEY, localTime.getMinute());
			json.set(SECOND_KEY, localTime.getSecond());
			json.set(NANO_KEY, localTime.getNano());
		} else {
			throw new JSONException("Unsupported type to JSON: {}", bean.getClass().getName());
		}
	}

	@Override
	public TemporalAccessor deserialize(JSON json) {
		final JSONObject jsonObject = (JSONObject) json;
		if (LocalDate.class.equals(this.temporalAccessorClass) || LocalDateTime.class.equals(this.temporalAccessorClass)) {
			final Integer year = jsonObject.getInt(YEAR_KEY);
			Assert.notNull(year, "Field 'year' must be not null");
			Integer month = jsonObject.getInt(MONTH_KEY);
			if (null == month) {
				final Month monthEnum = Month.valueOf(jsonObject.getStr(MONTH_KEY));
				Assert.notNull(monthEnum, "Field 'month' must be not null");
				month = monthEnum.getValue();
			}
			Integer day = jsonObject.getInt(DAY_KEY);
			if (null == day) {
				day = jsonObject.getInt("dayOfMonth");
				Assert.notNull(day, "Field 'day' or 'dayOfMonth' must be not null");
			}

			final LocalDate localDate = LocalDate.of(year, month, day);
			if (LocalDate.class.equals(this.temporalAccessorClass)) {
				return localDate;
			}

			final LocalTime localTime = LocalTime.of(
				jsonObject.getInt(HOUR_KEY, 0),
				jsonObject.getInt(MINUTE_KEY, 0),
				jsonObject.getInt(SECOND_KEY, 0),
				jsonObject.getInt(NANO_KEY, 0));

			return LocalDateTime.of(localDate, localTime);
		} else if (LocalTime.class.equals(this.temporalAccessorClass)) {
			return LocalTime.of(jsonObject.getInt(HOUR_KEY), jsonObject.getInt(MINUTE_KEY), jsonObject.getInt(SECOND_KEY), jsonObject.getInt(NANO_KEY));
		}

		throw new JSONException("Unsupported type from JSON: {}", this.temporalAccessorClass);
	}
}
