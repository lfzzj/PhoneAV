package com.lf.phoneav.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lf.phoneav.R;
import com.lf.phoneav.base.BaseActivity;
import com.lf.phoneav.bean.VideoItem;
import com.lf.phoneav.interfaces.Keys;
import com.lf.phoneav.util.Logger;
import com.lf.phoneav.util.PlayerUtll;
import com.lf.phoneav.util.Utils;
import com.lf.phoneav.view.VideoView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;

public class VideoPlayerActivity extends BaseActivity {

    private VideoView videoView;
    private TextView tvTitle;
    private VideoItem videoItem;
    private SeekBar sbVoice;
    private SeekBar sbVideo;
    private ImageView ivBattery;
    private TextView tvSystemTime;
    private TextView tvCurrentPosition;
    private View viewBrightness;
    private TextView tvDuration;
    private Button btnPre;
    private Button btnNext;
    private Button btnPlay;
    private Button btnFullscreen;
    private LinearLayout llTopCtrl;
    private LinearLayout llBottomCtrl;
    private LinearLayout llLoading;

    private BroadcastReceiver batteryChangedReceiver;
    private AudioManager audioManager;
    private int maxVolume;
    private int currentVolume;


    private float currentAlpha;

    /**
     * 显示系统时间
     */
    private static final int SHOW_SYSTEM_TIME = 0;
    /**
     * 更新播放进度
     */
    private static final int UPDATE_PLAY_PROGRESS = 1;

    /**
     * 隐藏控制面板
     */
    private static final int HIDE_CTRL_LAYOUT = 2;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_SYSTEM_TIME:
                    showSystemTime();
                    break;
                case UPDATE_PLAY_PROGRESS:
                    updatePlayProgress();
                    break;
                case HIDE_CTRL_LAYOUT:
                    showOrHideCtrlLayout();
                    break;
            }
        }
    };
    private ArrayList<VideoItem> videos;
    private int curPosition;
    private Uri videoUri;


    @Override
    public int getLayoutResID() {
        //横屏显示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return R.layout.activity_video_player;
    }

    @Override
    public void initView() {
        videoView = findView(R.id.video_view);
        tvTitle = findView(R.id.tv_title);
        ivBattery = findView(R.id.iv_battery);
        tvSystemTime = findView(R.id.tv_system_time);
        tvCurrentPosition = findView(R.id.tv_current_position);
        sbVoice = findView(R.id.sb_voice);
        sbVideo = findView(R.id.sb_video);
        viewBrightness = findView(R.id.view_brightness);
        viewBrightness.setVisibility(View.VISIBLE);
        tvDuration = findView(R.id.tv_duration);
        btnPre = findView(R.id.btn_pre);
        btnNext = findView(R.id.btn_next);
        btnPlay = findView(R.id.btn_play);
        btnFullscreen = findView(R.id.btn_fullscreen);
        llTopCtrl = findView(R.id.ll_top_ctrl);
        llBottomCtrl = findView(R.id.ll_bottom_ctrl);
        llLoading = findView(R.id.ll_loading);

        showSystemTime();
        initCtrlLayout();

    }


    /**
     * 初始化控制面板
     */
    private void initCtrlLayout() {
        llTopCtrl = findView(R.id.ll_top_ctrl);
        llBottomCtrl = findView(R.id.ll_bottom_ctrl);

        // 顶部控制栏的隐藏：Y方向移动控件的高度的负数
        // float translationY = ll_top_ctrl.getHeight();
        llTopCtrl.measure(0, 0);    // 让系统主动去测量这个View
        ViewHelper.setTranslationY(llTopCtrl, -llTopCtrl.getMeasuredHeight());

        // 底部控制栏的隐藏：Y方向移动控件的高度
        llBottomCtrl.measure(0, 0);// 让系统主动去测量这个View
        ViewHelper.setTranslationY(llBottomCtrl, llBottomCtrl.getMeasuredHeight());
    }

    /**
     * 显示系统时间
     */
    private void showSystemTime() {
        tvSystemTime.setText(DateFormat.format("kk:mm:ss", System.currentTimeMillis()));
        handler.sendEmptyMessageDelayed(SHOW_SYSTEM_TIME, 1000);
    }

    @Override
    public void initListener() {
        videoView.setOnPreparedListener(mOnPreparedListener);
        videoView.setOnCompletionListener(mOnCompletionListener);
        videoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        videoView.setOnInfoListener(mOnInfoListener);
        sbVoice.setOnSeekBarChangeListener(mOnVoiceSeekBarChangeListener);
        gestureDetector = new GestureDetector(this, mOnGestureListener);
        sbVideo.setOnSeekBarChangeListener(mOnVideoSeekBarChangeListener);
    }

    @Override
    public void initData() {
        videoUri = getIntent().getData();
        if (videoUri != null) {//从第三方跳转过来的
            videoView.setVideoURI(videoUri);
            btnNext.setEnabled(false);
            btnPre.setEnabled(false);
        } else {//从视频列表跳转过来的
            videos = (ArrayList<VideoItem>) getIntent().getSerializableExtra(Keys.ITEMS);
            curPosition = getIntent().getIntExtra(Keys.CURRENT_POSITION, -1);
            openVideo();
        }
        registerBatteryChangeReceiver();
        initVoice();
        setBrightness(0.0f);
    }

    /**
     * 打开一个视频
     */
    private void openVideo() {
        if (videos == null || videos.isEmpty() || curPosition == -1) {
            return;
        }
        btnPre.setEnabled(curPosition != 0);
        btnNext.setEnabled(curPosition != (videos.size() - 1));
        videoItem = videos.get(curPosition);
        String path = videoItem.getData();
        llLoading.setVisibility(View.VISIBLE);
        videoView.setVideoPath(path);
    }

    /**
     * 初始化音量
     */
    private void initVoice() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = getStreamVolume();
        sbVoice.setMax(maxVolume);
        sbVoice.setProgress(currentVolume);
    }

    /**
     * 获取当前音量
     */
    private int getStreamVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * 设置音量
     *
     * @param value 音量值
     */
    private void setStreamVolume(int value) {
        int flags = 0;    // 1-显示系统的音量浮动面板，0-不显示系统的音量浮动面板
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, flags);
    }

    /**
     * 设置屏幕亮度
     */
    private void setBrightness(float alpha) {
        ViewHelper.setAlpha(viewBrightness, alpha);    //  设置成完全透明
    }


    /**
     * 注册电量改变的接收者
     */
    private void registerBatteryChangeReceiver() {
        batteryChangedReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // 获取电量等级
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                PlayerUtll.updateBatteryBg(level, ivBattery);
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryChangedReceiver, filter);
    }


    @Override
    public void onClick(View v, int id) {
        cancelHideCtrlLayoutMessage();
        switch (id) {
            case R.id.btn_voice:    // 静音按钮
                mute();
                break;
            case R.id.btn_exit:        // 退出按钮
                finish();
                break;
            case R.id.btn_pre:        // 上一首按钮
                pre();
                break;
            case R.id.btn_play:        // 播放按钮
                play();
                break;
            case R.id.btn_next:        // 下一首按钮
                next();
                break;
            case R.id.btn_fullscreen:        // 全屏按钮
                togggleFullscreen();
                break;

            default:
                break;
        }
    }

    /**
     * 播放/暂停
     */
    private void play() {
        if (videoView.isPlaying()) {
            videoView.pause();
        } else {
            videoView.start();
        }
        updatePlayBtnBg();
    }

    private void updatePlayBtnBg() {
        int resId;
        if (videoView.isPlaying()) {
            resId = R.drawable.selector_btn_pause;
        } else {
            resId = R.drawable.selector_btn_play;
        }
        btnPlay.setBackgroundResource(resId);
    }


    /**
     * 上一首
     */
    private void pre() {
        if (curPosition != 0) {
            curPosition--;
            openVideo();
        }
    }

    /**
     * 下一首
     */
    private void next() {
        if (videos == null) {
            return;
        }
        if (curPosition != videos.size() - 1) {
            curPosition++;
            openVideo();
        }
    }


    /**
     * 静音或者恢复原来的音量
     */
    private void mute() {
        if (getStreamVolume() > 0) {
            // 如果当前音量大于0，则保存一下这个音量，然后设置为0
            currentVolume = getStreamVolume();
            setStreamVolume(0);
            sbVoice.setProgress(0);
        } else {
            // 如果当前音量为0，则恢复原来保存的音量
            setStreamVolume(currentVolume);
            sbVoice.setProgress(currentVolume);
        }
    }

    /**
     * 全屏或者恢复默认大小
     */
    private void togggleFullscreen() {
        videoView.toggleFullscreen();
        updateFullscreenBtnBg();
    }

    /**
     * 更新全屏按钮的背景图片
     */
    private void updateFullscreenBtnBg() {
        int resid;
        if (videoView.isFullscreen()) {
            // 如果当前是全屏的，则显示一个恢复默认大小的图片
            resid = R.drawable.selector_btn_defaultscreen;
        } else {
            // 如果当前不是全屏的，则显示一个全屏的图片
            resid = R.drawable.selector_btn_fullscreen;
        }
        btnFullscreen.setBackgroundResource(resid);
    }


    /**
     * 视频准备完成
     */
    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            videoView.start();
            updatePlayBtnBg();//更新播放按钮背景图片
            if (videoUri != null) {
                tvTitle.setText(videoUri.getPath());
            } else {
                tvTitle.setText(videoItem.getTitle());//设置标题
            }
            tvDuration.setText(Utils.formatMillis(videoView.getDuration()));//显示视频总时长
            sbVideo.setMax(videoView.getDuration());
            updatePlayProgress();
            hideLoading();
        }
    };

    /**
     * 隐藏loading界面,使用渐变的方式慢慢隐藏
     */
    private void hideLoading() {
        ViewPropertyAnimator.animate(llLoading)
                .alpha(0.0f).setDuration(1500).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                llLoading.setVisibility(View.GONE);
                ViewHelper.setAlpha(llLoading, 1.0f);
            }

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * 视频播放完成
     */
    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            videoView.seekTo(0);
            tvCurrentPosition.setText(Utils.formatMillis(0));
            sbVideo.setProgress(0);
        }
    };

    /**
     * 缓冲更新的监听器
     */
    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
            updateVideoSecondaryProgress(percent);
        }
    };

    /**
     * 更新视频缓冲进度
     *
     * @param percent 缓冲进度的百分比
     */
    private void updateVideoSecondaryProgress(int percent) {
        float percentFloat = percent / 100.0f;
        int secondaryprogress = (int) (videoView.getDuration() * percentFloat);
        sbVideo.setSecondaryProgress(secondaryprogress);
    }

    /**
     *
     */
    private MediaPlayer.OnInfoListener mOnInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START://视频卡了，正在缓冲
                    llLoading.setVisibility(View.VISIBLE);
                    return true;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END://缓冲到可以播放了
                    hideLoading();
                    return true;
            }
            return false;
        }
    };

    /**
     * 更新播放进度
     */
    private void updatePlayProgress() {
        tvCurrentPosition.setText(Utils.formatMillis(videoView.getCurrentPosition()));//设置当前播放位置
        sbVideo.setProgress(videoView.getCurrentPosition());//设置当前播放位置
        handler.sendEmptyMessageDelayed(UPDATE_PLAY_PROGRESS, 300);
    }

    private SeekBar.OnSeekBarChangeListener mOnVoiceSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        // 停止拖动SeekBar
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        // 开始拖动SeekBar
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        // 进度发生改变的时候
        // fromUser 表明是否是用户触发的
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                setStreamVolume(progress);
            }
        }

    };

    private GestureDetector.SimpleOnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        /** 是否是屏幕左边按下的 */
        private boolean isDownLeft;

        @Override
        public void onLongPress(MotionEvent e) {
            //三种方式可以实现
            //用代码的方式去点击这个按钮
//             btnPlay.performClick();
            //手动调用点击事件
//             onClick(btnPlay);
            //直接调用事件
            play();
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float distanceYY = e1.getY() - e2.getY();
            if (isDownLeft) {
                // 如果是在屏幕左边按下，则改变屏幕亮度
                changeBrightness(distanceYY);
            } else {
                // 如果是在屏幕右边按下，则改变音量值
                changeVolume(distanceYY);
            }

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            currentVolume = getStreamVolume();
            isDownLeft = e.getX() < Utils.getScreenWidth(VideoPlayerActivity.this) / 2;
            currentAlpha = ViewHelper.getAlpha(viewBrightness);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            togggleFullscreen();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            showOrHideCtrlLayout();
            return true;
        }

    };

    private SeekBar.OnSeekBarChangeListener mOnVideoSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        // 停止拖动SeekBar
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            sendHideCtrlLayoutMessage();
        }

        // 开始拖动SeekBar
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            cancelHideCtrlLayoutMessage();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {//是否是由用户触发
                videoView.seekTo(progress);
            }
        }
    };

    private GestureDetector gestureDetector;

    @Override
    public boolean onTouchEvent(MotionEvent event) {//处理触摸事件
// 把触摸事件传给手势监听器
        boolean result = gestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                cancelHideCtrlLayoutMessage();
                break;
            case MotionEvent.ACTION_UP:
                sendHideCtrlLayoutMessage();
                break;
        }
        return result;
    }

    /**
     * 根据移动屏幕的距离改变音量值
     *
     * @param distanceY 移动屏幕的距离
     */
    private void changeVolume(float distanceY) {
        // 1、	onTouchEvent（处理触摸事件）
        // 2、	GuestureDetector（手势识别）

        // 3、	计算在屏幕y方向的滑动距离（e1  - e2）

        // 4、	计算滑动的距离等于多少对应的音量值
        // a)	计算音量最大值与屏幕高的比例（最后要算哪值，则这个东西应该做为被除数）
        float scale = ((float) maxVolume) / Utils.getScreenHeight(VideoPlayerActivity.this);
        // b)	计算滑动的距离等于多少对应的音量值：移动距离 x 比例
        int moveVolume = (int) (distanceY * scale);

        // 5、	在原来音量的基础上加上计算出来的对应音量值
        int resultVolume = currentVolume + moveVolume;

        // 预防超出范围
        if (resultVolume < 0) {
            resultVolume = 0;
        } else if (resultVolume > maxVolume) {
            resultVolume = maxVolume;
        }

        Logger.i(VideoPlayerActivity.this, "resultVolume = " + resultVolume);

        // 6、	使用这个音量值
        setStreamVolume(resultVolume);
        sbVoice.setProgress(resultVolume);
    }

    /**
     * 改变屏幕亮度
     *
     * @param distanceY
     */
    protected void changeBrightness(float distanceY) {
        // 1、	onTouchEvent（处理触摸事件）
        // 2、	GuestureDetector（手势识别）

        // 3、	计算在屏幕y方向的滑动距离（e1  - e2）

        // 4、	计算滑动的距离等于多少对应的亮度值
        // a)	计算亮度最大值与屏幕高的比例（最后要算哪值，则这个东西应该做为被除数）
        float scale = 1.0f / Utils.getScreenHeight(VideoPlayerActivity.this);
        // b)	计算滑动的距离等于多少对应的亮度值：移动距离 x 比例
        float moveAlpha = distanceY * scale;

        // 5、	在原来亮度的基础上加上计算出来的对应亮度
        float resultAlpha = currentAlpha + moveAlpha;

        // 预防超出范围
        if (resultAlpha < 0) {
            resultAlpha = 0;
        } else if (resultAlpha > 0.8f) {
            resultAlpha = 0.8f;
        }

        Logger.i(VideoPlayerActivity.this, "resultAlpha = " + resultAlpha);

        // 6、	使用这个亮度值
        setBrightness(resultAlpha);
    }


    /**
     * 显示或隐藏控制面板
     */
    protected void showOrHideCtrlLayout() {
        if (ViewHelper.getTranslationY(llTopCtrl) == 0) {
            // 如果控制面板原来是显示的，则隐藏
            // 顶部控制栏的隐藏：Y方向移动控件的高度的负数
            ViewPropertyAnimator.animate(llTopCtrl).translationY(-llTopCtrl.getHeight());

            // 底部控制栏的隐藏：Y方向移动控件的高度
            ViewPropertyAnimator.animate(llBottomCtrl).translationY(llBottomCtrl.getHeight());
        } else {
            // 如果控制面板原来是隐藏的，则显示
            ViewPropertyAnimator.animate(llTopCtrl).translationY(0);
            ViewPropertyAnimator.animate(llBottomCtrl).translationY(0);
            sendHideCtrlLayoutMessage();
        }
    }

    /**
     * 发送隐藏控制面板的消息（5秒种后执行 ）
     */
    private void sendHideCtrlLayoutMessage() {
        cancelHideCtrlLayoutMessage();
        handler.sendEmptyMessageDelayed(HIDE_CTRL_LAYOUT, 5000);
    }

    /**
     * 取消隐藏控制面板的消息
     */
    private void cancelHideCtrlLayoutMessage() {
        handler.removeMessages(HIDE_CTRL_LAYOUT);
    }


    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        unregisterReceiver(batteryChangedReceiver);
        super.onDestroy();
    }
}
