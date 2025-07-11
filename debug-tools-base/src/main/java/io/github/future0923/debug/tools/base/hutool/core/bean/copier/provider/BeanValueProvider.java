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
package io.github.future0923.debug.tools.base.hutool.core.bean.copier.provider;

import io.github.future0923.debug.tools.base.hutool.core.bean.BeanUtil;
import io.github.future0923.debug.tools.base.hutool.core.bean.PropDesc;
import io.github.future0923.debug.tools.base.hutool.core.bean.copier.ValueProvider;
import io.github.future0923.debug.tools.base.hutool.core.lang.Editor;
import io.github.future0923.debug.tools.base.hutool.core.map.FuncKeyMap;
import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Bean的值提供者
 *
 * @author looly
 */
public class BeanValueProvider implements ValueProvider<String> {

	private final Object source;
	private final boolean ignoreError;
	final Map<String, PropDesc> sourcePdMap;

	/**
	 * 构造
	 *
	 * @param bean        Bean
	 * @param ignoreCase  是否忽略字段大小写
	 * @param ignoreError 是否忽略字段值读取错误
	 */
	public BeanValueProvider(Object bean, boolean ignoreCase, boolean ignoreError) {
		this(bean, ignoreCase, ignoreError, null);
	}

	/**
	 * 构造
	 *
	 * @param bean        Bean
	 * @param ignoreCase  是否忽略字段大小写
	 * @param ignoreError 是否忽略字段值读取错误
	 * @param keyEditor   键编辑器
	 */
	public BeanValueProvider(Object bean, boolean ignoreCase, boolean ignoreError, Editor<String> keyEditor) {
		this.source = bean;
		this.ignoreError = ignoreError;
		final Map<String, PropDesc> sourcePdMap = BeanUtil.getBeanDesc(source.getClass()).getPropMap(ignoreCase);
		// issue#2202@Github
		// 如果用户定义了键编辑器，则提供的map中的数据必须全部转换key
		// issue#I5VRHW@Gitee 使Function可以被序列化
		this.sourcePdMap = new FuncKeyMap<>(new HashMap<>(sourcePdMap.size(), 1), (Function<Object, String> & Serializable)(key) -> {
			if (ignoreCase && key instanceof CharSequence) {
				key = key.toString().toLowerCase();
			}
			if (null != keyEditor) {
				key = keyEditor.edit(key.toString());
			}
			return key.toString();
		});
		this.sourcePdMap.putAll(sourcePdMap);
	}

	@Override
	public Object value(String key, Type valueType) {
		final PropDesc sourcePd = getPropDesc(key, valueType);

		Object result = null;
		if (null != sourcePd) {
			result = sourcePd.getValue(this.source, valueType, this.ignoreError);
		}
		return result;
	}

	@Override
	public boolean containsKey(String key) {
		final PropDesc sourcePd = getPropDesc(key, null);

		// 字段描述不存在或忽略读的情况下，表示不存在
		return null != sourcePd && sourcePd.isReadable(false);
	}

	/**
	 * 获得属性描述
	 *
	 * @param key       字段名
	 * @param valueType 值类型，用于判断是否为Boolean，可以为null
	 * @return 属性描述
	 */
	private PropDesc getPropDesc(String key, Type valueType) {
		PropDesc sourcePd = sourcePdMap.get(key);
		if (null == sourcePd && (null == valueType || Boolean.class == valueType || boolean.class == valueType)) {
			//boolean类型字段字段名支持两种方式
			sourcePd = sourcePdMap.get(StrUtil.upperFirstAndAddPre(key, "is"));
		}

		return sourcePd;
	}
}
