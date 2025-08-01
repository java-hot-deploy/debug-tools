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
package io.github.future0923.debug.tools.hotswap.core.plugin.mybatis.dto;

/**
 * @author future0923
 */
public class MyBatisPlusMapperReloadDTO {

    private final ClassLoader userClassLoader;

    private final Class<?> clazz;

    private final byte[] bytes;

    private final String path;

    public MyBatisPlusMapperReloadDTO(ClassLoader userClassLoader, Class<?> clazz, byte[] bytes, String path) {
        this.userClassLoader = userClassLoader;
        this.clazz = clazz;
        this.bytes = bytes;
        this.path = path;
    }

    public ClassLoader getUserClassLoader() {
        return userClassLoader;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getPath() {
        return path;
    }
}
