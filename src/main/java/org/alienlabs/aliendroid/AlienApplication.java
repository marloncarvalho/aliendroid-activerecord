package org.alienlabs.aliendroid;

import org.alienlabs.aliendroid.util.Beans;

import roboguice.application.RoboApplication;

//TODO: criar respectivo módulo para usar em um RoboApplication
// vide http://code.google.com/p/google-guice/wiki/GettingStarted
public class AlienApplication extends RoboApplication {

	@Override
	public void onCreate() {
		super.onCreate();
		Beans.setInjector(getInjector());
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

}
