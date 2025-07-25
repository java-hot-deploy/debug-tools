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
package io.github.future0923.debug.tools.base.hutool.core.bean.copier;

import io.github.future0923.debug.tools.base.hutool.core.bean.PropDesc;
import io.github.future0923.debug.tools.base.hutool.core.collection.CollUtil;
import io.github.future0923.debug.tools.base.hutool.core.convert.Convert;
import io.github.future0923.debug.tools.base.hutool.core.convert.TypeConverter;
import io.github.future0923.debug.tools.base.hutool.core.convert.impl.DateConverter;
import io.github.future0923.debug.tools.base.hutool.core.lang.Editor;
import io.github.future0923.debug.tools.base.hutool.core.lang.func.Func1;
import io.github.future0923.debug.tools.base.hutool.core.lang.func.LambdaUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ArrayUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ClassUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ObjectUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * 属性拷贝选项<br>
 * 包括：<br>
 * 1、限制的类或接口，必须为目标对象的实现接口或父类，用于限制拷贝的属性，例如一个类我只想复制其父类的一些属性，就可以将editable设置为父类<br>
 * 2、是否忽略空值，当源对象的值为null时，true: 忽略而不注入此值，false: 注入null<br>
 * 3、忽略的属性列表，设置一个属性列表，不拷贝这些属性值<br>
 *
 * @author Looly
 */
public class CopyOptions implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 限制的类或接口，必须为目标对象的实现接口或父类，用于限制拷贝的属性，例如一个类我只想复制其父类的一些属性，就可以将editable设置为父类<br>
	 * 如果目标对象是Map，源对象是Bean，则作用于源对象上
	 */
	protected Class<?> editable;
	/**
	 * 是否忽略空值，当源对象的值为null时，true: 忽略而不注入此值，false: 注入null
	 */
	protected boolean ignoreNullValue;
	/**
	 * 属性过滤器，断言通过的属性才会被复制<br>
	 * 断言参数中Field为源对象的字段对象,如果源对象为Map，使用目标对象，Object为源对象的对应值
	 */
	private BiPredicate<Field, Object> propertiesFilter;
	/**
	 * 是否忽略字段注入错误
	 */
	protected boolean ignoreError;
	/**
	 * 是否忽略字段大小写
	 */
	protected boolean ignoreCase;
	/**
	 * 字段属性编辑器，用于自定义属性转换规则，例如驼峰转下划线等<br>
	 * 规则为，{@link Editor#edit(Object)}属性为源对象的字段名称或key，返回值为目标对象的字段名称或key
	 */
	private Editor<String> fieldNameEditor;
	/**
	 * 字段属性值编辑器，用于自定义属性值转换规则，例如null转""等
	 */
	protected BiFunction<String, Object, Object> fieldValueEditor;
	/**
	 * 是否支持transient关键字修饰和@Transient注解，如果支持，被修饰的字段或方法对应的字段将被忽略。
	 */
	protected boolean transientSupport = true;
	/**
	 * 是否覆盖目标值，如果不覆盖，会先读取目标对象的值，非{@code null}则写，否则忽略。如果覆盖，则不判断直接写
	 */
	protected boolean override = true;

	/**
	 * 是否自动转换为驼峰方式
	 */
	protected boolean autoTransCamelCase = true;

	/**
	 * 源对象和目标对象都是 {@code Map} 时, 需要忽略的源对象 {@code Map} key
	 */
	private Set<String> ignoreKeySet;

	/**
	 * 自定义类型转换器，默认使用全局万能转换器转换
	 */
	protected TypeConverter converter = (type, value) -> {
		if (null == value) {
			return null;
		}

		// 快速处理简单值类型的转换
		if (type instanceof Class){
			Class<?> targetType = (Class<?>) type;
			if (ClassUtil.isSimpleValueType(targetType) && targetType.isInstance(value)) {
				return targetType.cast(value);
			}
		}

		if (value instanceof IJSONTypeConverter) {
			return ((IJSONTypeConverter) value).toBean(ObjectUtil.defaultIfNull(type, Object.class));
		}

		return Convert.convertWithCheck(type, value, null, ignoreError);
	};

	/**
	 * 在Bean转换时，如果源是String，目标对象是Date或LocalDateTime，则可自定义转换格式
	 */
	private String formatIfDate;

	//region create

	/**
	 * 创建拷贝选项
	 *
	 * @return 拷贝选项
	 */
	public static CopyOptions create() {
		return new CopyOptions();
	}

	/**
	 * 创建拷贝选项
	 *
	 * @param editable         限制的类或接口，必须为目标对象的实现接口或父类，用于限制拷贝的属性
	 * @param ignoreNullValue  是否忽略空值，当源对象的值为null时，true: 忽略而不注入此值，false: 注入null
	 * @param ignoreProperties 忽略的属性列表，设置一个属性列表，不拷贝这些属性值
	 * @return 拷贝选项
	 */
	public static CopyOptions create(Class<?> editable, boolean ignoreNullValue, String... ignoreProperties) {
		return new CopyOptions(editable, ignoreNullValue, ignoreProperties);
	}
	//endregion

	/**
	 * 构造拷贝选项
	 */
	public CopyOptions() {
	}

	/**
	 * 构造拷贝选项
	 *
	 * @param editable         限制的类或接口，必须为目标对象的实现接口或父类，用于限制拷贝的属性
	 * @param ignoreNullValue  是否忽略空值，当源对象的值为null时，true: 忽略而不注入此值，false: 注入null
	 * @param ignoreProperties 忽略的目标对象中属性列表，设置一个属性列表，不拷贝这些属性值
	 */
	public CopyOptions(Class<?> editable, boolean ignoreNullValue, String... ignoreProperties) {
		this.propertiesFilter = (f, v) -> true;
		this.editable = editable;
		this.ignoreNullValue = ignoreNullValue;
		this.setIgnoreProperties(ignoreProperties);
	}

	/**
	 * 设置限制的类或接口，必须为目标对象的实现接口或父类，用于限制拷贝的属性
	 *
	 * @param editable 限制的类或接口
	 * @return CopyOptions
	 */
	public CopyOptions setEditable(Class<?> editable) {
		this.editable = editable;
		return this;
	}

	/**
	 * 设置是否忽略空值，当源对象的值为null时，true: 忽略而不注入此值，false: 注入null
	 *
	 * @param ignoreNullVall 是否忽略空值，当源对象的值为null时，true: 忽略而不注入此值，false: 注入null
	 * @return CopyOptions
	 */
	public CopyOptions setIgnoreNullValue(boolean ignoreNullVall) {
		this.ignoreNullValue = ignoreNullVall;
		return this;
	}

	/**
	 * 设置忽略空值，当源对象的值为null时，忽略而不注入此值
	 *
	 * @return CopyOptions
	 * @since 4.5.7
	 */
	public CopyOptions ignoreNullValue() {
		return setIgnoreNullValue(true);
	}

	/**
	 * 属性过滤器，断言通过的属性才会被复制<br>
	 * {@link BiPredicate#test(Object, Object)}返回{@code true}则属性通过，{@code false}不通过，抛弃之
	 *
	 * @param propertiesFilter 属性过滤器
	 * @return CopyOptions
	 */
	public CopyOptions setPropertiesFilter(BiPredicate<Field, Object> propertiesFilter) {
		this.propertiesFilter = propertiesFilter;
		return this;
	}

	/**
	 * 设置忽略的目标对象中属性列表，设置一个属性列表，不拷贝这些属性值
	 *
	 * @param ignoreProperties 忽略的目标对象中属性列表，设置一个属性列表，不拷贝这些属性值
	 * @return CopyOptions
	 */
	public CopyOptions setIgnoreProperties(String... ignoreProperties) {
		this.ignoreKeySet = CollUtil.newHashSet(ignoreProperties);
		return this;
	}

	/**
	 * 设置忽略的目标对象中属性列表，设置一个属性列表，不拷贝这些属性值，Lambda方式
	 *
	 * @param <P>   参数类型
	 * @param <R>   返回值类型
	 * @param funcs 忽略的目标对象中属性列表，设置一个属性列表，不拷贝这些属性值
	 * @return CopyOptions
	 * @since 5.8.0
	 */
	@SuppressWarnings("unchecked")
	public <P, R> CopyOptions setIgnoreProperties(Func1<P, R>... funcs) {
		this.ignoreKeySet = ArrayUtil.mapToSet(funcs, LambdaUtil::getFieldName);
		return this;
	}

	/**
	 * 设置是否忽略字段的注入错误
	 *
	 * @param ignoreError 是否忽略注入错误
	 * @return CopyOptions
	 */
	public CopyOptions setIgnoreError(boolean ignoreError) {
		this.ignoreError = ignoreError;
		return this;
	}

	/**
	 * 设置忽略字段的注入错误
	 *
	 * @return CopyOptions
	 * @since 4.5.7
	 */
	public CopyOptions ignoreError() {
		return setIgnoreError(true);
	}

	/**
	 * 设置是否忽略字段的大小写
	 *
	 * @param ignoreCase 是否忽略大小写
	 * @return CopyOptions
	 */
	public CopyOptions setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
		return this;
	}

	/**
	 * 设置忽略字段的大小写
	 *
	 * @return CopyOptions
	 * @since 4.5.7
	 */
	public CopyOptions ignoreCase() {
		return setIgnoreCase(true);
	}

	/**
	 * 设置拷贝属性的字段映射，用于不同的属性之前拷贝做对应表用<br>
	 * 需要注意的是，当使用ValueProvider作为数据提供者时，这个映射是相反的，即fieldMapping中key为目标Bean的名称，而value是提供者中的key
	 *
	 * @param fieldMapping 拷贝属性的字段映射，用于不同的属性之前拷贝做对应表用
	 * @return CopyOptions
	 */
	public CopyOptions setFieldMapping(Map<String, String> fieldMapping) {
		return setFieldNameEditor((key -> fieldMapping.getOrDefault(key, key)));
	}

	/**
	 * 设置字段属性编辑器，用于自定义属性转换规则，例如驼峰转下划线等<br>
	 * 此转换器只针对源端的字段做转换，请确认转换后与目标端字段一致<br>
	 * 当转换后的字段名为null时忽略这个字段<br>
	 * 需要注意的是，当使用ValueProvider作为数据提供者时，这个映射是相反的，即fieldMapping中key为目标Bean的名称，而value是提供者中的key
	 *
	 * @param fieldNameEditor 字段属性编辑器，用于自定义属性转换规则，例如驼峰转下划线等
	 * @return CopyOptions
	 * @since 5.4.2
	 */
	public CopyOptions setFieldNameEditor(Editor<String> fieldNameEditor) {
		this.fieldNameEditor = fieldNameEditor;
		return this;
	}

	/**
	 * 设置字段属性值编辑器，用于自定义属性值转换规则，例如null转""等<br>
	 *
	 * @param fieldValueEditor 字段属性值编辑器，用于自定义属性值转换规则，例如null转""等
	 * @return CopyOptions
	 * @since 5.7.15
	 */
	public CopyOptions setFieldValueEditor(BiFunction<String, Object, Object> fieldValueEditor) {
		this.fieldValueEditor = fieldValueEditor;
		return this;
	}

	/**
	 * 编辑字段值
	 *
	 * @param fieldName  字段名
	 * @param fieldValue 字段值
	 * @return 编辑后的字段值
	 * @since 5.7.15
	 */
	protected Object editFieldValue(String fieldName, Object fieldValue) {
		return (null != this.fieldValueEditor) ?
			this.fieldValueEditor.apply(fieldName, fieldValue) : fieldValue;
	}

	/**
	 * 设置是否支持transient关键字修饰和@Transient注解，如果支持，被修饰的字段或方法对应的字段将被忽略。
	 *
	 * @param transientSupport 是否支持
	 * @return this
	 * @since 5.4.2
	 */
	public CopyOptions setTransientSupport(boolean transientSupport) {
		this.transientSupport = transientSupport;
		return this;
	}

	/**
	 * 设置是否覆盖目标值，如果不覆盖，会先读取目标对象的值，为{@code null}则写，否则忽略。如果覆盖，则不判断直接写
	 *
	 * @param override 是否覆盖目标值
	 * @return this
	 * @since 5.7.17
	 */
	public CopyOptions setOverride(boolean override) {
		this.override = override;
		return this;
	}

	/**
	 * 设置是否自动转换为驼峰方式<br>
	 * 一般用于map转bean和bean转bean出现非驼峰格式时，在尝试转换失败的情况下，是否二次检查转为驼峰匹配<br>
	 * 此设置用于解决Bean和Map转换中的匹配问题而设置，并不是一个强制参数。
	 * <ol>
	 *     <li>当map转bean时，如果map中是下划线等非驼峰模式，自动匹配对应的驼峰字段，避免出现字段不拷贝问题。</li>
	 *     <li>当bean转bean时，由于字段命名不规范，使用了非驼峰方式，增加兼容性。</li>
	 * </ol>
	 * <p>
	 * 但是bean转Map和map转map时，没有使用这个参数，是因为没有匹配的必要，转map不存在无法匹配到的问题，因此此参数无效。
	 *
	 * @param autoTransCamelCase 是否自动转换为驼峰方式
	 * @return this
	 * @since 5.8.25
	 */
	public CopyOptions setAutoTransCamelCase(final boolean autoTransCamelCase) {
		this.autoTransCamelCase = autoTransCamelCase;
		return this;
	}

	/**
	 * 设置自定义类型转换器，默认使用全局万能转换器转换。
	 *
	 * @param converter 转换器
	 * @return this
	 * @since 5.8.0
	 */
	public CopyOptions setConverter(TypeConverter converter) {
		this.converter = converter;
		return this;
	}

	/**
	 * 获取日期格式，用于日期转字符串，默认为{@code null}
	 * @return 日期格式
	 */
	public String getFormatIfDate() {
		return formatIfDate;
	}

	/**
	 * 设置日期格式，用于日期转字符串，默认为{@code null}
	 * @param formatIfDate 日期格式
	 * @return this
	 */
	public CopyOptions setFormatIfDate(String formatIfDate) {
		this.formatIfDate = formatIfDate;
		return this;
	}

	/**
	 * 使用自定义转换器转换字段值<br>
	 * 如果自定义转换器为{@code null}，则返回原值。
	 *
	 * @param targetType 目标类型
	 * @param fieldValue 字段值
	 * @return 编辑后的字段值
	 * @since 5.8.0
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected Object convertField(Type targetType, Object fieldValue) {
		if((targetType instanceof Class && Date.class.isAssignableFrom((Class<?>) targetType)) && null != this.formatIfDate){
			return new DateConverter((Class) targetType, this.formatIfDate).convert(fieldValue, null);
		}

		return (null != this.converter) ?
			this.converter.convert(targetType, fieldValue) : fieldValue;
	}

	/**
	 * 转换字段名为编辑后的字段名
	 *
	 * @param fieldName 字段名
	 * @return 编辑后的字段名
	 * @since 5.4.2
	 */
	protected String editFieldName(String fieldName) {
		return (null != this.fieldNameEditor) ? this.fieldNameEditor.edit(fieldName) : fieldName;
	}

	/**
	 * 测试是否保留字段，{@code true}保留，{@code false}不保留
	 *
	 * @param field 字段
	 * @param value 值
	 * @return 是否保留
	 */
	protected boolean testPropertyFilter(Field field, Object value) {
		return null == this.propertiesFilter || this.propertiesFilter.test(field, value);
	}

	/**
	 * 测试是否保留key, {@code true} 不保留， {@code false} 保留
	 *
	 * @param key {@link Map} key
	 * @return 是否保留
	 */
	protected boolean testKeyFilter(Object key) {
		if (CollUtil.isEmpty(this.ignoreKeySet)) {
			return true;
		}

		if (ignoreCase) {
			// 忽略大小写时要遍历检查
			for (final String ignoreKey : this.ignoreKeySet) {
				if (StrUtil.equalsIgnoreCase(key.toString(), ignoreKey)) {
					return false;
				}
			}
		}

		return false == this.ignoreKeySet.contains(key);
	}

	/**
	 * 查找Map对应Bean的名称<br>
	 * 尝试原名称、转驼峰名称、isXxx去掉is的名称
	 *
	 * @param targetPropDescMap 目标bean的属性描述Map
	 * @param sKeyStr           键或字段名
	 * @return {@link PropDesc}
	 */
	protected PropDesc findPropDesc(final Map<String, PropDesc> targetPropDescMap, final String sKeyStr) {
		PropDesc propDesc = targetPropDescMap.get(sKeyStr);
		// 转驼峰尝试查找
		if (null == propDesc && this.autoTransCamelCase) {
			final String camelCaseKey = StrUtil.toCamelCase(sKeyStr);
			if (!StrUtil.equals(sKeyStr, camelCaseKey)) {
				// 只有转换为驼峰后与原key不同才重复查询，相同说明本身就是驼峰，不需要二次查询
				propDesc = targetPropDescMap.get(camelCaseKey);
			}
		}
		return propDesc;
	}
}
