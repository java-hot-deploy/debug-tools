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
package io.github.future0923.debug.tools.hotswap.core.util.signature;

import javassist.bytecode.Descriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Java类签名
 */
public class JavaClassSignature extends ClassSignatureBase {

    private final Class<?> clazz;

    public JavaClassSignature(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String getValue() throws Exception {
        List<String> strings = new ArrayList<>();
        if (hasElement(ClassSignatureElement.METHOD)) {
            boolean usePrivateMethod = hasElement(ClassSignatureElement.METHOD_PRIVATE);
            boolean useStaticMethod = hasElement(ClassSignatureElement.METHOD_STATIC);
            for (Method method : clazz.getDeclaredMethods()) {
                if (!usePrivateMethod && Modifier.isPrivate(method.getModifiers())) {
                    continue;
                }
                if (!useStaticMethod && Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                if (method.getName().startsWith(SWITCH_TABLE_METHOD_PREFIX)
                        || method.getName().startsWith(CLASS_CLINIT_METHOD_NAME)) {
                    continue;
                }
                strings.add(getMethodString(method));
            }
        }

        if (hasElement(ClassSignatureElement.CONSTRUCTOR)) {
            boolean usePrivateConstructor = hasElement(ClassSignatureElement.CONSTRUCTOR_PRIVATE);
            for (Constructor<?> method : clazz.getDeclaredConstructors()) {
                if (!usePrivateConstructor && Modifier.isPrivate(method.getModifiers())) {
                    continue;
                }
                strings.add(getConstructorString(method));
            }
        }

        if (hasElement(ClassSignatureElement.CLASS_ANNOTATION)) {
            strings.add(annotationToString(clazz.getAnnotations()));
        }

        if (hasElement(ClassSignatureElement.INTERFACES)) {
            for (Class<?> iClass : clazz.getInterfaces()) {
                strings.add(iClass.getName());
            }
        }

        if (hasElement(ClassSignatureElement.SUPER_CLASS)) {
            if (clazz.getSuperclass() != null && !clazz.getSuperclass().getName().equals(Object.class.getName())) {
                strings.add(clazz.getSuperclass().getName());
            }
        }

        if (hasElement(ClassSignatureElement.FIELD)) {
            boolean useStaticField = hasElement(ClassSignatureElement.FIELD_STATIC);
            boolean useFieldAnnotation = hasElement(ClassSignatureElement.FIELD_ANNOTATION);
            for (Field field : clazz.getDeclaredFields()) {
                if (!useStaticField && Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (field.getName().startsWith(SWITCH_TABLE_METHOD_PREFIX)) {
                    continue;
                }
                String fieldSignature = field.getType().getName() + " " + field.getName();
                if (useFieldAnnotation) {
                    fieldSignature += annotationToString(field.getAnnotations());
                }
                strings.add(fieldSignature + ";");
            }
        }

        Collections.sort(strings);
        StringBuilder strBuilder = new StringBuilder();
        for (String methodString : strings) {
            strBuilder.append(methodString);
        }

        return strBuilder.toString();
    }

    private String getConstructorString(Constructor<?> method) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(Modifier.toString(method.getModifiers())).append(" ");
        strBuilder.append(method.getName());
        strBuilder.append(getParams(method.getParameterTypes()));
        if (hasElement(ClassSignatureElement.METHOD_ANNOTATION)) {
            strBuilder.append(annotationToString(method.getDeclaredAnnotations()));
        }
        if (hasElement(ClassSignatureElement.METHOD_PARAM_ANNOTATION)) {
            strBuilder.append(annotationToString(method.getParameterAnnotations()));
        }
        if (hasElement(ClassSignatureElement.METHOD_EXCEPTION)) {
            strBuilder.append(Arrays.toString(sort(method.getExceptionTypes())));
        }
        strBuilder.append(";");
        return strBuilder.toString();
    }

    private String getMethodString(Method method) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(Modifier.toString(method.getModifiers())).append(" ");
        strBuilder.append(getName(method.getReturnType())).append(" ").append(method.getName());
        strBuilder.append(getParams(method.getParameterTypes()));
        if (hasElement(ClassSignatureElement.METHOD_ANNOTATION)) {
            strBuilder.append(annotationToString(method.getDeclaredAnnotations()));
        }
        if (hasElement(ClassSignatureElement.METHOD_PARAM_ANNOTATION)) {
            strBuilder.append(annotationToString(method.getParameterAnnotations()));
        }
        if (hasElement(ClassSignatureElement.METHOD_EXCEPTION)) {
            strBuilder.append(Arrays.toString(sort(method.getExceptionTypes())));
        }
        strBuilder.append(";");
        return strBuilder.toString();
    }

    private <T> T[] sort(T[] a) {
        a = Arrays.copyOf(a, a.length);
        Arrays.sort(a, ToStringComparator.INSTANCE);
        return a;
    }

    private String getParams(Class<?>[] parameterTypes) {
        StringBuilder strB = new StringBuilder("(");
        boolean first = true;
        for (Class<?> ctClass : parameterTypes) {
            if (!first) {
                strB.append(",");
            }
            else {
                first = false;
            }
            strB.append(getName(ctClass));
        }
        strB.append(")");
        return strB.toString();
    }

    private String getName(Class<?> ctClass) {
        if (ctClass.isArray()) {
            return Descriptor.toString(ctClass.getName());
        } else {
            return ctClass.getName();
        }
    }

}
