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
package io.github.future0923.debug.tools.base.hutool.core.io.unit;

import io.github.future0923.debug.tools.base.hutool.core.util.ArrayUtil;

import java.text.DecimalFormat;

/**
 * 数据大小工具类
 *
 * @author looly
 * @since 5.3.10
 */
public class DataSizeUtil {

	/**
	 * 解析数据大小字符串，转换为bytes大小
	 *
	 * @param text 数据大小字符串，类似于：12KB, 5MB等
	 * @return bytes大小
	 */
	public static long parse(String text) {
		return DataSize.parse(text).toBytes();
	}

	/**
	 * 可读的文件大小<br>
	 * 参考 http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
	 *
	 * @param size Long类型大小
	 * @return 大小
	 */
	public static String format(long size) {
		if (size <= 0) {
			return "0";
		}
		int digitGroups = Math.min(DataUnit.UNIT_NAMES.length-1, (int) (Math.log10(size) / Math.log10(1024)));
		return new DecimalFormat("#,##0.##")
				.format(size / Math.pow(1024, digitGroups)) + " " + DataUnit.UNIT_NAMES[digitGroups];
	}

	/**
	 * 根据单位，将文件大小转换为对应单位的大小
	 *
	 * @param size 文件大小
	 * @param fileDataUnit 单位
	 * @return 大小
	 * @since 5.8.34
	 */
	public static String format(Long size, DataUnit fileDataUnit){
		if (size <= 0) {
			return "0";
		}
		int digitGroups = ArrayUtil.indexOf(DataUnit.UNIT_NAMES,fileDataUnit.getSuffix());
		return new DecimalFormat("##0.##").format(size / Math.pow(1024, digitGroups)) + " " + DataUnit.UNIT_NAMES[digitGroups];
	}
}
