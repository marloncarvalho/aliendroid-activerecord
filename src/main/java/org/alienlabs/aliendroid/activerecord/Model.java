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
 * Seguindo padrão de projeto ActiveRecord, toda classe que é um 'Model' deve
 * herdar desta classe. Já terá por padrão os métodos triviais para persistir o
 * objeto, como insert() e update().
 * 
 * @author Marlon Silva Carvalho
 * @since 1.0.0
 */
abstract public class Model {
	
	public Integer _id;

	public Model(final Integer id) {
		this._id = id;
		load(id);
	}

	public Model() {
	}

	public void load(final Integer id) {
		final String tableName = Reflection.getSimpleClassName(this);
		final DBOpenHelper helper = Beans.getBean(DBOpenHelper.class);
		final Cursor cursor = helper.getReadableDatabase().query(tableName,
				Reflection.getNonStaticDeclaredFieldsNames(this.getClass()),
				"_id=?", new String[] { id.toString() }, null, null, null);
		if (cursor.moveToFirst()) {
			transform(cursor);
		}
		cursor.close();
	}

	public static <T extends Model> Model findFirst(final Class<T> cls,
			final String query, final String... params) {
		final List<T> list = Model.where(cls, query, params);
		Model model = null;
		if (list.size() > 0) {
			model = list.iterator().next();
		}
		return model;
	}

	public static <T extends Model> Model findFirst(final Class<T> cls) {
		final List<T> list = Model.where(cls, "1=1");
		Model model = null;
		if (list.size() > 0) {
			model = list.iterator().next();
		}
		return model;
	}

	/**
	 * Save the model to the database.
	 */
	public void save() {
		final ColumnMapper mapper = Beans.getBean(ColumnMapper.class);
		final ContentValues values = new ContentValues();

		final Field[] fields = Reflection.getNonStaticDeclaredFields(getClass());
		for (Field field : fields) {
			values.put(field.getName(), mapper.getValueFromObject(field, this));
		}

		final String tableName = Reflection.getSimpleClassName(this);
		final DBOpenHelper helper = Beans.getBean(DBOpenHelper.class);
		if (_id != null) {
			helper.getWritableDatabase().update(tableName, values, "_id=?",
					new String[] { _id.toString() });
		} else {
			helper.getWritableDatabase().insertOrThrow(tableName, null, values);
		}
	}

	/**
	 * Delete the model from database.
	 */
	public void delete() {
		final String tableName = Reflection.getSimpleClassName(this);
		final DBOpenHelper helper = Beans.getBean(DBOpenHelper.class);
		helper.getWritableDatabase().delete(tableName, "_id=?",
				new String[] { _id.toString() });
	}

	protected void transform(final Cursor cursor) {
		final ColumnMapper mapper = Beans.getBean(ColumnMapper.class);
		final Field[] fields = Reflection.getNonStaticDeclaredFields(this.getClass());
		for (Field field : fields) {
			mapper.setValueToObject(cursor, field, this);
		}
		_id = cursor.getInt(cursor.getColumnIndex("_id"));
	}

	public static <T extends Model> List<T> findAll(final Class<T> cls) {
		return where(cls, "1=1");
	}

	public static <T extends Model> List<T> where(final Class<T> cls,
			final String query, final String... params) {
		
		final String tableName = Reflection.getSimpleClassName(cls);
		final List<T> result = new ArrayList<T>();

		final DBOpenHelper helper = Beans.getBean(DBOpenHelper.class);
		final Cursor cursor = helper.getReadableDatabase().rawQuery(
				"select * from " + tableName + " where " + query, params);
		while (cursor.moveToNext()) {
			T model = Reflection.instantiate(cls);
			model.transform(cursor);
			result.add(model);
		}

		cursor.close();
		return result;
	}

	public static <T extends Model> int count(final Class<T> cls) {
		return count(cls, null);
	}

	public static <T extends Model> int count(final Class<T> cls,
			final String query, final String... params) {
		
		final String tableName = Reflection.getSimpleClassName(cls);
		int result = 0;

		StringBuffer sb = new StringBuffer();
		sb.append("select count(1) from ");
		sb.append(tableName);
		if (query != null) {
			sb.append(" where ");
			sb.append(query);
		}
		
		final DBOpenHelper helper = Beans.getBean(DBOpenHelper.class);
		final Cursor cursor = helper.getReadableDatabase().rawQuery(sb.toString(), params);
		if (cursor.moveToNext()) {
			result = cursor.getInt(0);
		}

		cursor.close();
		return result;
	}

	public static String getSQLCreateTable(final Class<?> cls) {
		final String tableName = Reflection.getSimpleClassName(cls);
		final StringBuilder sql = new StringBuilder();
		
		sql.append("CREATE TABLE  ");
		sql.append(tableName);
		sql.append(" (");
		sql.append(" _id INTEGER PRIMARY KEY");
		
		boolean first = true;
		final Field[] fields = Reflection.getNonStaticDeclaredFields(cls);
		
		for (Field field : fields) {
			if (first) {
				sql.append(", ");
				first = false;
			}
			sql.append(field.getName());
			sql.append(" ");
			sql.append(getType(field));
		}
		sql.append(");");
		
		return sql.toString();
	}

	private static String getType(final Field field) {
		final Class<?> cls = field.getType();
		
		String type = null;
		if (cls.getSimpleName().toLowerCase().equals("integer")
			|| cls.getSimpleName().toLowerCase().equals("long")
			|| cls.getSimpleName().toLowerCase().equals("short")
			|| cls.getSimpleName().toLowerCase().equals("boolean")) {
			type = "INTEGER";
		} else if (cls.getSimpleName().toLowerCase().equals("double")
				|| cls.getSimpleName().toLowerCase().equals("float")) {
			type = "REAL";
		} else if (cls.getSimpleName().toLowerCase().equals("date")) {
			type = "DATE";
		} else {
			type = "TEXT";
		}
		
		return type;
	}

}
