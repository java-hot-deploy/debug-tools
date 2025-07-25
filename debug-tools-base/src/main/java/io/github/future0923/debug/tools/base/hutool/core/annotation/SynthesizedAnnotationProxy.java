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
package io.github.future0923.debug.tools.base.hutool.core.annotation;

import io.github.future0923.debug.tools.base.hutool.core.lang.Assert;
import io.github.future0923.debug.tools.base.hutool.core.lang.Opt;
import io.github.future0923.debug.tools.base.hutool.core.text.CharSequenceUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ClassUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ObjectUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ReflectUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 合成注解代理类，用于为{@link SynthesizedAnnotation}生成对应的合成注解代理对象
 *
 * @author huangchengxing
 * @see SynthesizedAnnotation
 * @see AnnotationAttributeValueProvider
 */
public class SynthesizedAnnotationProxy implements InvocationHandler {

	private final AnnotationAttributeValueProvider annotationAttributeValueProvider;
	private final SynthesizedAnnotation annotation;
	private final Map<String, BiFunction<Method, Object[], Object>> methods;

	/**
	 * 创建一个代理注解，生成的代理对象将是{@link SyntheticProxyAnnotation}与指定的注解类的子类。
	 *
	 * @param <T>                              注解类型
	 * @param annotationType                   注解类型
	 * @param annotationAttributeValueProvider 注解属性值获取器
	 * @param annotation                       合成注解
	 * @return 代理注解
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T create(
			Class<T> annotationType,
			AnnotationAttributeValueProvider annotationAttributeValueProvider,
			SynthesizedAnnotation annotation) {
		if (ObjectUtil.isNull(annotation)) {
			return null;
		}
		final SynthesizedAnnotationProxy proxyHandler = new SynthesizedAnnotationProxy(annotationAttributeValueProvider, annotation);
		if (ObjectUtil.isNull(annotation)) {
			return null;
		}
		return (T) Proxy.newProxyInstance(
				annotationType.getClassLoader(),
				new Class[]{annotationType, SyntheticProxyAnnotation.class},
				proxyHandler
		);
	}

	/**
	 * 创建一个代理注解，生成的代理对象将是{@link SyntheticProxyAnnotation}与指定的注解类的子类。
	 *
	 * @param <T>            注解类型
	 * @param annotationType 注解类型
	 * @param annotation     合成注解
	 * @return 代理注解
	 */
	public static <T extends Annotation> T create(
			Class<T> annotationType, SynthesizedAnnotation annotation) {
		return create(annotationType, annotation, annotation);
	}

	/**
	 * 该类是否为通过{@code SynthesizedAnnotationProxy}生成的代理类
	 *
	 * @param annotationType 注解类型
	 * @return 是否
	 */
	public static boolean isProxyAnnotation(Class<?> annotationType) {
		return ClassUtil.isAssignable(SyntheticProxyAnnotation.class, annotationType);
	}

	SynthesizedAnnotationProxy(AnnotationAttributeValueProvider annotationAttributeValueProvider, SynthesizedAnnotation annotation) {
		Assert.notNull(annotationAttributeValueProvider, "annotationAttributeValueProvider must not null");
		Assert.notNull(annotation, "annotation must not null");
		this.annotationAttributeValueProvider = annotationAttributeValueProvider;
		this.annotation = annotation;
		this.methods = new HashMap<>(9);
		loadMethods();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return Opt.ofNullable(methods.get(method.getName()))
				.map(m -> m.apply(method, args))
				.orElseGet(() -> ReflectUtil.invoke(annotation.getAnnotation(), method, args));
	}

	// ========================= 代理方法 =========================

	void loadMethods() {
		// 非用户属性
		methods.put("toString", (method, args) -> proxyToString());
		methods.put("hashCode", (method, args) -> proxyHashCode());
		methods.put("getSynthesizedAnnotation", (method, args) -> proxyGetSynthesizedAnnotation());
		methods.put("getRoot", (method, args) -> annotation.getRoot());
		methods.put("getVerticalDistance", (method, args) -> annotation.getVerticalDistance());
		methods.put("getHorizontalDistance", (method, args) -> annotation.getHorizontalDistance());
		methods.put("hasAttribute", (method, args) -> annotation.hasAttribute((String) args[0], (Class<?>) args[1]));
		methods.put("getAttributes", (method, args) -> annotation.getAttributes());
		methods.put("setAttribute", (method, args) -> {
			throw new UnsupportedOperationException("proxied annotation can not reset attributes");
		});
		methods.put("getAttributeValue", (method, args) -> annotation.getAttributeValue((String) args[0]));
		methods.put("annotationType", (method, args) -> annotation.annotationType());

		// 可以被合成的用户属性
		Stream.of(ClassUtil.getDeclaredMethods(annotation.getAnnotation().annotationType()))
			.filter(m -> !methods.containsKey(m.getName()))
			.forEach(m -> methods.put(m.getName(), (method, args) -> proxyAttributeValue(method)));
	}

	private String proxyToString() {
		final String attributes = Stream.of(ClassUtil.getDeclaredMethods(annotation.getAnnotation().annotationType()))
				.filter(AnnotationUtil::isAttributeMethod)
				.map(method -> CharSequenceUtil.format(
						"{}={}", method.getName(), proxyAttributeValue(method))
				)
				.collect(Collectors.joining(", "));
		return CharSequenceUtil.format("@{}({})", annotation.annotationType().getName(), attributes);
	}

	private int proxyHashCode() {
		return Objects.hash(annotationAttributeValueProvider, annotation);
	}

	private Object proxyGetSynthesizedAnnotation() {
		return annotation;
	}

	private Object proxyAttributeValue(Method attributeMethod) {
		return annotationAttributeValueProvider.getAttributeValue(attributeMethod.getName(), attributeMethod.getReturnType());
	}

	/**
	 * 通过代理类生成的合成注解
	 *
	 * @author huangchengxing
	 */
	interface SyntheticProxyAnnotation extends SynthesizedAnnotation {

		/**
		 * 获取该代理注解对应的已合成注解
		 *
		 * @return 理注解对应的已合成注解
		 */
		SynthesizedAnnotation getSynthesizedAnnotation();

	}

}
