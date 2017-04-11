package com.mediaprojection6rooms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class ScreenCaptureImageActivity extends Activity implements OnClickListener {
	private static final int REQUEST_CODE = 1;
	private MediaProjectionManager mMediaProjectionManager;
	private ScreenRecorder mRecorder;
	private Button mButton, mButton2;
	private MsgReceiver msgReceiver;
	private boolean booleanExtra;
	private boolean task;
	private boolean flg;
	private MediaRecorder mMediaRecorder;// MediaRecorder对象
	private File mRecAudioFile; // 录制的音频文件
	private File mRecAudioPath; // 录制的音频文件路徑
	private String strTempFile = "/sdcard/recaudio.mp4";// 零时文件的前缀

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去标题
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
		setContentView(R.layout.activity_main);
		mButton = (Button) findViewById(R.id.button);
		mButton.setOnClickListener(this);
		mButton2 = (Button) findViewById(R.id.button2);
		// 动态注册广播接收器
		msgReceiver = new MsgReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.example.communication.RECEIVER");
		registerReceiver(msgReceiver, intentFilter);

		mButton2.setOnClickListener(this);
		mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
	}

	/**
	 * 得到数据
	 * 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);

		if (mediaProjection == null) {
			// Log.e("@@", "media projection is null");
			moveTaskToBack(true);// 回退Task
			return;
		}

		task = true;
		// video size
		final int width = 1280;
		final int height = 720;
		File file = new File(Environment.getExternalStorageDirectory(), "video.mp4");
		final int bitrate = 6000000;// 比特率
		if (!flg) {
			Toast.makeText(getApplicationContext(), flg + "", Toast.LENGTH_SHORT).show();
			startService(new Intent(this, SuspensefulService.class));
		}
		/**
		 * 启动线程去执行
		 */
		mRecorder = new ScreenRecorder(width, height, bitrate, 1, mediaProjection, file.getAbsolutePath());
		// mRecorder.start();
		Toast.makeText(getApplicationContext(), "录屏中...", Toast.LENGTH_SHORT).show();
		moveTaskToBack(true);// 回退Task

	}

	/**
	 * 开始截屏了
	 */

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.button) {
			Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
			startActivityForResult(captureIntent, REQUEST_CODE);
		}

		// }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mRecorder != null) {
			mRecorder.quit();
			mRecorder = null;
		}
		stopService(new Intent(getApplicationContext(), SuspensefulService.class));
	}

	/**
	 * 广播接收器
	 * 
	 * @author len
	 * 
	 */
	public class MsgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			booleanExtra = intent.getBooleanExtra("falng", true);
			flg = true;
			// startActivity(new
			// Intent(getApplicationContext(),ScreenCaptureImageActivity.class));
			if (booleanExtra) {
				if (mRecorder == null) {
					Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
					startActivityForResult(captureIntent, REQUEST_CODE);
					mRecorder.start();
					return;

				}
				new Thread() {
					public void run() {
						mRecorder.start();// 截屏开始
						mAudioStart();// 录音开始
					};
				}.start();

			} else {
				//
				mRecorder.quit();
				mAudioStop();// 录音结束
				mRecorder = null;
				task = false;
				MergedVideo mergedVideo = new MergedVideo();
				mergedVideo.start();// 启动线程去合并
				mergedVideo = null;
			}
			//
		}

		private void mAudioStop() {
			// TODO Auto-generated method stub
			if (mRecAudioFile != null) {
				/* ⑤停止录音 */
				mMediaRecorder.stop();
				mMediaRecorder.release();
				mMediaRecorder = null;
			}
		}

		private void mAudioStart() {
			try {
				/* ①Initial：实例化MediaRecorder对象 */
				mMediaRecorder = new MediaRecorder();
				/* ②setAudioSource/setVedioSource */
				mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
				/*
				 * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default
				 * THREE_GPP(3gp格式，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(
				 * 只支持音频且音频编码要求为AMR_NB)
				 */
				mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
				/* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default */
				mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
				/* ②设置输出文件的路径 */
				try {
					mRecAudioPath = Environment.getExternalStorageDirectory();// 得到SD卡得路径
					boolean card = hasSDCard();
					if (card) {
						mRecAudioFile = new File(strTempFile);
					} else {
						Toast.makeText(getApplicationContext(), "SD木有", Toast.LENGTH_SHORT).show();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				mMediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
				/* ③准备 */
				mMediaRecorder.prepare();
				/* ④开始 */
				mMediaRecorder.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 判断手机是否有SD卡。
	 * 
	 * @return 有SD卡返回true，没有返回false。
	 */
	public boolean hasSDCard() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}

}
