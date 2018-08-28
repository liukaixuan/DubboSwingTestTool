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
package tools.dubbotest.save;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liukaixuan(liukaixuan@gmail.com)
 */
public class MethodInfo {

	private String methodName;

	private Map<Integer, String> params = new HashMap<>();

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Map<Integer, String> getParams() {
		return params;
	}

	public String[] getOrderedParams() {
		String[] ss = new String[params.size()];

		for (int i = 0; i < ss.length; i++) {
			ss[i] = params.get(i);
		}

		return ss;
	}

	public void setParams(Map<Integer, String> params) {
		this.params = params;
	}
}
