package com.rom1v.vlcudpdeadlock;

import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.rom1v.vlcudpdeadlock.R;

public class MainActivity extends Activity implements SurfaceHolder.Callback, IVideoPlayer {

	private static final String TAG = "vlc-udp-deadlock";

	public static final String URL = "udp://@239.0.0.1:1234/";

	private LibVLC mLibvlc;
	private SurfaceView mSurface;
	private SurfaceHolder mSurfaceHolder;

	private boolean mSurfaceCreated;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new Handler();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		setContentView(R.layout.activity_main);

		mSurface = (SurfaceView) findViewById(R.id.surfaceView);
		mSurfaceHolder = mSurface.getHolder();
		mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
		mSurfaceHolder.setKeepScreenOn(true);
		mSurfaceHolder.addCallback(this);

		try {
			mLibvlc = DeadlockApp.getLibVLC();
			Log.d(TAG, "VLC initialized");
		} catch (LibVlcException e) {
			Log.d(TAG, "Cannot init VLC", e);
			finish();
			return;
		}

		mLibvlc.eventVideoPlayerActivityCreated(true);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mSurfaceHolder.setKeepScreenOn(false);
		mSurfaceHolder.removeCallback(this);
		mLibvlc.eventVideoPlayerActivityCreated(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()");
		if (mSurfaceCreated) {
			mLibvlc.play();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause()");
		stop();
	}

	@SuppressWarnings("unused")
	public void onClickPlay(View v) {
		Log.d(TAG, "onClickPlay()");
		play();
	}

	@SuppressWarnings("unused")
	public void onClickStop(View v) {
		Log.d(TAG, "onClickStop()");
		stop();
	}

	@SuppressWarnings("unused")
	public void onClickPlayStop(View v) {
		Log.d(TAG, "onClickPlayStop()");
		play();
		// stop almost immediately
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				stop();
			}
		});
	}

	private void play() {
		MediaList playlist = mLibvlc.getPrimaryMediaList();
		playlist.clear();
		playlist.add(new Media(mLibvlc, URL));
		Log.d(TAG, "before play()");
		mLibvlc.playIndex(0);
		Log.d(TAG, "after play()");
	}

	private void stop() {
		Log.d(TAG, "before stop()");
		mLibvlc.stop();
		Log.d(TAG, "after stop()");
	}

	/* Surface callbacks */

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated()");
		mSurfaceCreated = true;
		play();
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceholder, int format, int width, int height) {
		Log.d(TAG, String.format("surfaceChanged(width=%d, height=%d)", width, height));
		mLibvlc.attachSurface(mSurfaceHolder.getSurface(), this);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
		Log.d(TAG, "surfaceDestroyed()");
		mLibvlc.detachSurface();
		mSurfaceCreated = false;
	}

	@Override
	public void setSurfaceSize(final int width, final int height, final int visibleWidth,
	        final int visibleHeight, final int sarNum, final int sarDen) {
		Log.d(TAG, String.format("setSurfaceSize(width=%d, height=%d)", width, height));
		// setSurfaceSize is not called from the UI thread
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				changeSurfaceSize(width, height, visibleWidth, visibleHeight, sarNum, sarDen);
			}
		});
	}

	@SuppressWarnings("unused")
	private void changeSurfaceSize(int width, int height, int visibleWidth, int visibleHeight,
	        int sarNum, int sarDen) {
		// don't care about aspect-ratio
		mSurfaceHolder.setFixedSize(width, height);
		mSurface.invalidate();
	}

}
