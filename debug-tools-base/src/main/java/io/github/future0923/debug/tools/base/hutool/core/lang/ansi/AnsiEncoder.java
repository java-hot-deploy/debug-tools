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
package io.github.future0923.debug.tools.base.hutool.core.lang.ansi;


/**
 * 生成ANSI格式的编码输出
 *
 * @author Phillip Webb
 * @since 1.0.0
 */
public abstract class AnsiEncoder {

	private static final String ENCODE_JOIN = ";";
	private static final String ENCODE_START = "\033[";
	private static final String ENCODE_END = "m";
	private static final String RESET = "0;" + AnsiColor.DEFAULT;

	/**
	 * 创建ANSI字符串，参数中的{@link AnsiElement}会被转换为编码形式。
	 *
	 * @param elements 节点数组
	 * @return ANSI字符串
	 */
	public static String encode(Object... elements) {
		final StringBuilder sb = new StringBuilder();
		buildEnabled(sb, elements);
		return sb.toString();
	}

	/**
	 * 追加需要需转义的节点
	 *
	 * @param sb       {@link StringBuilder}
	 * @param elements 节点列表
	 */
	private static void buildEnabled(StringBuilder sb, Object[] elements) {
		boolean writingAnsi = false;
		boolean containsEncoding = false;
		for (Object element : elements) {
			if (null == element) {
				continue;
			}
			if (element instanceof AnsiElement) {
				containsEncoding = true;
				if (writingAnsi) {
					sb.append(ENCODE_JOIN);
				} else {
					sb.append(ENCODE_START);
					writingAnsi = true;
				}
			} else {
				if (writingAnsi) {
					sb.append(ENCODE_END);
					writingAnsi = false;
				}
			}
			sb.append(element);
		}

		// 恢复默认
		if (containsEncoding) {
			sb.append(writingAnsi ? ENCODE_JOIN : ENCODE_START);
			sb.append(RESET);
			sb.append(ENCODE_END);
		}
	}
}
