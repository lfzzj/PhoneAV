package com.lf.phoneav.util;

import android.util.Log;

public class Logger {


	public static boolean isShowLog = true;

	/**
	 * 显示信息级别的日记
	 * @param objTag
	 * @param msg
	 */
	public static void i(Object objTag, String msg) {
		if (!isShowLog) {
			return;
		}

		String tag;
		// 如果是String，则直接使用，如果是其它对象，则使用这个对象的类名
		if (objTag instanceof String) {
			tag = (String) objTag;
		} else if (objTag instanceof Class) {
			tag = ((Class) objTag).getSimpleName();
		} else {
			tag = objTag.getClass().getSimpleName();
		}


		Log.i(tag, msg);
	}


}
