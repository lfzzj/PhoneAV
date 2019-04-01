package com.lf.phoneav.interfaces;

import com.lf.phoneav.bean.AudioItem;

/**
 * 播放服务接口
 * @author dzl
 *
 */
public interface IPlayService {

	/** 打开一个音频 */
	void openAudio();

	/**播放 */
	void start();

	/**暂停*/
	void pause();

	/** 上一首 */
	void pre();

	/** 下一首 */
	void next();

	/** 是否正在播放  */
	boolean isPlaying();

	/** 获取当前的播放位置 */
	int getCurrentPosition();

	/** 获取音频的总时长 */
	int getDuration();

	/** 跳转 */
	void seekTo(int msec);

	/**
	 * 切换播放模式
	 * @return 返回切换之后的播放模式
	 */
	int switchPlayMode();

	/** 获取当前的播放模式 */
	int getCurrentPlayMode();

	/** 获取当前的音频JavaBean */
	AudioItem getCurrentAudioItem();
}














