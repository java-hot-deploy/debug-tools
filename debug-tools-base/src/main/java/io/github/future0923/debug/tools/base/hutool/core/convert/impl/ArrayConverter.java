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
package io.github.future0923.debug.tools.base.hutool.core.convert.impl;

import io.github.future0923.debug.tools.base.hutool.core.codec.Base64;
import io.github.future0923.debug.tools.base.hutool.core.collection.IterUtil;
import io.github.future0923.debug.tools.base.hutool.core.convert.AbstractConverter;
import io.github.future0923.debug.tools.base.hutool.core.convert.Convert;
import io.github.future0923.debug.tools.base.hutool.core.util.ArrayUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ByteUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.CharUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ObjectUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 数组转换器，包括原始类型数组
 *
 * @author Looly
 */
public class ArrayConverter extends AbstractConverter<Object> {
	private static final long serialVersionUID = 1L;

	private final Class<?> targetType;
	/**
	 * 目标元素类型
	 */
	private final Class<?> targetComponentType;

	/**
	 * 是否忽略元素转换错误
	 */
	private boolean ignoreElementError;

	/**
	 * 构造
	 *
	 * @param targetType 目标数组类型
	 */
	public ArrayConverter(Class<?> targetType) {
		this(targetType, false);
	}

	/**
	 * 构造
	 *
	 * @param targetType         目标数组类型
	 * @param ignoreElementError 是否忽略元素转换错误
	 */
	public ArrayConverter(Class<?> targetType, boolean ignoreElementError) {
		if (null == targetType) {
			// 默认Object数组
			targetType = Object[].class;
		}

		if (targetType.isArray()) {
			this.targetType = targetType;
			this.targetComponentType = targetType.getComponentType();
		} else {
			//用户传入类为非数组时，按照数组元素类型对待
			this.targetComponentType = targetType;
			this.targetType = ArrayUtil.getArrayType(targetType);
		}

		this.ignoreElementError = ignoreElementError;
	}

	@Override
	protected Object convertInternal(Object value) {
		return value.getClass().isArray() ? convertArrayToArray(value) : convertObjectToArray(value);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Class getTargetType() {
		return this.targetType;
	}

	/**
	 * 设置是否忽略元素转换错误
	 *
	 * @param ignoreElementError 是否忽略元素转换错误
	 * @since 5.4.3
	 */
	public void setIgnoreElementError(boolean ignoreElementError) {
		this.ignoreElementError = ignoreElementError;
	}

	// -------------------------------------------------------------------------------------- Private method start

	/**
	 * 数组对数组转换
	 *
	 * @param array 被转换的数组值
	 * @return 转换后的数组
	 */
	private Object convertArrayToArray(Object array) {
		final Class<?> valueComponentType = ArrayUtil.getComponentType(array);

		if (valueComponentType == targetComponentType) {
			return array;
		}

		final int len = ArrayUtil.length(array);
		final Object result = Array.newInstance(targetComponentType, len);

		for (int i = 0; i < len; i++) {
			Array.set(result, i, convertComponentType(Array.get(array, i)));
		}
		return result;
	}

	/**
	 * 非数组对数组转换
	 *
	 * @param value 被转换值
	 * @return 转换后的数组
	 */
	private Object convertObjectToArray(Object value) {
		if (value instanceof CharSequence) {
			if (targetComponentType == char.class || targetComponentType == Character.class) {
				return convertArrayToArray(value.toString().toCharArray());
			}

			//issue#2365
			// 字符串转bytes，首先判断是否为Base64，是则转换，否则按照默认getBytes方法。
			if(targetComponentType == byte.class){
				final String str = value.toString();
				if(Base64.isBase64(str)){
					return Base64.decode(value.toString());
				}
				return str.getBytes();
			}

			// 单纯字符串情况下按照逗号分隔后劈开
			final String[] strings = StrUtil.splitToArray(value.toString(), CharUtil.COMMA);
			return convertArrayToArray(strings);
		}

		Object result;
		if (value instanceof List) {
			// List转数组
			final List<?> list = (List<?>) value;
			result = Array.newInstance(targetComponentType, list.size());
			for (int i = 0; i < list.size(); i++) {
				Array.set(result, i, convertComponentType(list.get(i)));
			}
		} else if (value instanceof Collection) {
			// 集合转数组
			final Collection<?> collection = (Collection<?>) value;
			result = Array.newInstance(targetComponentType, collection.size());

			int i = 0;
			for (Object element : collection) {
				Array.set(result, i, convertComponentType(element));
				i++;
			}
		} else if (value instanceof Iterable) {
			// 可循环对象转数组，可循环对象无法获取长度，因此先转为List后转为数组
			final List<?> list = IterUtil.toList((Iterable<?>) value);
			result = Array.newInstance(targetComponentType, list.size());
			for (int i = 0; i < list.size(); i++) {
				Array.set(result, i, convertComponentType(list.get(i)));
			}
		} else if (value instanceof Iterator) {
			// 可循环对象转数组，可循环对象无法获取长度，因此先转为List后转为数组
			final List<?> list = IterUtil.toList((Iterator<?>) value);
			result = Array.newInstance(targetComponentType, list.size());
			for (int i = 0; i < list.size(); i++) {
				Array.set(result, i, convertComponentType(list.get(i)));
			}
		}else if (value instanceof Number && byte.class == targetComponentType) {
			// 用户可能想序列化指定对象
			result = ByteUtil.numberToBytes((Number)value);
		} else if (value instanceof Serializable && byte.class == targetComponentType) {
			// 用户可能想序列化指定对象
			result = ObjectUtil.serialize(value);
		} else {
			// everything else:
			result = convertToSingleElementArray(value);
		}

		return result;
	}

	/**
	 * 单元素数组
	 *
	 * @param value 被转换的值
	 * @return 数组，只包含一个元素
	 */
	private Object[] convertToSingleElementArray(Object value) {
		final Object[] singleElementArray = ArrayUtil.newArray(targetComponentType, 1);
		singleElementArray[0] = convertComponentType(value);
		return singleElementArray;
	}

	/**
	 * 转换元素类型
	 *
	 * @param value 值
	 * @return 转换后的值，转换失败若{@link #ignoreElementError}为true，返回null，否则抛出异常
	 * @since 5.4.3
	 */
	private Object convertComponentType(Object value) {
		return Convert.convertWithCheck(this.targetComponentType, value, null, this.ignoreElementError);
	}
	// -------------------------------------------------------------------------------------- Private method end
}
