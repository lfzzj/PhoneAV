package com.lf.phoneav.bean;
public class Lyric {

	/** 歌词的开始显示位置 */
	public int startShowPosition;
	/** 歌词内容 */
	public String text;

	public Lyric(int startShowPosition, String lyric) {
		super();
		this.startShowPosition = startShowPosition;
		this.text = lyric;
	}

	@Override
	public String toString() {
		return "Lyric [startShowPosition=" + startShowPosition + ", lyric=" + text + "]";
	}

}
