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
package io.github.future0923.debug.tools.base.hutool.core.lang;

import io.github.future0923.debug.tools.base.hutool.core.date.DateUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ClassLoaderUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.RandomUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.RuntimeUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;

import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MongoDB ID生成策略实现<br>
 * ObjectId由以下几部分组成：
 *
 * <pre>
 * 1. Time 时间戳。
 * 2. Machine 所在主机的唯一标识符，一般是机器主机名的散列值。
 * 3. PID 进程ID。确保同一机器中不冲突
 * 4. INC 自增计数器。确保同一秒内产生objectId的唯一性。
 * </pre>
 *
 * <table summary="" border="1">
 *     <tr>
 *         <td>时间戳</td>
 *         <td>机器ID</td>
 *         <td>进程ID</td>
 *         <td>自增计数器</td>
 *     </tr>
 *     <tr>
 *         <td>4</td>
 *         <td>3</td>
 *         <td>2</td>
 *         <td>3</td>
 *     </tr>
 * </table>
 *
 * 参考：http://blog.csdn.net/qxc1281/article/details/54021882
 *
 * @author looly
 * @since 4.0.0
 *
 */
public class ObjectId {

	/** 线程安全的下一个随机数,每次生成自增+1 */
	private static final AtomicInteger NEXT_INC = new AtomicInteger(RandomUtil.randomInt());
	/** 机器信息 */
	private static final int MACHINE = getMachinePiece() | getProcessPiece();

	/**
	 * 给定的字符串是否为有效的ObjectId
	 *
	 * @param s 字符串
	 * @return 是否为有效的ObjectId
	 */
	public static boolean isValid(String s) {
		if (s == null) {
			return false;
		}
		s = StrUtil.removeAll(s, "-");
		final int len = s.length();
		if (len != 24) {
			return false;
		}

		char c;
		for (int i = 0; i < len; i++) {
			c = s.charAt(i);
			if (c >= '0' && c <= '9') {
				continue;
			}
			if (c >= 'a' && c <= 'f') {
				continue;
			}
			if (c >= 'A' && c <= 'F') {
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * 获取一个objectId的bytes表现形式
	 *
	 * @return objectId
	 * @since 4.1.15
	 */
	public static byte[] nextBytes() {
		final ByteBuffer bb = ByteBuffer.wrap(new byte[12]);
		bb.putInt((int) DateUtil.currentSeconds());// 4位
		bb.putInt(MACHINE);// 4位
		bb.putInt(NEXT_INC.getAndIncrement());// 4位

		return bb.array();
	}

	/**
	 * 获取一个objectId用下划线分割
	 *
	 * @return objectId
	 */
	public static String next() {
		return next(false);
	}

	/**
	 * 获取一个objectId
	 *
	 * @param withHyphen 是否包含分隔符
	 * @return objectId
	 */
	public static String next(boolean withHyphen) {
		byte[] array = nextBytes();
		final StringBuilder buf = new StringBuilder(withHyphen ? 26 : 24);
		int t;
		for (int i = 0; i < array.length; i++) {
			if (withHyphen && i % 4 == 0 && i != 0) {
				buf.append("-");
			}
			t = array[i] & 0xff;
			if (t < 16) {
				buf.append('0');
			}
			buf.append(Integer.toHexString(t));

		}
		return buf.toString();
	}

	// ----------------------------------------------------------------------------------------- Private method start
	/**
	 * 获取机器码片段
	 *
	 * @return 机器码片段
	 */
	private static int getMachinePiece() {
		// 机器码
		int machinePiece;
		try {
			StringBuilder netSb = new StringBuilder();
			// 返回机器所有的网络接口
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			// 遍历网络接口
			while (e.hasMoreElements()) {
				NetworkInterface ni = e.nextElement();
				// 网络接口信息
				netSb.append(ni.toString());
			}
			// 保留后两位
			machinePiece = netSb.toString().hashCode() << 16;
		} catch (Throwable e) {
			// 出问题随机生成,保留后两位
			machinePiece = (RandomUtil.randomInt()) << 16;
		}
		return machinePiece;
	}

	/**
	 * 获取进程码片段
	 *
	 * @return 进程码片段
	 */
	private static int getProcessPiece() {
		// 进程码
		// 因为静态变量类加载可能相同,所以要获取进程ID + 加载对象的ID值
		final int processPiece;
		// 进程ID初始化
		int processId;
		try {
			processId = RuntimeUtil.getPid();
		} catch (Throwable t) {
			processId = RandomUtil.randomInt();
		}

		final ClassLoader loader = ClassLoaderUtil.getClassLoader();
		// 返回对象哈希码,无论是否重写hashCode方法
		int loaderId = (loader != null) ? System.identityHashCode(loader) : 0;

		// 进程ID + 对象加载ID
		// 保留前2位
		final String processSb = Integer.toHexString(processId) + Integer.toHexString(loaderId);
		processPiece = processSb.hashCode() & 0xFFFF;

		return processPiece;
	}
	// ----------------------------------------------------------------------------------------- Private method end
}
