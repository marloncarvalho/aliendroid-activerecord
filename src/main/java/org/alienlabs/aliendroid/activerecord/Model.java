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

	public Model() {
		super();
	}

	public Model(final Integer id) {
		this._id = id;
		//TODO: verificar esse comportamento
		//load(id);
	}

	private String getTableName() {
		return Reflection.getSimpleClassName(this);
	}
	
	private DBOpenHelper getHelper() {
		return Beans.getBean(DBOpenHelper.class);
	}
	
	private ColumnMapper getColumnMapper() {
		return Beans.getBean(ColumnMapper.class);
	}
	
	public void load(final Integer id) {
		final Cursor cursor = getHelper().getReadableDatabase().query(getTableName(),
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
		final List<T> list = Model.where(cls, null);
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
		final ColumnMapper mapper = getColumnMapper();
		final ContentValues values = new ContentValues();

		final Field[] fields = Reflection.getNonStaticDeclaredFields(getClass());
		for (Field field : fields) {
			values.put(field.getName(), mapper.getValueFromObject(field, this));
		}

		final String tableName = getTableName();
		final DBOpenHelper helper = getHelper();
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
		getHelper().getWritableDatabase().delete(getTableName(), "_id=?",
				new String[] { _id.toString() });
	}

	protected void transform(final Cursor cursor) {
		final ColumnMapper mapper = getColumnMapper();
		final Field[] fields = Reflection.getNonStaticDeclaredFields(this.getClass());
		for (Field field : fields) {
			mapper.setValueToObject(cursor, field, this);
		}
		this._id = cursor.getInt(cursor.getColumnIndex("_id"));
	}

	public static <T extends Model> List<T> findAll(final Class<T> cls) {
		return where(cls, null);
	}

	public static <T extends Model> List<T> where(final Class<T> cls,
			final String query, final String... params) {
		
		final String tableName = Reflection.getSimpleClassName(cls);
		final List<T> result = new ArrayList<T>();

		final StringBuffer sql = new StringBuffer();
		sql.append("SELECT * FROM ");
		sql.append(tableName);
		if (query != null) {
			sql.append(" WHERE ");
			sql.append(query);
		}
		
		final DBOpenHelper helper = Beans.getBean(DBOpenHelper.class);
		final Cursor cursor = helper.getReadableDatabase().rawQuery(sql.toString(), params);
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

		final StringBuffer sql = new StringBuffer();
		sql.append("SELECT count(1) FROM ");
		sql.append(tableName);
		if (query != null) {
			sql.append(" WHERE ");
			sql.append(query);
		}
		
		final DBOpenHelper helper = Beans.getBean(DBOpenHelper.class);
		final Cursor cursor = helper.getReadableDatabase().rawQuery(sql.toString(), params);
		if (cursor.moveToNext()) {
			result = cursor.getInt(0);
		}

		cursor.close();
		return result;
	}

	public static String getSQLCreateTable(final Class<?> cls) {
		final String tableName = Reflection.getSimpleClassName(cls);
		final StringBuilder sql = new StringBuilder();
		
		sql.append("CREATE TABLE ");
		sql.append(tableName);
		sql.append(" (_id INTEGER PRIMARY KEY");
		
		final Field[] fields = Reflection.getNonStaticDeclaredFields(cls);
		for (Field field : fields) {
			sql.append(", ");
			sql.append(field.getName());
			sql.append(" ");
			sql.append(getType(field));
		}
		sql.append(");");
		
		return sql.toString();
	}

	private static String getType(final Field field) {
		final String cls = field.getType().getSimpleName().toLowerCase();
		
		String type = null;
		if (cls.equals("string")
			|| cls.equals("char")
			|| cls.equals("character")) {
			type = "TEXT";
		} else if (cls.equals("integer")
			|| cls.equals("int")
			|| cls.equals("long")
			|| cls.equals("short")
			|| cls.equals("byte")
			|| cls.equals("boolean")) {
			type = "INTEGER";
		} else if (cls.equals("double")
				|| cls.equals("float")) {
			type = "REAL";
		} else if (cls.equals("date")) {
			type = "DATE";
		} else if (field.getType().isEnum()) {
			type = "INTEGER";
		} else {
			type = "TEXT";
		}
		
		return type;
	}

}
