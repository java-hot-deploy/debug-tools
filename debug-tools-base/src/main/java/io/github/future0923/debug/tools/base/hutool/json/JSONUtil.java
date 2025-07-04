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
package io.github.future0923.debug.tools.base.hutool.json;

import io.github.future0923.debug.tools.base.hutool.core.convert.NumberWithFormat;
import io.github.future0923.debug.tools.base.hutool.core.io.IORuntimeException;
import io.github.future0923.debug.tools.base.hutool.core.io.file.FileReader;
import io.github.future0923.debug.tools.base.hutool.core.lang.TypeReference;
import io.github.future0923.debug.tools.base.hutool.core.map.MapWrapper;
import io.github.future0923.debug.tools.base.hutool.core.util.ArrayUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ClassUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.HexUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ObjectUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.StrUtil;
import io.github.future0923.debug.tools.base.hutool.json.serialize.GlobalSerializeMapping;
import io.github.future0923.debug.tools.base.hutool.json.serialize.JSONArraySerializer;
import io.github.future0923.debug.tools.base.hutool.json.serialize.JSONDeserializer;
import io.github.future0923.debug.tools.base.hutool.json.serialize.JSONObjectSerializer;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * JSON工具类
 *
 * @author Looly
 */
public class JSONUtil {

	// -------------------------------------------------------------------- Pause start

	/**
	 * 创建JSONObject
	 *
	 * @return JSONObject
	 */
	public static JSONObject createObj() {
		return new JSONObject();
	}

	/**
	 * 创建JSONObject
	 *
	 * @param config JSON配置
	 * @return JSONObject
	 * @since 5.2.5
	 */
	public static JSONObject createObj(JSONConfig config) {
		return new JSONObject(config);
	}

	/**
	 * 创建 JSONArray
	 *
	 * @return JSONArray
	 */
	public static JSONArray createArray() {
		return new JSONArray();
	}

	/**
	 * 创建 JSONArray
	 *
	 * @param config JSON配置
	 * @return JSONArray
	 * @since 5.2.5
	 */
	public static JSONArray createArray(JSONConfig config) {
		return new JSONArray(config);
	}

	/**
	 * JSON字符串转JSONObject对象
	 *
	 * @param jsonStr JSON字符串
	 * @return JSONObject
	 */
	public static JSONObject parseObj(String jsonStr) {
		return new JSONObject(jsonStr);
	}

	/**
	 * JSON字符串转JSONObject对象<br>
	 * 此方法会忽略空值，但是对JSON字符串不影响
	 *
	 * @param obj Bean对象或者Map
	 * @return JSONObject
	 */
	public static JSONObject parseObj(Object obj) {
		return parseObj(obj, null);
	}

	/**
	 * JSON字符串转JSONObject对象<br>
	 * 此方法会忽略空值，但是对JSON字符串不影响
	 *
	 * @param obj    Bean对象或者Map
	 * @param config JSON配置
	 * @return JSONObject
	 * @since 5.3.1
	 */
	public static JSONObject parseObj(Object obj, JSONConfig config) {
		return new JSONObject(obj, ObjectUtil.defaultIfNull(config, JSONConfig::create));
	}

	/**
	 * JSON字符串转JSONObject对象
	 *
	 * @param obj             Bean对象或者Map
	 * @param ignoreNullValue 是否忽略空值，如果source为JSON字符串，不忽略空值
	 * @return JSONObject
	 * @since 3.0.9
	 */
	public static JSONObject parseObj(Object obj, boolean ignoreNullValue) {
		return new JSONObject(obj, ignoreNullValue);
	}

	/**
	 * JSON字符串转JSONObject对象
	 *
	 * @param obj             Bean对象或者Map
	 * @param ignoreNullValue 是否忽略空值，如果source为JSON字符串，不忽略空值
	 * @param isOrder         是否有序
	 * @return JSONObject
	 * @since 4.2.2
	 * @deprecated isOrder参数不再有效
	 */
	@SuppressWarnings("unused")
	@Deprecated
	public static JSONObject parseObj(Object obj, boolean ignoreNullValue, boolean isOrder) {
		return new JSONObject(obj, ignoreNullValue);
	}

	/**
	 * JSON字符串转JSONArray
	 *
	 * @param jsonStr JSON字符串
	 * @return JSONArray
	 */
	public static JSONArray parseArray(String jsonStr) {
		return new JSONArray(jsonStr);
	}

	/**
	 * JSON字符串转JSONArray
	 *
	 * @param arrayOrCollection 数组或集合对象
	 * @return JSONArray
	 * @since 3.0.8
	 */
	public static JSONArray parseArray(Object arrayOrCollection) {
		return parseArray(arrayOrCollection, null);
	}

	/**
	 * JSON字符串转JSONArray
	 *
	 * @param arrayOrCollection 数组或集合对象
	 * @param config            JSON配置
	 * @return JSONArray
	 * @since 5.3.1
	 */
	public static JSONArray parseArray(Object arrayOrCollection, JSONConfig config) {
		return new JSONArray(arrayOrCollection, config);
	}

	/**
	 * JSON字符串转JSONArray
	 *
	 * @param arrayOrCollection 数组或集合对象
	 * @param ignoreNullValue   是否忽略空值
	 * @return JSONArray
	 * @since 3.2.3
	 */
	public static JSONArray parseArray(Object arrayOrCollection, boolean ignoreNullValue) {
		return new JSONArray(arrayOrCollection, ignoreNullValue);
	}

	/**
	 * 转换对象为JSON，如果用户不配置JSONConfig，则JSON的有序与否与传入对象有关。<br>
	 * 支持的对象：
	 * <ul>
	 *     <li>String: 转换为相应的对象</li>
	 *     <li>Array、Iterable、Iterator：转换为JSONArray</li>
	 *     <li>Bean对象：转为JSONObject</li>
	 * </ul>
	 *
	 * @param obj 对象
	 * @return JSON
	 */
	public static JSON parse(Object obj) {
		return parse(obj, null);
	}

	/**
	 * 转换对象为JSON，如果用户不配置JSONConfig，则JSON的有序与否与传入对象有关。<br>
	 * 支持的对象：
	 * <ul>
	 *     <li>String: 转换为相应的对象</li>
	 *     <li>Array、Iterable、Iterator：转换为JSONArray</li>
	 *     <li>Bean对象：转为JSONObject</li>
	 * </ul>
	 *
	 * @param obj    对象
	 * @param config JSON配置，{@code null}使用默认配置
	 * @return JSON
	 * @since 5.3.1
	 */
	public static JSON parse(Object obj, JSONConfig config) {
		if (null == obj) {
			return null;
		}
		JSON json;
		if (obj instanceof JSON) {
			json = (JSON) obj;
		} else if (obj instanceof CharSequence) {
			final String jsonStr = StrUtil.trim((CharSequence) obj);
			json = isTypeJSONArray(jsonStr) ? parseArray(jsonStr, config) : parseObj(jsonStr, config);
		} else if (obj instanceof MapWrapper) {
			// MapWrapper实现了Iterable会被当作JSONArray，此处做修正
			json = parseObj(obj, config);
		} else if (obj instanceof Iterable || obj instanceof Iterator || ArrayUtil.isArray(obj)) {// 列表
			json = parseArray(obj, config);
		} else {// 对象
			json = parseObj(obj, config);
		}

		return json;
	}

	/**
	 * XML字符串转为JSONObject
	 *
	 * @param xmlStr XML字符串
	 * @return JSONObject
	 */
	public static JSONObject parseFromXml(String xmlStr) {
		return XML.toJSONObject(xmlStr);
	}

	// -------------------------------------------------------------------- Parse end

	// -------------------------------------------------------------------- Read start

	/**
	 * 读取JSON
	 *
	 * @param file    JSON文件
	 * @param charset 编码
	 * @return JSON（包括JSONObject和JSONArray）
	 * @throws IORuntimeException IO异常
	 */
	public static JSON readJSON(File file, Charset charset) throws IORuntimeException {
		return parse(FileReader.create(file, charset).readString());
	}

	/**
	 * 读取JSONObject
	 *
	 * @param file    JSON文件
	 * @param charset 编码
	 * @return JSONObject
	 * @throws IORuntimeException IO异常
	 */
	public static JSONObject readJSONObject(File file, Charset charset) throws IORuntimeException {
		return parseObj(FileReader.create(file, charset).readString());
	}

	/**
	 * 读取JSONArray
	 *
	 * @param file    JSON文件
	 * @param charset 编码
	 * @return JSONArray
	 * @throws IORuntimeException IO异常
	 */
	public static JSONArray readJSONArray(File file, Charset charset) throws IORuntimeException {
		return parseArray(FileReader.create(file, charset).readString());
	}
	// -------------------------------------------------------------------- Read end

	// -------------------------------------------------------------------- toString start

	/**
	 * 转为JSON字符串
	 *
	 * @param json         JSON
	 * @param indentFactor 每一级别的缩进
	 * @return JSON字符串
	 */
	public static String toJsonStr(JSON json, int indentFactor) {
		if (null == json) {
			return null;
		}
		return json.toJSONString(indentFactor);
	}

	/**
	 * 转为JSON字符串
	 *
	 * @param json JSON
	 * @return JSON字符串
	 */
	public static String toJsonStr(JSON json) {
		if (null == json) {
			return null;
		}
		return json.toJSONString(0);
	}

	/**
	 * 转为JSON字符串，并写出到write
	 *
	 * @param json JSON
	 * @param writer Writer
	 * @since 5.3.3
	 */
	public static void toJsonStr(JSON json, Writer writer) {
		if (null != json) {
			json.write(writer);
		}
	}

	/**
	 * 转为JSON字符串
	 *
	 * @param json JSON
	 * @return JSON字符串
	 */
	public static String toJsonPrettyStr(JSON json) {
		if (null == json) {
			return null;
		}
		return json.toJSONString(4);
	}

	/**
	 * 转换为JSON字符串
	 *
	 * @param obj 被转为JSON的对象
	 * @return JSON字符串
	 */
	public static String toJsonStr(Object obj) {
		return toJsonStr(obj, (JSONConfig) null);
	}

	/**
	 * 转换为JSON字符串
	 *
	 * @param obj 被转为JSON的对象
	 * @param jsonConfig JSON配置
	 * @return JSON字符串
	 * @since 5.7.12
	 */
	public static String toJsonStr(Object obj, JSONConfig jsonConfig) {
		if (null == obj) {
			return null;
		}
		if (obj instanceof CharSequence) {
			return StrUtil.str((CharSequence) obj);
		}
		return toJsonStr(parse(obj, jsonConfig));
	}

	/**
	 * 转换为JSON字符串并写出到writer
	 *
	 * @param obj 被转为JSON的对象
	 * @param writer Writer
	 * @since 5.3.3
	 */
	public static void toJsonStr(Object obj, Writer writer) {
		if (null != obj) {
			toJsonStr(parse(obj), writer);
		}
	}

	/**
	 * 转换为格式化后的JSON字符串
	 *
	 * @param obj Bean对象
	 * @return JSON字符串
	 */
	public static String toJsonPrettyStr(Object obj) {
		return toJsonPrettyStr(parse(obj));
	}

	/**
	 * 转换为XML字符串
	 *
	 * @param json JSON
	 * @return XML字符串
	 */
	public static String toXmlStr(JSON json) {
		return XML.toXml(json);
	}
	// -------------------------------------------------------------------- toString end

	// -------------------------------------------------------------------- toBean start

	/**
	 * JSON字符串转为实体类对象，转换异常将被抛出
	 *
	 * @param <T>        Bean类型
	 * @param jsonString JSON字符串
	 * @param beanClass  实体类对象
	 * @return 实体类对象
	 * @since 3.1.2
	 */
	public static <T> T toBean(String jsonString, Class<T> beanClass) {
		return toBean(parseObj(jsonString), beanClass);
	}

	/**
	 * JSON字符串转为实体类对象，转换异常将被抛出<br>
	 * 通过{@link JSONConfig}可选是否忽略大小写、忽略null等配置
	 *
	 * @param <T>        Bean类型
	 * @param jsonString JSON字符串
	 * @param config     JSON配置
	 * @param beanClass  实体类对象
	 * @return 实体类对象
	 * @since 5.8.0
	 */
	public static <T> T toBean(String jsonString, JSONConfig config, Class<T> beanClass) {
		return toBean(parseObj(jsonString, config), beanClass);
	}

	/**
	 * 转为实体类对象，转换异常将被抛出
	 *
	 * @param <T>       Bean类型
	 * @param json      JSONObject
	 * @param beanClass 实体类对象
	 * @return 实体类对象
	 */
	public static <T> T toBean(JSONObject json, Class<T> beanClass) {
		return null == json ? null : json.toBean(beanClass);
	}

	/**
	 * JSON字符串转为实体类对象，转换异常将被抛出
	 *
	 * @param <T>           Bean类型
	 * @param jsonString    JSON字符串
	 * @param typeReference {@link TypeReference}类型参考子类，可以获取其泛型参数中的Type类型
	 * @param ignoreError   是否忽略错误
	 * @return 实体类对象
	 * @since 4.3.2
	 */
	public static <T> T toBean(String jsonString, TypeReference<T> typeReference, boolean ignoreError) {
		return toBean(jsonString, typeReference.getType(), ignoreError);
	}

	/**
	 * JSON字符串转为实体类对象，转换异常将被抛出
	 *
	 * @param <T>         Bean类型
	 * @param jsonString  JSON字符串
	 * @param beanType    实体类对象类型
	 * @param ignoreError 是否忽略错误
	 * @return 实体类对象
	 * @since 4.3.2
	 */
	public static <T> T toBean(String jsonString, Type beanType, boolean ignoreError) {
		final JSON json = parse(jsonString, JSONConfig.create().setIgnoreError(ignoreError));
		if(null == json){
			return null;
		}
		return json.toBean(beanType);
	}

	/**
	 * 转为实体类对象
	 *
	 * @param <T>           Bean类型
	 * @param json          JSONObject
	 * @param typeReference {@link TypeReference}类型参考子类，可以获取其泛型参数中的Type类型
	 * @param ignoreError   是否忽略转换错误
	 * @return 实体类对象
	 * @since 4.6.2
	 */
	public static <T> T toBean(JSON json, TypeReference<T> typeReference, boolean ignoreError) {
		return toBean(json, typeReference.getType(), ignoreError);
	}

	/**
	 * 转为实体类对象
	 *
	 * @param <T>         Bean类型
	 * @param json        JSONObject
	 * @param beanType    实体类对象类型
	 * @param ignoreError 是否忽略转换错误
	 * @return 实体类对象
	 * @since 4.3.2
	 */
	@SuppressWarnings("deprecation")
	public static <T> T toBean(JSON json, Type beanType, boolean ignoreError) {
		if (null == json) {
			return null;
		}
		return json.toBean(beanType, ignoreError);
	}
	// -------------------------------------------------------------------- toBean end

	/**
	 * 将JSONArray字符串转换为Bean的List，默认为ArrayList
	 *
	 * @param <T>         Bean类型
	 * @param jsonArray   JSONArray字符串
	 * @param elementType List中元素类型
	 * @return List
	 * @since 5.5.2
	 */
	public static <T> List<T> toList(String jsonArray, Class<T> elementType) {
		return toList(parseArray(jsonArray), elementType);
	}

	/**
	 * 将JSONArray转换为Bean的List，默认为ArrayList
	 *
	 * @param <T>         Bean类型
	 * @param jsonArray   {@link JSONArray}
	 * @param elementType List中元素类型
	 * @return List
	 * @since 4.0.7
	 */
	public static <T> List<T> toList(JSONArray jsonArray, Class<T> elementType) {
		return null == jsonArray ? null : jsonArray.toList(elementType);
	}

	/**
	 * 通过表达式获取JSON中嵌套的对象<br>
	 * <ol>
	 * <li>.表达式，可以获取Bean对象中的属性（字段）值或者Map中key对应的值</li>
	 * <li>[]表达式，可以获取集合等对象中对应index的值</li>
	 * </ol>
	 * <p>
	 * 表达式栗子：
	 *
	 * <pre>
	 * persion
	 * persion.name
	 * persons[3]
	 * person.friends[5].name
	 * </pre>
	 *
	 * @param json       {@link JSON}
	 * @param expression 表达式
	 * @return 对象
	 * @see JSON#getByPath(String)
	 */
	public static Object getByPath(JSON json, String expression) {
		return getByPath(json, expression, null);
	}

	/**
	 * 通过表达式获取JSON中嵌套的对象<br>
	 * <ol>
	 * <li>.表达式，可以获取Bean对象中的属性（字段）值或者Map中key对应的值</li>
	 * <li>[]表达式，可以获取集合等对象中对应index的值</li>
	 * </ol>
	 * <p>
	 * 表达式栗子：
	 *
	 * <pre>
	 * persion
	 * persion.name
	 * persons[3]
	 * person.friends[5].name
	 * </pre>
	 *
	 * @param <T> 值类型
	 * @param json       {@link JSON}
	 * @param expression 表达式
	 * @param defaultValue 默认值
	 * @return 对象
	 * @see JSON#getByPath(String)
	 * @since 5.6.0
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getByPath(JSON json, String expression, T defaultValue) {
		if((null == json || StrUtil.isBlank(expression))){
			return defaultValue;
		}

		if(null != defaultValue){
			final Class<T> type = (Class<T>) defaultValue.getClass();
			return ObjectUtil.defaultIfNull(json.getByPath(expression, type), defaultValue);
		}
		return (T) json.getByPath(expression);
	}

	/**
	 * 设置表达式指定位置（或filed对应）的值<br>
	 * 若表达式指向一个JSONArray则设置其坐标对应位置的值，若指向JSONObject则put对应key的值<br>
	 * 注意：如果为JSONArray，则设置值得下标不能大于已有JSONArray的长度<br>
	 * <ol>
	 * <li>.表达式，可以获取Bean对象中的属性（字段）值或者Map中key对应的值</li>
	 * <li>[]表达式，可以获取集合等对象中对应index的值</li>
	 * </ol>
	 * <p>
	 * 表达式栗子：
	 *
	 * <pre>
	 * persion
	 * persion.name
	 * persons[3]
	 * person.friends[5].name
	 * </pre>
	 *
	 * @param json       JSON，可以为JSONObject或JSONArray
	 * @param expression 表达式
	 * @param value      值
	 */
	public static void putByPath(JSON json, String expression, Object value) {
		json.putByPath(expression, value);
	}

	/**
	 * 对所有双引号做转义处理（使用双反斜杠做转义）<br>
	 * 为了能在HTML中较好的显示，会将&lt;/转义为&lt;\/<br>
	 * JSON字符串中不能包含控制字符和未经转义的引号和反斜杠
	 *
	 * @param string 字符串
	 * @return 适合在JSON中显示的字符串
	 */
	public static String quote(String string) {
		return quote(string, true);
	}

	/**
	 * 对所有双引号做转义处理（使用双反斜杠做转义）<br>
	 * 为了能在HTML中较好的显示，会将&lt;/转义为&lt;\/<br>
	 * JSON字符串中不能包含控制字符和未经转义的引号和反斜杠
	 *
	 * @param string 字符串
	 * @param isWrap 是否使用双引号包装字符串
	 * @return 适合在JSON中显示的字符串
	 * @since 3.3.1
	 */
	public static String quote(String string, boolean isWrap) {
		StringWriter sw = new StringWriter();
		try {
			return quote(string, sw, isWrap).toString();
		} catch (IOException ignored) {
			// will never happen - we are writing to a string writer
			return StrUtil.EMPTY;
		}
	}

	/**
	 * 对所有双引号做转义处理（使用双反斜杠做转义）<br>
	 * 为了能在HTML中较好的显示，会将&lt;/转义为&lt;\/<br>
	 * JSON字符串中不能包含控制字符和未经转义的引号和反斜杠
	 *
	 * @param str    字符串
	 * @param writer Writer
	 * @return Writer
	 * @throws IOException IO异常
	 */
	public static Writer quote(String str, Writer writer) throws IOException {
		return quote(str, writer, true);
	}

	/**
	 * 对所有双引号做转义处理（使用双反斜杠做转义）<br>
	 * 为了能在HTML中较好的显示，会将&lt;/转义为&lt;\/<br>
	 * JSON字符串中不能包含控制字符和未经转义的引号和反斜杠
	 *
	 * @param str    字符串
	 * @param writer Writer
	 * @param isWrap 是否使用双引号包装字符串
	 * @return Writer
	 * @throws IOException IO异常
	 * @since 3.3.1
	 */
	public static Writer quote(String str, Writer writer, boolean isWrap) throws IOException {
		if (StrUtil.isEmpty(str)) {
			if (isWrap) {
				writer.write("\"\"");
			}
			return writer;
		}

		char c; // 当前字符
		int len = str.length();
		if (isWrap) {
			writer.write('"');
		}
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			switch (c) {
				case '\\':
				case '"':
					writer.write("\\");
					writer.write(c);
					break;
				default:
					writer.write(escape(c));
			}
		}
		if (isWrap) {
			writer.write('"');
		}
		return writer;
	}

	/**
	 * 转义显示不可见字符
	 *
	 * @param str 字符串
	 * @return 转义后的字符串
	 */
	public static String escape(String str) {
		if (StrUtil.isEmpty(str)) {
			return str;
		}

		final int len = str.length();
		final StringBuilder builder = new StringBuilder(len);
		char c;
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			builder.append(escape(c));
		}
		return builder.toString();
	}

	/**
	 * 在需要的时候包装对象<br>
	 * 包装包括：
	 * <ul>
	 * <li>{@code null} =》 {@code JSONNull.NULL}</li>
	 * <li>array or collection =》 JSONArray</li>
	 * <li>map =》 JSONObject</li>
	 * <li>standard property (Double, String, et al) =》 原对象</li>
	 * <li>来自于java包 =》 字符串</li>
	 * <li>其它 =》 尝试包装为JSONObject，否则返回{@code null}</li>
	 * </ul>
	 *
	 * @param object     被包装的对象
	 * @param jsonConfig JSON选项
	 * @return 包装后的值，null表示此值需被忽略
	 */
	public static Object wrap(Object object, JSONConfig jsonConfig) {
		if (object == null) {
			return jsonConfig.isIgnoreNullValue() ? null : JSONNull.NULL;
		}
		if (object instanceof JSON //
				|| ObjectUtil.isNull(object) //
				|| object instanceof JSONString //
				|| object instanceof CharSequence //
				|| object instanceof Number //
				|| ObjectUtil.isBasicType(object) //
		) {
			if(object instanceof Number && null != jsonConfig.getDateFormat()){
				// 当JSONConfig中设置了日期格式，则包装为NumberWithFormat，以便在Converter中使用自定义格式转换日期时间
				return new NumberWithFormat((Number) object, jsonConfig.getDateFormat());
			}
			return object;
		}

		try {
			// fix issue#1399@Github
			if(object instanceof SQLException){
				return object.toString();
			}

			// JSONArray
			if (object instanceof Iterable || ArrayUtil.isArray(object)) {
				return new JSONArray(object, jsonConfig);
			}
			// JSONObject
			if (object instanceof Map || object instanceof Map.Entry) {
				return new JSONObject(object, jsonConfig);
			}

			// 日期类型原样保存，便于格式化
			if (object instanceof Date
					|| object instanceof Calendar
					|| object instanceof TemporalAccessor
			) {
				return object;
			}
			// 枚举类保存其字符串形式（4.0.2新增）
			if (object instanceof Enum) {
				return object.toString();
			}

			// pr#3507
			// Class类型保存类名
			if (object instanceof Class<?>) {
				return ((Class<?>) object).getName();
			}

			// Java内部类不做转换
			if (ClassUtil.isJdkClass(object.getClass())) {
				return object.toString();
			}

			// 默认按照JSONObject对待
			return new JSONObject(object, jsonConfig);
		} catch (final Exception exception) {
			return null;
		}
	}

	/**
	 * 格式化JSON字符串，此方法并不严格检查JSON的格式正确与否
	 *
	 * @param jsonStr JSON字符串
	 * @return 格式化后的字符串
	 * @since 3.1.2
	 */
	public static String formatJsonStr(String jsonStr) {
		return JSONStrFormatter.format(jsonStr);
	}

	/**
	 * 是否为JSON字符串，首尾都为大括号或中括号判定为JSON字符串
	 *
	 * @param str 字符串
	 * @return 是否为JSON字符串
	 * @since 3.3.0
	 * @deprecated 方法名称有歧义，请使用 {@link #isTypeJSON(String)}
	 */
	@Deprecated
	public static boolean isJson(String str) {
		return isTypeJSON(str);
	}

	/**
	 * 是否为JSON类型字符串，首尾都为大括号或中括号判定为JSON字符串
	 *
	 * @param str 字符串
	 * @return 是否为JSON类型字符串
	 * @since 5.7.22
	 */
	public static boolean isTypeJSON(String str) {
		return isTypeJSONObject(str) || isTypeJSONArray(str);
	}

	/**
	 * 是否为JSONObject字符串，首尾都为大括号判定为JSONObject字符串
	 *
	 * @param str 字符串
	 * @return 是否为JSON字符串
	 * @since 3.3.0
	 * @deprecated 方法名称有歧义，请使用 {@link #isTypeJSONObject(String)}
	 */
	@Deprecated
	public static boolean isJsonObj(String str) {
		return isTypeJSONObject(str);
	}

	/**
	 * 是否为JSONObject类型字符串，首尾都为大括号判定为JSONObject字符串
	 *
	 * @param str 字符串
	 * @return 是否为JSON字符串
	 * @since 5.7.22
	 */
	public static boolean isTypeJSONObject(String str) {
		if (StrUtil.isBlank(str)) {
			return false;
		}
		return StrUtil.isWrap(StrUtil.trim(str), '{', '}');
	}

	/**
	 * 是否为JSONArray字符串，首尾都为中括号判定为JSONArray字符串
	 *
	 * @param str 字符串
	 * @return 是否为JSON字符串
	 * @since 3.3.0
	 * @deprecated 方法名称有歧义，请使用 {@link #isTypeJSONArray(String)}
	 */
	@Deprecated
	public static boolean isJsonArray(String str) {
		return isTypeJSONArray(str);
	}

	/**
	 * 是否为JSONArray类型的字符串，首尾都为中括号判定为JSONArray字符串
	 *
	 * @param str 字符串
	 * @return 是否为JSONArray类型字符串
	 * @since 5.7.22
	 */
	public static boolean isTypeJSONArray(String str) {
		if (StrUtil.isBlank(str)) {
			return false;
		}
		return StrUtil.isWrap(StrUtil.trim(str), '[', ']');
	}

	/**
	 * 是否为null对象，null的情况包括：
	 *
	 * <pre>
	 * 1. {@code null}
	 * 2. {@link JSONNull}
	 * </pre>
	 *
	 * @param obj 对象
	 * @return 是否为null
	 * @since 4.5.7
	 */
	public static boolean isNull(Object obj) {
		return null == obj || obj instanceof JSONNull;
	}

	/**
	 * XML转JSONObject<br>
	 * 转换过程中一些信息可能会丢失，JSON中无法区分节点和属性，相同的节点将被处理为JSONArray。
	 *
	 * @param xml XML字符串
	 * @return JSONObject
	 * @since 4.0.8
	 */
	public static JSONObject xmlToJson(String xml) {
		return XML.toJSONObject(xml);
	}

	/**
	 * 加入自定义的序列化器
	 *
	 * @param type       对象类型
	 * @param serializer 序列化器实现
	 * @see GlobalSerializeMapping#put(Type, JSONArraySerializer)
	 * @since 4.6.5
	 */
	public static void putSerializer(Type type, JSONArraySerializer<?> serializer) {
		GlobalSerializeMapping.put(type, serializer);
	}

	/**
	 * 加入自定义的序列化器
	 *
	 * @param type       对象类型
	 * @param serializer 序列化器实现
	 * @see GlobalSerializeMapping#put(Type, JSONObjectSerializer)
	 * @since 4.6.5
	 */
	public static void putSerializer(Type type, JSONObjectSerializer<?> serializer) {
		GlobalSerializeMapping.put(type, serializer);
	}

	/**
	 * 加入自定义的反序列化器
	 *
	 * @param type         对象类型
	 * @param deserializer 反序列化器实现
	 * @see GlobalSerializeMapping#put(Type, JSONDeserializer)
	 * @since 4.6.5
	 */
	public static void putDeserializer(Type type, JSONDeserializer<?> deserializer) {
		GlobalSerializeMapping.put(type, deserializer);
	}

	// --------------------------------------------------------------------------------------------- Private method start

	/**
	 * 转义不可见字符<br>
	 * 见：<a href="https://en.wikibooks.org/wiki/Unicode/Character_reference/0000-0FFF">https://en.wikibooks.org/wiki/Unicode/Character_reference/0000-0FFF</a>
	 *
	 * @param c 字符
	 * @return 转义后的字符串
	 */
	private static String escape(char c) {
		switch (c) {
			case '\b':
				return "\\b";
			case '\t':
				return "\\t";
			case '\n':
				return "\\n";
			case '\f':
				return "\\f";
			case '\r':
				return "\\r";
			default:
				if (c < StrUtil.C_SPACE || //
						(c >= '\u0080' && c <= '\u00a0') || //
						(c >= '\u2000' && c <= '\u2010') || //
						(c >= '\u2028' && c <= '\u202F') || //
						(c >= '\u2066' && c <= '\u206F')//
				) {
					return HexUtil.toUnicodeHex(c);
				} else {
					return Character.toString(c);
				}
		}
	}
	// --------------------------------------------------------------------------------------------- Private method end
}
