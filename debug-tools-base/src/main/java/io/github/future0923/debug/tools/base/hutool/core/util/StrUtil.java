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
package io.github.future0923.debug.tools.base.hutool.core.util;

import io.github.future0923.debug.tools.base.hutool.core.text.CharSequenceUtil;
import io.github.future0923.debug.tools.base.hutool.core.text.StrBuilder;
import io.github.future0923.debug.tools.base.hutool.core.text.StrFormatter;
import io.github.future0923.debug.tools.base.hutool.core.text.StrPool;
import io.github.future0923.debug.tools.base.hutool.core.text.TextSimilarity;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 字符串工具类
 *
 * @author xiaoleilu
 */
public class StrUtil extends CharSequenceUtil implements StrPool {

	// ------------------------------------------------------------------------ Blank

	/**
	 * <p>如果对象是字符串是否为空白，空白的定义如下：</p>
	 * <ol>
	 *     <li>{@code null}</li>
	 *     <li>空字符串：{@code ""}</li>
	 *     <li>空格、全角空格、制表符、换行符，等不可见字符</li>
	 * </ol>
	 *
	 * <p>例：</p>
	 * <ul>
	 *     <li>{@code StrUtil.isBlankIfStr(null)     // true}</li>
	 *     <li>{@code StrUtil.isBlankIfStr("")       // true}</li>
	 *     <li>{@code StrUtil.isBlankIfStr(" \t\n")  // true}</li>
	 *     <li>{@code StrUtil.isBlankIfStr("abc")    // false}</li>
	 * </ul>
	 *
	 * <p>注意：该方法与 {@link #isEmptyIfStr(Object)} 的区别是：
	 * 该方法会校验空白字符，且性能相对于 {@link #isEmptyIfStr(Object)} 略慢。</p>
	 *
	 * @param obj 对象
	 * @return 如果为字符串是否为空串
	 * @see StrUtil#isBlank(CharSequence)
	 * @since 3.3.0
	 */
	public static boolean isBlankIfStr(Object obj) {
		if (null == obj) {
			return true;
		} else if (obj instanceof CharSequence) {
			return isBlank((CharSequence) obj);
		}
		return false;
	}
	// ------------------------------------------------------------------------ Empty

	/**
	 * <p>如果对象是字符串是否为空串，空的定义如下：</p><br>
	 * <ol>
	 *     <li>{@code null}</li>
	 *     <li>空字符串：{@code ""}</li>
	 * </ol>
	 *
	 * <p>例：</p>
	 * <ul>
	 *     <li>{@code StrUtil.isEmptyIfStr(null)     // true}</li>
	 *     <li>{@code StrUtil.isEmptyIfStr("")       // true}</li>
	 *     <li>{@code StrUtil.isEmptyIfStr(" \t\n")  // false}</li>
	 *     <li>{@code StrUtil.isEmptyIfStr("abc")    // false}</li>
	 * </ul>
	 *
	 * <p>注意：该方法与 {@link #isBlankIfStr(Object)} 的区别是：该方法不校验空白字符。</p>
	 *
	 * @param obj 对象
	 * @return 如果为字符串是否为空串
	 * @since 3.3.0
	 */
	public static boolean isEmptyIfStr(Object obj) {
		if (null == obj) {
			return true;
		} else if (obj instanceof CharSequence) {
			return 0 == ((CharSequence) obj).length();
		}
		return false;
	}

	// ------------------------------------------------------------------------ Trim

	/**
	 * 给定字符串数组全部做去首尾空格
	 *
	 * @param strs 字符串数组
	 */
	public static void trim(String[] strs) {
		if (null == strs) {
			return;
		}
		String str;
		for (int i = 0; i < strs.length; i++) {
			str = strs[i];
			if (null != str) {
				strs[i] = trim(str);
			}
		}
	}

	/**
	 * 将对象转为字符串<br>
	 *
	 * <pre>
	 * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组
	 * 2、对象数组会调用Arrays.toString方法
	 * </pre>
	 *
	 * @param obj 对象
	 * @return 字符串
	 */
	public static String utf8Str(Object obj) {
		return str(obj, CharsetUtil.CHARSET_UTF_8);
	}

	/**
	 * 将对象转为字符串
	 *
	 * <pre>
	 * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组
	 * 2、对象数组会调用Arrays.toString方法
	 * </pre>
	 *
	 * @param obj         对象
	 * @param charsetName 字符集
	 * @return 字符串
	 * @deprecated 请使用 {@link #str(Object, Charset)}
	 */
	@Deprecated
	public static String str(Object obj, String charsetName) {
		return str(obj, Charset.forName(charsetName));
	}

	/**
	 * 将对象转为字符串
	 * <pre>
	 * 	 1、Byte数组和ByteBuffer会被转换为对应字符串的数组
	 * 	 2、对象数组会调用Arrays.toString方法
	 * </pre>
	 *
	 * @param obj     对象
	 * @param charset 字符集
	 * @return 字符串
	 */
	public static String str(Object obj, Charset charset) {
		if (null == obj) {
			return null;
		}

		if (obj instanceof String) {
			return (String) obj;
		} else if (obj instanceof byte[]) {
			return str((byte[]) obj, charset);
		} else if (obj instanceof Byte[]) {
			return str((Byte[]) obj, charset);
		} else if (obj instanceof ByteBuffer) {
			return str((ByteBuffer) obj, charset);
		} else if (ArrayUtil.isArray(obj)) {
			return ArrayUtil.toString(obj);
		}

		return obj.toString();
	}

	/**
	 * 将byte数组转为字符串
	 *
	 * @param bytes   byte数组
	 * @param charset 字符集
	 * @return 字符串
	 */
	public static String str(byte[] bytes, String charset) {
		return str(bytes, CharsetUtil.charset(charset));
	}

	/**
	 * 解码字节码
	 *
	 * @param data    字符串
	 * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
	 * @return 解码后的字符串
	 */
	public static String str(byte[] data, Charset charset) {
		if (data == null) {
			return null;
		}

		if (null == charset) {
			return new String(data);
		}
		return new String(data, charset);
	}

	/**
	 * 将Byte数组转为字符串
	 *
	 * @param bytes   byte数组
	 * @param charset 字符集
	 * @return 字符串
	 */
	public static String str(Byte[] bytes, String charset) {
		return str(bytes, CharsetUtil.charset(charset));
	}

	/**
	 * 解码字节码
	 *
	 * @param data    字符串
	 * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
	 * @return 解码后的字符串
	 */
	public static String str(Byte[] data, Charset charset) {
		if (data == null) {
			return null;
		}

		byte[] bytes = new byte[data.length];
		Byte dataByte;
		for (int i = 0; i < data.length; i++) {
			dataByte = data[i];
			bytes[i] = (null == dataByte) ? -1 : dataByte;
		}

		return str(bytes, charset);
	}

	/**
	 * 将编码的byteBuffer数据转换为字符串
	 *
	 * @param data    数据
	 * @param charset 字符集，如果为空使用当前系统字符集
	 * @return 字符串
	 */
	public static String str(ByteBuffer data, String charset) {
		if (data == null) {
			return null;
		}

		return str(data, CharsetUtil.charset(charset));
	}

	/**
	 * 将编码的byteBuffer数据转换为字符串
	 *
	 * @param data    数据
	 * @param charset 字符集，如果为空使用当前系统字符集
	 * @return 字符串
	 */
	public static String str(ByteBuffer data, Charset charset) {
		if (null == charset) {
			charset = Charset.defaultCharset();
		}
		return charset.decode(data).toString();
	}

	/**
	 * 调用对象的toString方法，null会返回“null”
	 *
	 * @param obj 对象
	 * @return 字符串
	 * @see String#valueOf(Object)
	 * @since 4.1.3
	 */
	public static String toString(Object obj) {
		return String.valueOf(obj);
	}

	/**
	 * 调用对象的toString方法，null会返回{@code null}
	 *
	 * @param obj 对象
	 * @return 字符串 or {@code null}
	 * @since 5.7.17
	 */
	public static String toStringOrNull(Object obj) {
		return null == obj ? null : obj.toString();
	}

	/**
	 * 调用对象的toString方法，null会返回空字符串 ""
	 *
	 * @param obj 对象
	 * @return {@link String }
     * @author Junwei Xu
	 */
	public static String toStringOrEmpty(Object obj) {
		// obj为空时, 返回 null 或 "null" 都不适用部分场景, 此处返回 "" 空字符串
		return null == obj ? EMPTY : obj.toString();
	}

	/**
	 * 创建StringBuilder对象
	 *
	 * @return StringBuilder对象
	 */
	public static StringBuilder builder() {
		return new StringBuilder();
	}

	/**
	 * 创建StrBuilder对象
	 *
	 * @return StrBuilder对象
	 * @since 4.0.1
	 */
	public static StrBuilder strBuilder() {
		return StrBuilder.create();
	}

	/**
	 * 创建StringBuilder对象
	 *
	 * @param capacity 初始大小
	 * @return StringBuilder对象
	 */
	public static StringBuilder builder(int capacity) {
		return new StringBuilder(capacity);
	}

	/**
	 * 创建StrBuilder对象
	 *
	 * @param capacity 初始大小
	 * @return StrBuilder对象
	 * @since 4.0.1
	 */
	public static StrBuilder strBuilder(int capacity) {
		return StrBuilder.create(capacity);
	}

	/**
	 * 获得StringReader
	 *
	 * @param str 字符串
	 * @return StringReader
	 */
	public static StringReader getReader(CharSequence str) {
		if (null == str) {
			return null;
		}
		return new StringReader(str.toString());
	}

	/**
	 * 获得StringWriter
	 *
	 * @return StringWriter
	 */
	public static StringWriter getWriter() {
		return new StringWriter();
	}

	/**
	 * 反转字符串<br>
	 * 例如：abcd =》dcba
	 *
	 * @param str 被反转的字符串
	 * @return 反转后的字符串
	 * @since 3.0.9
	 */
	public static String reverse(String str) {
		return new String(ArrayUtil.reverse(str.toCharArray()));
	}

	// ------------------------------------------------------------------------ fill

	/**
	 * 将已有字符串填充为规定长度，如果已有字符串超过这个长度则返回这个字符串<br>
	 * 字符填充于字符串前
	 *
	 * @param str        被填充的字符串
	 * @param filledChar 填充的字符
	 * @param len        填充长度
	 * @return 填充后的字符串
	 * @since 3.1.2
	 */
	public static String fillBefore(String str, char filledChar, int len) {
		return fill(str, filledChar, len, true);
	}

	/**
	 * 将已有字符串填充为规定长度，如果已有字符串超过这个长度则返回这个字符串<br>
	 * 字符填充于字符串后
	 *
	 * @param str        被填充的字符串
	 * @param filledChar 填充的字符
	 * @param len        填充长度
	 * @return 填充后的字符串
	 * @since 3.1.2
	 */
	public static String fillAfter(String str, char filledChar, int len) {
		return fill(str, filledChar, len, false);
	}

	/**
	 * 将已有字符串填充为规定长度，如果已有字符串超过这个长度则返回这个字符串
	 *
	 * @param str        被填充的字符串
	 * @param filledChar 填充的字符
	 * @param len        填充长度
	 * @param isPre      是否填充在前
	 * @return 填充后的字符串
	 * @since 3.1.2
	 */
	public static String fill(String str, char filledChar, int len, boolean isPre) {
		final int strLen = str.length();
		if (strLen > len) {
			return str;
		}

		String filledStr = StrUtil.repeat(filledChar, len - strLen);
		return isPre ? filledStr.concat(str) : str.concat(filledStr);
	}

	/**
	 * 计算两个字符串的相似度
	 *
	 * @param str1 字符串1
	 * @param str2 字符串2
	 * @return 相似度
	 * @since 3.2.3
	 */
	public static double similar(String str1, String str2) {
		return TextSimilarity.similar(str1, str2);
	}

	/**
	 * 计算两个字符串的相似度百分比
	 *
	 * @param str1  字符串1
	 * @param str2  字符串2
	 * @param scale 相似度
	 * @return 相似度百分比
	 * @since 3.2.3
	 */
	public static String similar(String str1, String str2, int scale) {
		return TextSimilarity.similar(str1, str2, scale);
	}

	/**
	 * 生成随机UUID
	 *
	 * @return UUID字符串
	 * @see IdUtil#randomUUID()
	 * @since 4.0.10
	 */
	public static String uuid() {
		return IdUtil.randomUUID();
	}

	/**
	 * 格式化文本，使用 {varName} 占位<br>
	 * map = {a: "aValue", b: "bValue"} format("{a} and {b}", map) ---=》 aValue and bValue
	 *
	 * @param template 文本模板，被替换的部分用 {key} 表示
	 * @param map      参数值对
	 * @return 格式化后的文本
	 */
	public static String format(CharSequence template, Map<?, ?> map) {
		return format(template, map, true);
	}

	/**
	 * 格式化文本，使用 {varName} 占位<br>
	 * map = {a: "aValue", b: "bValue"} format("{a} and {b}", map) ---=》 aValue and bValue
	 *
	 * @param template   文本模板，被替换的部分用 {key} 表示
	 * @param map        参数值对
	 * @param ignoreNull 是否忽略 {@code null} 值，忽略则 {@code null} 值对应的变量不被替换，否则替换为""
	 * @return 格式化后的文本
	 * @since 5.4.3
	 */
	public static String format(CharSequence template, Map<?, ?> map, boolean ignoreNull) {
		return StrFormatter.format(template, map, ignoreNull);
	}

	/**
	 * 截断字符串，使用其按照UTF-8编码为字节后不超过maxBytes长度。截断后自动追加省略号(...)
	 * 用于存储数据库varchar且编码为UTF-8的字段
	 *
	 * @param str      java字符串
	 * @param maxBytes 最大字节长度
	 * @return 截断后的字符
	 */
	public static String truncateUtf8(String str, int maxBytes) {
		Charset charset = StandardCharsets.UTF_8;
		//UTF-8编码单个字符最大长度4
		return truncateByByteLength(str, charset, maxBytes, 4, true);
	}

	/**
	 * 截断字符串，使用其按照指定编码为字节后不超过maxBytes长度<br>
	 * 此方法用于截取总bytes数不超过指定长度，如果字符出没有超出原样输出，如果超出了，则截取掉超出部分，并可选添加...，
	 * 但是添加“...”后总长度也不超过限制长度。
	 *
	 * @param str        原始字符串
	 * @param charset    指定编码
	 * @param maxBytes   最大字节数
	 * @param factor     速算因子，取该编码下单个字符的最大可能字节数
	 * @param appendDots 截断后是否追加省略号(...)
	 * @return 截断后的字符串
	 */
	public static String truncateByByteLength(String str, Charset charset, int maxBytes, int factor,
			boolean appendDots) {
		//字符数*速算因子<=最大字节数
		if (str == null || str.length() * factor <= maxBytes) {
			return str;
		}
		final byte[] sba = str.getBytes(charset);
		if (sba.length <= maxBytes) {
			return str;
		}
		//限制字节数
		final int limitBytes;
		if (appendDots) {
			limitBytes = maxBytes - "...".getBytes(charset).length;
		} else {
			limitBytes = maxBytes;
		}
		final ByteBuffer bb = ByteBuffer.wrap(sba, 0, limitBytes);
		final CharBuffer cb = CharBuffer.allocate(limitBytes);
		final CharsetDecoder decoder = charset.newDecoder();
		//忽略被截断的字符
		decoder.onMalformedInput(CodingErrorAction.IGNORE);
		decoder.decode(bb, cb, true);
		decoder.flush(cb);
		final String result = new String(cb.array(), 0, cb.position());
		if (appendDots) {
			return result + "...";
		}
		return result;
	}
}
