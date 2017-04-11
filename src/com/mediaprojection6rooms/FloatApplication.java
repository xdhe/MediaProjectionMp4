package com.mediaprojection6rooms;

import android.app.Application;
import android.view.WindowManager;

public class FloatApplication extends Application {
	private WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();

	public WindowManager.LayoutParams getWindowParams() {
		return windowParams;
	}
}
