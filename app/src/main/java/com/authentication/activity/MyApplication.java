package com.authentication.activity;

import org.litepal.LitePalApplication;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.config.PropertyConfigurator;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.HandlerThread;
import android.util.Log;

public class MyApplication extends LitePalApplication {
	private String rootPath;

	public static final Logger logger = LoggerFactory.getLogger();

	private HandlerThread handlerThread;

	public String getRootPath() {
		return rootPath;
	}

	public HandlerThread getHandlerThread() {
		return handlerThread;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		PropertyConfigurator.getConfigurator(this).configure();
		final FileAppender fa = (FileAppender) logger.getAppender(1);
		fa.setAppend(true);

		logger.debug("**********Enter Myapplication********");
		handlerThread = new HandlerThread("handlerThread",
				android.os.Process.THREAD_PRIORITY_BACKGROUND);
		// handlerThread = new HandlerThread("handlerThread");
		handlerThread.start();
		setRootPath();
	}

	private void setRootPath() {
		PackageManager manager = this.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			rootPath = info.applicationInfo.dataDir;
			Log.i("rootPath", "################rootPath=" + rootPath);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
