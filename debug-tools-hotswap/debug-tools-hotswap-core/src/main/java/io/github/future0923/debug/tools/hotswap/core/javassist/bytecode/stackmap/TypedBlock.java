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
package io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.stackmap;

import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.AccessFlag;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.BadBytecode;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.CodeAttribute;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.ConstPool;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.MethodInfo;

public class TypedBlock extends BasicBlock {
    public int stackTop, numLocals;
    // localsTypes is set to non-null when this block is first visited by a MapMaker.
    // see alreadySet().
    public TypeData[] localsTypes;
    public TypeData[] stackTypes;

    /**
     * Divides the method body into basic blocks.
     * The type information of the first block is initialized.
     *
     * @param optimize       if it is true and the method does not include
     *                      branches, this method returns null.
     */
    public static TypedBlock[] makeBlocks(MethodInfo minfo, CodeAttribute ca,
                                          boolean optimize)
        throws BadBytecode
    {
        TypedBlock[] blocks = (TypedBlock[])new Maker().make(minfo);
        if (optimize && blocks.length < 2)
            if (blocks.length == 0 || blocks[0].incoming == 0)
                return null;

        ConstPool pool = minfo.getConstPool();
        boolean isStatic = (minfo.getAccessFlags() & AccessFlag.STATIC) != 0;
        blocks[0].initFirstBlock(ca.getMaxStack(), ca.getMaxLocals(),
                                 pool.getClassName(), minfo.getDescriptor(),
                                 isStatic, minfo.isConstructor());
        return blocks;
    }

    protected TypedBlock(int pos) {
        super(pos);
        localsTypes = null;
    }

    @Override
    protected void toString2(StringBuffer sbuf) {
        super.toString2(sbuf);
        sbuf.append(",\n stack={");
        printTypes(sbuf, stackTop, stackTypes);
        sbuf.append("}, locals={");
        printTypes(sbuf, numLocals, localsTypes);
        sbuf.append('}');
    }

    private void printTypes(StringBuffer sbuf, int size,
                            TypeData[] types) {
        if (types == null)
            return;

        for (int i = 0; i < size; i++) {
            if (i > 0)
                sbuf.append(", ");

            TypeData td = types[i];
            sbuf.append(td == null ? "<>" : td.toString());
        }
    }

    public boolean alreadySet() {
        return localsTypes != null;
    }

    public void setStackMap(int st, TypeData[] stack, int nl, TypeData[] locals)
        throws BadBytecode
    {
        stackTop = st;
        stackTypes = stack;
        numLocals = nl;
        localsTypes = locals;
    }

    /*
     * Computes the correct value of numLocals.
     */
    public void resetNumLocals() {
        if (localsTypes != null) {
            int nl = localsTypes.length;
            while (nl > 0 && localsTypes[nl - 1].isBasicType() == TypeTag.TOP) {
                if (nl > 1) {
                    if (localsTypes[nl - 2].is2WordType())
                        break;
                }

                --nl;
            }

            numLocals = nl;
        }
    }

    public static class Maker extends BasicBlock.Maker {
        @Override
        protected BasicBlock makeBlock(int pos) {
            return new TypedBlock(pos);
        }

        @Override
        protected BasicBlock[] makeArray(int size) {
            return new TypedBlock[size];
        }
    }

    /**
     * Initializes the first block by the given method descriptor.
     *
     * @param block             the first basic block that this method initializes.
     * @param className         a dot-separated fully qualified class name.
     *                          For example, <code>javassist.bytecode.stackmap.BasicBlock</code>.
     * @param methodDesc        method descriptor.
     * @param isStatic          true if the method is a static method.
     * @param isConstructor     true if the method is a constructor.
     */
    void initFirstBlock(int maxStack, int maxLocals, String className,
                        String methodDesc, boolean isStatic, boolean isConstructor)
        throws BadBytecode
    {
        if (methodDesc.charAt(0) != '(')
            throw new BadBytecode("no method descriptor: " + methodDesc);

        stackTop = 0;
        stackTypes = TypeData.make(maxStack);
        TypeData[] locals = TypeData.make(maxLocals);
        if (isConstructor)
            locals[0] = new TypeData.UninitThis(className);
        else if (!isStatic)
            locals[0] = new TypeData.ClassName(className);

        int n = isStatic ? -1 : 0;
        int i = 1;
        try {
            while ((i = descToTag(methodDesc, i, ++n, locals)) > 0)
                if (locals[n].is2WordType())
                    locals[++n] = TypeTag.TOP;
        }
        catch (StringIndexOutOfBoundsException e) {
            throw new BadBytecode("bad method descriptor: "
                                  + methodDesc);
        }

        numLocals = n;
        localsTypes = locals;
    }

    private static int descToTag(String desc, int i,
                                 int n, TypeData[] types)
        throws BadBytecode
    {
        int i0 = i;
        int arrayDim = 0;
        char c = desc.charAt(i);
        if (c == ')')
            return 0;

        while (c == '[') {
            ++arrayDim;
            c = desc.charAt(++i);
        }

        if (c == 'L') {
            int i2 = desc.indexOf(';', ++i);
            if (arrayDim > 0)
                types[n] = new TypeData.ClassName(desc.substring(i0, ++i2));
            else
                types[n] = new TypeData.ClassName(desc.substring(i0 + 1, ++i2 - 1)
                                                      .replace('/', '.'));
            return i2;
        }
        else if (arrayDim > 0) {
            types[n] = new TypeData.ClassName(desc.substring(i0, ++i));
            return i;
        }
        else {
            TypeData t = toPrimitiveTag(c);
            if (t == null)
                throw new BadBytecode("bad method descriptor: " + desc);

            types[n] = t;
            return i + 1;
        }
    }

    private static TypeData toPrimitiveTag(char c) {
        switch (c) {
        case 'Z' :
        case 'C' :
        case 'B' :
        case 'S' :
        case 'I' :
            return TypeTag.INTEGER;
        case 'J' :
            return TypeTag.LONG;
        case 'F' :
            return TypeTag.FLOAT;
        case 'D' :
            return TypeTag.DOUBLE;
        case 'V' :
        default :
            return null;
        }
    }

    public static String getRetType(String desc) {
        int i = desc.indexOf(')');
        if (i < 0)
            return "java.lang.Object";

        char c = desc.charAt(i + 1);
        if (c == '[')
            return desc.substring(i + 1);
        else if (c == 'L')
            return desc.substring(i + 2, desc.length() - 1).replace('/', '.');
        else
            return "java.lang.Object";
    }
}
