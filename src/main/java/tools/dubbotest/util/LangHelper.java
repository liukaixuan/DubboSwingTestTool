/*
 * Copyright 2018 the original author or authors.
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
package tools.dubbotest.util;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Localization
 *
 * @author liukaixuan(liukaixuan@gmail.com)
 */
public class LangHelper {

	static ResourceBundle bundle;

	static Charset UTF8 = Charset.forName("UTF-8");
	static Charset ISO = Charset.forName("ISO-8859-1");

	public static String getText(String key) {
		if (bundle == null) {
			Locale locale = Locale.getDefault();//获取地区:默认

			bundle = ResourceBundle.getBundle("texts", locale);
		}

		String keyValue = new String(bundle.getString(key).getBytes(ISO), UTF8);

		return keyValue;
	}

}
