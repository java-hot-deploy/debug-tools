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
package io.github.future0923.debug.tools.base.hutool.core.text;

/**
 * 常用字符常量
 * @see StrPool
 * @author looly
 * @since 5.6.3
 */
public interface CharPool {
	/**
	 * 字符常量：空格符 {@code ' '}
	 */
	char SPACE = ' ';
	/**
	 * 字符常量：制表符 {@code '\t'}
	 */
	char TAB = '	';
	/**
	 * 字符常量：点 {@code '.'}
	 */
	char DOT = '.';
	/**
	 * 字符常量：斜杠 {@code '/'}
	 */
	char SLASH = '/';
	/**
	 * 字符常量：反斜杠 {@code '\\'}
	 */
	char BACKSLASH = '\\';
	/**
	 * 字符常量：回车符 {@code '\r'}
	 */
	char CR = '\r';
	/**
	 * 字符常量：换行符 {@code '\n'}
	 */
	char LF = '\n';
	/**
	 * 字符常量：减号（连接符） {@code '-'}
	 */
	char DASHED = '-';
	/**
	 * 字符常量：下划线 {@code '_'}
	 */
	char UNDERLINE = '_';
	/**
	 * 字符常量：逗号 {@code ','}
	 */
	char COMMA = ',';
	/**
	 * 字符常量：花括号（左） <code>'{'</code>
	 */
	char DELIM_START = '{';
	/**
	 * 字符常量：花括号（右） <code>'}'</code>
	 */
	char DELIM_END = '}';
	/**
	 * 字符常量：中括号（左） {@code '['}
	 */
	char BRACKET_START = '[';
	/**
	 * 字符常量：中括号（右） {@code ']'}
	 */
	char BRACKET_END = ']';
	/**
	 * 字符常量：双引号 {@code '"'}
	 */
	char DOUBLE_QUOTES = '"';
	/**
	 * 字符常量：单引号 {@code '\''}
	 */
	char SINGLE_QUOTE = '\'';
	/**
	 * 字符常量：与 {@code '&'}
	 */
	char AMP = '&';
	/**
	 * 字符常量：冒号 {@code ':'}
	 */
	char COLON = ':';
	/**
	 * 字符常量：艾特 {@code '@'}
	 */
	char AT = '@';
}
