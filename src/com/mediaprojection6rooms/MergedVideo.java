package com.mediaprojection6rooms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
/**
 * 合成新的视频
 * @author xiaodong
 *
 */
public class MergedVideo extends Thread {
	@Override
	public void run() {
		try {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			newMp4();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 生成新的MP4
	 * 
	 * @throws IOException
	 */
	private void newMp4() throws IOException {
		// TODO Auto-generated method stub要判断sd种是否有这文件还没做
		String audioEnglish = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator
				+ "recaudio.MP4";
		String video = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "video.mp4";
		boolean fileIsExists = fileIsExists(audioEnglish);
		boolean fileIsExistsVideo = fileIsExistsVideo(video);
		// String audioEnglish = "/sdcard/rrr.MP4";
		// String video = "/sdcard/re.mp4";
		if(fileIsExists && fileIsExistsVideo ){
		Movie countVideo = MovieCreator.build(video);
		Movie countAudioEnglish = MovieCreator.build(audioEnglish);
		Track audioTrackEnglish = countAudioEnglish.getTracks().get(0);
		countVideo.addTrack(audioTrackEnglish);
		{
			Container out = new DefaultMp4Builder().build(countVideo);
			FileOutputStream fos = new FileOutputStream(new File("/sdcard/hope.mp4"));
			out.writeContainer(fos.getChannel());
			fos.close();

		 }
		}
	}

	// 判断文件是否存在
	private boolean fileIsExistsVideo(String video) {
		try {
			File f2 = new File(video);
			if (!f2.exists()) {
				return false;
			}

		} catch (Exception e) {
			return false;
		}

		return true;
	}

	// 判断文件是否存在
	public boolean fileIsExists(String audioEnglish) {
		try {
			File f = new File(audioEnglish);
			if (!f.exists()) {
				return false;
			}

		} catch (Exception e) {
			return false;
		}

		return true;
	}
}
