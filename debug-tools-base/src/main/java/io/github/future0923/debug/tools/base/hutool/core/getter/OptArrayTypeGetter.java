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
package io.github.future0923.debug.tools.base.hutool.core.getter;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 可选默认值的数组类型的Get接口
 * 提供一个统一的接口定义返回不同类型的值（基本类型）<br>
 * 如果值不存在或获取错误，返回默认值
 *
 * @author Looly
 * @since 4.0.2
 *
 */
public interface OptArrayTypeGetter {
	/*-------------------------- 数组类型 start -------------------------------*/

	/**
	 * 获取Object型属性值数组
	 *
	 * @param key 属性名
	 * @param defaultValue 默认数组值
	 * @return 属性值列表
	 */
	Object[] getObjs(String key, Object[] defaultValue);

	/**
	 * 获取String型属性值数组
	 *
	 * @param key 属性名
	 * @param defaultValue 默认数组值
	 * @return 属性值列表
	 */
	String[] getStrs(String key, String[] defaultValue);

	/**
	 * 获取Integer型属性值数组
	 *
	 * @param key 属性名
	 * @param defaultValue 默认数组值
	 * @return 属性值列表
	 */
	Integer[] getInts(String key, Integer[] defaultValue);

	/**
	 * 获取Short型属性值数组
	 *
	 * @param key 属性名
	 * @param defaultValue 默认数组值
	 * @return 属性值列表
	 */
	Short[] getShorts(String key, Short[] defaultValue);

	/**
	 * 获取Boolean型属性值数组
	 *
	 * @param key 属性名
	 * @param defaultValue 默认数组值
	 * @return 属性值列表
	 */
	Boolean[] getBools(String key, Boolean[] defaultValue);

	/**
	 * 获取Long型属性值数组
	 *
	 * @param key 属性名
	 * @param defaultValue 默认数组值
	 * @return 属性值列表
	 */
	Long[] getLongs(String key, Long[] defaultValue);

	/**
	 * 获取Character型属性值数组
	 *
	 * @param key 属性名
	 * @param defaultValue 默认数组值
	 * @return 属性值列表
	 */
	Character[] getChars(String key, Character[] defaultValue);

	/**
	 * 获取Double型属性值数组
	 *
	 * @param key 属性名
	 * @param defaultValue 默认数组值
	 * @return 属性值列表
	 */
	Double[] getDoubles(String key, Double[] defaultValue);

	/**
	 * 获取Byte型属性值数组
	 *
	 * @param key 属性名
	 * @param defaultValue 默认数组值
	 * @return 属性值列表
	 */
	Byte[] getBytes(String key, Byte[] defaultValue);

	/**
	 * 获取BigInteger型属性值数组
	 *
	 * @param key 属性名
	 * @param defaultValue 默认数组值
	 * @return 属性值列表
	 */
	BigInteger[] getBigIntegers(String key, BigInteger[] defaultValue);

	/**
	 * 获取BigDecimal型属性值数组
	 *
	 * @param key 属性名
	 * @param defaultValue 默认数组值
	 * @return 属性值列表
	 */
	BigDecimal[] getBigDecimals(String key, BigDecimal[] defaultValue);
	/*-------------------------- 数组类型 end -------------------------------*/
}
