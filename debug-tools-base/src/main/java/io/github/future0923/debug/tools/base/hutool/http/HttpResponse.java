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
package io.github.future0923.debug.tools.base.hutool.http;

import io.github.future0923.debug.tools.base.hutool.core.collection.CollUtil;
import io.github.future0923.debug.tools.base.hutool.core.convert.Convert;
import io.github.future0923.debug.tools.base.hutool.core.io.FastByteArrayOutputStream;
import io.github.future0923.debug.tools.base.hutool.core.io.FileUtil;
import io.github.future0923.debug.tools.base.hutool.core.io.IORuntimeException;
import io.github.future0923.debug.tools.base.hutool.core.io.IoUtil;
import io.github.future0923.debug.tools.base.hutool.core.io.StreamProgress;
import io.github.future0923.debug.tools.base.hutool.core.io.resource.BytesResource;
import io.github.future0923.debug.tools.base.hutool.core.lang.Assert;
import io.github.future0923.debug.tools.base.hutool.core.util.ObjUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ReUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.URLUtil;
import io.github.future0923.debug.tools.base.hutool.http.cookie.GlobalCookieManager;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;

/**
 * Http响应类<br>
 * 非线程安全对象
 *
 * @author Looly
 */
public class HttpResponse extends HttpBase<HttpResponse> implements Closeable {

	/**
	 * Http配置
	 */
	protected HttpConfig config;
	/**
	 * 持有连接对象
	 */
	protected HttpConnection httpConnection;
	/**
	 * Http请求原始流
	 */
	protected InputStream in;
	/**
	 * 是否异步，异步下只持有流，否则将在初始化时直接读取body内容
	 */
	private volatile boolean isAsync;
	/**
	 * 响应状态码
	 */
	protected int status;
	/**
	 * 是否忽略读取Http响应体
	 */
	private final boolean ignoreBody;
	/**
	 * 从响应中获取的编码
	 */
	private Charset charsetFromResponse;

	/**
	 * 构造
	 *
	 * @param httpConnection {@link HttpConnection}
	 * @param config         Http配置
	 * @param charset        编码，从请求编码中获取默认编码
	 * @param isAsync        是否异步
	 * @param isIgnoreBody   是否忽略读取响应体
	 * @since 3.1.2
	 */
	protected HttpResponse(HttpConnection httpConnection, HttpConfig config, Charset charset, boolean isAsync, boolean isIgnoreBody) {
		this.httpConnection = httpConnection;
		this.config = config;
		this.charset = charset;
		this.isAsync = isAsync;
		this.ignoreBody = isIgnoreBody;
		initWithDisconnect();
	}

	/**
	 * 获取状态码
	 *
	 * @return 状态码
	 */
	public int getStatus() {
		return this.status;
	}

	/**
	 * 请求是否成功，判断依据为：状态码范围在200~299内。
	 *
	 * @return 是否成功请求
	 * @since 4.1.9
	 */
	public boolean isOk() {
		return this.status >= 200 && this.status < 300;
	}

	/**
	 * 同步<br>
	 * 如果为异步状态，则暂时不读取服务器中响应的内容，而是持有Http链接的{@link InputStream}。<br>
	 * 当调用此方法时，异步状态转为同步状态，此时从Http链接流中读取body内容并暂存在内容中。如果已经是同步状态，则不进行任何操作。
	 *
	 * @return this
	 */
	public HttpResponse sync() {
		return this.isAsync ? forceSync() : this;
	}

	// ---------------------------------------------------------------- Http Response Header start

	/**
	 * 获取内容编码
	 *
	 * @return String
	 */
	public String contentEncoding() {
		return header(Header.CONTENT_ENCODING);
	}

	/**
	 * 获取内容长度，以下情况长度无效：
	 * <ul>
	 *     <li>Transfer-Encoding: Chunked</li>
	 *     <li>Content-Encoding: XXX</li>
	 * </ul>
	 * 参考：https://blog.csdn.net/jiang7701037/article/details/86304302
	 *
	 * @return 长度，-1表示服务端未返回或长度无效
	 * @since 5.7.9
	 */
	public long contentLength() {
		long contentLength = Convert.toLong(header(Header.CONTENT_LENGTH), -1L);
		if (contentLength > 0 && (isChunked() || StrUtil.isNotBlank(contentEncoding()))) {
			//按照HTTP协议规范，在 Transfer-Encoding和Content-Encoding设置后 Content-Length 无效。
			contentLength = -1;
		}
		return contentLength;
	}

	/**
	 * 是否为gzip压缩过的内容
	 *
	 * @return 是否为gzip压缩过的内容
	 */
	public boolean isGzip() {
		final String contentEncoding = contentEncoding();
		return "gzip".equalsIgnoreCase(contentEncoding);
	}

	/**
	 * 是否为zlib(Deflate)压缩过的内容
	 *
	 * @return 是否为zlib(Deflate)压缩过的内容
	 * @since 4.5.7
	 */
	public boolean isDeflate() {
		final String contentEncoding = contentEncoding();
		return "deflate".equalsIgnoreCase(contentEncoding);
	}

	/**
	 * 是否为Transfer-Encoding:Chunked的内容
	 *
	 * @return 是否为Transfer-Encoding:Chunked的内容
	 * @since 4.6.2
	 */
	public boolean isChunked() {
		final String transferEncoding = header(Header.TRANSFER_ENCODING);
		return "Chunked".equalsIgnoreCase(transferEncoding);
	}

	/**
	 * 获取本次请求服务器返回的Cookie信息
	 *
	 * @return Cookie字符串
	 * @since 3.1.1
	 */
	public String getCookieStr() {
		return header(Header.SET_COOKIE);
	}

	/**
	 * 获取Cookie
	 *
	 * @return Cookie列表
	 * @since 3.1.1
	 */
	public List<HttpCookie> getCookies() {
		return GlobalCookieManager.getCookies(this.httpConnection);
	}

	/**
	 * 获取Cookie
	 *
	 * @param name Cookie名
	 * @return {@link HttpCookie}
	 * @since 4.1.4
	 */
	public HttpCookie getCookie(String name) {
		List<HttpCookie> cookie = getCookies();
		if (null != cookie) {
			for (HttpCookie httpCookie : cookie) {
				if (httpCookie.getName().equals(name)) {
					return httpCookie;
				}
			}
		}
		return null;
	}

	/**
	 * 获取Cookie值
	 *
	 * @param name Cookie名
	 * @return Cookie值
	 * @since 4.1.4
	 */
	public String getCookieValue(String name) {
		final HttpCookie cookie = getCookie(name);
		return (null == cookie) ? null : cookie.getValue();
	}
	// ---------------------------------------------------------------- Http Response Header end

	// ---------------------------------------------------------------- Body start

	/**
	 * 获得服务区响应流<br>
	 * 异步模式下获取Http原生流，同步模式下获取获取到的在内存中的副本<br>
	 * 如果想在同步模式下获取流，请先调用{@link #sync()}方法强制同步<br>
	 * 流获取后处理完毕需关闭此类
	 *
	 * @return 响应流
	 */
	public InputStream bodyStream() {
		if (isAsync) {
			return this.in;
		}
		return null == this.body ? null : this.body.getStream();
	}

	/**
	 * 获取响应流字节码<br>
	 * 此方法会转为同步模式
	 *
	 * @return byte[]
	 */
	@Override
	public byte[] bodyBytes() {
		sync();
		return super.bodyBytes();
	}

	/**
	 * 设置主体字节码<br>
	 * 需在此方法调用前使用charset方法设置编码，否则使用默认编码UTF-8
	 *
	 * @param bodyBytes 主体
	 * @return this
	 */
	public HttpResponse body(byte[] bodyBytes) {
		sync();
		if (null != bodyBytes) {
			this.body = new BytesResource(bodyBytes);
		}
		return this;
	}

	/**
	 * 获取响应主体
	 *
	 * @return String
	 * @throws HttpException 包装IO异常
	 */
	public String body() throws HttpException {
		return HttpUtil.getString(bodyBytes(), this.charset, null == this.charsetFromResponse);
	}

	/**
	 * 将响应内容写出到{@link OutputStream}<br>
	 * 异步模式下直接读取Http流写出，同步模式下将存储在内存中的响应内容写出<br>
	 * 写出后会关闭Http流（异步模式）
	 *
	 * @param out            写出的流
	 * @param isCloseOut     是否关闭输出流
	 * @param streamProgress 进度显示接口，通过实现此接口显示下载进度
	 * @return 写出bytes数
	 * @since 3.3.2
	 */
	public long writeBody(OutputStream out, boolean isCloseOut, StreamProgress streamProgress) {
		Assert.notNull(out, "[out] must be not null!");
		final long contentLength = contentLength();
		try {
			return copyBody(bodyStream(), out, contentLength, streamProgress, this.config.ignoreEOFError);
		} finally {
			IoUtil.close(this);
			if (isCloseOut) {
				IoUtil.close(out);
			}
		}
	}

	/**
	 * 将响应内容写出到文件<br>
	 * 异步模式下直接读取Http流写出，同步模式下将存储在内存中的响应内容写出<br>
	 * 写出后会关闭Http流（异步模式）
	 *
	 * @param targetFileOrDir 写出到的文件或目录
	 * @param streamProgress  进度显示接口，通过实现此接口显示下载进度
	 * @return 写出bytes数
	 * @since 3.3.2
	 */
	public long writeBody(File targetFileOrDir, StreamProgress streamProgress) {
		Assert.notNull(targetFileOrDir, "[targetFileOrDir] must be not null!");

		final File outFile = completeFileNameFromHeader(targetFileOrDir);
		return writeBody(FileUtil.getOutputStream(outFile), true, streamProgress);
	}

	/**
	 * 将响应内容写出到文件-避免未完成的文件<br>
	 * 异步模式下直接读取Http流写出，同步模式下将存储在内存中的响应内容写出<br>
	 * 写出后会关闭Http流（异步模式）<br>
	 * 来自：https://gitee.com/chinabugotech/hutool/pulls/407<br>
	 * 此方法原理是先在目标文件同级目录下创建临时文件，下载之，等下载完毕后重命名，避免因下载错误导致的文件不完整。
	 *
	 * @param targetFileOrDir 写出到的文件或目录
	 * @param tempFileSuffix  临时文件后缀，默认".temp"
	 * @param streamProgress  进度显示接口，通过实现此接口显示下载进度
	 * @return 写出bytes数
	 * @since 5.7.12
	 */
	public long writeBody(File targetFileOrDir, String tempFileSuffix, StreamProgress streamProgress) {
		Assert.notNull(targetFileOrDir, "[targetFileOrDir] must be not null!");

		File outFile = completeFileNameFromHeader(targetFileOrDir);

		if (StrUtil.isBlank(tempFileSuffix)) {
			tempFileSuffix = ".temp";
		} else {
			tempFileSuffix = StrUtil.addPrefixIfNot(tempFileSuffix, StrUtil.DOT);
		}

		// 目标文件真实名称
		final String fileName = outFile.getName();
		// 临时文件名称
		final String tempFileName = fileName + tempFileSuffix;

		// 临时文件
		outFile = new File(outFile.getParentFile(), tempFileName);

		long length;
		try {
			length = writeBody(outFile, streamProgress);
			// 重命名下载好的临时文件
			FileUtil.rename(outFile, fileName, true);
		} catch (Throwable e) {
			// 异常则删除临时文件
			FileUtil.del(outFile);
			throw new HttpException(e);
		}
		return length;
	}

	/**
	 * 将响应内容写出到文件<br>
	 * 异步模式下直接读取Http流写出，同步模式下将存储在内存中的响应内容写出<br>
	 * 写出后会关闭Http流（异步模式）
	 *
	 * @param targetFileOrDir 写出到的文件
	 * @param streamProgress  进度显示接口，通过实现此接口显示下载进度
	 * @return 写出的文件
	 * @since 5.6.4
	 */
	public File writeBodyForFile(File targetFileOrDir, StreamProgress streamProgress) {
		Assert.notNull(targetFileOrDir, "[targetFileOrDir] must be not null!");

		final File outFile = completeFileNameFromHeader(targetFileOrDir);
		writeBody(FileUtil.getOutputStream(outFile), true, streamProgress);

		return outFile;
	}

	/**
	 * 将响应内容写出到文件<br>
	 * 异步模式下直接读取Http流写出，同步模式下将存储在内存中的响应内容写出<br>
	 * 写出后会关闭Http流（异步模式）
	 *
	 * @param targetFileOrDir 写出到的文件或目录
	 * @return 写出bytes数
	 * @since 3.3.2
	 */
	public long writeBody(File targetFileOrDir) {
		return writeBody(targetFileOrDir, null);
	}

	/**
	 * 将响应内容写出到文件<br>
	 * 异步模式下直接读取Http流写出，同步模式下将存储在内存中的响应内容写出<br>
	 * 写出后会关闭Http流（异步模式）
	 *
	 * @param targetFileOrDir 写出到的文件或目录的路径
	 * @return 写出bytes数
	 * @since 3.3.2
	 */
	public long writeBody(String targetFileOrDir) {
		return writeBody(FileUtil.file(targetFileOrDir));
	}
	// ---------------------------------------------------------------- Body end

	@Override
	public void close() {
		IoUtil.close(this.in);
		this.in = null;
		// 关闭连接
		this.httpConnection.disconnectQuietly();
	}

	@Override
	public String toString() {
		StringBuilder sb = StrUtil.builder();
		sb.append("Response Headers: ").append(StrUtil.CRLF);
		for (Entry<String, List<String>> entry : this.headers.entrySet()) {
			sb.append("    ").append(entry).append(StrUtil.CRLF);
		}

		sb.append("Response Body: ").append(StrUtil.CRLF);
		sb.append("    ").append(this.body()).append(StrUtil.CRLF);

		return sb.toString();
	}

	/**
	 * 从响应头补全下载文件名
	 *
	 * @param targetFileOrDir 目标文件夹或者目标文件
	 * @return File 保存的文件
	 * @since 5.4.1
	 */
	public File completeFileNameFromHeader(File targetFileOrDir) {
		if (false == targetFileOrDir.isDirectory()) {
			// 非目录直接返回
			return targetFileOrDir;
		}

		// 从头信息中获取文件名
		String fileName = getFileNameFromDisposition(null);
		if (StrUtil.isBlank(fileName)) {
			final String path = httpConnection.getUrl().getPath();
			// 从路径中获取文件名
			fileName = StrUtil.subSuf(path, path.lastIndexOf('/') + 1);
			if (StrUtil.isBlank(fileName)) {
				// 编码后的路径做为文件名
				fileName = URLUtil.encodeQuery(path, charset);
			} else {
				// issue#I4K0FS@Gitee
				fileName = URLUtil.decode(fileName, charset);
			}
		}
		return FileUtil.file(targetFileOrDir, fileName);
	}

	/**
	 * 从Content-Disposition头中获取文件名
	 *
	 * @return 文件名，empty表示无
	 */
	public String getFileNameFromDisposition() {
		return getFileNameFromDisposition(null);
	}

	/**
	 * 从Content-Disposition头中获取文件名，以参数名为`filename`为例，规则为：
	 * <ul>
	 *     <li>首先按照RFC5987规范检查`filename*`参数对应的值，即：`filename*="example.txt"`，则获取`example.txt`</li>
	 *     <li>如果找不到`filename*`参数，则检查`filename`参数对应的值，即：`filename="example.txt"`，则获取`example.txt`</li>
	 * </ul>
	 * 按照规范，`Content-Disposition`可能返回多个，此处遍历所有返回头，并且`filename*`始终优先获取，即使`filename`存在并更靠前。<br>
	 * 参考：https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Content-Disposition
	 *
	 * @param paramName 文件参数名，如果为{@code null}则使用默认的`filename`
	 * @return 文件名，empty表示无
	 */
	public String getFileNameFromDisposition(String paramName) {
		paramName = ObjUtil.defaultIfNull(paramName, "filename");
		final List<String> dispositions = headerList(Header.CONTENT_DISPOSITION.getValue());
		String fileName = null;
		if (CollUtil.isNotEmpty(dispositions)) {

			// filename* 采用了 RFC 5987 中规定的编码方式，优先读取
			fileName = getFileNameFromDispositions(dispositions, StrUtil.addSuffixIfNot(paramName, "*"));
			if ((!StrUtil.endWith(fileName, "*")) && StrUtil.isBlank(fileName)) {
				fileName = getFileNameFromDispositions(dispositions, paramName);
			}
		}

		return fileName;
	}

	// ---------------------------------------------------------------- Private method start
	/**
	 * 从Content-Disposition头中获取文件名
	 *
	 * @param dispositions Content-Disposition头列表
	 * @param paramName    文件参数名
	 * @return 文件名，empty表示无
	 */
	private static String getFileNameFromDispositions(final List<String> dispositions, String paramName) {
		// 正则转义
		paramName = StrUtil.replace(paramName, "*", "\\*");
		String fileName = null;
		for (final String disposition : dispositions) {
			fileName = ReUtil.getGroup1(paramName + "=([^;]+)", disposition);
			if (StrUtil.isNotBlank(fileName)) {
				break;
			}
		}
		return getRfc5987Value(fileName);
	}

	/**
	 * 获取rfc5987标准的值，标准见：https://www.rfc-editor.org/rfc/rfc5987#section-3.2.1<br>
	 * 包括：
	 *
	 *<ul>
	 *     <li>Non-extended：无双引号包裹的值</li>
	 *     <li>Non-extended：双引号包裹的值</li>
	 *     <li>Extended notation：编码'语言'值</li>
	 *</ul>
	 *
	 * @param value 值
	 * @return 结果值
	 */
	private static String getRfc5987Value(final String value){
		final List<String> split = StrUtil.split(value, '\'');
		if(3 == split.size()){
			return split.get(2);
		}

		// 普通值
		return StrUtil.unWrap(value, '"');
	}

	/**
	 * 初始化Http响应，并在报错时关闭连接。<br>
	 * 初始化包括：
	 *
	 * <pre>
	 * 1、读取Http状态
	 * 2、读取头信息
	 * 3、持有Http流，并不关闭流
	 * </pre>
	 *
	 * @return this
	 * @throws HttpException IO异常
	 */
	private HttpResponse initWithDisconnect() throws HttpException {
		try {
			init();
		} catch (HttpException e) {
			this.httpConnection.disconnectQuietly();
			throw e;
		}
		return this;
	}

	/**
	 * 初始化Http响应<br>
	 * 初始化包括：
	 *
	 * <pre>
	 * 1、读取Http状态
	 * 2、读取头信息
	 * 3、持有Http流，并不关闭流
	 * </pre>
	 *
	 * @return this
	 * @throws HttpException IO异常
	 */
	private HttpResponse init() throws HttpException {
		// 获取响应状态码
		try {
			this.status = httpConnection.responseCode();
		} catch (IOException e) {
			if (false == (e instanceof FileNotFoundException)) {
				throw new HttpException(e);
			}
			// 服务器无返回内容，忽略之
		}


		// 读取响应头信息
		try {
			this.headers = httpConnection.headers();
		} catch (IllegalArgumentException e) {
			// ignore
			// StaticLog.warn(e, e.getMessage());
		}

		// 存储服务端设置的Cookie信息
		GlobalCookieManager.store(httpConnection);

		// 获取响应编码
		final Charset charset = httpConnection.getCharset();
		this.charsetFromResponse = charset;
		if (null != charset) {
			this.charset = charset;
		}

		// 获取响应内容流
		this.in = new HttpInputStream(this);

		// 同步情况下强制同步
		return this.isAsync ? this : forceSync();
	}

	/**
	 * 强制同步，用于初始化<br>
	 * 强制同步后变化如下：
	 *
	 * <pre>
	 * 1、读取body内容到内存
	 * 2、异步状态设为false（变为同步状态）
	 * 3、关闭Http流
	 * 4、断开与服务器连接
	 * </pre>
	 *
	 * @return this
	 */
	private HttpResponse forceSync() {
		// 非同步状态转为同步状态
		try {
			this.readBody(this.in);
		} catch (IORuntimeException e) {
			//noinspection StatementWithEmptyBody
			if (e.getCause() instanceof FileNotFoundException) {
				// 服务器无返回内容，忽略之
			} else {
				throw new HttpException(e);
			}
		} finally {
			if (this.isAsync) {
				this.isAsync = false;
			}
			this.close();
		}
		return this;
	}

	/**
	 * 读取主体，忽略EOFException异常
	 *
	 * @param in 输入流
	 * @throws IORuntimeException IO异常
	 */
	private void readBody(InputStream in) throws IORuntimeException {
		if (ignoreBody) {
			return;
		}

		final long contentLength = contentLength();
		final FastByteArrayOutputStream out = new FastByteArrayOutputStream((int) contentLength);
		copyBody(in, out, contentLength, null, this.config.ignoreEOFError);
		this.body = new BytesResource(out.toByteArray());
	}

	/**
	 * 将响应内容写出到{@link OutputStream}<br>
	 * 异步模式下直接读取Http流写出，同步模式下将存储在内存中的响应内容写出<br>
	 * 写出后会关闭Http流（异步模式）
	 *
	 * @param in               输入流
	 * @param out              写出的流
	 * @param contentLength    总长度，-1表示未知
	 * @param streamProgress   进度显示接口，通过实现此接口显示下载进度
	 * @param isIgnoreEOFError 是否忽略响应读取时可能的EOF异常
	 * @return 拷贝长度
	 */
	private static long copyBody(InputStream in, OutputStream out, long contentLength, StreamProgress streamProgress, boolean isIgnoreEOFError) {
		if (null == out) {
			throw new NullPointerException("[out] is null!");
		}

		long copyLength = -1;
		try {
			copyLength = IoUtil.copy(in, out, IoUtil.DEFAULT_BUFFER_SIZE, contentLength, streamProgress);
		} catch (IORuntimeException e) {
			//noinspection StatementWithEmptyBody
			if (isIgnoreEOFError
				&& (e.getCause() instanceof EOFException || StrUtil.containsIgnoreCase(e.getMessage(), "Premature EOF"))) {
				// 忽略读取HTTP流中的EOF错误
			} else {
				throw e;
			}
		}
		return copyLength;
	}
	// ---------------------------------------------------------------- Private method end
}
