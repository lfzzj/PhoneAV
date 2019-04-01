package com.lf.phoneav.ui.activity;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lf.phoneav.R;
import com.lf.phoneav.base.BaseActivity;
import com.lf.phoneav.bean.AudioItem;
import com.lf.phoneav.interfaces.IPlayService;
import com.lf.phoneav.interfaces.Keys;
import com.lf.phoneav.service.AudioPlayService;
import com.lf.phoneav.util.Logger;
import com.lf.phoneav.util.Utils;
import com.lf.phoneav.view.LyricView;

import java.util.ArrayList;

public class AudioPlayerActivity extends BaseActivity {

    private ServiceConnection conn;
    private BroadcastReceiver receiver;
    private IPlayService playService;

    private Button btnPlay;
    private TextView tvTitle;
    private TextView tvArtist;
    private ImageView ivVision;
    private TextView tvPlayTime;
    private SeekBar sbAudio;
    private Button btnPlayMode;
    private LyricView lyricView;

    private static final int UPDATE_PLAY_TIME = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PLAY_TIME:
                    updatePlayTime();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public int getLayoutResID() {
        return R.layout.activity_audio_player;
    }

    @Override
    public void initView() {
        btnPlay = findView(R.id.btn_play);
        tvTitle = findView(R.id.tv_title);
        tvArtist = findView(R.id.tv_artist);
        ivVision = findView(R.id.iv_vision);
        tvPlayTime = findView(R.id.tv_play_time);
        sbAudio = findView(R.id.sb_audio);
        btnPlayMode = findView(R.id.btn_play_mode);
        lyricView = findView(R.id.lyric_view);
    }

    @Override
    public void initListener() {
        sbAudio.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
    }

    @Override
    public void initData() {
        registerUpdateUIReceiver();
        connectService();
    }

    /**
     * 注册更新UI接收者
     */
    private void registerUpdateUIReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AudioItem audioItem = (AudioItem) intent.getSerializableExtra(Keys.ITEM);
                updateUI(audioItem);
            }
        };
        registerReceiver(receiver, new IntentFilter(AudioPlayService.ACTION_UPDATE_UI));
    }

    /**
     * 连接服务
     */
    private void connectService() {
        Intent intent = getIntent();
        ArrayList<AudioItem> audios = (ArrayList<AudioItem>) intent.getSerializableExtra(Keys.ITEMS);
        int currentPosition = intent.getIntExtra(Keys.CURRENT_POSITION, -1);

        Intent intentService = new Intent(this, AudioPlayService.class);
        intentService.putExtra(Keys.ITEMS, audios);
        intentService.putExtra(Keys.CURRENT_POSITION, currentPosition);
        startService(intentService);//这种方式开的服务于activity 无关

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                // 连接成功之后开始播放音乐
                playService = ((AudioPlayService.MyBinder) iBinder).playService;
                playService.openAudio();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(intentService, conn, BIND_AUTO_CREATE);//这种绑定方式与activity有关，能交互
    }


    /**
     * 更新UI
     *
     * @param item
     */
    public void updateUI(AudioItem item) {
        Logger.i(this, "updateUI");
        updatePlayBtnBg();

        tvTitle.setText(item.getTitle());
        tvArtist.setText(item.getArtist());

        lyricView.setMusicPath(item.getData());

        AnimationDrawable animation = (AnimationDrawable) ivVision.getBackground();
        animation.start();

        sbAudio.setMax(playService.getDuration());
        updatePlayTime();

        updatePlayModeBtnBg(playService.getCurrentPlayMode());
    }

    /**
     * 更新播放时间
     */
    private void updatePlayTime() {
        tvPlayTime.setText(Utils.formatMillis(playService.getCurrentPosition()) + "/" + Utils.formatMillis(playService.getDuration()));
        handler.sendEmptyMessageDelayed(UPDATE_PLAY_TIME, 30);

        sbAudio.setProgress(playService.getCurrentPosition());
        lyricView.updatePosition(playService.getCurrentPosition());

    }

    @Override
    public void onClick(View v, int id) {
        switch (v.getId()) {
            case R.id.btn_play:
                play();
                break;
            case R.id.btn_pre:
                playService.pre();
                break;
            case R.id.btn_next:
                playService.next();
                break;
            case R.id.btn_play_mode:
                switchPlayMode();
                break;
        }
    }

    /**
     * 切换当前播放模式
     */
    private void switchPlayMode() {
        int playMode = playService.switchPlayMode();
        updatePlayModeBtnBg(playMode);
    }

    /**
     * 更新播放模式的背景
     *
     * @param curPlayMode
     */
    private void updatePlayModeBtnBg(int curPlayMode) {
        int resId;
        switch (curPlayMode) {
            case AudioPlayService.PLAY_MODE_ORDER:
                resId = R.drawable.selector_btn_playmode_order;
                break;
            case AudioPlayService.PLAY_MODE_SINGLE:
                resId = R.drawable.selector_btn_playmode_single;
                break;
            case AudioPlayService.PLAY_MODE_RANDOM:
                resId = R.drawable.selector_btn_playmode_random;
                break;
            default:
                throw new RuntimeException("当前播放模式：" + curPlayMode);
        }
        btnPlayMode.setBackgroundResource(resId);
    }

    private void play() {
        if (playService.isPlaying()) {
            playService.pause();
        } else {
            playService.start();
        }

        updatePlayBtnBg();
    }

    /**
     * 更新播放按钮的背景
     */
    private void updatePlayBtnBg() {
        int resId;
        if (playService.isPlaying()) {
            resId = R.drawable.selector_btn_audio_pause;
        } else {
            resId = R.drawable.selector_btn_audio_play;
        }
        btnPlay.setBackgroundResource(resId);
    }

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                playService.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        unregisterReceiver(receiver);
    }

    private void sendNotification() {
        int icon = R.drawable.icon_notification;
        CharSequence tickerText = "hh";
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, tickerText, when);

        notification.flags = Notification.FLAG_ONGOING_EVENT;

        CharSequence contentTitle = "h";
        CharSequence contentText = "";
        int requestCode = 1;
        Intent intent = new Intent(this,AudioPlayerActivity.class);
    }
}
