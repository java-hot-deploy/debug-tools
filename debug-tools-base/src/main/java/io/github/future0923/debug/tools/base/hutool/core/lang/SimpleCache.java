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
package io.github.future0923.debug.tools.base.hutool.core.lang;

import io.github.future0923.debug.tools.base.hutool.core.collection.TransIter;
import io.github.future0923.debug.tools.base.hutool.core.exceptions.ExceptionUtil;
import io.github.future0923.debug.tools.base.hutool.core.lang.func.Func0;
import io.github.future0923.debug.tools.base.hutool.core.lang.mutable.Mutable;
import io.github.future0923.debug.tools.base.hutool.core.lang.mutable.MutableObj;
import io.github.future0923.debug.tools.base.hutool.core.map.SafeConcurrentHashMap;
import io.github.future0923.debug.tools.base.hutool.core.map.WeakConcurrentMap;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

/**
 * 简单缓存，无超时实现，默认使用{@link WeakConcurrentMap}实现缓存自动清理
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Looly
 */
public class SimpleCache<K, V> implements Iterable<Map.Entry<K, V>>, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 池
	 */
	private final Map<Mutable<K>, V> rawMap;
	// 乐观读写锁
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	/**
	 * 写的时候每个key一把锁，降低锁的粒度
	 */
	protected final Map<K, Lock> keyLockMap = new SafeConcurrentHashMap<>();

	/**
	 * 构造，默认使用{@link WeakHashMap}实现缓存自动清理
	 */
	public SimpleCache() {
		this(new WeakConcurrentMap<>());
	}

	/**
	 * 构造
	 * <p>
	 * 通过自定义Map初始化，可以自定义缓存实现。<br>
	 * 比如使用{@link WeakHashMap}则会自动清理key，使用HashMap则不会清理<br>
	 * 同时，传入的Map对象也可以自带初始化的键值对，防止在get时创建
	 * </p>
	 *
	 * @param initMap 初始Map，用于定义Map类型
	 */
	public SimpleCache(Map<Mutable<K>, V> initMap) {
		this.rawMap = initMap;
	}

	/**
	 * 从缓存池中查找值
	 *
	 * @param key 键
	 * @return 值
	 */
	public V get(K key) {
		lock.readLock().lock();
		try {
			return rawMap.get(MutableObj.of(key));
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * 从缓存中获得对象，当对象不在缓存中或已经过期返回Func0回调产生的对象
	 *
	 * @param key      键
	 * @param supplier 如果不存在回调方法，用于生产值对象
	 * @return 值对象
	 */
	public V get(K key, Func0<V> supplier) {
		return get(key, null, supplier);
	}

	/**
	 * 从缓存中获得对象，当对象不在缓存中或已经过期返回Func0回调产生的对象
	 *
	 * @param key            键
	 * @param validPredicate 检查结果对象是否可用，如是否断开连接等
	 * @param supplier       如果不存在回调方法或结果不可用，用于生产值对象
	 * @return 值对象
	 * @since 5.7.9
	 */
	public V get(K key, Predicate<V> validPredicate, Func0<V> supplier) {
		V v = get(key);
		if((null != validPredicate && null != v && false == validPredicate.test(v))){
			v = null;
		}
		if (null == v && null != supplier) {
			//每个key单独获取一把锁，降低锁的粒度提高并发能力，see pr#1385@Github
			final Lock keyLock = keyLockMap.computeIfAbsent(key, k -> new ReentrantLock());
			keyLock.lock();
			try {
				// 双重检查，防止在竞争锁的过程中已经有其它线程写入
				v = get(key);
				if (null == v || (null != validPredicate && false == validPredicate.test(v))) {
					try {
						v = supplier.call();
					} catch (Exception e) {
						throw ExceptionUtil.wrapRuntime(e);
					}
					put(key, v);
				}
			} finally {
				keyLock.unlock();
				keyLockMap.remove(key);
			}
		}

		return v;
	}

	/**
	 * 放入缓存
	 *
	 * @param key   键
	 * @param value 值
	 * @return 值
	 */
	public V put(K key, V value) {
		// 独占写锁
		lock.writeLock().lock();
		try {
			rawMap.put(MutableObj.of(key), value);
		} finally {
			lock.writeLock().unlock();
		}
		return value;
	}

	/**
	 * 移除缓存
	 *
	 * @param key 键
	 * @return 移除的值
	 */
	public V remove(K key) {
		// 独占写锁
		lock.writeLock().lock();
		try {
			return rawMap.remove(MutableObj.of(key));
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * 清空缓存池
	 */
	public void clear() {
		// 独占写锁
		lock.writeLock().lock();
		try {
			this.rawMap.clear();
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		return new TransIter<>(this.rawMap.entrySet().iterator(), (entry)-> new Map.Entry<K, V>() {
			@Override
			public K getKey() {
				return entry.getKey().get();
			}

			@Override
			public V getValue() {
				return entry.getValue();
			}

			@Override
			public V setValue(V value) {
				return entry.setValue(value);
			}
		});
	}
}
