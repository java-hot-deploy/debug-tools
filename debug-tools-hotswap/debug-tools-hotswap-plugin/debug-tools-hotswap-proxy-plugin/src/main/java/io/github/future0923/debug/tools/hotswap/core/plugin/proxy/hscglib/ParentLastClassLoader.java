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
package io.github.future0923.debug.tools.hotswap.core.plugin.proxy.hscglib;

import io.github.future0923.debug.tools.hotswap.core.plugin.proxy.utils.ProxyTransformationUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author future0923
 */
public class ParentLastClassLoader extends ClassLoader {

    public static final String[] EXCLUDED_PACKAGES = new String[] { "java.", "javax.", "sun.", "oracle." };

    public ParentLastClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        for (String excludedPackage : EXCLUDED_PACKAGES) {
            if (name.startsWith(excludedPackage))
                return super.loadClass(name, resolve);
        }
        Class<?> clazz = loadClassFromThisClassLoader(name);
        if (clazz == null)
            return super.loadClass(name, resolve);

        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    protected Class<?> loadClassFromThisClassLoader(String name) throws ClassNotFoundException {
        Class<?> result = findLoadedClass(name);
        if (result != null) {
            return result;
        }
        byte[] bytes = readClass(name);
        if (bytes != null) {
            return defineClass(name, bytes, 0, bytes.length);
        }
        return null;
    }

    protected byte[] readClass(String name) throws ClassNotFoundException {
        InputStream is = getParent().getResourceAsStream(name.replace('.', '/') + ".class");
        if (is == null) {
            return null;
        }
        try {
            return ProxyTransformationUtils.copyToByteArray(is);
        } catch (IOException ex) {
            throw new ClassNotFoundException("Could not read: " + name, ex);
        }
    }
}
