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
package io.github.future0923.debug.tools.base.hutool.core.comparator;

import io.github.future0923.debug.tools.base.hutool.core.lang.Assert;

import java.util.Comparator;

/**
 * 按照指定类型顺序排序，对象顺序取决于对象对应的类在数组中的位置。
 *
 * <p>如果对比的两个对象类型相同，返回{@code 0}，默认如果对象类型不在列表中，则排序在前</p>
 * <p>此类来自Spring，有所改造</p>
 *
 * @param <T> 用于比较的对象类型
 * @author Phillip Webb
 * @since 5.4.1
 */
public class InstanceComparator<T> implements Comparator<T> {

	private final boolean atEndIfMiss;
	private final Class<?>[] instanceOrder;

	/**
	 * 构造
	 *
	 * @param instanceOrder 用于比较排序的对象类型数组，排序按照数组位置排序
	 */
	public InstanceComparator(Class<?>... instanceOrder) {
		this(false, instanceOrder);
	}

	/**
	 * 构造
	 *
	 * @param atEndIfMiss   如果不在列表中是否排在后边
	 * @param instanceOrder 用于比较排序的对象类型数组，排序按照数组位置排序
	 */
	public InstanceComparator(boolean atEndIfMiss, Class<?>... instanceOrder) {
		Assert.notNull(instanceOrder, "'instanceOrder' array must not be null");
		this.atEndIfMiss = atEndIfMiss;
		this.instanceOrder = instanceOrder;
	}


	@Override
	public int compare(T o1, T o2) {
		int i1 = getOrder(o1);
		int i2 = getOrder(o2);
		return Integer.compare(i1, i2);
	}

	/**
	 * 查找对象类型所在列表的位置
	 *
	 * @param object 对象
	 * @return 位置，未找到位置根据{@link #atEndIfMiss}取不同值，false返回-1，否则返回列表长度
	 */
	private int getOrder(T object) {
		if (object != null) {
			for (int i = 0; i < this.instanceOrder.length; i++) {
				if (this.instanceOrder[i].isInstance(object)) {
					return i;
				}
			}
		}
		return this.atEndIfMiss ? this.instanceOrder.length : -1;
	}
}
