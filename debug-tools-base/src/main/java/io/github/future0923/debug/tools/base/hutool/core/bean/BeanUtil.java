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
package io.github.future0923.debug.tools.base.hutool.core.bean;

import io.github.future0923.debug.tools.base.hutool.core.bean.copier.BeanCopier;
import io.github.future0923.debug.tools.base.hutool.core.bean.copier.CopyOptions;
import io.github.future0923.debug.tools.base.hutool.core.bean.copier.ValueProvider;
import io.github.future0923.debug.tools.base.hutool.core.collection.CollUtil;
import io.github.future0923.debug.tools.base.hutool.core.collection.ListUtil;
import io.github.future0923.debug.tools.base.hutool.core.convert.Convert;
import io.github.future0923.debug.tools.base.hutool.core.lang.Dict;
import io.github.future0923.debug.tools.base.hutool.core.lang.Editor;
import io.github.future0923.debug.tools.base.hutool.core.map.CaseInsensitiveMap;
import io.github.future0923.debug.tools.base.hutool.core.map.MapUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ArrayUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ClassUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ModifierUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ObjectUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ReflectUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Bean工具类
 *
 * <p>
 * 把一个拥有对属性进行set和get方法的类，我们就可以称之为JavaBean。
 * </p>
 *
 * @author Looly
 * @since 3.1.2
 */
public class BeanUtil {

	/**
	 * 判断是否为可读的Bean对象，判定方法是：
	 *
	 * <pre>
	 *     1、是否存在只有无参数的getXXX方法或者isXXX方法
	 *     2、是否存在public类型的字段
	 * </pre>
	 *
	 * @param clazz 待测试类
	 * @return 是否为可读的Bean对象
	 * @see #hasGetter(Class)
	 * @see #hasPublicField(Class)
	 */
	public static boolean isReadableBean(Class<?> clazz) {
		return hasGetter(clazz) || hasPublicField(clazz);
	}

	/**
	 * 判断是否为Bean对象，判定方法是：
	 *
	 * <pre>
	 *     1、是否存在只有一个参数的setXXX方法
	 *     2、是否存在public类型的字段
	 * </pre>
	 *
	 * @param clazz 待测试类
	 * @return 是否为Bean对象
	 * @see #hasSetter(Class)
	 * @see #hasPublicField(Class)
	 */
	public static boolean isBean(Class<?> clazz) {
		return hasSetter(clazz) || hasPublicField(clazz);
	}

	/**
	 * 判断是否有Setter方法<br>
	 * 判定方法是否存在只有一个参数的setXXX方法
	 *
	 * @param clazz 待测试类
	 * @return 是否为Bean对象
	 * @since 4.2.2
	 */
	public static boolean hasSetter(Class<?> clazz) {
		if(null == clazz){
			return false;
		}
		// issue#I9VTZG，排除定义setXXX的预定义类
		if(Dict.class == clazz){
			return false;
		}

		if (ClassUtil.isNormalClass(clazz)) {
			for (Method method : clazz.getMethods()) {
				if (method.getParameterCount() == 1 && method.getName().startsWith("set")) {
					// 检测包含标准的setXXX方法即视为标准的JavaBean
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 判断是否为Bean对象<br>
	 * 判定方法是否存在只有无参数的getXXX方法或者isXXX方法
	 *
	 * @param clazz 待测试类
	 * @return 是否为Bean对象
	 * @since 4.2.2
	 */
	public static boolean hasGetter(Class<?> clazz) {
		if (ClassUtil.isNormalClass(clazz)) {
			for (Method method : clazz.getMethods()) {
				if (method.getParameterCount() == 0) {
					final String name = method.getName();
					if (name.startsWith("get") || name.startsWith("is")) {
						if (false == "getClass".equals(name)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * 指定类中是否有public类型字段(static字段除外)
	 *
	 * @param clazz 待测试类
	 * @return 是否有public类型字段
	 * @since 5.1.0
	 */
	public static boolean hasPublicField(Class<?> clazz) {
		if(null == clazz){
			return false;
		}
		if (ClassUtil.isNormalClass(clazz)) {
			for (Field field : clazz.getFields()) {
				if (ModifierUtil.isPublic(field) && false == ModifierUtil.isStatic(field)) {
					//非static的public字段
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 创建动态Bean
	 *
	 * @param bean 普通Bean或Map
	 * @return {@link DynaBean}
	 * @since 3.0.7
	 */
	public static DynaBean createDynaBean(Object bean) {
		return new DynaBean(bean);
	}

	/**
	 * 查找类型转换器 {@link PropertyEditor}
	 *
	 * @param type 需要转换的目标类型
	 * @return {@link PropertyEditor}
	 */
	public static PropertyEditor findEditor(Class<?> type) {
		return PropertyEditorManager.findEditor(type);
	}

	/**
	 * 获取{@link BeanDesc} Bean描述信息
	 *
	 * @param clazz Bean类
	 * @return {@link BeanDesc}
	 * @since 3.1.2
	 */
	public static BeanDesc getBeanDesc(Class<?> clazz) {
		return BeanDescCache.INSTANCE.getBeanDesc(clazz, () -> new BeanDesc(clazz));
	}

	/**
	 * 遍历Bean的属性
	 *
	 * @param clazz  Bean类
	 * @param action 每个元素的处理类
	 * @since 5.4.2
	 */
	public static void descForEach(Class<?> clazz, Consumer<? super PropDesc> action) {
		getBeanDesc(clazz).getProps().forEach(action);
	}

	// --------------------------------------------------------------------------------------------------------- PropertyDescriptor

	/**
	 * 获得Bean字段描述数组
	 *
	 * @param clazz Bean类
	 * @return 字段描述数组
	 * @throws BeanException 获取属性异常
	 */
	public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) throws BeanException {
		BeanInfo beanInfo;
		try {
			beanInfo = Introspector.getBeanInfo(clazz);
		} catch (IntrospectionException e) {
			throw new BeanException(e);
		}
		return ArrayUtil.filter(beanInfo.getPropertyDescriptors(), t -> {
			// 过滤掉getClass方法
			return false == "class".equals(t.getName());
		});
	}

	/**
	 * 获得字段名和字段描述Map，获得的结果会缓存在 {@link BeanInfoCache}中
	 *
	 * @param clazz      Bean类
	 * @param ignoreCase 是否忽略大小写
	 * @return 字段名和字段描述Map
	 * @throws BeanException 获取属性异常
	 */
	public static Map<String, PropertyDescriptor> getPropertyDescriptorMap(Class<?> clazz, boolean ignoreCase) throws BeanException {
		return BeanInfoCache.INSTANCE.getPropertyDescriptorMap(clazz, ignoreCase, () -> internalGetPropertyDescriptorMap(clazz, ignoreCase));
	}

	/**
	 * 获得字段名和字段描述Map。内部使用，直接获取Bean类的PropertyDescriptor
	 *
	 * @param clazz      Bean类
	 * @param ignoreCase 是否忽略大小写
	 * @return 字段名和字段描述Map
	 * @throws BeanException 获取属性异常
	 */
	private static Map<String, PropertyDescriptor> internalGetPropertyDescriptorMap(Class<?> clazz, boolean ignoreCase) throws BeanException {
		final PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(clazz);
		final Map<String, PropertyDescriptor> map = ignoreCase ? new CaseInsensitiveMap<>(propertyDescriptors.length, 1f)
				: new HashMap<>(propertyDescriptors.length, 1);

		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			map.put(propertyDescriptor.getName(), propertyDescriptor);
		}
		return map;
	}

	/**
	 * 获得Bean类属性描述，大小写敏感
	 *
	 * @param clazz     Bean类
	 * @param fieldName 字段名
	 * @return PropertyDescriptor
	 * @throws BeanException 获取属性异常
	 */
	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, final String fieldName) throws BeanException {
		return getPropertyDescriptor(clazz, fieldName, false);
	}

	/**
	 * 获得Bean类属性描述
	 *
	 * @param clazz      Bean类
	 * @param fieldName  字段名
	 * @param ignoreCase 是否忽略大小写
	 * @return PropertyDescriptor
	 * @throws BeanException 获取属性异常
	 */
	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, final String fieldName, boolean ignoreCase) throws BeanException {
		final Map<String, PropertyDescriptor> map = getPropertyDescriptorMap(clazz, ignoreCase);
		return (null == map) ? null : map.get(fieldName);
	}

	/**
	 * 获得字段值，通过反射直接获得字段值，并不调用getXXX方法<br>
	 * 对象同样支持Map类型，fieldNameOrIndex即为key
	 *
	 * <ul>
	 *     <li>Map: fieldNameOrIndex需为key，获取对应value</li>
	 *     <li>Collection: fieldNameOrIndex当为数字，返回index对应值，非数字遍历集合返回子bean对应name值</li>
	 *     <li>Array: fieldNameOrIndex当为数字，返回index对应值，非数字遍历数组返回子bean对应name值</li>
	 * </ul>
	 *
	 * @param bean             Bean对象
	 * @param fieldNameOrIndex 字段名或序号，序号支持负数
	 * @return 字段值
	 */
	public static Object getFieldValue(Object bean, String fieldNameOrIndex) {
		if (null == bean || null == fieldNameOrIndex) {
			return null;
		}

		if (bean instanceof Map) {
			return ((Map<?, ?>) bean).get(fieldNameOrIndex);
		} else if (bean instanceof Collection) {
			try {
				return CollUtil.get((Collection<?>) bean, Integer.parseInt(fieldNameOrIndex));
			} catch (NumberFormatException e) {
				// 非数字，see pr#254@Gitee
				return CollUtil.map((Collection<?>) bean, (beanEle) -> getFieldValue(beanEle, fieldNameOrIndex), false);
			}
		} else if (ArrayUtil.isArray(bean)) {
			try {
				return ArrayUtil.get(bean, Integer.parseInt(fieldNameOrIndex));
			} catch (NumberFormatException e) {
				// 非数字，see pr#254@Gitee
				return ArrayUtil.map(bean, Object.class, (beanEle) -> getFieldValue(beanEle, fieldNameOrIndex));
			}
		} else {// 普通Bean对象
			return ReflectUtil.getFieldValue(bean, fieldNameOrIndex);
		}
	}

	/**
	 * 设置字段值，通过反射设置字段值，并不调用setXXX方法<br>
	 * 对象同样支持Map类型，fieldNameOrIndex即为key，支持：
	 * <ul>
	 *     <li>Map</li>
	 *     <li>List</li>
	 *     <li>Bean</li>
	 * </ul>
	 *
	 * @param bean             Bean
	 * @param fieldNameOrIndex 字段名或序号，序号支持负数
	 * @param value            值
	 * @return bean，当为数组时，返回一个新的数组
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static Object setFieldValue(Object bean, String fieldNameOrIndex, Object value) {
		if (bean instanceof Map) {
			((Map) bean).put(fieldNameOrIndex, value);
		} else if (bean instanceof List) {
			ListUtil.setOrPadding((List) bean, Convert.toInt(fieldNameOrIndex), value);
		} else if (ArrayUtil.isArray(bean)) {
			// issue#3008，追加产生新数组，此处返回新数组
			return ArrayUtil.setOrAppend(bean, Convert.toInt(fieldNameOrIndex), value);
		} else {
			// 普通Bean对象
			ReflectUtil.setFieldValue(bean, fieldNameOrIndex, value);
		}
		return bean;
	}

	/**
	 * 解析Bean中的属性值
	 *
	 * @param <T>        属性值类型
	 * @param bean       Bean对象，支持Map、List、Collection、Array
	 * @param expression 表达式，例如：person.friend[5].name
	 * @return Bean属性值，bean为{@code null}或者express为空，返回{@code null}
	 * @see BeanPath#get(Object)
	 * @since 3.0.7
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getProperty(Object bean, String expression) {
		if (null == bean || StrUtil.isBlank(expression)) {
			return null;
		}
		return (T) BeanPath.create(expression).get(bean);
	}

	/**
	 * 解析Bean中的属性值
	 *
	 * @param bean       Bean对象，支持Map、List、Collection、Array
	 * @param expression 表达式，例如：person.friend[5].name
	 * @param value      属性值
	 * @see BeanPath#get(Object)
	 * @since 4.0.6
	 */
	public static void setProperty(Object bean, String expression, Object value) {
		BeanPath.create(expression).set(bean, value);
	}

	// --------------------------------------------------------------------------------------------- mapToBean

	/**
	 * Map转换为Bean对象
	 *
	 * @param <T>           Bean类型
	 * @param map           {@link Map}
	 * @param beanClass     Bean Class
	 * @param isIgnoreError 是否忽略注入错误
	 * @return Bean
	 * @deprecated 请使用 {@link #toBean(Object, Class)} 或 {@link #toBeanIgnoreError(Object, Class)}
	 */
	@Deprecated
	public static <T> T mapToBean(Map<?, ?> map, Class<T> beanClass, boolean isIgnoreError) {
		return fillBeanWithMap(map, ReflectUtil.newInstanceIfPossible(beanClass), isIgnoreError);
	}

	/**
	 * Map转换为Bean对象<br>
	 * 忽略大小写
	 *
	 * @param <T>           Bean类型
	 * @param map           Map
	 * @param beanClass     Bean Class
	 * @param isIgnoreError 是否忽略注入错误
	 * @return Bean
	 * @deprecated 请使用 {@link #toBeanIgnoreCase(Object, Class, boolean)}
	 */
	@Deprecated
	public static <T> T mapToBeanIgnoreCase(Map<?, ?> map, Class<T> beanClass, boolean isIgnoreError) {
		return fillBeanWithMapIgnoreCase(map, ReflectUtil.newInstanceIfPossible(beanClass), isIgnoreError);
	}

	/**
	 * Map转换为Bean对象
	 *
	 * @param <T>         Bean类型
	 * @param map         {@link Map}
	 * @param beanClass   Bean Class
	 * @param copyOptions 转Bean选项
	 * @return Bean
	 * @deprecated 请使用 {@link #toBean(Object, Class, CopyOptions)}
	 */
	@Deprecated
	public static <T> T mapToBean(Map<?, ?> map, Class<T> beanClass, CopyOptions copyOptions) {
		return fillBeanWithMap(map, ReflectUtil.newInstanceIfPossible(beanClass), copyOptions);
	}

	/**
	 * Map转换为Bean对象
	 *
	 * @param <T>           Bean类型
	 * @param map           {@link Map}
	 * @param beanClass     Bean Class
	 * @param isToCamelCase 是否将Map中的下划线风格key转换为驼峰风格
	 * @param copyOptions   转Bean选项
	 * @return Bean
	 * @deprecated isToCamelCase参数无效，请使用 {@link #toBean(Object, Class, CopyOptions)}
	 */
	@Deprecated
	public static <T> T mapToBean(Map<?, ?> map, Class<T> beanClass, boolean isToCamelCase, CopyOptions copyOptions) {
		return fillBeanWithMap(map, ReflectUtil.newInstanceIfPossible(beanClass), isToCamelCase, copyOptions);
	}

	// --------------------------------------------------------------------------------------------- fillBeanWithMap

	/**
	 * 使用Map填充Bean对象
	 *
	 * @param <T>           Bean类型
	 * @param map           Map
	 * @param bean          Bean
	 * @param isIgnoreError 是否忽略注入错误
	 * @return Bean
	 */
	public static <T> T fillBeanWithMap(Map<?, ?> map, T bean, boolean isIgnoreError) {
		return fillBeanWithMap(map, bean, false, isIgnoreError);
	}

	/**
	 * 使用Map填充Bean对象，可配置将下划线转换为驼峰
	 *
	 * @param <T>           Bean类型
	 * @param map           Map
	 * @param bean          Bean
	 * @param isToCamelCase 是否将下划线模式转换为驼峰模式
	 * @param isIgnoreError 是否忽略注入错误
	 * @return Bean
	 * @deprecated isToCamelCase参数无效，请使用{@link #fillBeanWithMap(Map, Object, boolean)}
	 */
	@Deprecated
	public static <T> T fillBeanWithMap(Map<?, ?> map, T bean, boolean isToCamelCase, boolean isIgnoreError) {
		return fillBeanWithMap(map, bean, isToCamelCase, CopyOptions.create().setIgnoreError(isIgnoreError));
	}

	/**
	 * 使用Map填充Bean对象，忽略大小写
	 *
	 * @param <T>           Bean类型
	 * @param map           Map
	 * @param bean          Bean
	 * @param isIgnoreError 是否忽略注入错误
	 * @return Bean
	 */
	public static <T> T fillBeanWithMapIgnoreCase(Map<?, ?> map, T bean, boolean isIgnoreError) {
		return fillBeanWithMap(map, bean, CopyOptions.create().setIgnoreCase(true).setIgnoreError(isIgnoreError));
	}

	/**
	 * 使用Map填充Bean对象
	 *
	 * @param <T>         Bean类型
	 * @param map         Map
	 * @param bean        Bean
	 * @param copyOptions 属性复制选项 {@link CopyOptions}
	 * @return Bean
	 */
	public static <T> T fillBeanWithMap(Map<?, ?> map, T bean, CopyOptions copyOptions) {
		if (MapUtil.isEmpty(map)) {
			return bean;
		}
		copyProperties(map, bean, copyOptions);
		return bean;
	}

	/**
	 * 使用Map填充Bean对象
	 *
	 * @param <T>           Bean类型
	 * @param map           Map
	 * @param bean          Bean
	 * @param isToCamelCase 是否将Map中的下划线风格key转换为驼峰风格
	 * @param copyOptions   属性复制选项 {@link CopyOptions}
	 * @return Bean
	 * @since 3.3.1
	 * @deprecated isToCamelCase参数无效，请使用{@link #fillBeanWithMap(Map, Object, CopyOptions)}
	 */
	@Deprecated
	public static <T> T fillBeanWithMap(Map<?, ?> map, T bean, boolean isToCamelCase, CopyOptions copyOptions) {
		if (MapUtil.isEmpty(map)) {
			return bean;
		}

		// issue#3452，参数无效，MapToBeanCopier中已经有转驼峰逻辑
//		if (isToCamelCase) {
//			map = MapUtil.toCamelCaseMap(map);
//		}
		copyProperties(map, bean, copyOptions);
		return bean;
	}

	// --------------------------------------------------------------------------------------------- fillBean

	/**
	 * 对象或Map转Bean
	 *
	 * @param <T>    转换的Bean类型
	 * @param source Bean对象或Map
	 * @param clazz  目标的Bean类型
	 * @return Bean对象
	 * @since 4.1.20
	 */
	public static <T> T toBean(Object source, Class<T> clazz) {
		return toBean(source, clazz, null);
	}

	/**
	 * 对象或Map转Bean，忽略字段转换时发生的异常
	 *
	 * @param <T>    转换的Bean类型
	 * @param source Bean对象或Map
	 * @param clazz  目标的Bean类型
	 * @return Bean对象
	 * @since 5.4.0
	 */
	public static <T> T toBeanIgnoreError(Object source, Class<T> clazz) {
		return toBean(source, clazz, CopyOptions.create().setIgnoreError(true));
	}

	/**
	 * 对象或Map转Bean，忽略字段转换时发生的异常
	 *
	 * @param <T>         转换的Bean类型
	 * @param source      Bean对象或Map
	 * @param clazz       目标的Bean类型
	 * @param ignoreError 是否忽略注入错误
	 * @return Bean对象
	 * @since 5.4.0
	 */
	public static <T> T toBeanIgnoreCase(Object source, Class<T> clazz, boolean ignoreError) {
		return toBean(source, clazz,
				CopyOptions.create()
						.setIgnoreCase(true)
						.setIgnoreError(ignoreError));
	}

	/**
	 * 对象或Map转Bean
	 *
	 * @param <T>     转换的Bean类型
	 * @param source  Bean对象或Map
	 * @param clazz   目标的Bean类型
	 * @param options 属性拷贝选项
	 * @return Bean对象
	 * @since 5.2.4
	 */
	public static <T> T toBean(Object source, Class<T> clazz, CopyOptions options) {
		return toBean(source, () -> ReflectUtil.newInstanceIfPossible(clazz), options);
	}

	/**
	 * 对象或Map转Bean
	 *
	 * @param <T>            转换的Bean类型
	 * @param source         Bean对象或Map
	 * @param targetSupplier 目标的Bean创建器
	 * @param options        属性拷贝选项
	 * @return Bean对象
	 * @since 5.8.0
	 */
	public static <T> T toBean(Object source, Supplier<T> targetSupplier, CopyOptions options) {
		if (null == source || null == targetSupplier) {
			return null;
		}
		final T target = targetSupplier.get();
		copyProperties(source, target, options);
		return target;
	}

	/**
	 * ServletRequest 参数转Bean
	 *
	 * @param <T>           Bean类型
	 * @param beanClass     Bean Class
	 * @param valueProvider 值提供者
	 * @param copyOptions   拷贝选项，见 {@link CopyOptions}
	 * @return Bean
	 */
	public static <T> T toBean(Class<T> beanClass, ValueProvider<String> valueProvider, CopyOptions copyOptions) {
		if (null == beanClass || null == valueProvider) {
			return null;
		}
		return fillBean(ReflectUtil.newInstanceIfPossible(beanClass), valueProvider, copyOptions);
	}

	/**
	 * 填充Bean的核心方法
	 *
	 * @param <T>           Bean类型
	 * @param bean          Bean
	 * @param valueProvider 值提供者
	 * @param copyOptions   拷贝选项，见 {@link CopyOptions}
	 * @return Bean
	 */
	public static <T> T fillBean(T bean, ValueProvider<String> valueProvider, CopyOptions copyOptions) {
		if (null == valueProvider) {
			return bean;
		}

		return BeanCopier.create(valueProvider, bean, copyOptions).copy();
	}

	// --------------------------------------------------------------------------------------------- beanToMap

	/**
	 * 将bean的部分属性转换成map<br>
	 * 可选拷贝哪些属性值，默认是不忽略值为{@code null}的值的。
	 *
	 * @param bean       bean
	 * @param properties 需要拷贝的属性值，{@code null}或空表示拷贝所有值
	 * @return Map
	 * @since 5.8.0
	 */
	public static Map<String, Object> beanToMap(Object bean, String... properties) {
		int mapSize = 16;
		Editor<String> keyEditor = null;
		if (ArrayUtil.isNotEmpty(properties)) {
			mapSize = properties.length;
			final Set<String> propertiesSet = CollUtil.set(false, properties);
			keyEditor = property -> propertiesSet.contains(property) ? property : null;
		}

		// 指明了要复制的属性 所以不忽略null值
		return beanToMap(bean, new LinkedHashMap<>(mapSize, 1), false, keyEditor);
	}

	/**
	 * 对象转Map
	 *
	 * @param bean              bean对象
	 * @param isToUnderlineCase 是否转换为下划线模式
	 * @param ignoreNullValue   是否忽略值为空的字段
	 * @return Map
	 */
	public static Map<String, Object> beanToMap(Object bean, boolean isToUnderlineCase, boolean ignoreNullValue) {
		if (null == bean) {
			return null;
		}
		return beanToMap(bean, new LinkedHashMap<>(), isToUnderlineCase, ignoreNullValue);
	}

	/**
	 * 对象转Map
	 *
	 * @param bean              bean对象
	 * @param targetMap         目标的Map
	 * @param isToUnderlineCase 是否转换为下划线模式
	 * @param ignoreNullValue   是否忽略值为空的字段
	 * @return Map
	 * @since 3.2.3
	 */
	public static Map<String, Object> beanToMap(Object bean, Map<String, Object> targetMap, final boolean isToUnderlineCase, boolean ignoreNullValue) {
		if (null == bean) {
			return null;
		}

		return beanToMap(bean, targetMap, ignoreNullValue, key -> isToUnderlineCase ? StrUtil.toUnderlineCase(key) : key);
	}

	/**
	 * 对象转Map<br>
	 * 通过实现{@link Editor} 可以自定义字段值，如果这个Editor返回null则忽略这个字段，以便实现：
	 *
	 * <pre>
	 * 1. 字段筛选，可以去除不需要的字段
	 * 2. 字段变换，例如实现驼峰转下划线
	 * 3. 自定义字段前缀或后缀等等
	 * </pre>
	 *
	 * @param bean            bean对象
	 * @param targetMap       目标的Map
	 * @param ignoreNullValue 是否忽略值为空的字段
	 * @param keyEditor       属性字段（Map的key）编辑器，用于筛选、编辑key，如果这个Editor返回null则忽略这个字段
	 * @return Map
	 * @since 4.0.5
	 */
	public static Map<String, Object> beanToMap(Object bean, Map<String, Object> targetMap, boolean ignoreNullValue, Editor<String> keyEditor) {
		if (null == bean) {
			return null;
		}

		return BeanCopier.create(bean, targetMap,
				CopyOptions.create()
						.setIgnoreNullValue(ignoreNullValue)
						.setFieldNameEditor(keyEditor)
		).copy();
	}

	/**
	 * 对象转Map<br>
	 * 通过自定义{@link CopyOptions} 完成抓换选项，以便实现：
	 *
	 * <pre>
	 * 1. 字段筛选，可以去除不需要的字段
	 * 2. 字段变换，例如实现驼峰转下划线
	 * 3. 自定义字段前缀或后缀等等
	 * 4. 字段值处理
	 * ...
	 * </pre>
	 *
	 * @param bean        bean对象
	 * @param targetMap   目标的Map
	 * @param copyOptions 拷贝选项
	 * @return Map
	 * @since 5.7.15
	 */
	public static Map<String, Object> beanToMap(Object bean, Map<String, Object> targetMap, CopyOptions copyOptions) {
		if (null == bean) {
			return null;
		}

		return BeanCopier.create(bean, targetMap, copyOptions).copy();
	}

	// --------------------------------------------------------------------------------------------- copyProperties

	/**
	 * 按照Bean对象属性创建对应的Class对象，并忽略某些属性
	 *
	 * @param <T>              对象类型
	 * @param source           源Bean对象
	 * @param tClass           目标Class
	 * @param ignoreProperties 不拷贝的的属性列表
	 * @return 目标对象
	 */
	public static <T> T copyProperties(Object source, Class<T> tClass, String... ignoreProperties) {
		if (null == source) {
			return null;
		}
		T target = ReflectUtil.newInstanceIfPossible(tClass);
		copyProperties(source, target, CopyOptions.create().setIgnoreProperties(ignoreProperties));
		return target;
	}

	/**
	 * 复制Bean对象属性<br>
	 * 限制类用于限制拷贝的属性，例如一个类我只想复制其父类的一些属性，就可以将editable设置为父类
	 *
	 * @param source           源Bean对象
	 * @param target           目标Bean对象
	 * @param ignoreProperties 不拷贝的的属性列表
	 */
	public static void copyProperties(Object source, Object target, String... ignoreProperties) {
		copyProperties(source, target, CopyOptions.create().setIgnoreProperties(ignoreProperties));
	}

	/**
	 * 复制Bean对象属性<br>
	 *
	 * @param source     源Bean对象
	 * @param target     目标Bean对象
	 * @param ignoreCase 是否忽略大小写
	 */
	public static void copyProperties(Object source, Object target, boolean ignoreCase) {
		BeanCopier.create(source, target, CopyOptions.create().setIgnoreCase(ignoreCase)).copy();
	}

	/**
	 * 复制Bean对象属性<br>
	 * 限制类用于限制拷贝的属性，例如一个类我只想复制其父类的一些属性，就可以将editable设置为父类
	 *
	 * @param source      源Bean对象
	 * @param target      目标Bean对象
	 * @param copyOptions 拷贝选项，见 {@link CopyOptions}
	 */
	public static void copyProperties(Object source, Object target, CopyOptions copyOptions) {
		if (null == source) {
			return;
		}
		BeanCopier.create(source, target, ObjectUtil.defaultIfNull(copyOptions, CopyOptions::create)).copy();
	}

	/**
	 * 复制集合中的Bean属性<br>
	 * 此方法遍历集合中每个Bean，复制其属性后加入一个新的{@link List}中。
	 *
	 * @param collection  原Bean集合
	 * @param targetType  目标Bean类型
	 * @param copyOptions 拷贝选项
	 * @param <T>         Bean类型
	 * @return 复制后的List
	 * @since 5.6.4
	 */
	public static <T> List<T> copyToList(Collection<?> collection, Class<T> targetType, CopyOptions copyOptions) {
		if (null == collection) {
			return null;
		}
		if (collection.isEmpty()) {
			return new ArrayList<>(0);
		}

		// issue#3091
		if(ClassUtil.isBasicType(targetType) || String.class == targetType){
			return Convert.toList(targetType, collection);
		}

		return collection.stream().map((source) -> {
			final T target = ReflectUtil.newInstanceIfPossible(targetType);
			copyProperties(source, target, copyOptions);
			return target;
		}).collect(Collectors.toList());
	}

	/**
	 * 复制集合中的Bean属性<br>
	 * 此方法遍历集合中每个Bean，复制其属性后加入一个新的{@link List}中。
	 *
	 * @param collection 原Bean集合
	 * @param targetType 目标Bean类型
	 * @param <T>        Bean类型
	 * @return 复制后的List
	 * @since 5.6.6
	 */
	public static <T> List<T> copyToList(Collection<?> collection, Class<T> targetType) {
		return copyToList(collection, targetType, CopyOptions.create());
	}

	/**
	 * 给定的Bean的类名是否匹配指定类名字符串<br>
	 * 如果isSimple为{@code true}，则只匹配类名而忽略包名，例如：cn.hutool.TestEntity只匹配TestEntity<br>
	 * 如果isSimple为{@code false}，则匹配包括包名的全类名，例如：cn.hutool.TestEntity匹配cn.hutool.TestEntity
	 *
	 * @param bean          Bean
	 * @param beanClassName Bean的类名
	 * @param isSimple      是否只匹配类名而忽略包名，true表示忽略包名
	 * @return 是否匹配
	 * @since 4.0.6
	 */
	public static boolean isMatchName(Object bean, String beanClassName, boolean isSimple) {
		if (null == bean || StrUtil.isBlank(beanClassName)) {
			return false;
		}
		return ClassUtil.getClassName(bean, isSimple).equals(isSimple ? StrUtil.upperFirst(beanClassName) : beanClassName);
	}

	/**
	 * 编辑Bean的字段，static字段不会处理<br>
	 * 例如需要对指定的字段做判空操作、null转""操作等等。
	 *
	 * @param bean   bean
	 * @param editor 编辑器函数
	 * @param <T>    被编辑的Bean类型
	 * @return bean
	 * @since 5.6.4
	 */
	public static <T> T edit(T bean, Editor<Field> editor) {
		if (bean == null) {
			return null;
		}

		final Field[] fields = ReflectUtil.getFields(bean.getClass());
		for (Field field : fields) {
			if (ModifierUtil.isStatic(field)) {
				continue;
			}
			editor.edit(field);
		}
		return bean;
	}

	/**
	 * 把Bean里面的String属性做trim操作。此方法直接对传入的Bean做修改。
	 * <p>
	 * 通常bean直接用来绑定页面的input，用户的输入可能首尾存在空格，通常保存数据库前需要把首尾空格去掉
	 *
	 * @param <T>          Bean类型
	 * @param bean         Bean对象
	 * @param ignoreFields 不需要trim的Field名称列表（不区分大小写）
	 * @return 处理后的Bean对象
	 */
	public static <T> T trimStrFields(T bean, String... ignoreFields) {
		return edit(bean, (field) -> {
			if (ignoreFields != null && ArrayUtil.containsIgnoreCase(ignoreFields, field.getName())) {
				// 不处理忽略的Fields
				return field;
			}
			if (String.class.equals(field.getType())) {
				// 只有String的Field才处理
				final String val = (String) ReflectUtil.getFieldValue(bean, field);
				if (null != val) {
					final String trimVal = StrUtil.trim(val);
					if (false == val.equals(trimVal)) {
						// Field Value不为null，且首尾有空格才处理
						ReflectUtil.setFieldValue(bean, field, trimVal);
					}
				}
			}
			return field;
		});
	}

	/**
	 * 判断Bean是否为非空对象，非空对象表示本身不为{@code null}或者含有非{@code null}属性的对象
	 *
	 * @param bean             Bean对象
	 * @param ignoreFieldNames 忽略检查的字段名
	 * @return 是否为非空，{@code true} - 非空 / {@code false} - 空
	 * @since 5.0.7
	 */
	public static boolean isNotEmpty(Object bean, String... ignoreFieldNames) {
		return false == isEmpty(bean, ignoreFieldNames);
	}

	/**
	 * 判断Bean是否为空对象，空对象表示本身为{@code null}或者所有属性都为{@code null}<br>
	 * 此方法不判断static属性
	 *
	 * @param bean             Bean对象
	 * @param ignoreFieldNames 忽略检查的字段名
	 * @return 是否为空，{@code true} - 空 / {@code false} - 非空
	 * @since 4.1.10
	 */
	public static boolean isEmpty(Object bean, String... ignoreFieldNames) {
		if (null != bean) {
			for (Field field : ReflectUtil.getFields(bean.getClass())) {
				if (ModifierUtil.isStatic(field)) {
					continue;
				}
				if ((false == ArrayUtil.contains(ignoreFieldNames, field.getName()))
						&& null != ReflectUtil.getFieldValue(bean, field)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 判断Bean是否包含值为{@code null}的属性<br>
	 * 对象本身为{@code null}也返回true
	 *
	 * @param bean             Bean对象
	 * @param ignoreFieldNames 忽略检查的字段名
	 * @return 是否包含值为<code>null</code>的属性，{@code true} - 包含 / {@code false} - 不包含
	 * @since 4.1.10
	 */
	public static boolean hasNullField(Object bean, String... ignoreFieldNames) {
		if (null == bean) {
			return true;
		}
		for (Field field : ReflectUtil.getFields(bean.getClass())) {
			if (ModifierUtil.isStatic(field)) {
				continue;
			}
			if ((false == ArrayUtil.contains(ignoreFieldNames, field.getName()))
					&& null == ReflectUtil.getFieldValue(bean, field)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取Getter或Setter方法名对应的字段名称，规则如下：
	 * <ul>
	 *     <li>getXxxx获取为xxxx，如getName得到name。</li>
	 *     <li>setXxxx获取为xxxx，如setName得到name。</li>
	 *     <li>isXxxx获取为xxxx，如isName得到name。</li>
	 *     <li>其它不满足规则的方法名抛出{@link IllegalArgumentException}</li>
	 * </ul>
	 *
	 * @param getterOrSetterName Getter或Setter方法名
	 * @return 字段名称
	 * @throws IllegalArgumentException 非Getter或Setter方法
	 * @since 5.7.23
	 */
	public static String getFieldName(String getterOrSetterName) {
		if (getterOrSetterName.startsWith("get") || getterOrSetterName.startsWith("set")) {
			return StrUtil.removePreAndLowerFirst(getterOrSetterName, 3);
		} else if (getterOrSetterName.startsWith("is")) {
			return StrUtil.removePreAndLowerFirst(getterOrSetterName, 2);
		} else {
			throw new IllegalArgumentException("Invalid Getter or Setter name: " + getterOrSetterName);
		}
	}

	/**
	 * 判断source与target的所有公共字段的值是否相同
	 *
	 * @param source           待检测对象1
	 * @param target           待检测对象2
	 * @param ignoreProperties 不需要检测的字段
	 * @return 判断结果，如果为true则证明所有字段的值都相同
	 * @author Takak11
	 * @since 5.8.4
	 */
	public static boolean isCommonFieldsEqual(Object source, Object target, String... ignoreProperties) {

		if (null == source && null == target) {
			return true;
		}
		if (null == source || null == target) {
			return false;
		}

		final Map<String, Object> sourceFieldsMap = BeanUtil.beanToMap(source);
		final Map<String, Object> targetFieldsMap = BeanUtil.beanToMap(target);

		final Set<String> sourceFields = sourceFieldsMap.keySet();
		sourceFields.removeAll(Arrays.asList(ignoreProperties));

		for (String field : sourceFields) {
			if(sourceFieldsMap.containsKey(field) && targetFieldsMap.containsKey(field)){
				if (ObjectUtil.notEqual(sourceFieldsMap.get(field), targetFieldsMap.get(field))) {
					return false;
				}
			}
		}

		return true;
	}
}
