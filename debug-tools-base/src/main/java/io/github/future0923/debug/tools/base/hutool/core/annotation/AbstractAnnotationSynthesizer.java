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

import io.github.future0923.debug.tools.base.hutool.core.annotation.scanner.AnnotationScanner;
import io.github.future0923.debug.tools.base.hutool.core.collection.CollUtil;
import io.github.future0923.debug.tools.base.hutool.core.lang.Assert;
import io.github.future0923.debug.tools.base.hutool.core.map.MapUtil;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * {@link AnnotationSynthesizer}的基本实现
 *
 * @author huangchengxing
 */
public abstract class AbstractAnnotationSynthesizer<T> implements AnnotationSynthesizer {

	/**
	 * 合成注解来源最初来源
	 */
	protected final T source;

	/**
	 * 包含根注解以及其元注解在内的全部注解实例
	 */
	protected final Map<Class<? extends Annotation>, SynthesizedAnnotation> synthesizedAnnotationMap;

	/**
	 * 已经合成过的注解对象
	 */
	private final Map<Class<? extends Annotation>, Annotation> synthesizedProxyAnnotations;

	/**
	 * 合成注解选择器
	 */
	protected final SynthesizedAnnotationSelector annotationSelector;

	/**
	 * 合成注解属性处理器
	 */
	protected final Collection<SynthesizedAnnotationPostProcessor> postProcessors;

	/**
	 * 注解扫描器
	 */
	protected final AnnotationScanner annotationScanner;

	/**
	 * 构造一个注解合成器
	 *
	 * @param source                   当前查找的注解对象
	 * @param annotationSelector       合成注解选择器
	 * @param annotationPostProcessors 注解后置处理器
	 * @param annotationScanner        注解扫描器，该扫描器需要支持扫描注解类
	 */
	protected AbstractAnnotationSynthesizer(
		T source,
		SynthesizedAnnotationSelector annotationSelector,
		Collection<SynthesizedAnnotationPostProcessor> annotationPostProcessors,
		AnnotationScanner annotationScanner) {
		Assert.notNull(source, "source must not null");
		Assert.notNull(annotationSelector, "annotationSelector must not null");
		Assert.notNull(annotationPostProcessors, "annotationPostProcessors must not null");
		Assert.notNull(annotationPostProcessors, "annotationScanner must not null");

		this.source = source;
		this.annotationSelector = annotationSelector;
		this.annotationScanner = annotationScanner;
		this.postProcessors = CollUtil.unmodifiable(
			CollUtil.sort(annotationPostProcessors, Comparator.comparing(SynthesizedAnnotationPostProcessor::order))
		);
		this.synthesizedProxyAnnotations = new LinkedHashMap<>();
		this.synthesizedAnnotationMap = MapUtil.unmodifiable(loadAnnotations());
		annotationPostProcessors.forEach(processor ->
			synthesizedAnnotationMap.values().forEach(synthesized -> processor.process(synthesized, this))
		);
	}

	/**
	 * 加载合成注解的必要属性
	 *
	 * @return 合成注解
	 */
	protected abstract Map<Class<? extends Annotation>, SynthesizedAnnotation> loadAnnotations();

	/**
	 * 根据指定的注解类型和对应注解对象，合成最终所需的合成注解
	 *
	 * @param annotationType 注解类型
	 * @param annotation     合成注解对象
	 * @param <A>            注解类型
	 * @return 最终所需的合成注解
	 */
	protected abstract <A extends Annotation> A synthesize(Class<A> annotationType, SynthesizedAnnotation annotation);

	/**
	 * 获取合成注解来源最初来源
	 *
	 * @return 合成注解来源最初来源
	 */
	@Override
	public T getSource() {
		return source;
	}

	/**
	 * 合成注解选择器
	 *
	 * @return 注解选择器
	 */
	@Override
	public SynthesizedAnnotationSelector getAnnotationSelector() {
		return annotationSelector;
	}

	/**
	 * 获取合成注解后置处理器
	 *
	 * @return 合成注解后置处理器
	 */
	@Override
	public Collection<SynthesizedAnnotationPostProcessor> getAnnotationPostProcessors() {
		return postProcessors;
	}

	/**
	 * 获取已合成的注解
	 *
	 * @param annotationType 注解类型
	 * @return 已合成的注解
	 */
	@Override
	public SynthesizedAnnotation getSynthesizedAnnotation(Class<?> annotationType) {
		return synthesizedAnnotationMap.get(annotationType);
	}

	/**
	 * 获取全部的合成注解
	 *
	 * @return 合成注解
	 */
	@Override
	public Map<Class<? extends Annotation>, SynthesizedAnnotation> getAllSynthesizedAnnotation() {
		return synthesizedAnnotationMap;
	}

	/**
	 * 获取合成注解
	 *
	 * @param annotationType 注解类型
	 * @param <A>            注解类型
	 * @return 类型
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <A extends Annotation> A synthesize(Class<A> annotationType) {
		A annotation = (A)synthesizedProxyAnnotations.get(annotationType);
		if (Objects.nonNull(annotation)) {
			return annotation;
		}
		synchronized (synthesizedProxyAnnotations) {
			annotation = (A)synthesizedProxyAnnotations.get(annotationType);
			if (Objects.isNull(annotation)) {
				final SynthesizedAnnotation synthesizedAnnotation = synthesizedAnnotationMap.get(annotationType);
				annotation = synthesize(annotationType, synthesizedAnnotation);
				synthesizedProxyAnnotations.put(annotationType, annotation);
			}
		}
		return annotation;
	}

}
