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
package io.github.future0923.debug.tools.hotswap.core.javassist.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.future0923.debug.tools.hotswap.core.javassist.CannotCompileException;
import io.github.future0923.debug.tools.hotswap.core.javassist.CtBehavior;

/**
 * Creates bytecode that when executed calls back to the instance's result method.
 *
 * <p>Example of how to create and insert a callback:</p>
 * <pre>
 * ctMethod.insertAfter(new Callback("Thread.currentThread()") {
 *     public void result(Object[] objects) {
 *         Thread thread = (Thread) objects[0];
 *         // do something with thread...
 *     }
 * }.sourceCode());
 * </pre>
 * <p>Contains utility methods for inserts callbacks in <code>CtBehaviour</code>, example:</p>
 * <pre>
 * insertAfter(ctBehaviour, new Callback("Thread.currentThread(), dummyString") {
 *     public void result(Object[] objects) {
 *         Thread thread = (Thread) objects[0];
 *         // do something with thread...
 *     }
 * });
 * </pre>
 *
 * @author Marten Hedborg
 * @author Shigeru Chiba
 */
public abstract class Callback {

    public static Map<String,Callback> callbacks = new HashMap<String,Callback>();

    private final String sourceCode;

    /**
     * Constructs a new <code>Callback</code> object.
     *
     * @param src       The source code representing the inserted callback bytecode.
     *                  Can be one or many single statements each returning one object.
     *                  If many single statements are used they must be comma separated.
     */
    public Callback(String src){
        String uuid = UUID.randomUUID().toString();
        callbacks.put(uuid, this);
        sourceCode = "((javassist.tools.Callback) javassist.tools.Callback.callbacks.get(\""+uuid+"\")).result(new Object[]{"+src+"});";
    }

    /**
     * Gets called when bytecode is executed
     *
     * @param objects   Objects that the bytecode in callback returns
     */
    public abstract void result(Object[] objects);

    @Override
    public String toString(){
        return sourceCode();
    }

    public String sourceCode(){
        return sourceCode;
    }

    /**
     * Utility method to insert callback at the beginning of the body.
     *
     * @param callback  The callback
     *
     * @see CtBehavior#insertBefore(String)
     */
    public static void insertBefore(CtBehavior behavior, Callback callback)
            throws CannotCompileException
    {
        behavior.insertBefore(callback.toString());
    }

    /**
     * Utility method to inserts callback at the end of the body.
     * The callback is inserted just before every return instruction.
     * It is not executed when an exception is thrown.
     *
     * @param behavior  The behaviour to insert callback in
     * @param callback  The callback
     *
     * @see CtBehavior#insertAfter(String, boolean)
     */
    public static void insertAfter(CtBehavior behavior,Callback callback)
            throws CannotCompileException
    {
        behavior.insertAfter(callback.toString(), false);
    }

    /**
     * Utility method to inserts callback at the end of the body.
     * The callback is inserted just before every return instruction.
     * It is not executed when an exception is thrown.
     *
     * @param behavior  The behaviour to insert callback in
     * @param callback  The callback representing the inserted.
     * @param asFinally True if the inserted is executed
     *                  Not only when the control normally returns
     *                  but also when an exception is thrown.
     *                  If this parameter is true, the inserted code cannot
     *                  access local variables.
     *
     * @see CtBehavior#insertAfter(String, boolean)
     */
    public static void insertAfter(CtBehavior behavior, Callback callback, boolean asFinally)
            throws CannotCompileException
    {
        behavior.insertAfter(callback.toString(), asFinally);
    }

    /**
     * Utility method to inserts callback at the specified line in the body.
     *
     * @param behavior  The behaviour to insert callback in
     * @param callback  The callback representing.
     * @param lineNum   The line number.  The callback is inserted at the
     *                  beginning of the code at the line specified by this
     *                  line number.
     *
     * @return      The line number at which the callback has been inserted.
     *
     * @see CtBehavior#insertAt(int, String)
     */
    public static int insertAt(CtBehavior behavior, Callback callback, int lineNum)
            throws CannotCompileException
    {
        return behavior.insertAt(lineNum, callback.toString());
    }
}
