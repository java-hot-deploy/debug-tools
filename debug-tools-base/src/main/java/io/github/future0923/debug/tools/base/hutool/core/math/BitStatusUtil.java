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
package io.github.future0923.debug.tools.base.hutool.core.math;

/**
 * 通过位运算表示状态的工具类<br>
 * 参数必须是 `偶数` 且 `大于等于0`！
 *
 * 工具实现见博客：https://blog.starxg.com/2020/11/bit-status/
 *
 * @author huangxingguang，senssic
 * @since 5.6.6
 */
public class BitStatusUtil {

	/**
	 * 增加状态
	 *
	 * @param states 原状态
	 * @param stat   要添加的状态
	 * @return 新的状态值
	 */
	public static int add(int states, int stat) {
		check(states, stat);
		return states | stat;
	}

	/**
	 * 判断是否含有状态
	 *
	 * @param states 原状态
	 * @param stat   要判断的状态
	 * @return true：有
	 */
	public static boolean has(int states, int stat) {
		check(states, stat);
		return (states & stat) == stat;
	}

	/**
	 * 删除一个状态
	 *
	 * @param states 原状态
	 * @param stat   要删除的状态
	 * @return 新的状态值
	 */
	public static int remove(int states, int stat) {
		check(states, stat);
		if (has(states, stat)) {
			return states ^ stat;
		}
		return states;
	}

	/**
	 * 清空状态就是0
	 *
	 * @return 0
	 */
	public static int clear() {
		return 0;
	}

	/**
	 * 检查
	 * <ul>
	 *     <li>必须大于0</li>
	 *     <li>必须为偶数</li>
	 * </ul>
	 *
	 * @param args 被检查的状态
	 */
	private static void check(int... args) {
		for (int arg : args) {
			if (arg < 0) {
				throw new IllegalArgumentException(arg + " 必须大于等于0");
			}
			if ((arg & 1) == 1) {
				throw new IllegalArgumentException(arg + " 不是偶数");
			}
		}
	}
}
