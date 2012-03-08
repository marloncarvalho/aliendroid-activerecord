package org.alienlabs.aliendroid.event;

import android.database.sqlite.SQLiteDatabase;

public class DatabaseCreation {
	private SQLiteDatabase database;

	public DatabaseCreation(SQLiteDatabase db) {
		this.setDatabase(db);
	}

	public SQLiteDatabase getDatabase() {
		return database;
	}

	public void setDatabase(SQLiteDatabase database) {
		this.database = database;
	}
}
