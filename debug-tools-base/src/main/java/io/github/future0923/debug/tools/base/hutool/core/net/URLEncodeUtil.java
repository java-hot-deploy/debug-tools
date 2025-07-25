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
package io.github.future0923.debug.tools.base.hutool.core.net;

import io.github.future0923.debug.tools.base.hutool.core.exceptions.UtilException;
import io.github.future0923.debug.tools.base.hutool.core.util.CharsetUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;

import java.nio.charset.Charset;

/**
 * URL编码工具<br>
 * TODO 在6.x中移除此工具（无法很好区分URL编码和www-form编码）
 *
 * @since 5.7.13
 * @author looly
 */
public class URLEncodeUtil {
	/**
	 * 编码URL，默认使用UTF-8编码<br>
	 * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。
	 *
	 * @param url URL
	 * @return 编码后的URL
	 * @throws UtilException UnsupportedEncodingException
	 */
	public static String encodeAll(String url) {
		return encodeAll(url, CharsetUtil.CHARSET_UTF_8);
	}

	/**
	 * 编码URL<br>
	 * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。
	 *
	 * @param url     URL
	 * @param charset 编码，为null表示不编码
	 * @return 编码后的URL
	 * @throws UtilException UnsupportedEncodingException
	 */
	public static String encodeAll(String url, Charset charset) throws UtilException {
		return RFC3986.UNRESERVED.encode(url, charset);
	}

	/**
	 * 编码URL，默认使用UTF-8编码<br>
	 * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。<br>
	 * 此方法用于URL自动编码，类似于浏览器中键入地址自动编码，对于像类似于“/”的字符不再编码
	 *
	 * @param url URL
	 * @return 编码后的URL
	 * @throws UtilException UnsupportedEncodingException
	 * @since 3.1.2
	 */
	public static String encode(String url) throws UtilException {
		return encode(url, CharsetUtil.CHARSET_UTF_8);
	}

	/**
	 * 编码字符为 application/x-www-form-urlencoded<br>
	 * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。<br>
	 * 此方法用于URL自动编码，类似于浏览器中键入地址自动编码，对于像类似于“/”的字符不再编码
	 *
	 * @param url     被编码内容
	 * @param charset 编码
	 * @return 编码后的字符
	 * @since 4.4.1
	 */
	public static String encode(String url, Charset charset) {
		return RFC3986.PATH.encode(url, charset);
	}

	/**
	 * 编码URL，默认使用UTF-8编码<br>
	 * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。<br>
	 * 此方法用于POST请求中的请求体自动编码，转义大部分特殊字符
	 *
	 * @param url URL
	 * @return 编码后的URL
	 * @throws UtilException UnsupportedEncodingException
	 * @since 3.1.2
	 */
	public static String encodeQuery(String url) throws UtilException {
		return encodeQuery(url, CharsetUtil.CHARSET_UTF_8);
	}

	/**
	 * 编码字符为URL中查询语句<br>
	 * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。<br>
	 * 此方法用于POST请求中的请求体自动编码，转义大部分特殊字符
	 *
	 * @param url     被编码内容
	 * @param charset 编码
	 * @return 编码后的字符
	 * @since 4.4.1
	 */
	public static String encodeQuery(String url, Charset charset) {
		return RFC3986.QUERY.encode(url, charset);
	}

	/**
	 * 编码URL，默认使用UTF-8编码<br>
	 * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。<br>
	 * 此方法用于URL的Segment中自动编码，转义大部分特殊字符
	 *
	 * <pre>
	 * pchar = unreserved（不处理） / pct-encoded / sub-delims（子分隔符） / "@"
	 * unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
	 * sub-delims = "!" / "$" / "&amp;" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
	 * </pre>
	 *
	 * @param url URL
	 * @return 编码后的URL
	 * @throws UtilException UnsupportedEncodingException
	 * @since 5.6.5
	 */
	public static String encodePathSegment(String url) throws UtilException {
		return encodePathSegment(url, CharsetUtil.CHARSET_UTF_8);
	}

	/**
	 * 编码字符为URL中查询语句<br>
	 * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。<br>
	 * 此方法用于URL的Segment中自动编码，转义大部分特殊字符
	 *
	 * <pre>
	 * pchar = unreserved / pct-encoded / sub-delims / ":" / "@"
	 * unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
	 * sub-delims = "!" / "$" / "&amp;" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
	 * </pre>
	 *
	 * @param url     被编码内容
	 * @param charset 编码
	 * @return 编码后的字符
	 * @since 5.6.5
	 */
	public static String encodePathSegment(String url, Charset charset) {
		if (StrUtil.isEmpty(url)) {
			return url;
		}
		return RFC3986.SEGMENT.encode(url, charset);
	}

	/**
	 * 编码URL，默认使用UTF-8编码<br>
	 * URL的Fragment URLEncoder<br>
	 * 默认的编码器针对Fragment，定义如下：
	 *
	 * <pre>
	 * fragment    = *( pchar / "/" / "?" )
	 * pchar       = unreserved / pct-encoded / sub-delims / ":" / "@"
	 * unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
	 * sub-delims  = "!" / "$" / "&amp;" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
	 * </pre>
	 *
	 * 具体见：https://datatracker.ietf.org/doc/html/rfc3986#section-3.5
	 *
	 * @param url     被编码内容
	 * @return 编码后的字符
	 * @since 5.7.13
	 */
	public static String encodeFragment(String url) throws UtilException {
		return encodeFragment(url, CharsetUtil.CHARSET_UTF_8);
	}

	/**
	 * URL的Fragment URLEncoder<br>
	 * 默认的编码器针对Fragment，定义如下：
	 *
	 * <pre>
	 * fragment    = *( pchar / "/" / "?" )
	 * pchar       = unreserved / pct-encoded / sub-delims / ":" / "@"
	 * unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
	 * sub-delims  = "!" / "$" / "&amp;" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
	 * </pre>
	 *
	 * 具体见：https://datatracker.ietf.org/doc/html/rfc3986#section-3.5
	 *
	 * @param url     被编码内容
	 * @param charset 编码
	 * @return 编码后的字符
	 * @since 5.7.13
	 */
	public static String encodeFragment(String url, Charset charset) {
		if (StrUtil.isEmpty(url)) {
			return url;
		}
		return RFC3986.FRAGMENT.encode(url, charset);
	}
}
