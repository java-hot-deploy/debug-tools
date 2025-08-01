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

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * @author future0923
 */
public class MockHttpSession implements HttpSession {

    /**
     * The session cookie name.
     */
    public static final String SESSION_COOKIE_NAME = "JSESSION";


    private static int nextId = 1;

    private String id;

    private final long creationTime = System.currentTimeMillis();

    private int maxInactiveInterval;

    private long lastAccessedTime = System.currentTimeMillis();

    private final ServletContext servletContext;

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    private boolean invalid = false;

    private boolean isNew = true;


    /**
     * Create a new MockHttpSession with a default {@link MockServletContext}.
     * @see MockServletContext
     */
    public MockHttpSession() {
        this(null);
    }

    /**
     * Create a new MockHttpSession.
     * @param servletContext the ServletContext that the session runs in
     */
    public MockHttpSession(@Nullable ServletContext servletContext) {
        this(servletContext, null);
    }

    /**
     * Create a new MockHttpSession.
     * @param servletContext the ServletContext that the session runs in
     * @param id a unique identifier for this session
     */
    public MockHttpSession(@Nullable ServletContext servletContext, @Nullable String id) {
        this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
        this.id = (id != null ? id : Integer.toString(nextId++));
    }


    @Override
    public long getCreationTime() {
        assertIsValid();
        return this.creationTime;
    }

    @Override
    public String getId() {
        return this.id;
    }

    /**
     * As of Servlet 3.1, the id of a session can be changed.
     * @return the new session id
     * @since 4.0.3
     */
    public String changeSessionId() {
        this.id = Integer.toString(nextId++);
        return this.id;
    }

    public void access() {
        this.lastAccessedTime = System.currentTimeMillis();
        this.isNew = false;
    }

    @Override
    public long getLastAccessedTime() {
        assertIsValid();
        return this.lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @Override
    public javax.servlet.http.HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException("getSessionContext");
    }

    @Override
    public Object getAttribute(String name) {
        assertIsValid();
        Assert.notNull(name, "Attribute name must not be null");
        return this.attributes.get(name);
    }

    @Override
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        assertIsValid();
        return Collections.enumeration(new LinkedHashSet<>(this.attributes.keySet()));
    }

    @Override
    public String[] getValueNames() {
        assertIsValid();
        return StringUtils.toStringArray(this.attributes.keySet());
    }

    @Override
    public void setAttribute(String name, @Nullable Object value) {
        assertIsValid();
        Assert.notNull(name, "Attribute name must not be null");
        if (value != null) {
            Object oldValue = this.attributes.put(name, value);
            if (value != oldValue) {
                if (oldValue instanceof HttpSessionBindingListener) {
                    ((HttpSessionBindingListener) oldValue).valueUnbound(new HttpSessionBindingEvent(this, name, oldValue));
                }
                if (value instanceof HttpSessionBindingListener) {
                    ((HttpSessionBindingListener) value).valueBound(new HttpSessionBindingEvent(this, name, value));
                }
            }
        }
        else {
            removeAttribute(name);
        }
    }

    @Override
    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        assertIsValid();
        Assert.notNull(name, "Attribute name must not be null");
        Object value = this.attributes.remove(name);
        if (value instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
        }
    }

    @Override
    public void removeValue(String name) {
        removeAttribute(name);
    }

    /**
     * Clear all of this session's attributes.
     */
    public void clearAttributes() {
        for (Iterator<Map.Entry<String, Object>> it = this.attributes.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Object> entry = it.next();
            String name = entry.getKey();
            Object value = entry.getValue();
            it.remove();
            if (value instanceof HttpSessionBindingListener) {
                ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
            }
        }
    }

    /**
     * Invalidates this session then unbinds any objects bound to it.
     * @throws IllegalStateException if this method is called on an already invalidated session
     */
    @Override
    public void invalidate() {
        assertIsValid();
        this.invalid = true;
        clearAttributes();
    }

    public boolean isInvalid() {
        return this.invalid;
    }

    /**
     * Convenience method for asserting that this session has not been
     * {@linkplain #invalidate() invalidated}.
     * @throws IllegalStateException if this session has been invalidated
     */
    private void assertIsValid() {
        Assert.state(!isInvalid(), "The session has already been invalidated");
    }

    public void setNew(boolean value) {
        this.isNew = value;
    }

    @Override
    public boolean isNew() {
        assertIsValid();
        return this.isNew;
    }

    /**
     * Serialize the attributes of this session into an object that can be
     * turned into a byte array with standard Java serialization.
     * @return a representation of this session's serialized state
     */
    public Serializable serializeState() {
        HashMap<String, Serializable> state = new HashMap<>();
        for (Iterator<Map.Entry<String, Object>> it = this.attributes.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Object> entry = it.next();
            String name = entry.getKey();
            Object value = entry.getValue();
            it.remove();
            if (value instanceof Serializable) {
                state.put(name, (Serializable) value);
            }
            else {
                // Not serializable... Servlet containers usually automatically
                // unbind the attribute in this case.
                if (value instanceof HttpSessionBindingListener) {
                    ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
                }
            }
        }
        return state;
    }

    /**
     * Deserialize the attributes of this session from a state object created by
     * {@link #serializeState()}.
     * @param state a representation of this session's serialized state
     */
    @SuppressWarnings("unchecked")
    public void deserializeState(Serializable state) {
        Assert.isTrue(state instanceof Map, "Serialized state needs to be of type [java.util.Map]");
        this.attributes.putAll((Map<String, Object>) state);
    }

}
