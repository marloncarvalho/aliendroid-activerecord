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
package org.alienlabs.aliendroid.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.alienlabs.aliendroid.activerecord.Model;

import roboguice.util.Ln;
import android.content.Context;
import android.content.pm.PackageManager;
import dalvik.system.DexFile;

/**
 * Special utility responsible to get informations about classes looking inside DEX files.
 * 
 * @author Marlon Silva Carvalho
 * @since 1.0.0
 */
public class Dex {

	/**
	 * Look in the DEX files all 'Model' classes. In other words, we're looking for all
	 * classes that are child of {@link Model}.
	 * 
	 * @param context Android Context.
	 * @return List containing all found Models.
	 */
	public static List<Class<?>> getModels(final Context context) {
		ArrayList<Class<?>> modelClasses = new ArrayList<Class<?>>();
		try {
			String path = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).sourceDir;
			DexFile dexfile = new DexFile(path);
			Enumeration<?> entries = dexfile.entries();

			while (entries.hasMoreElements()) {
				String name = (String) entries.nextElement();
				Class<?> discoveredClass = null;
				Class<?> superClass = null;
				try {
					discoveredClass = Class.forName(name, true, context.getClass().getClassLoader());
					superClass = discoveredClass.getSuperclass();
				} catch (ClassNotFoundException e) {
					Ln.e("AlienDroid", e.getMessage());
				}

				if ((discoveredClass == null) || (superClass == null) || (superClass.getName().indexOf("Model") != -1)) {
					continue;
				}
				modelClasses.add(discoveredClass);
			}

		} catch (IOException e) {
			Ln.e("Demoiselle", e.getMessage());
		} catch (PackageManager.NameNotFoundException e) {
			Ln.e("Demoiselle", e.getMessage());
		}

		return modelClasses;
	}

}
