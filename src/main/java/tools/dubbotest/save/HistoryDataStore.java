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

import com.alibaba.dubbo.common.utils.IOUtils;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import tools.dubbotest.JavaTypeHandlers;

import java.io.*;
import java.lang.reflect.Method;

/**
 * 存储历史数据
 *
 * @author liukaixuan(liukaixuan@gmail.com)
 */
public class HistoryDataStore {

	private static final Logger log = Logger.getLogger(HistoryDataStore.class);

	private final File file;

	private HistoryData historyData;

	public static HistoryDataStore getInstance() {
		String homeDir = System.getProperty("user.home");
		File file = null;

		if (StringUtils.isEmpty(homeDir)) {
			file = new File("dubbo_ui_test.data");
		} else {
			file = new File(homeDir, ".dubbo_ui_test_data");
		}

		return new HistoryDataStore(file);
	}

	private HistoryDataStore(File file) {
		this.file = file;

		initAndLoad();
	}

	private void initAndLoad() {
		if (file.exists()) {
			FileReader reader = null;

			try {
				reader = new FileReader(file);

				this.historyData = new Gson().fromJson(reader, HistoryData.class);

			} catch (Exception e) {
				//删掉文件
				file.delete();

				log.error("failed to load history data from :" + file.getAbsolutePath(), e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
					}
				}
			}
		}

		if (this.historyData == null) {
			this.historyData = new HistoryData();

			this.switchToJar(
					"http://IP:port/nexus/service/local/repositories/releases/content/com/.../xxxx-api-1.1.jar");

			this.setJarDataInfo("zookeeper://zookeeper address:2181", "com.xx.xxx.api.XXXToTestService", "", "1.0.0");
		}
	}

	public JarData switchToJar(String jarLocation) {
		JarData data = this.historyData.getJarInfos().get(jarLocation);

		if (data == null) {
			//init
			data = new JarData();
			data.setJarLocation(jarLocation);

			this.historyData.getJarInfos().put(jarLocation, data);
		}

		historyData.setCurrentJarData(data);

		return data;
	}

	public MethodInfo getMethodInfo(String serviceName, Method method) {
		String key = serviceName + "|" + method.toGenericString();

		MethodInfo info = historyData.getMethodInfos().get(key);

		if (info == null || method.getParameterTypes().length != info.getParams().size()) {
			//init
			info = new MethodInfo();

			Class[] cls = method.getParameterTypes();
			for (int i = 0; i < cls.length; i++) {
				Class type = cls[i];

				String defaultValue = String.valueOf(JavaTypeHandlers.getDefaultValue(type));

				info.getParams().put(i, defaultValue);
			}

			historyData.getMethodInfos().put(key, info);
		}

		return info;
	}

	public HistoryData getHistoryData() {
		return this.historyData;
	}

	public JarData getCurrentJarData() {
		return this.historyData.getCurrentJarData();
	}

	public String getCurrentZookeeper() {
		return this.historyData.getZookeeper();
	}

	public void setJarDataInfo(String zookeeper, String service, String group, String serviceVersion) {
		this.historyData.setZookeeper(zookeeper);

		JarData data = this.getCurrentJarData();

		data.setGroup(group);
		data.setService(service);
		data.setServiceVersion(serviceVersion);
	}

	public void setMethodInfo(String serviceName, Method method, String[] params) {
		MethodInfo info = this.getMethodInfo(serviceName, method);

		for (int i = 0; i < params.length; i++) {
			info.getParams().put(i, params[i]);
		}
	}

	public void storeToFile() {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				log.error("failed to create history data file:" + file.getAbsolutePath(), e);
				return;
			}
		}

		Writer w = null;
		try {
			w = new FileWriter(this.file);

			String content = new Gson().toJson(this.historyData);

			IOUtils.write(w, content);
		} catch (Exception e) {
			log.error("failed to save history data file:" + file.getAbsolutePath(), e);
		} finally {
			if (w != null) {
				try {
					w.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
