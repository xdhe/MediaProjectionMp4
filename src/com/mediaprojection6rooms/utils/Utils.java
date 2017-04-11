package com.mediaprojection6rooms.utils;

import java.util.Formatter;
import java.util.Locale;
/*
 * 把毫秒转为秒
 */
public class Utils {
	 private   StringBuilder mFormatBuilder=new StringBuilder();;
		private   Formatter mFormatter=new Formatter(mFormatBuilder, Locale.getDefault());;
		
		public   String stringForTime(int timeMs) {
			int totalSeconds = timeMs / 1000;
			int seconds = totalSeconds % 60;
			
			int minutes = (totalSeconds / 60) % 60;
			
			int hours = totalSeconds / 3600;

			mFormatBuilder.setLength(0);
			if (hours > 0) {
				return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds)
						.toString();
			} else {
				return mFormatter.format("%02d:%02d", minutes, seconds).toString();
			}
		}
}
