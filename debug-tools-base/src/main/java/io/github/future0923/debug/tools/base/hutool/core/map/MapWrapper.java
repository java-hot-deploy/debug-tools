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
package io.github.future0923.debug.tools.base.hutool.core.map;

import io.github.future0923.debug.tools.base.hutool.core.util.ObjectUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Map包装类，通过包装一个已有Map实现特定功能。例如自定义Key的规则或Value规则
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author looly
 * @since 4.3.3
 */
public class MapWrapper<K, V> implements Map<K, V>, Iterable<Map.Entry<K, V>>, Serializable, Cloneable {
	private static final long serialVersionUID = -7524578042008586382L;

	/**
	 * 默认增长因子
	 */
	protected static final float DEFAULT_LOAD_FACTOR = 0.75f;
	/**
	 * 默认初始大小
	 */
	protected static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

	private Map<K, V> raw;

	/**
	 * 构造<br>
	 * 通过传入一个Map从而确定Map的类型，子类需创建一个空的Map，而非传入一个已有Map，否则值可能会被修改
	 *
	 * @param mapFactory 空Map创建工厂
	 * @since 5.8.0
	 */
	public MapWrapper(Supplier<Map<K, V>> mapFactory) {
		this(mapFactory.get());
	}

	/**
	 * 构造
	 *
	 * @param raw 被包装的Map
	 */
	public MapWrapper(Map<K, V> raw) {
		this.raw = raw;
	}

	/**
	 * 获取原始的Map
	 *
	 * @return Map
	 */
	public Map<K, V> getRaw() {
		return this.raw;
	}

	@Override
	public int size() {
		return raw.size();
	}

	@Override
	public boolean isEmpty() {
		return raw.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return raw.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return raw.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return raw.get(key);
	}

	@Override
	public V put(K key, V value) {
		return raw.put(key, value);
	}

	@Override
	public V remove(Object key) {
		return raw.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		raw.putAll(m);
	}

	@Override
	public void clear() {
		raw.clear();
	}

	@Override
	public Collection<V> values() {
		return raw.values();
	}

	@Override
	public Set<K> keySet() {
		return raw.keySet();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return raw.entrySet();
	}

	@Override
	public Iterator<Entry<K, V>> iterator() {
		return this.entrySet().iterator();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MapWrapper<?, ?> that = (MapWrapper<?, ?>) o;
		return Objects.equals(raw, that.raw);
	}

	@Override
	public int hashCode() {
		return Objects.hash(raw);
	}

	@Override
	public String toString() {
		return raw.toString();
	}


	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		raw.forEach(action);
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		raw.replaceAll(function);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return raw.putIfAbsent(key, value);
	}

	@Override
	public boolean remove(Object key, Object value) {
		return raw.remove(key, value);
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return raw.replace(key, oldValue, newValue);
	}

	@Override
	public V replace(K key, V value) {
		return raw.replace(key, value);
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return raw.computeIfAbsent(key, mappingFunction);
	}

	// 重写默认方法的意义在于，如果被包装的Map自定义了这些默认方法，包装类就可以保持这些行为的一致性
	//---------------------------------------------------------------------------- Override default methods start
	@Override
	public V getOrDefault(Object key, V defaultValue) {
		return raw.getOrDefault(key, defaultValue);
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return raw.computeIfPresent(key, remappingFunction);
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return raw.compute(key, remappingFunction);
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return raw.merge(key, value, remappingFunction);
	}

	@Override
	public MapWrapper<K, V> clone() throws CloneNotSupportedException {
		@SuppressWarnings("unchecked") final MapWrapper<K, V> clone = (MapWrapper<K, V>) super.clone();
		clone.raw = ObjectUtil.clone(raw);
		return clone;
	}

	//---------------------------------------------------------------------------- Override default methods end

	// region 序列化与反序列化重写
	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeObject(this.raw);
	}

	@SuppressWarnings("unchecked")
	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		raw = (Map<K, V>) in.readObject();
	}
	// endregion
}
