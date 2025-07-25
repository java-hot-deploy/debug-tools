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
package io.github.future0923.debug.tools.base.hutool.core.util;

import io.github.future0923.debug.tools.base.hutool.core.collection.CollUtil;
import io.github.future0923.debug.tools.base.hutool.core.lang.Assert;
import io.github.future0923.debug.tools.base.hutool.core.lang.func.Func1;
import io.github.future0923.debug.tools.base.hutool.core.lang.func.LambdaUtil;
import io.github.future0923.debug.tools.base.hutool.core.map.MapUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 枚举工具类
 *
 * @author looly
 * @since 3.3.0
 */
public class EnumUtil {

	/**
	 * 指定类是否为Enum类
	 *
	 * @param clazz 类
	 * @return 是否为Enum类
	 */
	public static boolean isEnum(Class<?> clazz) {
		return Assert.notNull(clazz).isEnum();
	}

	/**
	 * 指定类是否为Enum类
	 *
	 * @param obj 类
	 * @return 是否为Enum类
	 */
	public static boolean isEnum(Object obj) {
		return Assert.notNull(obj).getClass().isEnum();
	}

	/**
	 * Enum对象转String，调用{@link Enum#name()} 方法
	 *
	 * @param e Enum
	 * @return name值
	 * @since 4.1.13
	 */
	public static String toString(Enum<?> e) {
		return null != e ? e.name() : null;
	}

	/**
	 * 获取给定位置的枚举值
	 *
	 * @param <E>       枚举类型泛型
	 * @param enumClass 枚举类
	 * @param index     枚举索引
	 * @return 枚举值，null表示无此对应枚举
	 * @since 5.1.6
	 */
	public static <E extends Enum<E>> E getEnumAt(Class<E> enumClass, int index) {
		if (null == enumClass) {
			return null;
		}
		final E[] enumConstants = enumClass.getEnumConstants();
		if (index < 0) {
			index = enumConstants.length + index;
		}

		return index >= 0 && index < enumConstants.length ? enumConstants[index] : null;
	}

	/**
	 * 字符串转枚举，调用{@link Enum#valueOf(Class, String)}
	 *
	 * @param <E>       枚举类型泛型
	 * @param enumClass 枚举类
	 * @param value     值
	 * @return 枚举值
	 * @since 4.1.13
	 */
	public static <E extends Enum<E>> E fromString(Class<E> enumClass, String value) {
		if (null == enumClass || StrUtil.isBlank(value)) {
			return null;
		}
		return Enum.valueOf(enumClass, value);
	}

	/**
	 * 字符串转枚举，调用{@link Enum#valueOf(Class, String)}<br>
	 * 如果无枚举值，返回默认值
	 *
	 * @param <E>          枚举类型泛型
	 * @param enumClass    枚举类
	 * @param value        值
	 * @param defaultValue 无对应枚举值返回的默认值
	 * @return 枚举值
	 * @since 4.5.18
	 */
	public static <E extends Enum<E>> E fromString(Class<E> enumClass, String value, E defaultValue) {
		return ObjectUtil.defaultIfNull(fromStringQuietly(enumClass, value), defaultValue);
	}

	/**
	 * 字符串转枚举，调用{@link Enum#valueOf(Class, String)}，转换失败返回{@code null} 而非报错
	 *
	 * @param <E>       枚举类型泛型
	 * @param enumClass 枚举类
	 * @param value     值
	 * @return 枚举值
	 * @since 4.5.18
	 */
	public static <E extends Enum<E>> E fromStringQuietly(Class<E> enumClass, String value) {
		try {
			return fromString(enumClass, value);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * 模糊匹配转换为枚举，给定一个值，匹配枚举中定义的所有字段名（包括name属性），一旦匹配到返回这个枚举对象，否则返回null
	 *
	 * @param <E>       枚举类型
	 * @param enumClass 枚举类
	 * @param value     值
	 * @return 匹配到的枚举对象，未匹配到返回null
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> E likeValueOf(Class<E> enumClass, Object value) {
		if (null == enumClass || null == value) {
			return null;
		}
		if (value instanceof CharSequence) {
			value = value.toString().trim();
		}

		final Field[] fields = ReflectUtil.getFields(enumClass);
		final Enum<?>[] enums = enumClass.getEnumConstants();
		String fieldName;
		for (Field field : fields) {
			fieldName = field.getName();
			if (field.getType().isEnum() || "ENUM$VALUES".equals(fieldName) || "ordinal".equals(fieldName)) {
				// 跳过一些特殊字段
				continue;
			}
			for (Enum<?> enumObj : enums) {
				if (ObjectUtil.equal(value, ReflectUtil.getFieldValue(enumObj, field))) {
					return (E) enumObj;
				}
			}
		}
		return null;
	}

	/**
	 * 枚举类中所有枚举对象的name列表
	 *
	 * @param clazz 枚举类
	 * @return name列表
	 */
	public static List<String> getNames(Class<? extends Enum<?>> clazz) {
		if (null == clazz) {
			return null;
		}
		final Enum<?>[] enums = clazz.getEnumConstants();
		if (null == enums) {
			return null;
		}
		final List<String> list = new ArrayList<>(enums.length);
		for (Enum<?> e : enums) {
			list.add(e.name());
		}
		return list;
	}

	/**
	 * 获得枚举类中各枚举对象下指定字段的值
	 *
	 * @param clazz     枚举类
	 * @param fieldName 字段名，最终调用getXXX方法
	 * @return 字段值列表
	 */
	public static List<Object> getFieldValues(Class<? extends Enum<?>> clazz, String fieldName) {
		if (null == clazz || StrUtil.isBlank(fieldName)) {
			return null;
		}
		final Enum<?>[] enums = clazz.getEnumConstants();
		if (null == enums) {
			return null;
		}
		final List<Object> list = new ArrayList<>(enums.length);
		for (Enum<?> e : enums) {
			list.add(ReflectUtil.getFieldValue(e, fieldName));
		}
		return list;
	}

	/**
	 * 获得枚举类中所有的字段名<br>
	 * 除用户自定义的字段名，也包括“name”字段，例如：
	 *
	 * <pre>
	 *   EnumUtil.getFieldNames(Color.class) == ["name", "index"]
	 * </pre>
	 *
	 * @param clazz 枚举类
	 * @return 字段名列表
	 * @since 4.1.20
	 */
	public static List<String> getFieldNames(Class<? extends Enum<?>> clazz) {
		if (null == clazz) {
			return null;
		}
		final List<String> names = new ArrayList<>();
		final Field[] fields = ReflectUtil.getFields(clazz);
		String name;
		for (Field field : fields) {
			name = field.getName();
			if (field.getType().isEnum() || name.contains("$VALUES") || "ordinal".equals(name)) {
				continue;
			}
			if (false == names.contains(name)) {
				names.add(name);
			}
		}
		return names;
	}

	/**
	 * 通过 某字段对应值 获取 枚举，获取不到时为 {@code null}
	 *
	 * @param enumClass 枚举类
	 * @param predicate 条件
	 * @param <E>       枚举类型
	 * @return 对应枚举 ，获取不到时为 {@code null}
	 * @since 5.8.0
	 */
	public static <E extends Enum<E>> E getBy(Class<E> enumClass, Predicate<? super E> predicate) {
		return getBy(enumClass, predicate, null);
	}

	/**
	 * 通过 某字段对应值 获取 枚举，获取不到时为 {@code defaultEnum}
	 *
	 * @param enumClass   枚举类
	 * @param predicate   条件
	 * @param defaultEnum 获取不到时的默认枚举值
	 * @param <E>         枚举类型
	 * @return 对应枚举 ，获取不到时为 {@code defaultEnum}
	 */
	public static <E extends Enum<E>> E getBy(Class<E> enumClass, Predicate<? super E> predicate, E defaultEnum) {
		if (null == enumClass || null == predicate) {
			return null;
		}
		return Arrays.stream(enumClass.getEnumConstants())
			.filter(predicate).findFirst().orElse(defaultEnum);
	}

	/**
	 * 通过 某字段对应值 获取 枚举，获取不到时为 {@code null}
	 * <p>
	 * {@link LambdaUtil#getRealClass(Func1)}} 是相对耗时的
	 * 如果枚举值比较多,那么{@link EnumUtil#getBy(Func1, Object)} 方法
	 * 大部分时间都是被{@link LambdaUtil#getRealClass(Func1)}}所消耗的
	 * <br>
	 * 如果可以在编码过程中可以提供对应的枚举类 该方法与枚举的{@code Enum.values()}方法是差不多的。
	 *
	 * @param enumClass 枚举类， 为{@code null}返回{@code null}
	 * @param condition 条件字段，为{@code null}返回{@code null}
	 * @param value     条件字段值
	 * @param <E>       枚举类型
	 * @param <C>       字段类型
	 * @return 对应枚举 ，获取不到时为 {@code null}
	 */
	public static <E extends Enum<E>, C> E getBy(Class<E> enumClass, Func1<E, C> condition, C value) {
		if (null == condition) {
			return null;
		}
		return getBy(enumClass, constant -> ObjUtil.equals(condition.callWithRuntimeException(constant), value));
	}

	/**
	 * 通过 某字段对应值 获取 枚举，获取不到时为 {@code defaultEnum}
	 *
	 * @param enumClass   枚举类， 为{@code null}返回{@code null}
	 * @param condition   条件字段，为{@code null}返回{@code null}
	 * @param value       条件字段值
	 * @param defaultEnum 获取不到时的默认枚举值
	 * @param <E>         枚举类型
	 * @param <C>         字段类型
	 * @return 对应枚举 ，获取不到时为 {@code defaultEnum}
	 */
	public static <E extends Enum<E>, C> E getBy(Class<E> enumClass, Func1<E, C> condition, C value, E defaultEnum) {
		return ObjectUtil.defaultIfNull(getBy(enumClass, condition, value), defaultEnum);
	}

	/**
	 * 通过 某字段对应值 获取 枚举，获取不到时为 {@code null}
	 *
	 * @param condition 条件字段，为{@code null}返回{@code null}
	 * @param value     条件字段值
	 * @param <E>       枚举类型
	 * @param <C>       字段类型
	 * @return 对应枚举 ，获取不到时为 {@code null}
	 */
	public static <E extends Enum<E>, C> E getBy(Func1<E, C> condition, C value) {
		if (null == condition) {
			return null;
		}
		final Class<E> implClass = LambdaUtil.getRealClass(condition);
		return getBy(implClass, condition, value);
	}

	/**
	 * 通过 某字段对应值 获取 枚举，获取不到时为 {@code defaultEnum}
	 *
	 * @param <E>         枚举类型
	 * @param <C>         字段类型
	 * @param condition   条件字段
	 * @param value       条件字段值
	 * @param defaultEnum 条件找不到则返回结果使用这个
	 * @return 对应枚举 ，获取不到时为 {@code null}
	 * @since 5.8.8
	 */
	public static <E extends Enum<E>, C> E getBy(Func1<E, C> condition, C value, E defaultEnum) {
		return ObjectUtil.defaultIfNull(getBy(condition, value), defaultEnum);
	}

	/**
	 * 通过 某字段对应值 获取 枚举中另一字段值，获取不到时为 {@code null}
	 *
	 * @param field     你想要获取的字段，{@code null}返回{@code null}
	 * @param condition 条件字段，{@code null}返回{@code null}
	 * @param value     条件字段值
	 * @param <E>       枚举类型
	 * @param <F>       想要获取的字段类型
	 * @param <C>       条件字段类型
	 * @return 对应枚举中另一字段值 ，获取不到时为 {@code null}
	 * @since 5.8.0
	 */
	public static <E extends Enum<E>, F, C> F getFieldBy(Func1<E, F> field, Function<E, C> condition, C value) {
		if (null == field || null == condition) {
			return null;
		}
		final Class<E> implClass = LambdaUtil.getRealClass(field);
		return Arrays.stream(implClass.getEnumConstants())
			// 过滤
			.filter(constant -> ObjUtil.equals(condition.apply(constant), value))
			// 获取第一个并转换为结果
			.findFirst()
			.map(field::callWithRuntimeException)
			.orElse(null);
	}

	/**
	 * 获取枚举字符串值和枚举对象的Map对应，使用LinkedHashMap保证有序<br>
	 * 结果中键为枚举名，值为枚举对象
	 *
	 * @param <E>       枚举类型
	 * @param enumClass 枚举类
	 * @return 枚举字符串值和枚举对象的Map对应，使用LinkedHashMap保证有序
	 * @since 4.0.2
	 */
	public static <E extends Enum<E>> LinkedHashMap<String, E> getEnumMap(final Class<E> enumClass) {
		if (null == enumClass) {
			return null;
		}
		final LinkedHashMap<String, E> map = new LinkedHashMap<>();
		for (final E e : enumClass.getEnumConstants()) {
			map.put(e.name(), e);
		}
		return map;
	}

	/**
	 * 获得枚举名对应指定字段值的Map<br>
	 * 键为枚举名，值为字段值
	 *
	 * @param clazz     枚举类
	 * @param fieldName 字段名，最终调用getXXX方法
	 * @return 枚举名对应指定字段值的Map
	 */
	public static Map<String, Object> getNameFieldMap(Class<? extends Enum<?>> clazz, String fieldName) {
		if (null == clazz || StrUtil.isBlank(fieldName)) {
			return null;
		}
		final Enum<?>[] enums = clazz.getEnumConstants();
		if (null == enums) {
			return null;
		}
		final Map<String, Object> map = MapUtil.newHashMap(enums.length, true);
		for (Enum<?> e : enums) {
			map.put(e.name(), ReflectUtil.getFieldValue(e, fieldName));
		}
		return map;
	}

	/**
	 * 判断某个值是存在枚举中
	 *
	 * @param <E>       枚举类型
	 * @param enumClass 枚举类
	 * @param val       需要查找的值
	 * @return 是否存在
	 */
	public static <E extends Enum<E>> boolean contains(final Class<E> enumClass, String val) {
		final LinkedHashMap<String, E> enumMap = getEnumMap(enumClass);
		if (CollUtil.isEmpty(enumMap)) {
			return false;
		}
		return enumMap.containsKey(val);
	}

	/**
	 * 判断某个值是不存在枚举中
	 *
	 * @param <E>       枚举类型
	 * @param enumClass 枚举类
	 * @param val       需要查找的值
	 * @return 是否不存在
	 */
	public static <E extends Enum<E>> boolean notContains(final Class<E> enumClass, String val) {
		return false == contains(enumClass, val);
	}

	/**
	 * 忽略大小检查某个枚举值是否匹配指定值
	 *
	 * @param e   枚举值
	 * @param val 需要判断的值
	 * @return 是非匹配
	 */
	public static boolean equalsIgnoreCase(final Enum<?> e, String val) {
		return StrUtil.equalsIgnoreCase(toString(e), val);
	}

	/**
	 * 检查某个枚举值是否匹配指定值
	 *
	 * @param e   枚举值
	 * @param val 需要判断的值
	 * @return 是非匹配
	 */
	public static boolean equals(final Enum<?> e, String val) {
		return StrUtil.equals(toString(e), val);
	}
}
