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


import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

/**
 * 比较工具类
 *
 * @author looly
 */
public class CompareUtil {

	/**
	 * 获取自然排序器，即默认排序器
	 *
	 * @param <E> 排序节点类型
	 * @return 默认排序器
	 * @since 5.7.21
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Comparable<? super E>> Comparator<E> naturalComparator() {
		return ComparableComparator.INSTANCE;
	}

	/**
	 * 对象比较，比较结果取决于comparator，如果被比较对象为null，传入的comparator对象应处理此情况<br>
	 * 如果传入comparator为null，则使用默认规则比较（此时被比较对象必须实现Comparable接口）
	 *
	 * <p>
	 * 一般而言，如果c1 &lt; c2，返回数小于0，c1==c2返回0，c1 &gt; c2 大于0
	 *
	 * @param <T>        被比较对象类型
	 * @param c1         对象1
	 * @param c2         对象2
	 * @param comparator 比较器
	 * @return 比较结果
	 * @see Comparator#compare(Object, Object)
	 * @since 4.6.9
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static <T> int compare(T c1, T c2, Comparator<T> comparator) {
		if (null == comparator) {
			return compare((Comparable) c1, (Comparable) c2);
		}
		return comparator.compare(c1, c2);
	}

	/**
	 * {@code null}安全的对象比较，{@code null}对象小于任何对象
	 *
	 * @param <T> 被比较对象类型
	 * @param c1  对象1，可以为{@code null}
	 * @param c2  对象2，可以为{@code null}
	 * @return 比较结果，如果c1 &lt; c2，返回数小于0，c1==c2返回0，c1 &gt; c2 大于0
	 * @see Comparator#compare(Object, Object)
	 */
	public static <T extends Comparable<? super T>> int compare(T c1, T c2) {
		return compare(c1, c2, false);
	}

	/**
	 * {@code null}安全的对象比较
	 *
	 * @param <T>           被比较对象类型（必须实现Comparable接口）
	 * @param c1            对象1，可以为{@code null}
	 * @param c2            对象2，可以为{@code null}
	 * @param isNullGreater 当被比较对象为null时是否排在后面，true表示null大于任何对象，false反之
	 * @return 比较结果，如果c1 &lt; c2，返回数小于0，c1==c2返回0，c1 &gt; c2 大于0
	 * @see Comparator#compare(Object, Object)
	 */
	public static <T extends Comparable<? super T>> int compare(T c1, T c2, boolean isNullGreater) {
		if (c1 == c2) {
			return 0;
		} else if (c1 == null) {
			return isNullGreater ? 1 : -1;
		} else if (c2 == null) {
			return isNullGreater ? -1 : 1;
		}
		return c1.compareTo(c2);
	}

	/**
	 * 自然比较两个对象的大小，比较规则如下：
	 *
	 * <pre>
	 * 1、如果实现Comparable调用compareTo比较
	 * 2、o1.equals(o2)返回0
	 * 3、比较hashCode值
	 * 4、比较toString值
	 * </pre>
	 *
	 * @param <T>           被比较对象类型
	 * @param o1            对象1
	 * @param o2            对象2
	 * @param isNullGreater null值是否做为最大值
	 * @return 比较结果，如果o1 &lt; o2，返回数小于0，o1==o2返回0，o1 &gt; o2 大于0
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> int compare(T o1, T o2, boolean isNullGreater) {
		if (o1 == o2) {
			return 0;
		} else if (null == o1) {// null 排在后面
			return isNullGreater ? 1 : -1;
		} else if (null == o2) {
			return isNullGreater ? -1 : 1;
		}

		if (o1 instanceof Comparable && o2 instanceof Comparable) {
			//如果bean可比较，直接比较bean
			return ((Comparable) o1).compareTo(o2);
		}

		if (o1.equals(o2)) {
			return 0;
		}

		int result = Integer.compare(o1.hashCode(), o2.hashCode());
		if (0 == result) {
			result = compare(o1.toString(), o2.toString());
		}

		return result;
	}

	/**
	 * 中文比较器
	 *
	 * @param keyExtractor 从对象中提取中文(参与比较的内容)
	 * @param <T>          对象类型
	 * @return 中文比较器
	 * @since 5.4.3
	 */
	public static <T> Comparator<T> comparingPinyin(Function<T, String> keyExtractor) {
		return comparingPinyin(keyExtractor, false);
	}

	/**
	 * 中文（拼音）比较器
	 *
	 * @param keyExtractor 从对象中提取中文(参与比较的内容)
	 * @param reverse      是否反序
	 * @param <T>          对象类型
	 * @return 中文比较器
	 * @since 5.4.3
	 */
	public static <T> Comparator<T> comparingPinyin(Function<T, String> keyExtractor, boolean reverse) {
		Objects.requireNonNull(keyExtractor);
		PinyinComparator pinyinComparator = new PinyinComparator();
		if (reverse) {
			return (o1, o2) -> pinyinComparator.compare(keyExtractor.apply(o2), keyExtractor.apply(o1));
		}
		return (o1, o2) -> pinyinComparator.compare(keyExtractor.apply(o1), keyExtractor.apply(o2));
	}

	/**
	 * 索引比较器<br>
	 * 通过keyExtractor函数，提取对象的某个属性或规则，根据提供的排序数组，完成比较<br>
	 *
	 * @param keyExtractor 从对象中提取中文(参与比较的内容)
	 * @param objs         参与排序的数组，数组的元素位置决定了对象的排序先后
	 * @param <T>          对象类型
	 * @param <U>          数组对象类型
	 * @return 索引比较器
	 * @since 5.8.0
	 */
	@SuppressWarnings("unchecked")
	public static <T, U> Comparator<T> comparingIndexed(Function<? super T, ? extends U> keyExtractor, U... objs) {
		return comparingIndexed(keyExtractor, false, objs);
	}

	/**
	 * 索引比较器<br>
	 * 通过keyExtractor函数，提取对象的某个属性或规则，根据提供的排序数组，完成比较<br>
	 *
	 * @param keyExtractor 从对象中提取排序键的函数(参与比较的内容)
	 * @param atEndIfMiss  如果不在列表中是否排在后边
	 * @param objs         参与排序的数组，数组的元素位置决定了对象的排序先后
	 * @param <T>          对象类型
	 * @param <U>          数组对象类型
	 * @return 索引比较器
	 * @since 5.8.0
	 */
	@SuppressWarnings("unchecked")
	public static <T, U> Comparator<T> comparingIndexed(Function<? super T, ? extends U> keyExtractor, boolean atEndIfMiss, U... objs) {
		Objects.requireNonNull(keyExtractor);
		IndexedComparator<U> indexedComparator = new IndexedComparator<>(atEndIfMiss, objs);
		return (o1, o2) -> indexedComparator.compare(keyExtractor.apply(o1), keyExtractor.apply(o2));
	}
}
