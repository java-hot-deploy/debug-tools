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

import io.github.future0923.debug.tools.hotswap.core.util.spring.util.Assert;
import io.github.future0923.debug.tools.hotswap.core.util.spring.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

/**
 * {@link Resource} implementation for {@code java.io.File} handles. Obviously
 * supports resolution as File, and also as URL. Implements the extended
 * {@link WritableResource} interface.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see File
 */
public class FileSystemResource extends AbstractResource implements WritableResource {

    private final File file;

    private final String path;

    /**
     * Create a new {@code FileSystemResource} from a {@link File} handle.
     * <p>
     * Note: When building relative resources via {@link #createRelative}, the
     * relative path will apply <i>at the same directory level</i>: e.g. new
     * File("C:/dir1"), relative path "dir2" -> "C:/dir2"! If you prefer to have
     * relative paths built underneath the given root directory, use the
     * {@link #FileSystemResource(String) constructor with a file path} to
     * append a trailing slash to the root path: "C:/dir1/", which indicates
     * this directory as root for all relative paths.
     * 
     * @param file
     *            a File handle
     */
    public FileSystemResource(File file) {
        Assert.notNull(file, "File must not be null");
        this.file = file;
        this.path = StringUtils.cleanPath(file.getPath());
    }

    /**
     * Create a new {@code FileSystemResource} from a file path.
     * <p>
     * Note: When building relative resources via {@link #createRelative}, it
     * makes a difference whether the specified resource base path here ends
     * with a slash or not. In the case of "C:/dir1/", relative paths will be
     * built underneath that root: e.g. relative path "dir2" -> "C:/dir1/dir2".
     * In the case of "C:/dir1", relative paths will apply at the same directory
     * level: relative path "dir2" -> "C:/dir2".
     * 
     * @param path
     *            a file path
     */
    public FileSystemResource(String path) {
        Assert.notNull(path, "Path must not be null");
        this.file = new File(path);
        this.path = StringUtils.cleanPath(path);
    }

    /**
     * Return the file path for this resource.
     */
    public final String getPath() {
        return this.path;
    }

    /**
     * This implementation returns whether the underlying file exists.
     * 
     * @see File#exists()
     */
    @Override
    public boolean exists() {
        return this.file.exists();
    }

    /**
     * This implementation checks whether the underlying file is marked as
     * readable (and corresponds to an actual file with content, not to a
     * directory).
     * 
     * @see File#canRead()
     * @see File#isDirectory()
     */
    @Override
    public boolean isReadable() {
        return (this.file.canRead() && !this.file.isDirectory());
    }

    /**
     * This implementation opens a FileInputStream for the underlying file.
     * 
     * @see FileInputStream
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(this.file);
    }

    /**
     * This implementation returns a URL for the underlying file.
     * 
     * @see File#toURI()
     */
    @Override
    public URL getURL() throws IOException {
        return this.file.toURI().toURL();
    }

    /**
     * This implementation returns a URI for the underlying file.
     * 
     * @see File#toURI()
     */
    @Override
    public URI getURI() throws IOException {
        return this.file.toURI();
    }

    /**
     * This implementation returns the underlying File reference.
     */
    @Override
    public File getFile() {
        return this.file;
    }

    /**
     * This implementation returns the underlying File's length.
     */
    @Override
    public long contentLength() throws IOException {
        return this.file.length();
    }

    /**
     * This implementation creates a FileSystemResource, applying the given path
     * relative to the path of the underlying file of this resource descriptor.
     * 
     * @see StringUtils#applyRelativePath(String,
     *      String)
     */
    @Override
    public Resource createRelative(String relativePath) {
        String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
        return new FileSystemResource(pathToUse);
    }

    /**
     * This implementation returns the name of the file.
     * 
     * @see File#getName()
     */
    @Override
    public String getFilename() {
        return this.file.getName();
    }

    /**
     * This implementation returns a description that includes the absolute path
     * of the file.
     * 
     * @see File#getAbsolutePath()
     */
    @Override
    public String getDescription() {
        return "file [" + this.file.getAbsolutePath() + "]";
    }

    // implementation of WritableResource

    /**
     * This implementation checks whether the underlying file is marked as
     * writable (and corresponds to an actual file with content, not to a
     * directory).
     * 
     * @see File#canWrite()
     * @see File#isDirectory()
     */
    @Override
    public boolean isWritable() {
        return (this.file.canWrite() && !this.file.isDirectory());
    }

    /**
     * This implementation opens a FileOutputStream for the underlying file.
     * 
     * @see FileOutputStream
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(this.file);
    }

    /**
     * This implementation compares the underlying File references.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj == this || (obj instanceof FileSystemResource && this.path.equals(((FileSystemResource) obj).path)));
    }

    /**
     * This implementation returns the hash code of the underlying File
     * reference.
     */
    @Override
    public int hashCode() {
        return this.path.hashCode();
    }

}