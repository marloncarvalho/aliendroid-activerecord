package org.alienlabs.aliendroid.util;

import com.google.inject.Injector;

public class Beans {
	private static Injector injector;

	public static void setInjector(Injector p_injector) {
		injector = p_injector;
	}

	public static <T> T getBean(Class<T> beanClass) {
		return injector.getInstance(beanClass);
	}

	public static Injector getInjector() {
		return injector;
	}

}
