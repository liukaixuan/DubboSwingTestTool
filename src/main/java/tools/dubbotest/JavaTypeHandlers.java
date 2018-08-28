/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package tools.dubbotest;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liukaixuan(liukaixuan@gmail.com)
 */
public class JavaTypeHandlers {
	private static Logger log = LoggerFactory.getLogger(DubboFactory.class);

	public static final Map COMMON_DATA_TYPE_HANDLERS = new HashMap();

	static {
		try {
			COMMON_DATA_TYPE_HANDLERS.put("int", IntegerHandler.class.newInstance());
			COMMON_DATA_TYPE_HANDLERS.put(Integer.class.getName(), IntegerHandler.class.newInstance());

			COMMON_DATA_TYPE_HANDLERS.put("boolean", BooleanHandler.class.newInstance());
			COMMON_DATA_TYPE_HANDLERS.put(Boolean.class.getName(), BooleanHandler.class.newInstance());

			COMMON_DATA_TYPE_HANDLERS.put("long", LongHandler.class.newInstance());
			COMMON_DATA_TYPE_HANDLERS.put(Long.class.getName(), LongHandler.class.newInstance());

			COMMON_DATA_TYPE_HANDLERS.put("float", FloatHandler.class.newInstance());
			COMMON_DATA_TYPE_HANDLERS.put(Float.class.getName(), FloatHandler.class.newInstance());

			COMMON_DATA_TYPE_HANDLERS.put("double", DoubleHandler.class.newInstance());
			COMMON_DATA_TYPE_HANDLERS.put(Double.class.getName(), DoubleHandler.class.newInstance());

			COMMON_DATA_TYPE_HANDLERS.put("java.util.Date", DateHandler.class.newInstance());

			COMMON_DATA_TYPE_HANDLERS.put(String.class.getName(), StringHandler.class.newInstance());

		} catch (InstantiationException e) {
			log.error("init IDataTypeHandler failed.", e);
		} catch (IllegalAccessException e) {
			log.error("init IDataTypeHandler failed.", e);
		}
	}

	/**
	 * 将字符串转换为指定的数据类型。
	 *
	 * @param value 要转换的字符串
	 * @param cls   要转换成的类型，如int, float等。
	 */
	public static Object convertValueToType(String value, Class cls) throws ParseException, ClassNotFoundException {
		IDataTypeHandler mh = (IDataTypeHandler) COMMON_DATA_TYPE_HANDLERS.get(cls.getName());

		if (mh != null) {
			return mh.getValue(value);
		}

		//转成json
		Gson gson = new Gson();
		return gson.fromJson(value, cls);
	}

	public static Object getDefaultValue(Class cls) {
		IDataTypeHandler mh = (IDataTypeHandler) COMMON_DATA_TYPE_HANDLERS.get(cls.getName());

		if (mh != null) {
			return mh.getDefaultValue();
		}

		if (cls.isPrimitive()) {
			return null;
		}

		Object instance = null;
		try {
			instance = cls.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();

			return null;
		}

		//转成json
		Gson gson = new Gson();
		return gson.toJson(instance);
	}

}

class IntegerHandler implements IDataTypeHandler {

	public Object getValue(String fieldValue) {
		return Integer.valueOf(fieldValue);
	}

	public Object getDefaultValue() {
		return 0;
	}

}

class StringHandler implements IDataTypeHandler {

	public Object getValue(String fieldValue) {
		return fieldValue;
	}

	public Object getDefaultValue() {
		return null;
	}
}

class LongHandler implements IDataTypeHandler {

	public Object getValue(String fieldValue) {
		return Long.valueOf(fieldValue);
	}

	public Object getDefaultValue() {
		return 0;
	}
}

class FloatHandler implements IDataTypeHandler {

	public Object getValue(String fieldValue) {
		return Float.valueOf(fieldValue);
	}

	public Object getDefaultValue() {
		return 0;
	}
}

class DoubleHandler implements IDataTypeHandler {

	public Object getValue(String fieldValue) {
		return Double.valueOf(fieldValue);
	}

	public Object getDefaultValue() {
		return 0;
	}
}

class DateHandler implements IDataTypeHandler {

	public Object getValue(String fieldValue) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return sdf.parse(fieldValue);
	}

	public Object getDefaultValue() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return sdf.format(new Date());
	}
}

class BooleanHandler implements IDataTypeHandler {

	public Object getValue(String fieldValue) {
		char c = fieldValue.charAt(0);
		if (c == '1' || c == 'y' || c == 'Y' || c == 't' || c == 'T')
			return Boolean.TRUE;

		return Boolean.FALSE;
	}

	public Object getDefaultValue() {
		return false;
	}

}

