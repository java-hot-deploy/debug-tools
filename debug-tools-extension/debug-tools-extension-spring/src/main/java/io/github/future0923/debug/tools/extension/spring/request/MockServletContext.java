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
package io.github.future0923.debug.tools.extension.spring.request;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MimeType;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author future0923
 */
public class MockServletContext implements ServletContext {

    /** Default Servlet name used by Tomcat, Jetty, JBoss, and GlassFish: {@value}. */
    private static final String COMMON_DEFAULT_SERVLET_NAME = "default";

    private static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir";

    private static final Set<SessionTrackingMode> DEFAULT_SESSION_TRACKING_MODES = new LinkedHashSet<>(4);

    static {
        DEFAULT_SESSION_TRACKING_MODES.add(SessionTrackingMode.COOKIE);
        DEFAULT_SESSION_TRACKING_MODES.add(SessionTrackingMode.URL);
        DEFAULT_SESSION_TRACKING_MODES.add(SessionTrackingMode.SSL);
    }


    private final Log logger = LogFactory.getLog(getClass());

    private final ResourceLoader resourceLoader;

    private final String resourceBasePath;

    private String contextPath = "";

    private final Map<String, ServletContext> contexts = new HashMap<>();

    private int majorVersion = 3;

    private int minorVersion = 1;

    private int effectiveMajorVersion = 3;

    private int effectiveMinorVersion = 1;

    private final Map<String, RequestDispatcher> namedRequestDispatchers = new HashMap<>();

    private String defaultServletName = COMMON_DEFAULT_SERVLET_NAME;

    private final Map<String, String> initParameters = new LinkedHashMap<>();

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    private String servletContextName = "MockServletContext";

    private final Set<String> declaredRoles = new LinkedHashSet<>();

    @Nullable
    private Set<SessionTrackingMode> sessionTrackingModes;

    private final SessionCookieConfig sessionCookieConfig = new MockSessionCookieConfig();

    private int sessionTimeout;

    @Nullable
    private String requestCharacterEncoding;

    @Nullable
    private String responseCharacterEncoding;

    private final Map<String, MediaType> mimeTypes = new LinkedHashMap<>();


    /**
     * Create a new {@code MockServletContext}, using no base path and a
     * {@link DefaultResourceLoader} (i.e. the classpath root as WAR root).
     * @see org.springframework.core.io.DefaultResourceLoader
     */
    public MockServletContext() {
        this("", null);
    }

    /**
     * Create a new {@code MockServletContext}, using a {@link DefaultResourceLoader}.
     * @param resourceBasePath the root directory of the WAR (should not end with a slash)
     * @see org.springframework.core.io.DefaultResourceLoader
     */
    public MockServletContext(String resourceBasePath) {
        this(resourceBasePath, null);
    }

    /**
     * Create a new {@code MockServletContext}, using the specified {@link ResourceLoader}
     * and no base path.
     * @param resourceLoader the ResourceLoader to use (or null for the default)
     */
    public MockServletContext(@Nullable ResourceLoader resourceLoader) {
        this("", resourceLoader);
    }


    public MockServletContext(String resourceBasePath, @Nullable ResourceLoader resourceLoader) {
        this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
        this.resourceBasePath = resourceBasePath;

        // Use JVM temp dir as ServletContext temp dir.
        String tempDir = System.getProperty(TEMP_DIR_SYSTEM_PROPERTY);
        if (tempDir != null) {
            this.attributes.put(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File(tempDir));
        }

        registerNamedDispatcher(this.defaultServletName, new MockRequestDispatcher(this.defaultServletName));
    }

    /**
     * Build a full resource location for the given path, prepending the resource
     * base path of this {@code MockServletContext}.
     * @param path the path as specified
     * @return the full resource path
     */
    protected String getResourceLocation(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return this.resourceBasePath + path;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    public void registerContext(String contextPath, ServletContext context) {
        this.contexts.put(contextPath, context);
    }

    @Override
    public ServletContext getContext(String contextPath) {
        if (this.contextPath.equals(contextPath)) {
            return this;
        }
        return this.contexts.get(contextPath);
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    @Override
    public int getMajorVersion() {
        return this.majorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    @Override
    public int getMinorVersion() {
        return this.minorVersion;
    }

    public void setEffectiveMajorVersion(int effectiveMajorVersion) {
        this.effectiveMajorVersion = effectiveMajorVersion;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return this.effectiveMajorVersion;
    }

    public void setEffectiveMinorVersion(int effectiveMinorVersion) {
        this.effectiveMinorVersion = effectiveMinorVersion;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return this.effectiveMinorVersion;
    }

    @Override
    @Nullable
    public String getMimeType(String filePath) {
        String extension = StringUtils.getFilenameExtension(filePath);
        if (this.mimeTypes.containsKey(extension)) {
            return this.mimeTypes.get(extension).toString();
        }
        else {
            return MediaTypeFactory.getMediaType(filePath).
                    map(MimeType::toString)
                    .orElse(null);
        }
    }

    /**
     * Adds a mime type mapping for use by {@link #getMimeType(String)}.
     * @param fileExtension a file extension, such as {@code txt}, {@code gif}
     * @param mimeType the mime type
     */
    public void addMimeType(String fileExtension, MediaType mimeType) {
        Assert.notNull(fileExtension, "'fileExtension' must not be null");
        this.mimeTypes.put(fileExtension, mimeType);
    }

    @Override
    @Nullable
    public Set<String> getResourcePaths(String path) {
        String actualPath = (path.endsWith("/") ? path : path + "/");
        String resourceLocation = getResourceLocation(actualPath);
        Resource resource = null;
        try {
            resource = this.resourceLoader.getResource(resourceLocation);
            File file = resource.getFile();
            String[] fileList = file.list();
            if (ObjectUtils.isEmpty(fileList)) {
                return null;
            }
            Set<String> resourcePaths = new LinkedHashSet<>(fileList.length);
            for (String fileEntry : fileList) {
                String resultPath = actualPath + fileEntry;
                if (resource.createRelative(fileEntry).getFile().isDirectory()) {
                    resultPath += "/";
                }
                resourcePaths.add(resultPath);
            }
            return resourcePaths;
        }
        catch (InvalidPathException | IOException ex ) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not get resource paths for " +
                        (resource != null ? resource : resourceLocation), ex);
            }
            return null;
        }
    }

    @Override
    @Nullable
    public URL getResource(String path) throws MalformedURLException {
        String resourceLocation = getResourceLocation(path);
        Resource resource = null;
        try {
            resource = this.resourceLoader.getResource(resourceLocation);
            if (!resource.exists()) {
                return null;
            }
            return resource.getURL();
        }
        catch (MalformedURLException ex) {
            throw ex;
        }
        catch (InvalidPathException | IOException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not get URL for resource " +
                        (resource != null ? resource : resourceLocation), ex);
            }
            return null;
        }
    }

    @Override
    @Nullable
    public InputStream getResourceAsStream(String path) {
        String resourceLocation = getResourceLocation(path);
        Resource resource = null;
        try {
            resource = this.resourceLoader.getResource(resourceLocation);
            if (!resource.exists()) {
                return null;
            }
            return resource.getInputStream();
        }
        catch (InvalidPathException | IOException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not open InputStream for resource " +
                        (resource != null ? resource : resourceLocation), ex);
            }
            return null;
        }
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        Assert.isTrue(path.startsWith("/"),
                () -> "RequestDispatcher path [" + path + "] at ServletContext level must start with '/'");
        return new MockRequestDispatcher(path);
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String path) {
        return this.namedRequestDispatchers.get(path);
    }

    public void registerNamedDispatcher(String name, RequestDispatcher requestDispatcher) {
        Assert.notNull(name, "RequestDispatcher name must not be null");
        Assert.notNull(requestDispatcher, "RequestDispatcher must not be null");
        this.namedRequestDispatchers.put(name, requestDispatcher);
    }

    /**
     * Unregister the {@link RequestDispatcher} with the given name.
     * @param name the name of the dispatcher to unregister
     * @see #getNamedDispatcher
     * @see #registerNamedDispatcher
     */
    public void unregisterNamedDispatcher(String name) {
        Assert.notNull(name, "RequestDispatcher name must not be null");
        this.namedRequestDispatchers.remove(name);
    }

    /**
     * Get the name of the <em>default</em> {@code Servlet}.
     * <p>Defaults to {@literal 'default'}.
     * @see #setDefaultServletName
     */
    public String getDefaultServletName() {
        return this.defaultServletName;
    }

    public void setDefaultServletName(String defaultServletName) {
        Assert.hasText(defaultServletName, "defaultServletName must not be null or empty");
        unregisterNamedDispatcher(this.defaultServletName);
        this.defaultServletName = defaultServletName;
        registerNamedDispatcher(this.defaultServletName, new MockRequestDispatcher(this.defaultServletName));
    }

    @Deprecated
    @Override
    @Nullable
    public Servlet getServlet(String name) {
        return null;
    }

    @Override
    @Deprecated
    public Enumeration<Servlet> getServlets() {
        return Collections.enumeration(Collections.emptySet());
    }

    @Override
    @Deprecated
    public Enumeration<String> getServletNames() {
        return Collections.enumeration(Collections.emptySet());
    }

    @Override
    public void log(String message) {
        logger.info(message);
    }

    @Override
    @Deprecated
    public void log(Exception ex, String message) {
        logger.info(message, ex);
    }

    @Override
    public void log(String message, Throwable ex) {
        logger.info(message, ex);
    }

    @Override
    @Nullable
    public String getRealPath(String path) {
        String resourceLocation = getResourceLocation(path);
        Resource resource = null;
        try {
            resource = this.resourceLoader.getResource(resourceLocation);
            return resource.getFile().getAbsolutePath();
        }
        catch (InvalidPathException | IOException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not determine real path of resource " +
                        (resource != null ? resource : resourceLocation), ex);
            }
            return null;
        }
    }

    @Override
    public String getServerInfo() {
        return "MockServletContext";
    }

    @Override
    public String getInitParameter(String name) {
        Assert.notNull(name, "Parameter name must not be null");
        return this.initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initParameters.keySet());
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        Assert.notNull(name, "Parameter name must not be null");
        if (this.initParameters.containsKey(name)) {
            return false;
        }
        this.initParameters.put(name, value);
        return true;
    }

    public void addInitParameter(String name, String value) {
        Assert.notNull(name, "Parameter name must not be null");
        this.initParameters.put(name, value);
    }

    @Override
    @Nullable
    public Object getAttribute(String name) {
        Assert.notNull(name, "Attribute name must not be null");
        return this.attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(new LinkedHashSet<>(this.attributes.keySet()));
    }

    @Override
    public void setAttribute(String name, @Nullable Object value) {
        Assert.notNull(name, "Attribute name must not be null");
        if (value != null) {
            this.attributes.put(name, value);
        }
        else {
            this.attributes.remove(name);
        }
    }

    @Override
    public void removeAttribute(String name) {
        Assert.notNull(name, "Attribute name must not be null");
        this.attributes.remove(name);
    }

    public void setServletContextName(String servletContextName) {
        this.servletContextName = servletContextName;
    }

    @Override
    public String getServletContextName() {
        return this.servletContextName;
    }

    @Override
    @Nullable
    public ClassLoader getClassLoader() {
        return ClassUtils.getDefaultClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames) {
        Assert.notNull(roleNames, "Role names array must not be null");
        for (String roleName : roleNames) {
            Assert.hasLength(roleName, "Role name must not be empty");
            this.declaredRoles.add(roleName);
        }
    }

    public Set<String> getDeclaredRoles() {
        return Collections.unmodifiableSet(this.declaredRoles);
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
            throws IllegalStateException, IllegalArgumentException {
        this.sessionTrackingModes = sessionTrackingModes;
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return DEFAULT_SESSION_TRACKING_MODES;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return (this.sessionTrackingModes != null ?
                Collections.unmodifiableSet(this.sessionTrackingModes) : DEFAULT_SESSION_TRACKING_MODES);
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return this.sessionCookieConfig;
    }

    @Override  // on Servlet 4.0
    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    @Override  // on Servlet 4.0
    public int getSessionTimeout() {
        return this.sessionTimeout;
    }

    @Override  // on Servlet 4.0
    public void setRequestCharacterEncoding(@Nullable String requestCharacterEncoding) {
        this.requestCharacterEncoding = requestCharacterEncoding;
    }

    @Override  // on Servlet 4.0
    @Nullable
    public String getRequestCharacterEncoding() {
        return this.requestCharacterEncoding;
    }

    @Override  // on Servlet 4.0
    public void setResponseCharacterEncoding(@Nullable String responseCharacterEncoding) {
        this.responseCharacterEncoding = responseCharacterEncoding;
    }

    @Override  // on Servlet 4.0
    @Nullable
    public String getResponseCharacterEncoding() {
        return this.responseCharacterEncoding;
    }


    //---------------------------------------------------------------------
    // Unsupported Servlet 3.0 registration methods
    //---------------------------------------------------------------------

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        throw new UnsupportedOperationException();
    }

    @Override  // on Servlet 4.0
    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
        throw new UnsupportedOperationException();
    }

    /**
     * This method always returns {@code null}.
     * @see javax.servlet.ServletContext#getServletRegistration(String)
     */
    @Override
    @Nullable
    public ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    /**
     * This method always returns an {@linkplain Collections#emptyMap empty map}.
     * @see javax.servlet.ServletContext#getServletRegistrations()
     */
    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return Collections.emptyMap();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
        throw new UnsupportedOperationException();
    }

    /**
     * This method always returns {@code null}.
     * @see javax.servlet.ServletContext#getFilterRegistration(String)
     */
    @Override
    @Nullable
    public FilterRegistration getFilterRegistration(String filterName) {
        return null;
    }

    /**
     * This method always returns an {@linkplain Collections#emptyMap empty map}.
     * @see javax.servlet.ServletContext#getFilterRegistrations()
     */
    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return Collections.emptyMap();
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> c) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVirtualServerName() {
        throw new UnsupportedOperationException();
    }

}