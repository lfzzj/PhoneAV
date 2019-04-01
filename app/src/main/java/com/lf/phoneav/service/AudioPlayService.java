package com.lf.phoneav.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.lf.phoneav.R;
import com.lf.phoneav.bean.AudioItem;
import com.lf.phoneav.interfaces.IPlayService;
import com.lf.phoneav.interfaces.Keys;
import com.lf.phoneav.util.Logger;
import com.lf.phoneav.util.SPUtil;

import java.security.Key;
import java.util.ArrayList;
import java.util.Random;

public class AudioPlayService extends Service implements IPlayService {

    private ArrayList<AudioItem> audioList;
    private int curPostion;

    public static final String ACTION_UPDATE_UI = "action_update_ui";

    private MediaPlayer mMediaplayer;
    private AudioItem curAudio;

    public static final int PLAY_MODE_ORDER = 1;//顺序
    public static final int PLAY_MODE_SINGLE = 2;//单曲
    public static final int PLAY_MODE_RANDOM = 3;//随机
    private int curPlayMode = 1;

    private SharedPreferences sp;
    private Random random;

    @Override
    public void onCreate() {
        Logger.i(this, "onCreate");
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        random = new Random();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.i(this, "onStartCommand");
        curPlayMode = sp.getInt(Keys.CURRENT_PLAY_MODE, 1);
        audioList = (ArrayList<AudioItem>) intent.getSerializableExtra(Keys.ITEMS);
        curPostion = intent.getIntExtra(Keys.CURRENT_POSITION, -1);
        return super.onStartCommand(intent, flags, startId);
    }

    public class MyBinder extends Binder {
        public IPlayService playService;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.i(this, "onBind");
        MyBinder binder = new MyBinder();
        binder.playService = this;
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.i(this, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 打开音频
     */
    @Override
    public void openAudio() {
        if (audioList == null || audioList.isEmpty() || curPostion == -1) {
            return;
        }
        curAudio = audioList.get(curPostion);
        String path = curAudio.getData();
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        sendBroadcast(i);

        release();

        try {
            mMediaplayer = new MediaPlayer();
            mMediaplayer.setOnPreparedListener(mOnPreparedListener);
            mMediaplayer.setOnCompletionListener(mOnCompletionListener);
            mMediaplayer.setDataSource(path);
            mMediaplayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void release() {
        if (mMediaplayer != null) {
            mMediaplayer.reset();
            mMediaplayer.release();
            mMediaplayer = null;
        }
    }

    /**
     * service调用activity的方法 是通过发广播
     */
    @Override
    public void start() {
        if (mMediaplayer != null) {
            mMediaplayer.start();
        }
    }

    @Override
    public void pause() {
        if (mMediaplayer != null) {
            mMediaplayer.pause();
        }
    }

    @Override
    public void pre() {//上一首
        changeAudios(true);
    }

    @Override
    public void next() {//下一首
        changeAudios(false);
    }

    /**
     * 切换音频
     *
     * @param isPre 是否是上一首
     */
    private void changeAudios(boolean isPre) {
        if (mMediaplayer == null) {
            return;
        }
        switch (curPlayMode) {
            case AudioPlayService.PLAY_MODE_ORDER:
                if (isPre) {//上一首
                    if (curPostion != 0) {
                        curPostion--;
                    } else {
                        curPostion = audioList.size() - 1;
                    }
                } else {//下一首
                    if (audioList != null && audioList.size() != 0) {
                        if (curPostion != audioList.size() - 1) {
                            curPostion++;
                        } else {
                            curPostion = 0;
                        }
                    }
                }
                break;
            case AudioPlayService.PLAY_MODE_SINGLE:
                break;
            case AudioPlayService.PLAY_MODE_RANDOM:
                curPostion = random.nextInt(audioList.size());
                break;
            default:
                throw new RuntimeException("当前播放模式：" + curPlayMode);
        }
        openAudio();
    }

    @Override
    public boolean isPlaying() {
        if (mMediaplayer != null) {
            return mMediaplayer.isPlaying();
        }
        return false;
    }

    @Override
    public int getCurrentPosition() {
        if (mMediaplayer != null) {
            return mMediaplayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public int getDuration() {
        if (mMediaplayer != null) {
            return mMediaplayer.getDuration();
        }
        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (mMediaplayer != null) {
            mMediaplayer.seekTo(msec);
        }
    }

    @Override
    public int switchPlayMode() {//切换播放模式
        switch (curPlayMode) {
            case PLAY_MODE_ORDER:
                curPlayMode = PLAY_MODE_SINGLE;
                break;
            case PLAY_MODE_SINGLE:
                curPlayMode = PLAY_MODE_RANDOM;
                break;
            case PLAY_MODE_RANDOM:
                curPlayMode = PLAY_MODE_ORDER;
                break;
            default:
                throw new RuntimeException("当前播放模式：" + curPlayMode);
        }
        sp.edit().putInt(Keys.CURRENT_PLAY_MODE, curPlayMode).commit();
        return curPlayMode;
    }

    @Override
    public int getCurrentPlayMode() {
        return curPlayMode;
    }

    @Override
    public AudioItem getCurrentAudioItem() {
        return null;
    }

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            start();
            notifyUpdateUI();
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            next();
        }
    };

    /**
     * 通知界面更新
     */
    private void notifyUpdateUI() {
        Intent intent = new Intent(ACTION_UPDATE_UI);
        intent.putExtra(Keys.ITEM, curAudio);
        sendBroadcast(intent);
    }

}
