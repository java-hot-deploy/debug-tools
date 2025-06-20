/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.future0923.debug.tools.server.utils;

import io.github.future0923.debug.tools.vm.JvmToolsUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * @author future0923
 */
public class BeanInstanceUtils {

    public static Object getInstance(Class<?> targetClass, Method targetMethod) {
        Object instance = getInstance(targetClass);
        if (!Modifier.isPublic(targetMethod.getModifiers())) {
            return DebugToolsEnvUtils.getTargetObject(instance);
        } else {
            return instance;
        }
    }

    public static <T> T[] getInstances(Class<T> targetClass) {
        return JvmToolsUtils.getInstances(targetClass);
    }

    /**
     * 获取实例对象
     * <p>优先通过spring 上下文获取
     * <p>获取不到从solon 上下文获取
     * <p>获取不到从jvm中获取，如果有多个取第最后一个
     * <p>获取不到调用构造方法创建
     */
    public static Object getInstance(Class<?> clazz) {
        try {
            Object firstBean = DebugToolsEnvUtils.getLastBean(clazz);
            if (firstBean != null) {
                return firstBean;
            }
        } catch (Throwable ignored) {
            // 加载不到从JVM中获取
        }
        Object[] instances = JvmToolsUtils.getInstances(clazz);
        if (instances.length == 0) {
            return instantiate(clazz);
        } else {
            return instances[instances.length - 1];
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T instantiate(Class<T> clazz) {
        if (clazz == null || clazz.isInterface()) {
            throw new IllegalArgumentException("Specified class is null or interface. " + clazz);
        }
        Optional<Constructor<?>> noArgConstructorOpt = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0).findFirst();
        Object obj;
        try {
            if (noArgConstructorOpt.isPresent()) {
                Constructor<?> constructor = noArgConstructorOpt.get();
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                obj = constructor.newInstance();
            } else {
                Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                Object[] objects = IntStream.range(0, constructor.getParameterCount()).mapToObj(i -> null).toArray();
                obj = constructor.newInstance(objects);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("instantiate Exception" + clazz, e);
        }
        return (T) obj;
    }
}
