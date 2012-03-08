package org.alienlabs.aliendroid.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Reflection {

	public static String getSimpleClassName(Object object) {
		return object.getClass().getSimpleName();
	}

	public static String getSimpleClassName(Class<?> object) {
		return object.getSimpleName();
	}

	public static Object getFieldValue(String sField, Object object) {
		Object result = null;
		try {
			Field field = object.getClass().getDeclaredField(sField);
			boolean acessible = field.isAccessible();
			field.setAccessible(true);
			result = field.get(object);
			field.setAccessible(acessible);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	public static String[] getNonStaticDeclaredFieldsNames(Class<?> type) {
		Field[] fields = Reflection.getNonStaticDeclaredFields(type);
		String[] fieldsName = new String[fields.length];
		for (int idx = 0; idx < fields.length; idx++) {
			fieldsName[idx] = fields[idx].getName();
		}
		return fieldsName;
	}

	public static Field[] getNonStaticDeclaredFields(Class<?> type) {
		List<Field> fields = new ArrayList<Field>();

		if (type != null) {
			for (Field field : type.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers()) && !field.getType().equals(type.getDeclaringClass())) {
					fields.add(field);
				}
			}
		}

		return fields.toArray(new Field[0]);
	}

	public static <T> T instantiate(Class<T> cls) {
		T o = null;
		try {
			o = cls.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return o;
	}

	public static void setFieldValue(final String f, final Object object, final Object value) {
		try {
			Field field = object.getClass().getDeclaredField(f);
			boolean acessible = field.isAccessible();
			field.setAccessible(true);
			field.set(object, value);
			field.setAccessible(acessible);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object callMethod(Object object, String methodName, Object... params) {
		try {
			for (Method method : object.getClass().getMethods()) {
				if (method.getName().equals(methodName)) {
					return method.invoke(object, params);
				}
			}
			return null;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}