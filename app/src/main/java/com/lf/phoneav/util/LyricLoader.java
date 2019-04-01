package com.lf.phoneav.util;
import com.lf.phoneav.bean.Lyric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LyricLoader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String musicPath = "G:\\classes\\term73\\手机影音\\资料\\视频和音乐资源\\test\\audio\\TongHua.mp3";
		ArrayList<Lyric> lyrics = loadLyric(musicPath);
		for (Lyric lyric : lyrics) {
			System.out.println(lyric);
		}
	}

	/**
	 * 加载歌词
	 * @param musicPath 音乐路径
	 * @return
	 */
	public static ArrayList<Lyric> loadLyric(String musicPath) {
		ArrayList<Lyric> lyrics = null;
		String prefix = musicPath.substring(0, musicPath.lastIndexOf("."));	// 删除.mp3
		// 拼接歌词文件
		File lrcFile = new File(prefix + ".lrc");
		File txtFile = new File(prefix + ".txt");
		if (lrcFile.exists()) {
			lyrics = readFile(lrcFile);
		} else if (txtFile.exists()) {
			lyrics = readFile(txtFile);
		}

		if (lyrics == null) {
			return null;
		}

		// 按时间的先后顺序进行排序
		Collections.sort(lyrics, new Comparator<Lyric>() {
			@Override
			public int compare(Lyric o1, Lyric o2) {
				return Integer.valueOf(o1.startShowPosition).compareTo(o2.startShowPosition);
			}
		});

		return lyrics;
	}

	/**
	 * 读取歌词文件
	 * @param lrcFile
	 * @return
	 */
	private static ArrayList<Lyric> readFile(File lrcFile) {
		ArrayList<Lyric> lyrics = new ArrayList<Lyric>();
		try {
			InputStream in = new FileInputStream(lrcFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "GBK"));
			String line;
			while ((line = reader.readLine()) != null) {
				// 读到一行歌词：[02:09.20][01:02.20]我的天空 星星都亮了
				String[] strings = line.split("]");
				String lyricText = strings[strings.length - 1];	// 获取歌词文本

				// 遍历所有的时间:[02:09.20
				for (int i = 0; i < strings.length - 1; i++) {
					int startShowPosition = parseTime(strings[i]);
					lyrics.add(new Lyric(startShowPosition, lyricText));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lyrics;
	}

	/**
	 * 解析[02:09.20这样的字符串，解析成毫秒值
	 * @param time
	 * @return
	 */
	private static int parseTime(String time) {
		String minuteStr = time.substring(1, 3);	// 取出02
		String secondStr = time.substring(4, 6);	// 取出09
		String millisStr = time.substring(7, 9);	// 取出20
		int minuteMillis = Integer.parseInt(minuteStr) * 60 * 1000;
		int secondMillis = Integer.parseInt(secondStr) * 1000;
		int millis = Integer.parseInt(millisStr) * 10;
		return minuteMillis + secondMillis + millis;
	}

}
