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
package io.github.future0923.debug.tools.hotswap.core.util.spring.io.resource;
/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * Interface for a resource descriptor that abstracts from the actual type of
 * underlying resource, such as a file or class path resource.
 *
 * <p>
 * An InputStream can be opened for every resource if it exists in physical
 * form, but a URL or File handle can just be returned for certain resources.
 * The actual behavior is implementation-specific.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see #getInputStream()
 * @see #getURL()
 * @see #getURI()
 * @see #getFile()
 * @see WritableResource
 * @see ContextResource
 * @see FileSystemResource
 * @see ClassPathResource
 * @see UrlResource
 * @see ByteArrayResource
 * @see InputStreamResource
 * @see PathResource
 */
public interface Resource extends InputStreamSource {

    /**
     * Return whether this resource actually exists in physical form.
     * <p>
     * This method performs a definitive existence check, whereas the existence
     * of a {@code Resource} handle only guarantees a valid descriptor handle.
     */
    boolean exists();

    /**
     * Return whether the contents of this resource can be read, e.g. via
     * {@link #getInputStream()} or {@link #getFile()}.
     * <p>
     * Will be {@code true} for typical resource descriptors; note that actual
     * content reading may still fail when attempted. However, a value of
     * {@code false} is a definitive indication that the resource content cannot
     * be read.
     * 
     * @see #getInputStream()
     */
    boolean isReadable();

    /**
     * Return whether this resource represents a handle with an open stream. If
     * true, the InputStream cannot be read multiple times, and must be read and
     * closed to avoid resource leaks.
     * <p>
     * Will be {@code false} for typical resource descriptors.
     */
    boolean isOpen();

    /**
     * Return a URL handle for this resource.
     * 
     * @throws IOException
     *             if the resource cannot be resolved as URL, i.e. if the
     *             resource is not available as descriptor
     */
    URL getURL() throws IOException;

    /**
     * Return a URI handle for this resource.
     * 
     * @throws IOException
     *             if the resource cannot be resolved as URI, i.e. if the
     *             resource is not available as descriptor
     */
    URI getURI() throws IOException;

    /**
     * Return a File handle for this resource.
     * 
     * @throws IOException
     *             if the resource cannot be resolved as absolute file path,
     *             i.e. if the resource is not available in a file system
     */
    File getFile() throws IOException;

    /**
     * Determine the content length for this resource.
     * 
     * @throws IOException
     *             if the resource cannot be resolved (in the file system or as
     *             some other known physical resource type)
     */
    long contentLength() throws IOException;

    /**
     * Determine the last-modified timestamp for this resource.
     * 
     * @throws IOException
     *             if the resource cannot be resolved (in the file system or as
     *             some other known physical resource type)
     */
    long lastModified() throws IOException;

    /**
     * Create a resource relative to this resource.
     * 
     * @param relativePath
     *            the relative path (relative to this resource)
     * @return the resource handle for the relative resource
     * @throws IOException
     *             if the relative resource cannot be determined
     */
    Resource createRelative(String relativePath) throws IOException;

    /**
     * Determine a filename for this resource, i.e. typically the last part of
     * the path: for example, "myfile.txt".
     * <p>
     * Returns {@code null} if this type of resource does not have a filename.
     */
    String getFilename();

    /**
     * Return a description for this resource, to be used for error output when
     * working with the resource.
     * <p>
     * Implementations are also encouraged to return this value from their
     * {@code toString} method.
     * 
     * @see Object#toString()
     */
    String getDescription();

}