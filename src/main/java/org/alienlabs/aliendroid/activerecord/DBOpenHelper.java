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

import java.util.List;

import org.alienlabs.aliendroid.event.DatabaseCreated;
import org.alienlabs.aliendroid.event.DatabaseCreation;
import org.alienlabs.aliendroid.util.Beans;
import org.alienlabs.aliendroid.util.Dex;

import roboguice.event.EventManager;
import roboguice.util.Ln;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Classe auxiliar que ajuda a abrir e manter uma conex‹o com o banco de dados.
 * 
 * @author Marlon Silva Carvalho
 * @since 1.0.0
 */
@Singleton
public class DBOpenHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "BR2012.db";
	private static final int VERSION = 1;

	@Inject
	private EventManager eventManager;

	public DBOpenHelper() {
		super(Beans.getBean(Context.class), DATABASE_NAME, null, VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		Ln.d("Firing DatabaseCreation Event.");
		eventManager.fire(new DatabaseCreation(db));

		Ln.d("Creating Tables.");
		List<Class<?>> tables = Dex.getModels(Beans.getBean(Context.class));
		for (Class<?> table : tables) {
			db.execSQL(Model.getSQLCreateTable(table));
			Ln.d("Table for class " + table.toString() + " created.");
		}

		Ln.d("Firing DatabaseCreated Event.");
		eventManager.fire(new DatabaseCreated(db));
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
	}

}
