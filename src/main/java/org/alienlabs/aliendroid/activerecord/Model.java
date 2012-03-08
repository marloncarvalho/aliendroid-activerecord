/*
 * AlienDroid Framework
 * Copyright (C) 2012 Marlon Silva Carvalho
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.alienlabs.aliendroid.activerecord;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.alienlabs.aliendroid.util.Beans;
import org.alienlabs.aliendroid.util.Reflection;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Seguindo padrão de projeto ActiveRecord, toda classe que é um 'Model' deve herdar desta classe.
 * Já terá por padrão os métodos triviais para persistir o objeto, como insert() e update().
 * 
 * @author Marlon Silva Carvalho
 * @since 1.0.0
 */
abstract public class Model {
	public Integer id;

	public Model(final Integer id) {
		this.id = id;
		load();
	}

	public Model() {
	}

	/**
	 * Save the model to the database.
	 */
	public void save() {
		ColumnMapper mapper = Beans.getBean(ColumnMapper.class);
		ContentValues values = new ContentValues();

		Field[] fields = Reflection.getNonStaticDeclaredFields(getClass());
		for (Field field : fields) {
			values.put(field.getName(), mapper.getValueFromObject(field, this));
		}

		String tableName = Reflection.getSimpleClassName(this);
		DBOpenHelper helper = Beans.getBean(DBOpenHelper.class);
		if (id != null) {
			helper.getWritableDatabase().update(tableName, values, "_id=?", new String[] { id.toString() });
		} else {
			helper.getWritableDatabase().insertOrThrow(tableName, null, values);
		}
	}

	/**
	 * Delete the model from database.
	 */
	public void delete() {
		String tableName = Reflection.getSimpleClassName(this);
		DBOpenHelper helper = Beans.getBean(DBOpenHelper.class);
		helper.getWritableDatabase().delete(tableName, "_id=?", new String[] { id.toString() });
	}

	/**
	 * Loads the object state from database.
	 */
	public void load() {
		String tableName = Reflection.getSimpleClassName(this);
		DBOpenHelper helper = Beans.getBean(DBOpenHelper.class);
		Cursor cursor = helper.getReadableDatabase().query(tableName,
				Reflection.getNonStaticDeclaredFieldsNames(this.getClass()), "_id=?", new String[] { id.toString() }, null,
				null, null);

		if (cursor.moveToFirst()) {
			transform(cursor);
		}
		cursor.close();
	}

	protected void transform(final Cursor cursor) {
		ColumnMapper mapper = Beans.getBean(ColumnMapper.class);
		Field[] fields = Reflection.getNonStaticDeclaredFields(this.getClass());
		for (Field field : fields) {
			mapper.setValueToObject(cursor, field, this);
		}
	}

	public static <T extends Model> List<T> findAll(final Class<T> cls) {
		return where(cls, "1=1");
	}

	public static <T extends Model> List<T> where(final Class<T> cls, final String query, final String... params) {
		String tableName = Reflection.getSimpleClassName(cls);
		List<T> result = new ArrayList<T>();

		DBOpenHelper helper = Beans.getBean(DBOpenHelper.class);
		Cursor cursor = helper.getReadableDatabase().rawQuery("select * from " + tableName + " where " + query, params);
		while (cursor.moveToNext()) {
			T model = Reflection.instantiate(cls);
			model.transform(cursor);
			result.add(model);
		}

		cursor.close();
		return result;
	}

	public static String getSQLCreateTable(final Class<?> cls) {
		String tableName = Reflection.getSimpleClassName(cls);
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE  ");
		sql.append(tableName);
		sql.append(" (");
		sql.append(" _id INTEGER PRIMARY KEY, ");
		Field[] fields = Reflection.getNonStaticDeclaredFields(cls);
		for (Field field : fields) {
			sql.append(field.getName());
			sql.append(" ");
			sql.append(getType(field));
			sql.append(", ");
		}
		sql.delete(sql.length() - 2, sql.length());
		sql.append(");");
		return sql.toString();
	}

	private static String getType(final Field field) {
		String type = null;
		Class<?> cls = field.getType();
		if (cls.getSimpleName().toLowerCase().equals("date")) {
			type = "DATE";
		}
		if (cls.getSimpleName().toLowerCase().equals("string")) {
			type = "TEXT";
		}
		if (cls.getSimpleName().toLowerCase().equals("integer") || cls.getSimpleName().toLowerCase().equals("boolean")) {
			type = "INTEGER";
		}
		if (cls.getSimpleName().toLowerCase().equals("double") || cls.getSimpleName().toLowerCase().equals("float")) {
			type = "REAL";
		}

		return type;
	}

}
