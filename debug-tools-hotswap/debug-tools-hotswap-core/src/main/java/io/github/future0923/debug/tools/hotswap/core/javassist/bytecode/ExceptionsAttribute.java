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
package io.github.future0923.debug.tools.hotswap.core.javassist.bytecode;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * <code>Exceptions_attribute</code>.
 */
public class ExceptionsAttribute extends AttributeInfo {
    /**
     * The name of this attribute <code>"Exceptions"</code>.
     */
    public static final String tag = "Exceptions";

    ExceptionsAttribute(ConstPool cp, int n, DataInputStream in)
        throws IOException
    {
        super(cp, n, in);
    }

    /**
     * Constructs a copy of an exceptions attribute.
     *
     * @param cp                constant pool table.
     * @param src               source attribute.
     */
    private ExceptionsAttribute(ConstPool cp, ExceptionsAttribute src,
                                Map<String,String> classnames) {
        super(cp, tag);
        copyFrom(src, classnames);
    }

    /**
     * Constructs a new exceptions attribute.
     *
     * @param cp                constant pool table.
     */
    public ExceptionsAttribute(ConstPool cp) {
        super(cp, tag);
        byte[] data = new byte[2];
        data[0] = data[1] = 0;  // empty
        this.info = data;
    }

    /**
     * Makes a copy.  Class names are replaced according to the
     * given <code>Map</code> object.
     *
     * @param newCp     the constant pool table used by the new copy.
     * @param classnames        pairs of replaced and substituted
     *                          class names.  It can be <code>null</code>.
     */
    @Override
    public AttributeInfo copy(ConstPool newCp, Map<String,String> classnames) {
        return new ExceptionsAttribute(newCp, this, classnames);
    }

    /**
     * Copies the contents from a source attribute.
     * Specified class names are replaced during the copy.
     *
     * @param srcAttr           source Exceptions attribute
     * @param classnames        pairs of replaced and substituted
     *                          class names.
     */
    private void copyFrom(ExceptionsAttribute srcAttr, Map<String,String> classnames) {
        ConstPool srcCp = srcAttr.constPool;
        ConstPool destCp = this.constPool;
        byte[] src = srcAttr.info;
        int num = src.length;
        byte[] dest = new byte[num];
        dest[0] = src[0];
        dest[1] = src[1];       // the number of elements.
        for (int i = 2; i < num; i += 2) {
            int index = ByteArray.readU16bit(src, i);
            ByteArray.write16bit(srcCp.copy(index, destCp, classnames),
                                 dest, i);
        }

        this.info = dest;
    }

    /**
     * Returns <code>exception_index_table[]</code>.
     */
    public int[] getExceptionIndexes() {
        byte[] blist = info;
        int n = blist.length;
        if (n <= 2)
            return null;

        int[] elist = new int[n / 2 - 1];
        int k = 0;
        for (int j = 2; j < n; j += 2)
            elist[k++] = ((blist[j] & 0xff) << 8) | (blist[j + 1] & 0xff);

        return elist;
    }

    /**
     * Returns the names of exceptions that the method may throw.
     */
    public String[] getExceptions() {
        byte[] blist = info;
        int n = blist.length;
        if (n <= 2)
            return null;

        String[] elist = new String[n / 2 - 1];
        int k = 0;
        for (int j = 2; j < n; j += 2) {
            int index = ((blist[j] & 0xff) << 8) | (blist[j + 1] & 0xff);
            elist[k++] = constPool.getClassInfo(index);
        }

        return elist;
    }

    /**
     * Sets <code>exception_index_table[]</code>.
     */
    public void setExceptionIndexes(int[] elist) {
        int n = elist.length;
        byte[] blist = new byte[n * 2 + 2];
        ByteArray.write16bit(n, blist, 0);
        for (int i = 0; i < n; ++i)
            ByteArray.write16bit(elist[i], blist, i * 2 + 2);

        info = blist;
    }

    /**
     * Sets the names of exceptions that the method may throw.
     */
    public void setExceptions(String[] elist) {
        int n = elist.length;
        byte[] blist = new byte[n * 2 + 2];
        ByteArray.write16bit(n, blist, 0);
        for (int i = 0; i < n; ++i)
            ByteArray.write16bit(constPool.addClassInfo(elist[i]),
                                 blist, i * 2 + 2);

        info = blist;
    }

    /**
     * Returns <code>number_of_exceptions</code>.
     */
    public int tableLength() { return info.length / 2 - 1; }

    /**
     * Returns the value of <code>exception_index_table[nth]</code>.
     */
    public int getException(int nth) {
        int index = nth * 2 + 2;        // nth >= 0
        return ((info[index] & 0xff) << 8) | (info[index + 1] & 0xff);
    }
}
