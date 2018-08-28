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
public class HistoryData {

	private String zookeeper;

	private JarData currentJarData = new JarData();

	private Map<String, JarData> jarInfos = new HashMap<>();

	private Map<String, MethodInfo> methodInfos = new HashMap<>();

	public JarData getCurrentJarData() {
		return currentJarData;
	}

	public void setCurrentJarData(JarData currentJarData) {
		this.currentJarData = currentJarData;
	}

	public Map<String, JarData> getJarInfos() {
		return jarInfos;
	}

	public void setJarInfos(Map<String, JarData> jarInfos) {
		this.jarInfos = jarInfos;
	}

	public Map<String, MethodInfo> getMethodInfos() {
		return methodInfos;
	}

	public void setMethodInfos(Map<String, MethodInfo> methodInfos) {
		this.methodInfos = methodInfos;
	}

	public String getZookeeper() {
		return zookeeper;
	}

	public void setZookeeper(String zookeeper) {
		this.zookeeper = zookeeper;
	}

}
