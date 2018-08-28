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
package tools.dubbotest;

import com.alibaba.dubbo.config.ReferenceConfig;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liukaixuan(liukaixuan@gmail.com)
 */
public class ApiInstance {

	static ConcurrentHashMap<String, ApiInstance> instances = new ConcurrentHashMap<>();

	final String address;
	final String group;
	final String version;
	final String className;
	final String jarLocation;

	Object apiClientInstance;
	ConcurrentHashMap<String, Method> apiMethods = new ConcurrentHashMap<>();
	List<String> methodSignatures = new LinkedList<>();

	private ApiInstance(String address, String group, String version, String className, String jarLocation)
			throws Throwable {
		this.address = address;
		this.group = group;
		this.version = version;
		this.className = className;
		this.jarLocation = jarLocation;

		init();
	}

	public static ApiInstance getInstance(String address, String group, String version, String className,
			String jarLocation) throws Throwable {
		String referenceKey = address + "-" + group + "-" + version + "-" + className;

		ApiInstance instance = instances.get(referenceKey);

		if (instance == null) {
			instance = new ApiInstance(address, group, version, className, jarLocation);
			instances.put(referenceKey, instance);
		}

		return instance;
	}

	private void init() throws Throwable {
		ReferenceConfig reference = DubboFactory.getReferenceConfig(address, group, version, className, jarLocation);

		if (reference == null) {
			throw new ClassNotFoundException("class not found: " + className);
		}

		apiClientInstance = reference.get();

		Class cls = Class.forName(className);

		Method[] methods = cls.getMethods();
		Method[] objectMethods = Object.class.getMethods();

		for (Method m : methods) {

			boolean matched = false;

			for (Method mm : objectMethods) {
				if (m.equals(mm)) {
					matched = true;
					break;
				}
			}

			if (!matched) {
				apiMethods.put(m.toGenericString(), m);
				methodSignatures.add(m.toGenericString());
			}
		}
	}

	public Map<String, Method> getApiMethods() {
		return apiMethods;
	}

	public List<String> getApiMethodSignatures() {
		return methodSignatures;
	}

	public Object invokeMethod(String methodSignature, String... params) throws Exception {
		Method next = apiMethods.get(methodSignature);

		Class[] cls = next.getParameterTypes();

		Object[] os = new Object[cls.length];
		for (int i = 0; i < cls.length; i++) {
			os[i] = JavaTypeHandlers.convertValueToType(params[i], cls[i]);
		}

		Object ret = next.invoke(apiClientInstance, os);

		return ret;
	}

}
