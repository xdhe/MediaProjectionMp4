package com.mediaprojection6rooms;

import java.io.IOException;
import java.util.List;

import com.mediaprojection6rooms.utils.DisplayUtil;
import com.mediaprojection6rooms.utils.Utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SuspensefulService extends Service implements OnClickListener, SurfaceHolder.Callback {
	private static final int SUPENMP = 1;
	private View view;
	private LayoutParams wmParams;
	private int sW;
	private int sH;
	private LinearLayout ll_drag;
	private WindowManager wm;
	private WindowManager.LayoutParams params;
	private WindowManager wm2;
	private WindowManager.LayoutParams params2;
	private WindowManager wm3;
	private WindowManager.LayoutParams params3;
	private View addressView;
	private Intent intent = new Intent("com.example.communication.RECEIVER");
	private Utils utils;
	private TextView tv_start;// 录制时间
	private ImageView imageview;
	private int time = 0;
	private boolean isDestroy;
	private TextView tv_camera;// 相机
	private TextView tv_chat;// 聊天
	private Context context;
	private OrientationEventListener mOrEventListener;// 设备方向监听器
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SUPENMP:
				tv_start.setText(utils.stringForTime(time));
				time += 1000;
				if (!isDestroy) {
					mHandler.sendEmptyMessageDelayed(SUPENMP, 1000);
				}
				break;

			default:
				break;
			}
		};
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		context = this;
		utils = new Utils();
		if (addressView == null) {
			initWindowManager();

		}
	}

	/**
	 * 主窗口
	 */
	private void initWindowManager() {
		// TODO Auto-generated method stub
		// 加载布局
		addressView = View.inflate(this, R.layout.bo_view, null);
		tv_start = (TextView) addressView.findViewById(R.id.tv_start);
		imageview = (ImageView) addressView.findViewById(R.id.imageview);
		imageview.setImageResource(R.drawable.left_start_live_click);

		tv_chat = (TextView) addressView.findViewById(R.id.tv_chat);// 聊天
		tv_camera = (TextView) addressView.findViewById(R.id.tv_camera);// 相机

		// 初始化布局参数
		wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		params = new WindowManager.LayoutParams();
		params.width = WindowManager.LayoutParams.WRAP_CONTENT; // 宽度自适应
		params.height = WindowManager.LayoutParams.WRAP_CONTENT; // 高度自适应
		params.format = PixelFormat.TRANSLUCENT;// 设置成半透明的
		params.type = WindowManager.LayoutParams.TYPE_PHONE; // 使addressView能移动
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; // 使addressView不用获得焦点
		params.gravity = Gravity.LEFT | Gravity.TOP;
		// 显示到窗口中
		wm.addView(addressView, params);
		imageview.setOnClickListener(this);
		tv_camera.setOnClickListener(this);
		tv_chat.setOnClickListener(this);
		imageview.setOnTouchListener(new OnTouchListener() {
			private int firstX;
			private int firstY;
			private boolean flang;
			private int distanceX;
			private int distanceY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {// 得到当前事件的坐标
				int x = (int) event.getRawX();
				int y = (int) event.getRawY();
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					firstX = (int) x;
					firstY = (int) y;
					break;
				case MotionEvent.ACTION_MOVE:
					distanceX = x - firstX;
					distanceY = y - firstY;
					// 更新addressView的位置
					params.x = params.x + distanceX;
					params.y = params.y + distanceY;
					wm.updateViewLayout(addressView, params);
					// 更新firstX和firstY
					firstX = x;
					firstY = y;
					break;
				case MotionEvent.ACTION_UP:

					break;
				default:
					break;
				}
				return false;

			}
		});
	}

	private int firstX2;
	private int firstY2;
	private int distanceX2;
	private int distanceY2;
	private OnTouchListener onTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {// 得到当前事件的坐标
			int x = (int) event.getRawX();
			int y = (int) event.getRawY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				firstX2 = (int) x;
				firstY2 = (int) y;
				break;
			case MotionEvent.ACTION_MOVE:
				distanceX2 = x - firstX2;
				distanceY2 = y - firstY2;
				// 更新addressView的位置
				params2.x = params2.x + distanceX2;
				params2.y = params2.y + distanceY2;
				wm2.updateViewLayout(cameraView, params2);
				// 更新firstX和firstY
				firstX2 = x;
				firstY2 = y;
				break;
			case MotionEvent.ACTION_UP:

				break;
			default:
				break;
			}
			return false;

		}
	};

	private boolean falng;
	private View cameraView;// 相机
	private SurfaceView surfaceSv;
	private Camera mCamera;
	private SurfaceHolder mHolder;
	private View chatView;// 聊天
	private LinearLayout ll_root_chat;
	private LinearLayout lv_suspend;
	private boolean chat;
	private boolean camera;
	private boolean chatShow;
	private LinearLayout ll_root_chat2;
	private EditText et_dialog;// 聊天框
	private ImageView v_send;
	private TextView tv_text;
	private TextView tv_toptext;

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isDestroy = true;
		wm.removeView(addressView);
		wm2.removeView(cameraView);
		wm3.removeView(chatView);
		mHandler.removeCallbacksAndMessages(null);// 移除未处理的消息
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.imageview:
			moveView();// 主窗口
			break;
		case R.id.tv_camera:
			if (cameraView == null) {
				CameraView();// 相机
				camera = true;
				break;
			}
			if (!camera) {
				CameraView();// 相机
				cameraView.setVisibility(view.VISIBLE);
				camera = true;
			} else {
				releaseCamera();
				camera = false;
				cameraView.setVisibility(view.GONE);
				mOrEventListener.disable();// 同时也移除横竖屏监听
				// wm2.removeView(cameraView);
			}
			break;
		case R.id.tv_chat:
			if (chatView == null) {
				ChatView();// 聊天
				chat = true;
				break;
			}
			if (!chat) {
				chatView.setVisibility(view.VISIBLE);
				chat = true;
			} else {
				chat = false;
				chatView.setVisibility(view.GONE);
				// wm3.removeView(chatView);
			}
			break;
		default:
			break;
		}
	}

	private String text;
	private boolean flags;

	/**
	 * 聊天窗口
	 */
	private void ChatView() {
		// TODO Auto-generated method stub
		chatView = View.inflate(this, R.layout.chat_view, null);
		ll_root_chat = (LinearLayout) chatView.findViewById(R.id.ll_root_chat);
		ll_root_chat2 = (LinearLayout) chatView.findViewById(R.id.ll_root_chat2);
		lv_suspend = (LinearLayout) chatView.findViewById(R.id.lv_suspend);
		et_dialog = (EditText) chatView.findViewById(R.id.et_dialog);
		tv_toptext = (TextView) chatView.findViewById(R.id.tv_toptext);
		v_send = (ImageView) chatView.findViewById(R.id.iv_send);
		tv_text = (TextView) chatView.findViewById(R.id.tv_text);
		// 初始化布局参数
		wm3 = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		params3 = new WindowManager.LayoutParams();
		params3.width = WindowManager.LayoutParams.WRAP_CONTENT; // 宽度自适应
		params3.height = WindowManager.LayoutParams.WRAP_CONTENT; // 高度自适应
		params3.format = PixelFormat.TRANSLUCENT;// 设置成半透明的
		params3.type = WindowManager.LayoutParams.TYPE_PHONE; // 使addressView能移动
		params3.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; //
		// 使addressView不用获得焦点
		params3.gravity = Gravity.LEFT | Gravity.TOP;
		// 显示到窗口中
		wm3.addView(chatView, params3);
		ll_root_chat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (chatShow) {
					ll_root_chat2.setVisibility(view.GONE);
					lv_suspend.setVisibility(view.GONE);

					params3.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; // 使addressView不用获得焦点
					tv_toptext.setText(text);
					wm3.updateViewLayout(chatView, params3);
					chatShow = false;
				} else {
					ll_root_chat2.setVisibility(view.VISIBLE);
					lv_suspend.setVisibility(view.VISIBLE);
					tv_toptext.setText("聊天");
					params3.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;// 这个是可以获得一次外部焦点，当获取外部焦点的时候
																						// 把View再改成
					wm3.updateViewLayout(chatView, params3);
					// // 使addressView不用获得焦点

					chatShow = true;

				}
			}
		});
		v_send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), "聊聊", 0).show();
				text = et_dialog.getText().toString();
				// lv_suspend.setAdapter(new
				// SendAdapter(SuspensefulService.this,text));
				tv_text.setText(text);
				et_dialog.setText("");

			}
		});
		OnTouchListener onTouchListenerchat = new OnTouchListener() {
			private int firstX3;
			private int firstY3;
			private int distanceX3;
			private int distanceY3;

			@Override
			public boolean onTouch(View v, MotionEvent event) {// 得到当前事件的坐标
				int x = (int) event.getRawX();
				int y = (int) event.getRawY();
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					firstX3 = (int) x;
					firstY3 = (int) y;
					break;
				case MotionEvent.ACTION_MOVE:
					distanceX3 = x - firstX3;
					distanceY3 = y - firstY3;
					// 更新addressView的位置
					params3.x = params3.x + distanceX3;
					params3.y = params3.y + distanceY3;
					wm3.updateViewLayout(chatView, params3);
					// 更新firstX和firstY
					firstX3 = x;
					firstY3 = y;
					break;
				case MotionEvent.ACTION_UP:

					break;
				default:
					break;
				}
				return false;

			}
		};

		ll_root_chat.setOnTouchListener(onTouchListenerchat);
		chatView.setOnTouchListener(onTouchListenerchat);

	}

	/**
	 * 相机
	 */
	private void CameraView() {
		// TODO Auto-generated method stub

		cameraView = View.inflate(this, R.layout.camera_view, null);
		surfaceSv = (SurfaceView) cameraView.findViewById(R.id.id_area_sv);

		mHolder = surfaceSv.getHolder(); // 获得句柄
		mHolder.addCallback(this);// 添加回调
		Point screenMetrics = DisplayUtil.getScreenMetrics(getApplicationContext());
		// Toast.makeText(getApplicationContext(), "宽>>>>" + screenMetrics.x +
		// "高>>>>>>" + screenMetrics.y, 0).show();
		// 初始化布局参数
		wm2 = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		params2 = new WindowManager.LayoutParams();
		params2.width = screenMetrics.x / 4; // 宽度自适应
		params2.height = screenMetrics.y / 4; // 高度自适应
		params2.format = PixelFormat.TRANSLUCENT;// 设置成半透明的
		params2.type = WindowManager.LayoutParams.TYPE_PHONE; // 使addressView能移动
		params2.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; // 使addressView不用获得焦点
		params2.gravity = Gravity.LEFT | Gravity.TOP;

		initCamera();
		// 显示到窗口中
		wm2.addView(cameraView, params2);
		cameraView.setOnTouchListener(onTouchListener);

	}

	/**
	 * 设备方向监听器
	 */
	private final void startOrientationChangeListener() {
		mOrEventListener = new OrientationEventListener(this) {

			@Override
			public void onOrientationChanged(int rotation) {
				WindowManager wmm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
				Display display = wmm.getDefaultDisplay();
				if (display.getWidth() > display.getHeight()) {// 横屏
					mCamera.setDisplayOrientation(0);// 左倾90度（倾斜）
				} else {// 竖屏
					mCamera.setDisplayOrientation(90);// 左倾90度（倾斜）
				}
			}
		};
		mOrEventListener.enable();

	}

	/**
	 * 初始化相机
	 */
	private void initCamera() {

		if (mCamera == null) {

			/**
			 * 记得释放camera，方便其他应用调用
			 */
			releaseCamera();
			mCamera = Camera.open(1);
			// LayoutParams lp = (LayoutParams) surfaceSv.getLayoutParams();
			// lp.width = 240;
			// lp.height =320;
			// surfaceSv.setLayoutParams(lp);
			mCamera.setDisplayOrientation(90);// 左倾90度（倾斜）
			new Thread() {
				public void run() {
					startOrientationChangeListener(); // 启动设备方向监听器
				};
			}.start();

			mCamera.cancelAutoFocus();// 才会自动对焦。
			setStartPreview(mCamera, mHolder);
			if (mHolder != null) {
				setStartPreview(mCamera, mHolder);
			}
		}

	}

	public void onPause() {
		/**
		 * 记得释放camera，方便其他应用调用
		 */
		// releaseCamera();
	}

	/**
	 * 释放mCamera
	 */
	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();// 停掉原来摄像头的预览
			mCamera.release();
			mCamera = null;
		}
	}

	/**
	 * 移动的窗口
	 */
	private void moveView() {
		if (!falng) {
			falng = true;
			imageview.setImageResource(R.drawable.left_stop_live_click);
			tv_camera.setVisibility(view.VISIBLE);
			tv_chat.setVisibility(View.VISIBLE);
			tv_start.setVisibility(view.VISIBLE);
			mHandler.sendEmptyMessage(SUPENMP);
		} else {
			falng = false;
			tv_start.setVisibility(view.GONE);
			imageview.setImageResource(R.drawable.left_start_live_click);
			time = 0;
			 mHandler.removeCallbacksAndMessages(null);
		}
		intent.putExtra("falng", falng);
		sendBroadcast(intent); // 发送广播
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		setStartPreview(mCamera, mHolder);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			Camera.Parameters parameters = mCamera.getParameters();
			List<Size> sizes = parameters.getSupportedPreviewSizes();
			Size optimalSize = getOptimalPreviewSize(sizes, getResources().getDisplayMetrics().widthPixels,
					getResources().getDisplayMetrics().heightPixels);

			parameters.setPreviewSize(optimalSize.width, optimalSize.height);
			mCamera.setParameters(parameters);
			// }
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		setStartPreview(mCamera, mHolder);

	}

	private Size getOptimalPreviewSize(List<Size> sizes, int h, int w) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) h / w;

		if (sizes == null)
			return null;

		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// 当surfaceview关闭时，关闭预览并释放资源
		/**
		 * 记得释放camera，方便其他应用调用
		 */
		releaseCamera();
		holder = null;
		surfaceSv = null;
	}

	/**
	 * 设置camera显示取景画面,并预览
	 * 
	 * @param camera
	 */
	private void setStartPreview(Camera camera, SurfaceHolder holder) {
		try {
			// LayoutParams lp = (LayoutParams) surfaceSv.getLayoutParams();
			// lp.width = 240;
			// lp.height =320;
			// surfaceSv.setLayoutParams(lp);
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (IOException e) {
		}
	}

}
