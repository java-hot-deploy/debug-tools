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
package io.github.future0923.debug.tools.hotswap.core.util.spring.util;


import java.util.Collection;
import java.util.Map;

/**
 * Assertion utility class that assists in validating arguments.
 *
 * <p>
 * Useful for identifying programmer errors early and clearly at runtime.
 *
 * <p>
 * For example, if the contract of a public method states it does not allow
 * {@code null} arguments, {@code Assert} can be used to validate that contract.
 * Doing this clearly indicates a contract violation when it occurs and protects
 * the class's invariants.
 *
 * <p>
 * Typically used to validate method arguments rather than configuration
 * properties, to check for cases that are usually programmer errors rather than
 * configuration errors. In contrast to configuration initialization code, there
 * is usually no point in falling back to defaults in such methods.
 *
 * <p>
 * This class is similar to JUnit's assertion library. If an argument value is
 * deemed invalid, an {@link IllegalArgumentException} is thrown (typically).
 * For example:
 *
 * <pre class="code">
 * Assert.notNull(clazz, "The class must not be null");
 * Assert.isTrue(i > 0, "The value must be greater than zero");
 * </pre>
 *
 * <p>
 * Mainly for internal use within the framework; consider
 * <a href="http://commons.apache.org/proper/commons-lang/">Apache's Commons
 * Lang</a> for a more comprehensive suite of {@code String} utilities.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @author Rob Harrop
 * @author Sam Brannen
 * @since 1.1.2
 */
public abstract class Assert {

    /**
     * Assert a boolean expression, throwing {@code IllegalArgumentException} if
     * the test result is {@code false}.
     * 
     * <pre class="code">
     * Assert.isTrue(i &gt; 0, "The value must be greater than zero");
     * </pre>
     * 
     * @param expression
     *            a boolean expression
     * @param message
     *            the exception message to use if the assertion fails
     * @throws IllegalArgumentException
     *             if expression is {@code false}
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert a boolean expression, throwing {@code IllegalArgumentException} if
     * the test result is {@code false}.
     * 
     * <pre class="code">
     * Assert.isTrue(i &gt; 0);
     * </pre>
     * 
     * @param expression
     *            a boolean expression
     * @throws IllegalArgumentException
     *             if expression is {@code false}
     */
    public static void isTrue(boolean expression) {
        isTrue(expression, "[Assertion failed] - this expression must be true");
    }

    /**
     * Assert that an object is {@code null} .
     * 
     * <pre class="code">
     * Assert.isNull(value, "The value must be null");
     * </pre>
     * 
     * @param object
     *            the object to check
     * @param message
     *            the exception message to use if the assertion fails
     * @throws IllegalArgumentException
     *             if the object is not {@code null}
     */
    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an object is {@code null} .
     * 
     * <pre class="code">
     * Assert.isNull(value);
     * </pre>
     * 
     * @param object
     *            the object to check
     * @throws IllegalArgumentException
     *             if the object is not {@code null}
     */
    public static void isNull(Object object) {
        isNull(object, "[Assertion failed] - the object argument must be null");
    }

    /**
     * Assert that an object is not {@code null} .
     * 
     * <pre class="code">
     * Assert.notNull(clazz, "The class must not be null");
     * </pre>
     * 
     * @param object
     *            the object to check
     * @param message
     *            the exception message to use if the assertion fails
     * @throws IllegalArgumentException
     *             if the object is {@code null}
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an object is not {@code null} .
     * 
     * <pre class="code">
     * Assert.notNull(clazz);
     * </pre>
     * 
     * @param object
     *            the object to check
     * @throws IllegalArgumentException
     *             if the object is {@code null}
     */
    public static void notNull(Object object) {
        notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    /**
     * Assert that the given String is not empty; that is, it must not be
     * {@code null} and not the empty String.
     * 
     * <pre class="code">
     * Assert.hasLength(name, "Name must not be empty");
     * </pre>
     * 
     * @param text
     *            the String to check
     * @param message
     *            the exception message to use if the assertion fails
     * @see StringUtils#hasLength
     * @throws IllegalArgumentException
     *             if the text is empty
     */
    public static void hasLength(String text, String message) {
        if (!StringUtils.hasLength(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that the given String is not empty; that is, it must not be
     * {@code null} and not the empty String.
     * 
     * <pre class="code">
     * Assert.hasLength(name);
     * </pre>
     * 
     * @param text
     *            the String to check
     * @see StringUtils#hasLength
     * @throws IllegalArgumentException
     *             if the text is empty
     */
    public static void hasLength(String text) {
        hasLength(text, "[Assertion failed] - this String argument must have length; it must not be null or empty");
    }

    /**
     * Assert that the given String has valid text content; that is, it must not
     * be {@code null} and must contain at least one non-whitespace character.
     * 
     * <pre class="code">
     * Assert.hasText(name, "'name' must not be empty");
     * </pre>
     * 
     * @param text
     *            the String to check
     * @param message
     *            the exception message to use if the assertion fails
     * @see StringUtils#hasText
     * @throws IllegalArgumentException
     *             if the text does not contain valid text content
     */
    public static void hasText(String text, String message) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that the given String has valid text content; that is, it must not
     * be {@code null} and must contain at least one non-whitespace character.
     * 
     * <pre class="code">
     * Assert.hasText(name, "'name' must not be empty");
     * </pre>
     * 
     * @param text
     *            the String to check
     * @see StringUtils#hasText
     * @throws IllegalArgumentException
     *             if the text does not contain valid text content
     */
    public static void hasText(String text) {
        hasText(text, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
    }

    /**
     * Assert that the given text does not contain the given substring.
     * 
     * <pre class="code">
     * Assert.doesNotContain(name, "rod", "Name must not contain 'rod'");
     * </pre>
     * 
     * @param textToSearch
     *            the text to search
     * @param substring
     *            the substring to find within the text
     * @param message
     *            the exception message to use if the assertion fails
     * @throws IllegalArgumentException
     *             if the text contains the substring
     */
    public static void doesNotContain(String textToSearch, String substring, String message) {
        if (StringUtils.hasLength(textToSearch) && StringUtils.hasLength(substring) && textToSearch.contains(substring)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that the given text does not contain the given substring.
     * 
     * <pre class="code">
     * Assert.doesNotContain(name, "rod");
     * </pre>
     * 
     * @param textToSearch
     *            the text to search
     * @param substring
     *            the substring to find within the text
     * @throws IllegalArgumentException
     *             if the text contains the substring
     */
    public static void doesNotContain(String textToSearch, String substring) {
        doesNotContain(textToSearch, substring, "[Assertion failed] - this String argument must not contain the substring [" + substring + "]");
    }

    /**
     * Assert that an array has elements; that is, it must not be {@code null}
     * and must have at least one element.
     * 
     * <pre class="code">
     * Assert.notEmpty(array, "The array must have elements");
     * </pre>
     * 
     * @param array
     *            the array to check
     * @param message
     *            the exception message to use if the assertion fails
     * @throws IllegalArgumentException
     *             if the object array is {@code null} or has no elements
     */
    public static void notEmpty(Object[] array, String message) {
        if (ObjectUtils.isEmpty(array)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an array has elements; that is, it must not be {@code null}
     * and must have at least one element.
     * 
     * <pre class="code">
     * Assert.notEmpty(array);
     * </pre>
     * 
     * @param array
     *            the array to check
     * @throws IllegalArgumentException
     *             if the object array is {@code null} or has no elements
     */
    public static void notEmpty(Object[] array) {
        notEmpty(array, "[Assertion failed] - this array must not be empty: it must contain at least 1 element");
    }

    /**
     * Assert that an array has no null elements. Note: Does not complain if the
     * array is empty!
     * 
     * <pre class="code">
     * Assert.noNullElements(array, "The array must have non-null elements");
     * </pre>
     * 
     * @param array
     *            the array to check
     * @param message
     *            the exception message to use if the assertion fails
     * @throws IllegalArgumentException
     *             if the object array contains a {@code null} element
     */
    public static void noNullElements(Object[] array, String message) {
        if (array != null) {
            for (Object element : array) {
                if (element == null) {
                    throw new IllegalArgumentException(message);
                }
            }
        }
    }

    /**
     * Assert that an array has no null elements. Note: Does not complain if the
     * array is empty!
     * 
     * <pre class="code">
     * Assert.noNullElements(array);
     * </pre>
     * 
     * @param array
     *            the array to check
     * @throws IllegalArgumentException
     *             if the object array contains a {@code null} element
     */
    public static void noNullElements(Object[] array) {
        noNullElements(array, "[Assertion failed] - this array must not contain any null elements");
    }

    /**
     * Assert that a collection has elements; that is, it must not be
     * {@code null} and must have at least one element.
     * 
     * <pre class="code">
     * Assert.notEmpty(collection, "Collection must have elements");
     * </pre>
     * 
     * @param collection
     *            the collection to check
     * @param message
     *            the exception message to use if the assertion fails
     * @throws IllegalArgumentException
     *             if the collection is {@code null} or has no elements
     */
    public static void notEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that a collection has elements; that is, it must not be
     * {@code null} and must have at least one element.
     * 
     * <pre class="code">
     * Assert.notEmpty(collection, "Collection must have elements");
     * </pre>
     * 
     * @param collection
     *            the collection to check
     * @throws IllegalArgumentException
     *             if the collection is {@code null} or has no elements
     */
    public static void notEmpty(Collection<?> collection) {
        notEmpty(collection, "[Assertion failed] - this collection must not be empty: it must contain at least 1 element");
    }

    /**
     * Assert that a Map has entries; that is, it must not be {@code null} and
     * must have at least one entry.
     * 
     * <pre class="code">
     * Assert.notEmpty(map, "Map must have entries");
     * </pre>
     * 
     * @param map
     *            the map to check
     * @param message
     *            the exception message to use if the assertion fails
     * @throws IllegalArgumentException
     *             if the map is {@code null} or has no entries
     */
    public static void notEmpty(Map<?, ?> map, String message) {
        if (CollectionUtils.isEmpty(map)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that a Map has entries; that is, it must not be {@code null} and
     * must have at least one entry.
     * 
     * <pre class="code">
     * Assert.notEmpty(map);
     * </pre>
     * 
     * @param map
     *            the map to check
     * @throws IllegalArgumentException
     *             if the map is {@code null} or has no entries
     */
    public static void notEmpty(Map<?, ?> map) {
        notEmpty(map, "[Assertion failed] - this map must not be empty; it must contain at least one entry");
    }

    /**
     * Assert that the provided object is an instance of the provided class.
     * 
     * <pre class="code">
     * Assert.instanceOf(Foo.class, foo);
     * </pre>
     * 
     * @param clazz
     *            the required class
     * @param obj
     *            the object to check
     * @throws IllegalArgumentException
     *             if the object is not an instance of clazz
     * @see Class#isInstance
     */
    public static void isInstanceOf(Class<?> clazz, Object obj) {
        isInstanceOf(clazz, obj, "");
    }

    /**
     * Assert that the provided object is an instance of the provided class.
     * 
     * <pre class="code">
     * Assert.instanceOf(Foo.class, foo);
     * </pre>
     * 
     * @param type
     *            the type to check against
     * @param obj
     *            the object to check
     * @param message
     *            a message which will be prepended to the message produced by
     *            the function itself, and which may be used to provide context.
     *            It should normally end in ":" or "." so that the generated
     *            message looks OK when appended to it.
     * @throws IllegalArgumentException
     *             if the object is not an instance of clazz
     * @see Class#isInstance
     */
    public static void isInstanceOf(Class<?> type, Object obj, String message) {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            throw new IllegalArgumentException((StringUtils.hasLength(message) ? message + " " : "") + "Object of class [" + (obj != null ? obj.getClass().getName() : "null") + "] must be an instance of " + type);
        }
    }

    /**
     * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
     * 
     * <pre class="code">
     * Assert.isAssignable(Number.class, myClass);
     * </pre>
     * 
     * @param superType
     *            the super type to check
     * @param subType
     *            the sub type to check
     * @throws IllegalArgumentException
     *             if the classes are not assignable
     */
    public static void isAssignable(Class<?> superType, Class<?> subType) {
        isAssignable(superType, subType, "");
    }

    /**
     * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
     * 
     * <pre class="code">
     * Assert.isAssignable(Number.class, myClass);
     * </pre>
     * 
     * @param superType
     *            the super type to check against
     * @param subType
     *            the sub type to check
     * @param message
     *            a message which will be prepended to the message produced by
     *            the function itself, and which may be used to provide context.
     *            It should normally end in ":" or "." so that the generated
     *            message looks OK when appended to it.
     * @throws IllegalArgumentException
     *             if the classes are not assignable
     */
    public static void isAssignable(Class<?> superType, Class<?> subType, String message) {
        notNull(superType, "Type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            throw new IllegalArgumentException((StringUtils.hasLength(message) ? message + " " : "") + subType + " is not assignable to " + superType);
        }
    }

    /**
     * Assert a boolean expression, throwing {@code IllegalStateException} if
     * the test result is {@code false}. Call isTrue if you wish to throw
     * IllegalArgumentException on an assertion failure.
     * 
     * <pre class="code">
     * Assert.state(id == null, "The id property must not already be initialized");
     * </pre>
     * 
     * @param expression
     *            a boolean expression
     * @param message
     *            the exception message to use if the assertion fails
     * @throws IllegalStateException
     *             if expression is {@code false}
     */
    public static void state(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Assert a boolean expression, throwing {@link IllegalStateException} if
     * the test result is {@code false}.
     * <p>
     * Call {@link #isTrue(boolean)} if you wish to throw
     * {@link IllegalArgumentException} on an assertion failure.
     * 
     * <pre class="code">
     * Assert.state(id == null);
     * </pre>
     * 
     * @param expression
     *            a boolean expression
     * @throws IllegalStateException
     *             if the supplied expression is {@code false}
     */
    public static void state(boolean expression) {
        state(expression, "[Assertion failed] - this state invariant must be true");
    }

}