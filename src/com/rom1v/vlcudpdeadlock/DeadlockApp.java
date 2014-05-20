package com.rom1v.vlcudpdeadlock;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;

import android.app.Application;
import android.content.Context;

public class DeadlockApp extends Application {

	private static Context sContext;

	@Override
	public void onCreate() {
		super.onCreate();
		sContext = this;
	}

	public static synchronized LibVLC getLibVLC() throws LibVlcException {
		LibVLC instance = LibVLC.getExistingInstance();
		if (instance == null) {
			instance = LibVLC.getInstance();
			instance.setHardwareAcceleration(LibVLC.HW_ACCELERATION_AUTOMATIC);
			instance.setAout(LibVLC.AOUT_OPENSLES);
			instance.setTimeStretching(true);
			instance.setChroma("RV32");
			instance.setVerboseMode(true);
			instance.init(sContext);
		}
		return instance;
	}

}
