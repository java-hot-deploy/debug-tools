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
package io.github.future0923.debug.tools.base.hutool.core.compress;

import io.github.future0923.debug.tools.base.hutool.core.exceptions.UtilException;
import io.github.future0923.debug.tools.base.hutool.core.io.FileUtil;
import io.github.future0923.debug.tools.base.hutool.core.io.IORuntimeException;
import io.github.future0923.debug.tools.base.hutool.core.io.IoUtil;
import io.github.future0923.debug.tools.base.hutool.core.lang.Filter;
import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ZipUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Zip文件或流读取器，一般用于Zip文件解压
 *
 * @author looly
 * @since 5.7.8
 */
public class ZipReader implements Closeable {

	// size of uncompressed zip entry shouldn't be bigger of compressed in MAX_SIZE_DIFF times
	private static final int DEFAULT_MAX_SIZE_DIFF = 100;

	private ZipFile zipFile;
	private ZipInputStream in;
	/**
	 * 检查ZipBomb文件差异倍数，-1表示不检查ZipBomb
	 */
	private int maxSizeDiff = DEFAULT_MAX_SIZE_DIFF;

	/**
	 * 创建ZipReader
	 *
	 * @param zipFile 生成的Zip文件
	 * @param charset 编码
	 * @return ZipReader
	 */
	public static ZipReader of(File zipFile, Charset charset) {
		return new ZipReader(zipFile, charset);
	}

	/**
	 * 创建ZipReader
	 *
	 * @param in      Zip输入的流，一般为输入文件流
	 * @param charset 编码
	 * @return ZipReader
	 */
	public static ZipReader of(InputStream in, Charset charset) {
		return new ZipReader(in, charset);
	}

	/**
	 * 构造
	 *
	 * @param zipFile 读取的的Zip文件
	 * @param charset 编码
	 */
	public ZipReader(File zipFile, Charset charset) {
		this.zipFile = ZipUtil.toZipFile(zipFile, charset);
	}

	/**
	 * 构造
	 *
	 * @param zipFile 读取的的Zip文件
	 */
	public ZipReader(ZipFile zipFile) {
		this.zipFile = zipFile;
	}

	/**
	 * 构造
	 *
	 * @param in      读取的的Zip文件流
	 * @param charset 编码
	 */
	public ZipReader(InputStream in, Charset charset) {
		this.in = new ZipInputStream(in, charset);
	}

	/**
	 * 构造
	 *
	 * @param zin 读取的的Zip文件流
	 */
	public ZipReader(ZipInputStream zin) {
		this.in = zin;
	}

	/**
	 * 设置检查ZipBomb文件差异倍数，-1表示不检查ZipBomb
	 *
	 * @param maxSizeDiff 检查ZipBomb文件差异倍数，-1表示不检查ZipBomb
	 * @return this
	 * @since 5.8.21
	 */
	public ZipReader setMaxSizeDiff(final int maxSizeDiff) {
		this.maxSizeDiff = maxSizeDiff;
		return this;
	}

	/**
	 * 获取指定路径的文件流<br>
	 * 如果是文件模式，则直接获取Entry对应的流，如果是流模式，则遍历entry后，找到对应流返回
	 *
	 * @param path 路径
	 * @return 文件流
	 */
	public InputStream get(String path) {
		if (null != this.zipFile) {
			final ZipFile zipFile = this.zipFile;
			final ZipEntry entry = zipFile.getEntry(path);
			if (null != entry) {
				return ZipUtil.getStream(zipFile, entry);
			}
		} else {
			try {
				ZipEntry zipEntry;
				while (null != (zipEntry = in.getNextEntry())) {
					if (zipEntry.getName().equals(path)) {
						return this.in;
					}
				}
			} catch (IOException e) {
				throw new IORuntimeException(e);
			}
		}

		return null;
	}

	/**
	 * 解压到指定目录中
	 *
	 * @param outFile 解压到的目录
	 * @return 解压的目录
	 * @throws IORuntimeException IO异常
	 */
	public File readTo(File outFile) throws IORuntimeException {
		return readTo(outFile, null);
	}

	/**
	 * 解压到指定目录中
	 *
	 * @param outFile     解压到的目录
	 * @param entryFilter 过滤器，排除不需要的文件
	 * @return 解压的目录
	 * @throws IORuntimeException IO异常
	 * @since 5.7.12
	 */
	public File readTo(File outFile, Filter<ZipEntry> entryFilter) throws IORuntimeException {
		read((zipEntry) -> {
			if (null == entryFilter || entryFilter.accept(zipEntry)) {
				//gitee issue #I4ZDQI
				String path = zipEntry.getName();
				if (FileUtil.isWindows()) {
					// Win系统下
					path = StrUtil.replace(path, "*", "_");
				}
				// FileUtil.file会检查slip漏洞，漏洞说明见http://blog.nsfocus.net/zip-slip-2/
				final File outItemFile = FileUtil.file(outFile, path);
				if (zipEntry.isDirectory()) {
					// 目录
					//noinspection ResultOfMethodCallIgnored
					outItemFile.mkdirs();
				} else {
					InputStream in;
					if (null != this.zipFile) {
						in = ZipUtil.getStream(this.zipFile, zipEntry);
					} else {
						in = this.in;
					}
					// 文件
					FileUtil.writeFromStream(in, outItemFile, false);
				}
			}
		});
		return outFile;
	}

	/**
	 * 读取并处理Zip文件中的每一个{@link ZipEntry}
	 *
	 * @param consumer {@link ZipEntry}处理器
	 * @return this
	 * @throws IORuntimeException IO异常
	 */
	public ZipReader read(Consumer<ZipEntry> consumer) throws IORuntimeException {
		if (null != this.zipFile) {
			readFromZipFile(consumer);
		} else {
			readFromStream(consumer);
		}
		return this;
	}

	@Override
	public void close() throws IORuntimeException {
		if (null != this.zipFile) {
			IoUtil.close(this.zipFile);
		} else {
			IoUtil.close(this.in);
		}
	}

	/**
	 * 读取并处理Zip文件中的每一个{@link ZipEntry}
	 *
	 * @param consumer {@link ZipEntry}处理器
	 */
	private void readFromZipFile(Consumer<ZipEntry> consumer) {
		final Enumeration<? extends ZipEntry> em = zipFile.entries();
		while (em.hasMoreElements()) {
			consumer.accept(checkZipBomb(em.nextElement()));
		}
	}

	/**
	 * 读取并处理Zip流中的每一个{@link ZipEntry}
	 *
	 * @param consumer {@link ZipEntry}处理器
	 * @throws IORuntimeException IO异常
	 */
	private void readFromStream(Consumer<ZipEntry> consumer) throws IORuntimeException {
		try {
			ZipEntry zipEntry;
			while (null != (zipEntry = in.getNextEntry())) {
				consumer.accept(zipEntry);
				// 检查ZipBomb放在读取内容之后，以便entry中的信息正常读取
				checkZipBomb(zipEntry);
			}
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * 检查Zip bomb漏洞
	 *
	 * @param entry {@link ZipEntry}
	 * @return 检查后的{@link ZipEntry}
	 */
	private ZipEntry checkZipBomb(ZipEntry entry) {
		if (null == entry) {
			return null;
		}
		if(maxSizeDiff < 0 || entry.isDirectory()){
			// 目录不检查
			return entry;
		}

		final long compressedSize = entry.getCompressedSize();
		final long uncompressedSize = entry.getSize();
		if (compressedSize < 0 || uncompressedSize < 0 ||
				// 默认压缩比例是100倍，一旦发现压缩率超过这个阈值，被认为是Zip bomb
				compressedSize * maxSizeDiff < uncompressedSize) {
			throw new UtilException("Zip bomb attack detected, invalid sizes: compressed {}, uncompressed {}, name {}",
					compressedSize, uncompressedSize, entry.getName());
		}
		return entry;
	}
}
