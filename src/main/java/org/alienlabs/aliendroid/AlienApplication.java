package org.alienlabs.aliendroid;

import org.alienlabs.aliendroid.util.Beans;

import roboguice.application.RoboApplication;

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
