package com.lf.phoneav.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.lf.phoneav.R;
import com.lf.phoneav.bean.Lyric;
import com.lf.phoneav.util.Logger;
import com.lf.phoneav.util.LyricLoader;

import java.util.ArrayList;

public class LyricView extends View {

    /**
     * 高亮歌词的颜色
     */
    private int highlightColor = Color.GREEN;
    /**
     * 默认歌词的颜色
     */
    private int defaultColor = Color.WHITE;
    /**
     * 高亮歌词的大小
     */
    private float highlightSize;
    /**
     * 默认歌词的大小
     */
    private float defaultSize;

    /**
     * 歌词数据
     */
    private ArrayList<Lyric> lyrics;
    private Paint paint;

    /**
     * 高亮索引
     */
    private int highlightIndex;
    private float hightLightY;
    private float rowHeight;
    private int currentPosition;

    public LyricView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        highlightSize = getResources().getDimension(R.dimen.highlight_size);
        defaultSize = getResources().getDimension(R.dimen.default_size);

        paint = new Paint();
        paint.setColor(defaultColor);
        paint.setTextSize(defaultSize);
        paint.setAntiAlias(true);//抗锯齿

        lyrics = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            lyrics.add(new Lyric(i * 2000, "我是歌词第" + i + "行"));
        }
        highlightIndex = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (lyrics == null) {
            drawCenterText(canvas,"找不到对应的歌词");
            return;
        }
        Lyric lyric = lyrics.get(highlightIndex);
        if (highlightIndex != lyrics.size() - 1) {
            // 高亮行歌词已经显示的时间 = 当前播放时间 - 高亮行的开始显示时间
            int showedTime = currentPosition - lyric.startShowPosition;

            // 总显示时间 = 下一行歌词的开始显示时间 - 高亮行的开始显示时间
            int totalTime = lyrics.get(highlightIndex + 1).startShowPosition - lyric.startShowPosition;

            // 计算一个比例：高亮行歌词已经显示的时间 / 总显示时间
            float scale = ((float) showedTime) / totalTime;

            // 计算移动距离：比例 * 行高
            float translateY = scale * rowHeight;
            Logger.i(this, "translateY = " + translateY);
            canvas.translate(0, -translateY);

        }


        //画高亮行文本
        drawCenterText(canvas, lyric.text);

        rowHeight = getTextHeight("哈") + 10;
        //画高亮上面的文本
        for (int i = 0; i < highlightIndex; i++) {
            float y = hightLightY - (highlightIndex - i) * rowHeight;
            drawHorizotalText(canvas, lyrics.get(i).text, y, false);
        }
        //画高亮下面的文本
        for (int i = highlightIndex + 1; i < lyrics.size(); i++) {
            float y = hightLightY + (i - highlightIndex) * rowHeight;
            drawHorizotalText(canvas, lyrics.get(i).text, y, false);
        }

    }

    /**
     * 画说垂直都居中的文本
     */
    private void drawCenterText(Canvas canvas, String text) {
        int textHeight = getTextHeight(text);
        hightLightY = getHeight() / 2 + textHeight / 2;
        drawHorizotalText(canvas, text, hightLightY, true);
    }

    /**
     * 画水平居中的文本
     *
     * @param canvas
     * @param text
     * @param y
     * @param isHightLight 是否是高亮行
     */
    private void drawHorizotalText(Canvas canvas, String text, float y, boolean isHightLight) {
        paint.setTextSize(isHightLight ? highlightSize : defaultSize);
        paint.setColor(isHightLight ? highlightColor : defaultColor);
        int textWidth = getTextWidth(text);
        // x = LyricView.width / 2 - 歌词文本.width / 2
        float x = getWidth() / 2 - textWidth / 2;
        canvas.drawText(text, x, y, paint);
    }

    /**
     * 获取文本宽
     *
     * @param text
     * @return
     */
    private int getTextWidth(String text) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int textWidth = bounds.width();
        return textWidth;
    }

    /**
     * 获取文本搞
     *
     * @param text
     * @return
     */
    private int getTextHeight(String text) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int textHeight = bounds.height();
        return textHeight;
    }

    /**
     * 更新当前的播放位置
     *
     * @param currentPosition 音频当前的播放位置
     */
    public void updatePosition(int currentPosition) {
        this.currentPosition = currentPosition;
        if (lyrics == null) {
            return;
        }

        /*让歌词滚动起来：
		找出高亮行
			if(当前播放的位置 > 歌词的开始显示时间)
				if(是最后一行） ｛
					当前行就是高亮行
				｝else if(当前播放的位置 < 下一行歌词的开始显示时间）{
					当前行就是高亮行
				}
			}
		重新调用onDraw*/

        for (int i = 0; i < lyrics.size(); i++) {
            int startShowPosition = lyrics.get(i).startShowPosition;
            if (currentPosition > startShowPosition) {
                if (i == lyrics.size() - 1) {
                    highlightIndex = i;
                    break;
                } else if (currentPosition < lyrics.get(i + 1).startShowPosition) {
                    highlightIndex = i;
                    break;
                }
            }
        }
        invalidate();

    }

    /**
     * 设置音乐路径（这个方法内部会去加载这个音乐路径下的相应的歌词，并解析显示出来
     *
     * @param musicPath
     */
    public void setMusicPath(String musicPath) {
        highlightIndex = 0;
        lyrics = LyricLoader.loadLyric(musicPath);
    }
}
