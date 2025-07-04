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
package io.github.future0923.debug.tools.hotswap.core.javassist.expr;

import io.github.future0923.debug.tools.hotswap.core.javassist.CannotCompileException;
import io.github.future0923.debug.tools.hotswap.core.javassist.CtBehavior;
import io.github.future0923.debug.tools.hotswap.core.javassist.CtClass;
import io.github.future0923.debug.tools.hotswap.core.javassist.CtPrimitiveType;
import io.github.future0923.debug.tools.hotswap.core.javassist.NotFoundException;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.BadBytecode;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.Bytecode;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.CodeAttribute;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.CodeIterator;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.ConstPool;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.Descriptor;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.MethodInfo;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.Opcode;
import io.github.future0923.debug.tools.hotswap.core.javassist.compiler.CompileError;
import io.github.future0923.debug.tools.hotswap.core.javassist.compiler.Javac;
import io.github.future0923.debug.tools.hotswap.core.javassist.compiler.JvstCodeGen;
import io.github.future0923.debug.tools.hotswap.core.javassist.compiler.JvstTypeChecker;
import io.github.future0923.debug.tools.hotswap.core.javassist.compiler.ProceedHandler;
import io.github.future0923.debug.tools.hotswap.core.javassist.compiler.ast.ASTList;

/**
 * Array creation.
 *
 * <p>This class does not provide methods for obtaining the initial
 * values of array elements.
 */
public class NewArray extends Expr {
    int opcode;

    protected NewArray(int pos, CodeIterator i, CtClass declaring,
                       MethodInfo m, int op) {
        super(pos, i, declaring, m);
        opcode = op;
    }

    /**
     * Returns the method or constructor containing the array creation
     * represented by this object.
     */
    @Override
    public CtBehavior where() { return super.where(); }

    /**
     * Returns the line number of the source line containing the
     * array creation.
     *
     * @return -1       if this information is not available.
     */
    @Override
    public int getLineNumber() {
        return super.getLineNumber();
    }

    /**
     * Returns the source file containing the array creation.
     *
     * @return null     if this information is not available.
     */
    @Override
    public String getFileName() {
        return super.getFileName();
    }

    /**
     * Returns the list of exceptions that the expression may throw.
     * This list includes both the exceptions that the try-catch statements
     * including the expression can catch and the exceptions that
     * the throws declaration allows the method to throw.
     */
    @Override
    public CtClass[] mayThrow() {
        return super.mayThrow();
    }

    /**
     * Returns the type of array components.  If the created array is
     * a two-dimensional array of <code>int</code>,
     * the type returned by this method is
     * not <code>int[]</code> but <code>int</code>.
     */
    public CtClass getComponentType() throws NotFoundException {
        if (opcode == Opcode.NEWARRAY) {
            int atype = iterator.byteAt(currentPos + 1);
            return getPrimitiveType(atype);
        }
        else if (opcode == Opcode.ANEWARRAY
                 || opcode == Opcode.MULTIANEWARRAY) {
            int index = iterator.u16bitAt(currentPos + 1);
            String desc = getConstPool().getClassInfo(index);
            int dim = Descriptor.arrayDimension(desc);
            desc = Descriptor.toArrayComponent(desc, dim);
            return Descriptor.toCtClass(desc, thisClass.getClassPool());
        }
        else
            throw new RuntimeException("bad opcode: " + opcode);
    }

    CtClass getPrimitiveType(int atype) {
        switch (atype) {
        case Opcode.T_BOOLEAN :
            return CtClass.booleanType;
        case Opcode.T_CHAR :
            return CtClass.charType;
        case Opcode.T_FLOAT :
            return CtClass.floatType;
        case Opcode.T_DOUBLE :
            return CtClass.doubleType;
        case Opcode.T_BYTE :
            return CtClass.byteType;
        case Opcode.T_SHORT :
            return CtClass.shortType;
        case Opcode.T_INT :
            return CtClass.intType;
        case Opcode.T_LONG :
            return CtClass.longType;
        default :
            throw new RuntimeException("bad atype: " + atype);        
        }
    }

    /**
     * Returns the dimension of the created array.
     */
    public int getDimension() {
        if (opcode == Opcode.NEWARRAY)
            return 1;
        else if (opcode == Opcode.ANEWARRAY
                 || opcode == Opcode.MULTIANEWARRAY) {
            int index = iterator.u16bitAt(currentPos + 1);
            String desc = getConstPool().getClassInfo(index);
            return Descriptor.arrayDimension(desc)
                    + (opcode == Opcode.ANEWARRAY ? 1 : 0);
        }
        else
            throw new RuntimeException("bad opcode: " + opcode);
    }

    /**
     * Returns the number of dimensions of arrays to be created.
     * If the opcode is multianewarray, this method returns the second
     * operand.  Otherwise, it returns 1.
     */
    public int getCreatedDimensions() {
        if (opcode == Opcode.MULTIANEWARRAY)
            return iterator.byteAt(currentPos + 3);
        return 1;
    }

    /**
     * Replaces the array creation with the bytecode derived from
     * the given source text.
     *
     * <p>$0 is available even if the called method is static.
     * If the field access is writing, $_ is available but the value
     * of $_ is ignored.
     *
     * @param statement         a Java statement except try-catch.
     */
    @Override
    public void replace(String statement) throws CannotCompileException {
        try {
            replace2(statement);
        }
        catch (CompileError e) { throw new CannotCompileException(e); }
        catch (NotFoundException e) { throw new CannotCompileException(e); }
        catch (BadBytecode e) {
            throw new CannotCompileException("broken method");
        }
    }

    private void replace2(String statement)
        throws CompileError, NotFoundException, BadBytecode,
               CannotCompileException
    {
        thisClass.getClassFile();   // to call checkModify().
        ConstPool constPool = getConstPool();
        int pos = currentPos;
        CtClass retType;
        int codeLength;
        int index = 0;
        int dim = 1;
        String desc;
        if (opcode == Opcode.NEWARRAY) {
            index = iterator.byteAt(currentPos + 1);    // atype
            CtPrimitiveType cpt = (CtPrimitiveType)getPrimitiveType(index); 
            desc = "[" + cpt.getDescriptor();
            codeLength = 2;
        }
        else if (opcode == Opcode.ANEWARRAY) {
            index = iterator.u16bitAt(pos + 1);
            desc = constPool.getClassInfo(index);
            if (desc.startsWith("["))
                desc = "[" + desc;
            else
                desc = "[L" + desc + ";";

            codeLength = 3;
        }
        else if (opcode == Opcode.MULTIANEWARRAY) {
            index = iterator.u16bitAt(currentPos + 1);
            desc = constPool.getClassInfo(index);
            dim = iterator.byteAt(currentPos + 3);
            codeLength = 4;
        }
        else
            throw new RuntimeException("bad opcode: " + opcode);

        retType = Descriptor.toCtClass(desc, thisClass.getClassPool());

        Javac jc = new Javac(thisClass);
        CodeAttribute ca = iterator.get();

        CtClass[] params = new CtClass[dim];
        for (int i = 0; i < dim; ++i)
            params[i] = CtClass.intType;

        int paramVar = ca.getMaxLocals();
        jc.recordParams(javaLangObject, params,
                        true, paramVar, withinStatic());

        /* Is $_ included in the source code?
         */
        checkResultValue(retType, statement);
        int retVar = jc.recordReturnType(retType, true);
        jc.recordProceed(new ProceedForArray(retType, opcode, index, dim));

        Bytecode bytecode = jc.getBytecode();
        storeStack(params, true, paramVar, bytecode);
        jc.recordLocalVariables(ca, pos);

        bytecode.addOpcode(ACONST_NULL);        // initialize $_
        bytecode.addAstore(retVar);

        jc.compileStmnt(statement);
        bytecode.addAload(retVar);

        replace0(pos, bytecode, codeLength);
    }

    /* <array type> $proceed(<dim> ..)
     */
    static class ProceedForArray implements ProceedHandler {
        CtClass arrayType;
        int opcode;
        int index, dimension;

        ProceedForArray(CtClass type, int op, int i, int dim) {
            arrayType = type;
            opcode = op;
            index = i;
            dimension = dim;
        }

        @Override
        public void doit(JvstCodeGen gen, Bytecode bytecode, ASTList args)
            throws CompileError
        {
            int num = gen.getMethodArgsLength(args);
            if (num != dimension)
                throw new CompileError(Javac.proceedName
                        + "() with a wrong number of parameters");

            gen.atMethodArgs(args, new int[num],
                             new int[num], new String[num]);
            bytecode.addOpcode(opcode);
            if (opcode == Opcode.ANEWARRAY)
                bytecode.addIndex(index);
            else if (opcode == Opcode.NEWARRAY)
                bytecode.add(index);
            else /* if (opcode == Opcode.MULTIANEWARRAY) */ {
                bytecode.addIndex(index);
                bytecode.add(dimension);
                bytecode.growStack(1 - dimension);
            }

            gen.setType(arrayType);
        }

        @Override
        public void setReturnType(JvstTypeChecker c, ASTList args)
            throws CompileError
        {
            c.setType(arrayType);
        }
    }
}
