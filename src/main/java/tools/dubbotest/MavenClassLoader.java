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

import org.apache.log4j.Logger;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author liukaixuan(liukaixuan@gmail.com)
 */
public class MavenClassLoader {

	private static final Logger log = Logger.getLogger(MavenClassLoader.class);

	public ClassLoader getClassLoader(String pom) throws Throwable {
		URL url = buildURL(pom);

		ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
		ClassLoader parentParent = contextLoader.getParent();

		URLClassLoader maven = new URLClassLoader(new URL[] { url }, parentParent);

		Unsafe UNSAFE = getUnsafe();
		long PARENT_LOADER_OFFSET = UNSAFE.objectFieldOffset(ClassLoader.class.getDeclaredField("parent"));
		UNSAFE.putObject(contextLoader, PARENT_LOADER_OFFSET, maven);

		return maven;
	}

	protected URL buildURL(String jarLocation) {
		try {
			return new URL(jarLocation);
		} catch (MalformedURLException e) {
			log.error("failed to build jarLocation url:" + jarLocation, e);
			return null;
		}
	}

	private Unsafe getUnsafe() throws Throwable {
		Class<?> unsafeClass = Unsafe.class;

		for (Field f : unsafeClass.getDeclaredFields()) {
			if ("theUnsafe".equals(f.getName())) {
				f.setAccessible(true);
				return (Unsafe) f.get(null);
			}
		}

		throw new IllegalAccessException("no declared field: theUnsafe");
	}

}
