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

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本类的部分代码来源于网络，记不清出处了。
 *
 * @author liukaixuan(liukaixuan@gmail.com)
 */
public class DubboFactory {

	private static Logger logger = LoggerFactory.getLogger(DubboFactory.class);
	/**
	 * 当前应用的信息
	 */
	private static ApplicationConfig application = new ApplicationConfig();
	/**
	 * 注册中心信息缓存
	 */
	private static Map<String, RegistryConfig> registryConfigCache = new ConcurrentHashMap<>();
	/**
	 * 各个业务方的ReferenceConfig缓存
	 */
	private static Map<String, ReferenceConfig> referenceCache = new ConcurrentHashMap<>();

	private static Map<String, String> jarLoaded = new ConcurrentHashMap<>();

	static ClassLoader mavenLoader = null;

	static {
		application.setName("dubbo-factory");
	}

	/**
	 * 获取注册中心信息
	 *
	 * @param address zk注册地址
	 * @param group   dubbo服务所在的组
	 * @return
	 */
	private static RegistryConfig getRegistryConfig(String address, String group, String version) {
		String key = address + "-" + group + "-" + version;
		RegistryConfig registryConfig = registryConfigCache.get(key);
		if (null == registryConfig) {
			registryConfig = new RegistryConfig();
			registryConfig.setAddress(address);
			registryConfigCache.put(key, registryConfig);
		}
		return registryConfig;
	}

	private static void loadDubboClass(String jarLocation) throws Throwable {
		MavenClassLoader maven = new MavenClassLoader();
		mavenLoader = maven.getClassLoader(jarLocation);
	}

	/**
	 * 获取服务的代理对象
	 *
	 * @param address
	 * @param group
	 * @return
	 */
	public static ReferenceConfig getReferenceConfig(String address, String group, String version, String className,
			String jarLocation) throws Throwable {

		if (jarLoaded.get(jarLocation) == null) {
			loadDubboClass(jarLocation);

			jarLoaded.put(jarLocation, jarLocation);
		}

		String referenceKey = address + "-" + group + "-" + version + "-" + className;
		ReferenceConfig referenceConfig = referenceCache.get(referenceKey);
		if (null == referenceConfig) {
			referenceConfig = new ReferenceConfig<>();
			referenceConfig.setApplication(application);
			referenceConfig.setRegistry(getRegistryConfig(address, group, version));
			referenceConfig.setInterface(className);
			referenceConfig.setVersion(version);
			referenceConfig.setGroup(group);
			referenceCache.put(referenceKey, referenceConfig);
		}
		return referenceConfig;
	}

	public static Map<String, Method> getMethods(String className) throws ClassNotFoundException {
		ConcurrentHashMap<String, Method> ret = new ConcurrentHashMap<>();

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
				ret.put(m.toGenericString(), m);
			}
		}

		return ret;
	}

}
