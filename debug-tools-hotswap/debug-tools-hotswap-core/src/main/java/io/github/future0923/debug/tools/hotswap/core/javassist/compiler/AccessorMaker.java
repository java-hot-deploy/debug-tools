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
package io.github.future0923.debug.tools.hotswap.core.javassist.compiler;

import java.util.HashMap;
import java.util.Map;

import io.github.future0923.debug.tools.hotswap.core.javassist.CannotCompileException;
import io.github.future0923.debug.tools.hotswap.core.javassist.ClassPool;
import io.github.future0923.debug.tools.hotswap.core.javassist.CtClass;
import io.github.future0923.debug.tools.hotswap.core.javassist.NotFoundException;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.AccessFlag;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.Bytecode;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.ClassFile;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.ConstPool;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.Descriptor;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.ExceptionsAttribute;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.FieldInfo;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.MethodInfo;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.SyntheticAttribute;

/**
 * AccessorMaker maintains accessors to private members of an enclosing
 * class.  It is necessary for compiling a method in an inner class.
 */
public class AccessorMaker {
    private CtClass clazz;
    private int uniqueNumber;
    private Map<String,Object> accessors;

    static final String lastParamType = "io.github.future0923.debug.tools.hotswap.core.javassist.runtime.Inner";

    public AccessorMaker(CtClass c) {
        clazz = c;
        uniqueNumber = 1;
        accessors = new HashMap<String,Object>();
    }

    public String getConstructor(CtClass c, String desc, MethodInfo orig)
        throws CompileError
    {
        String key = "<init>:" + desc;
        String consDesc = (String)accessors.get(key);
        if (consDesc != null)
            return consDesc;     // already exists.

        consDesc = Descriptor.appendParameter(lastParamType, desc);
        ClassFile cf = clazz.getClassFile();    // turn on the modified flag.
        try {
            ConstPool cp = cf.getConstPool();
            ClassPool pool = clazz.getClassPool();
            MethodInfo minfo
                = new MethodInfo(cp, MethodInfo.nameInit, consDesc);
            minfo.setAccessFlags(0);
            minfo.addAttribute(new SyntheticAttribute(cp));
            ExceptionsAttribute ea = orig.getExceptionsAttribute();
            if (ea != null)
                minfo.addAttribute(ea.copy(cp, null));

            CtClass[] params = Descriptor.getParameterTypes(desc, pool);
            Bytecode code = new Bytecode(cp);
            code.addAload(0);
            int regno = 1;
            for (int i = 0; i < params.length; ++i)
                regno += code.addLoad(regno, params[i]);
            code.setMaxLocals(regno + 1);    // the last parameter is added.
            code.addInvokespecial(clazz, MethodInfo.nameInit, desc);

            code.addReturn(null);
            minfo.setCodeAttribute(code.toCodeAttribute());
            cf.addMethod(minfo);
        }
        catch (CannotCompileException e) {
            throw new CompileError(e);
        }
        catch (NotFoundException e) {
            throw new CompileError(e);
        }

        accessors.put(key, consDesc);
        return consDesc;
    }

    /**
     * Returns the name of the method for accessing a private method.
     *
     * @param name      the name of the private method.
     * @param desc      the descriptor of the private method.
     * @param accDesc   the descriptor of the accessor method.  The first
     *                  parameter type is <code>clazz</code>.
     *                  If the private method is static,
     *              <code>accDesc</code> must be identical to <code>desc</code>.
     *
     * @param orig      the method info of the private method.
     * @return
     */
    public String getMethodAccessor(String name, String desc, String accDesc,
                                    MethodInfo orig)
        throws CompileError
    {
        String key = name + ":" + desc;
        String accName = (String)accessors.get(key);
        if (accName != null)
            return accName;     // already exists.

        ClassFile cf = clazz.getClassFile();    // turn on the modified flag.
        accName = findAccessorName(cf);
        try {
            ConstPool cp = cf.getConstPool();
            ClassPool pool = clazz.getClassPool();
            MethodInfo minfo
                = new MethodInfo(cp, accName, accDesc);
            minfo.setAccessFlags(AccessFlag.STATIC);
            minfo.addAttribute(new SyntheticAttribute(cp));
            ExceptionsAttribute ea = orig.getExceptionsAttribute();
            if (ea != null)
                minfo.addAttribute(ea.copy(cp, null));

            CtClass[] params = Descriptor.getParameterTypes(accDesc, pool);
            int regno = 0;
            Bytecode code = new Bytecode(cp);
            for (int i = 0; i < params.length; ++i)
                regno += code.addLoad(regno, params[i]);

            code.setMaxLocals(regno);
            if (desc == accDesc)
                code.addInvokestatic(clazz, name, desc);
            else
                code.addInvokevirtual(clazz, name, desc);

            code.addReturn(Descriptor.getReturnType(desc, pool));
            minfo.setCodeAttribute(code.toCodeAttribute());
            cf.addMethod(minfo);
        }
        catch (CannotCompileException e) {
            throw new CompileError(e);
        }
        catch (NotFoundException e) {
            throw new CompileError(e);
        }

        accessors.put(key, accName);
        return accName;
    }

    /**
     * Returns the method_info representing the added getter.
     */
    public MethodInfo getFieldGetter(FieldInfo finfo, boolean is_static)
        throws CompileError
    {
        String fieldName = finfo.getName();
        String key = fieldName + ":getter";
        Object res = accessors.get(key);
        if (res != null)
            return (MethodInfo)res;     // already exists.

        ClassFile cf = clazz.getClassFile();    // turn on the modified flag.
        String accName = findAccessorName(cf);
        try {
            ConstPool cp = cf.getConstPool();
            ClassPool pool = clazz.getClassPool();
            String fieldType = finfo.getDescriptor();
            String accDesc;
            if (is_static)
                accDesc = "()" + fieldType;
            else
                accDesc = "(" + Descriptor.of(clazz) + ")" + fieldType;

            MethodInfo minfo = new MethodInfo(cp, accName, accDesc);
            minfo.setAccessFlags(AccessFlag.STATIC);
            minfo.addAttribute(new SyntheticAttribute(cp));
            Bytecode code = new Bytecode(cp);
            if (is_static) {
                code.addGetstatic(Bytecode.THIS, fieldName, fieldType);
            }
            else {
                code.addAload(0);
                code.addGetfield(Bytecode.THIS, fieldName, fieldType);
                code.setMaxLocals(1);
            }

            code.addReturn(Descriptor.toCtClass(fieldType, pool));
            minfo.setCodeAttribute(code.toCodeAttribute());
            cf.addMethod(minfo);
            accessors.put(key, minfo);
            return minfo;
        }
        catch (CannotCompileException e) {
            throw new CompileError(e);
        }
        catch (NotFoundException e) {
            throw new CompileError(e);
        }
    }

    /**
     * Returns the method_info representing the added setter.
     */
    public MethodInfo getFieldSetter(FieldInfo finfo, boolean is_static)
        throws CompileError
    {
        String fieldName = finfo.getName();
        String key = fieldName + ":setter";
        Object res = accessors.get(key);
        if (res != null)
            return (MethodInfo)res;     // already exists.

        ClassFile cf = clazz.getClassFile();    // turn on the modified flag.
        String accName = findAccessorName(cf);
        try {
            ConstPool cp = cf.getConstPool();
            ClassPool pool = clazz.getClassPool();
            String fieldType = finfo.getDescriptor();
            String accDesc;
            if (is_static)
                accDesc = "(" + fieldType + ")V";
            else
                accDesc = "(" + Descriptor.of(clazz) + fieldType + ")V";

            MethodInfo minfo = new MethodInfo(cp, accName, accDesc);
            minfo.setAccessFlags(AccessFlag.STATIC);
            minfo.addAttribute(new SyntheticAttribute(cp));
            Bytecode code = new Bytecode(cp);
            int reg;
            if (is_static) {
                reg = code.addLoad(0, Descriptor.toCtClass(fieldType, pool));
                code.addPutstatic(Bytecode.THIS, fieldName, fieldType);
            }
            else {
                code.addAload(0);
                reg = code.addLoad(1, Descriptor.toCtClass(fieldType, pool))
                      + 1;
                code.addPutfield(Bytecode.THIS, fieldName, fieldType);
            }

            code.addReturn(null);
            code.setMaxLocals(reg);
            minfo.setCodeAttribute(code.toCodeAttribute());
            cf.addMethod(minfo);
            accessors.put(key, minfo);
            return minfo;
        }
        catch (CannotCompileException e) {
            throw new CompileError(e);
        }
        catch (NotFoundException e) {
            throw new CompileError(e);
        }
    }

    private String findAccessorName(ClassFile cf) {
        String accName;
        do {
            accName = "access$" + uniqueNumber++;
        } while (cf.getMethod(accName) != null);
        return accName;
    }
}
