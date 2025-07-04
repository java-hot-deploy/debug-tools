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

import io.github.future0923.debug.tools.base.hutool.core.map.TableMap;
import io.github.future0923.debug.tools.base.hutool.core.util.ArrayUtil;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 组合注解 对JDK的原生注解机制做一个增强，支持类似Spring的组合注解。<br>
 * 核心实现使用了递归获取指定元素上的注解以及注解的注解，以实现复合注解的获取。
 *
 * @author Succy, Looly
 * @since 4.0.9
 **/

public class CombinationAnnotationElement implements AnnotatedElement, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 创建CombinationAnnotationElement
	 *
	 * @param element   需要解析注解的元素：可以是Class、Method、Field、Constructor、ReflectPermission
	 * @param predicate 过滤器，{@link Predicate#test(Object)}返回{@code true}保留，否则不保留
	 * @return CombinationAnnotationElement
	 * @since 5.8.0
	 */
	public static CombinationAnnotationElement of(AnnotatedElement element, Predicate<Annotation> predicate) {
		return new CombinationAnnotationElement(element, predicate);
	}

	/**
	 * 注解类型与注解对象对应表
	 */
	private Map<Class<? extends Annotation>, Annotation> annotationMap;
	/**
	 * 直接注解类型与注解对象对应表
	 */
	private Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap;
	/**
	 * 过滤器
	 */
	private final Predicate<Annotation> predicate;

	/**
	 * 构造
	 *
	 * @param element 需要解析注解的元素：可以是Class、Method、Field、Constructor、ReflectPermission
	 */
	public CombinationAnnotationElement(AnnotatedElement element) {
		this(element, null);
	}

	/**
	 * 构造
	 *
	 * @param element   需要解析注解的元素：可以是Class、Method、Field、Constructor、ReflectPermission
	 * @param predicate 过滤器，{@link Predicate#test(Object)}返回{@code true}保留，否则不保留
	 * @since 5.8.0
	 */
	public CombinationAnnotationElement(AnnotatedElement element, Predicate<Annotation> predicate) {
		this.predicate = predicate;
		init(element);
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return annotationMap.containsKey(annotationClass);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		Annotation annotation = annotationMap.get(annotationClass);
		return (annotation == null) ? null : (T) annotation;
	}

	@Override
	public Annotation[] getAnnotations() {
		final Collection<Annotation> annotations = this.annotationMap.values();
		return annotations.toArray(new Annotation[0]);
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		final Collection<Annotation> annotations = this.declaredAnnotationMap.values();
		return annotations.toArray(new Annotation[0]);
	}

	/**
	 * 初始化
	 *
	 * @param element 元素
	 */
	private void init(AnnotatedElement element) {
		final Annotation[] declaredAnnotations = element.getDeclaredAnnotations();
		this.declaredAnnotationMap = new TableMap<>();
		parseDeclared(declaredAnnotations);

		final Annotation[] annotations = element.getAnnotations();
		// pr#1323 如果子类重写了父类的注解，虽然两者数组内部元素一样的，但是数组中的顺序可能不一样
		// getAnnotations()的包含父类，getDeclaredAnnotations()不包含父类。他们两是一个包含关系，只会存在后者的注解元素大于等于前者的情况。
		if (declaredAnnotations.length == annotations.length) {
			this.annotationMap = this.declaredAnnotationMap;
		} else {
			this.annotationMap = new TableMap<>();
			parse(annotations);
		}
	}

	/**
	 * 进行递归解析注解，直到全部都是元注解为止
	 *
	 * @param annotations Class, Method, Field等
	 */
	private void parseDeclared(Annotation[] annotations) {
		if(ArrayUtil.isEmpty(annotations)){
			return;
		}

		Class<? extends Annotation> annotationType;
		// 直接注解
		for (Annotation annotation : annotations) {
			annotationType = annotation.annotationType();
			// issue#I5FQGW@Gitee：跳过元注解和已经处理过的注解，防止递归调用
			if (AnnotationUtil.isNotJdkMateAnnotation(annotationType)
					&& false == declaredAnnotationMap.containsKey(annotationType)) {
				if(test(annotation)){
					declaredAnnotationMap.put(annotationType, annotation);
				}
				// 测试不通过的注解，不影响继续递归
				parseDeclared(annotationType.getDeclaredAnnotations());
			}
		}
	}

	/**
	 * 进行递归解析注解，直到全部都是元注解为止
	 *
	 * @param annotations Class, Method, Field等
	 */
	private void parse(Annotation[] annotations) {
		if(ArrayUtil.isEmpty(annotations)){
			return;
		}

		Class<? extends Annotation> annotationType;
		for (Annotation annotation : annotations) {
			annotationType = annotation.annotationType();
			// issue#I5FQGW@Gitee：跳过元注解和已经处理过的注解，防止递归调用
			if (AnnotationUtil.isNotJdkMateAnnotation(annotationType)
					&& false == annotationMap.containsKey(annotationType)) {
				if(test(annotation)){
					annotationMap.put(annotationType, annotation);
				}
				// 测试不通过的注解，不影响继续递归
				parse(annotationType.getAnnotations());
			}
		}
	}

	/**
	 * 检查给定的注解是否符合过滤条件
	 *
	 * @param annotation 注解对象
	 * @return 是否符合条件
	 */
	private boolean test(Annotation annotation) {
		return null == this.predicate || this.predicate.test(annotation);
	}
}
