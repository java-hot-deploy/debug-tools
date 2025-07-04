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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

/**
 * {@code null}友好的比较器包装，如果nullGreater，则{@code null} &gt; non-null，否则反之。<br>
 * 如果二者皆为{@code null}，则为相等，返回0。<br>
 * 如果二者都非{@code null}，则使用传入的比较器排序。<br>
 * 传入比较器为{@code null}，则看被比较的两个对象是否都实现了{@link Comparable}实现则调用{@link Comparable#compareTo(Object)}。
 * 如果两者至少一个未实现，则视为所有元素相等。
 *
 * @param <T> 被比较的对象
 * @author looly
 * @since 5.7.10
 */
public class NullComparator<T> implements Comparator<T>, Serializable {
	private static final long serialVersionUID = 1L;

	protected final boolean nullGreater;
	protected final Comparator<T> comparator;

	/**
	 * 构造
	 * @param nullGreater 是否{@code null}最大，排在最后
	 * @param comparator 实际比较器
	 */
	@SuppressWarnings("unchecked")
	public NullComparator(boolean nullGreater, Comparator<? super T> comparator) {
		this.nullGreater = nullGreater;
		this.comparator = (Comparator<T>) comparator;
	}

	@Override
	public int compare(T a, T b) {
		if (a == b) {
			return 0;
		}if (a == null) {
			return nullGreater ? 1 : -1;
		} else if (b == null) {
			return nullGreater ? -1 : 1;
		} else {
			return doCompare(a, b);
		}
	}

	@Override
	public Comparator<T> thenComparing(Comparator<? super T> other) {
		Objects.requireNonNull(other);
		return new NullComparator<>(nullGreater, comparator == null ? other : comparator.thenComparing(other));
	}

	/**
	 * 不检查{@code null}的比较方法<br>
	 * 用户可自行重写此方法自定义比较方式
	 *
	 * @param a A值
	 * @param b B值
	 * @return 比较结果，-1:a小于b，0:相等，1:a大于b
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected int doCompare(T a, T b) {
		if (null == comparator) {
			if (a instanceof Comparable && b instanceof Comparable) {
				return ((Comparable) a).compareTo(b);
			}
			return 0;
		}

		return comparator.compare(a, b);
	}
}
