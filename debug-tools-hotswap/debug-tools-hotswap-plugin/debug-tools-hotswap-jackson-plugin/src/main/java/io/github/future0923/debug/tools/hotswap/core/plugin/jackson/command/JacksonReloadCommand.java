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
package io.github.future0923.debug.tools.hotswap.core.plugin.jackson.command;

import io.github.future0923.debug.tools.base.hutool.core.util.ReflectUtil;
import io.github.future0923.debug.tools.base.logging.Logger;
import io.github.future0923.debug.tools.hotswap.core.command.MergeableCommand;
import io.github.future0923.debug.tools.hotswap.core.util.ReflectionHelper;
import io.github.future0923.debug.tools.vm.JvmToolsUtils;

import java.util.Map;

/**
 * @author future0923
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class JacksonReloadCommand extends MergeableCommand {

    private static final Logger logger = Logger.getLogger(JacksonReloadCommand.class);

    private final Class<?> clazz;

    private final ClassLoader appClassLoader;

    public JacksonReloadCommand(Class<?> clazz, ClassLoader appClassLoader) {
        this.clazz = clazz;
        this.appClassLoader = appClassLoader;
    }

    @Override
    public void executeCommand() {
        Class<?> objectMapperClass;
        try {
            objectMapperClass = appClassLoader.loadClass("com.fasterxml.jackson.databind.ObjectMapper");
        } catch (ClassNotFoundException e) {
            return;
        }
        Class deserializerCacheClass;
        try {
            deserializerCacheClass = appClassLoader.loadClass("com.fasterxml.jackson.databind.deser.DeserializerCache");
        } catch (ClassNotFoundException e) {
            return;
        }
        Class serializerCacheClass;
        try {
            serializerCacheClass = appClassLoader.loadClass("com.fasterxml.jackson.databind.ser.SerializerCache");
        } catch (ClassNotFoundException e) {
            return;
        }
        Object[] objectMappers = JvmToolsUtils.getInstances(objectMapperClass);
        if (objectMappers == null || objectMappers.length == 0) {
            return;
        }
        Object javaType;
        try {
            Object ob = objectMapperClass.getConstructor().newInstance();
            javaType = ReflectUtil.invoke(ob, "constructType", clazz);
        } catch (Exception e) {
            javaType = ReflectUtil.invoke(objectMappers[0], "constructType", clazz);
        }
        boolean changed = false;
        for (Object objectMapper : objectMappers) {
            Map _rootDeserializers = (Map) ReflectionHelper.getNoException(objectMapper, objectMapperClass, "_rootDeserializers");
            if (_rootDeserializers != null) {
                boolean remove = _rootDeserializers.remove(javaType) != null;
                if (!changed && remove) {
                    changed = true;
                }
            }
        }
        Object[] deserializerCaches = JvmToolsUtils.getInstances(deserializerCacheClass);
        for (Object deserializerCache : deserializerCaches) {
            ReflectUtil.invoke(deserializerCache, "flushCachedDeserializers");
            Map _incompleteDeserializers = (Map) ReflectionHelper.getNoException(deserializerCache, deserializerCacheClass, "_incompleteDeserializers");
            if (_incompleteDeserializers != null) {
                _incompleteDeserializers.remove(javaType);
            }
        }
        Object[] serializerCaches = JvmToolsUtils.getInstances(serializerCacheClass);
        for (Object serializerCache : serializerCaches) {
            ReflectUtil.invoke(serializerCache, "flush");
        }
        for (Object objectMapper : objectMappers) {
            try {
                // 强制触发 Jackson introspect
                ReflectUtil.invoke(objectMapper, "writeValueAsString", clazz.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                return;
            }
        }

        if (changed) {
            logger.reload("Class '{}' has been reloaded.", clazz.getName());
        }
    }
}
