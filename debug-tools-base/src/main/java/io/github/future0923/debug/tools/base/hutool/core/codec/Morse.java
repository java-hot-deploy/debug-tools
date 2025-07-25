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
package io.github.future0923.debug.tools.base.hutool.core.codec;

import io.github.future0923.debug.tools.base.hutool.core.lang.Assert;
import io.github.future0923.debug.tools.base.hutool.core.util.CharUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 莫尔斯电码的编码和解码实现<br>
 * 参考：https://github.com/TakWolf-Deprecated/Java-MorseCoder
 *
 * @author looly, TakWolf
 * @since 4.4.1
 */
public class Morse {

	private static final Map<Integer, String> ALPHABETS = new HashMap<>(); // code point -> morse
	private static final Map<String, Integer> DICTIONARIES = new HashMap<>(); // morse -> code point

	/**
	 * 注册莫尔斯电码表
	 *
	 * @param abc 字母和字符
	 * @param dict 二进制
	 */
	private static void registerMorse(Character abc, String dict) {
		ALPHABETS.put((int) abc, dict);
		DICTIONARIES.put(dict, (int) abc);
	}

	static {
		// Letters
		registerMorse('A', "01");
		registerMorse('B', "1000");
		registerMorse('C', "1010");
		registerMorse('D', "100");
		registerMorse('E', "0");
		registerMorse('F', "0010");
		registerMorse('G', "110");
		registerMorse('H', "0000");
		registerMorse('I', "00");
		registerMorse('J', "0111");
		registerMorse('K', "101");
		registerMorse('L', "0100");
		registerMorse('M', "11");
		registerMorse('N', "10");
		registerMorse('O', "111");
		registerMorse('P', "0110");
		registerMorse('Q', "1101");
		registerMorse('R', "010");
		registerMorse('S', "000");
		registerMorse('T', "1");
		registerMorse('U', "001");
		registerMorse('V', "0001");
		registerMorse('W', "011");
		registerMorse('X', "1001");
		registerMorse('Y', "1011");
		registerMorse('Z', "1100");
		// Numbers
		registerMorse('0', "11111");
		registerMorse('1', "01111");
		registerMorse('2', "00111");
		registerMorse('3', "00011");
		registerMorse('4', "00001");
		registerMorse('5', "00000");
		registerMorse('6', "10000");
		registerMorse('7', "11000");
		registerMorse('8', "11100");
		registerMorse('9', "11110");
		// Punctuation
		registerMorse('.', "010101");
		registerMorse(',', "110011");
		registerMorse('?', "001100");
		registerMorse('\'', "011110");
		registerMorse('!', "101011");
		registerMorse('/', "10010");
		registerMorse('(', "10110");
		registerMorse(')', "101101");
		registerMorse('&', "01000");
		registerMorse(':', "111000");
		registerMorse(';', "101010");
		registerMorse('=', "10001");
		registerMorse('+', "01010");
		registerMorse('-', "100001");
		registerMorse('_', "001101");
		registerMorse('"', "010010");
		registerMorse('$', "0001001");
		registerMorse('@', "011010");
	}

	private final char dit; // short mark or dot
	private final char dah; // longer mark or dash
	private final char split;

	/**
	 * 构造
	 */
	public Morse() {
		this(CharUtil.DOT, CharUtil.DASHED, CharUtil.SLASH);
	}

	/**
	 * 构造
	 *
	 * @param dit 点表示的字符
	 * @param dah 横线表示的字符
	 * @param split 分隔符
	 */
	public Morse(char dit, char dah, char split) {
		this.dit = dit;
		this.dah = dah;
		this.split = split;
	}

	/**
	 * 编码
	 *
	 * @param text 文本
	 * @return 密文
	 */
	public String encode(String text) {
		Assert.notNull(text, "Text should not be null.");

		text = text.toUpperCase();
		final StringBuilder morseBuilder = new StringBuilder();
		final int len = text.codePointCount(0, text.length());
		for (int i = 0; i < len; i++) {
			int codePoint = text.codePointAt(i);
			String word = ALPHABETS.get(codePoint);
			if (word == null) {
				word = Integer.toBinaryString(codePoint);
			}
			morseBuilder.append(word.replace('0', dit).replace('1', dah)).append(split);
		}
		return morseBuilder.toString();
	}

	/**
	 * 解码
	 *
	 * @param morse 莫尔斯电码
	 * @return 明文
	 */
	public String decode(String morse) {
		Assert.notNull(morse, "Morse should not be null.");

		final char dit = this.dit;
		final char dah = this.dah;
		final char split = this.split;
		if (false == StrUtil.containsOnly(morse, dit, dah, split)) {
			throw new IllegalArgumentException("Incorrect morse.");
		}
		final List<String> words = StrUtil.split(morse, split);
		final StringBuilder textBuilder = new StringBuilder();
		Integer codePoint;
		for (String word : words) {
			if(StrUtil.isEmpty(word)){
				continue;
			}
			word = word.replace(dit, '0').replace(dah, '1');
			codePoint = DICTIONARIES.get(word);
			if (codePoint == null) {
				codePoint = Integer.valueOf(word, 2);
			}
			textBuilder.appendCodePoint(codePoint);
		}
		return textBuilder.toString();
	}
}
