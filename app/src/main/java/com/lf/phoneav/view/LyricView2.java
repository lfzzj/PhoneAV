package com.lf.phoneav.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.lf.phoneav.R;
import com.lf.phoneav.bean.Lyric;
import com.lf.phoneav.util.Logger;
import com.lf.phoneav.util.LyricLoader;

import java.util.ArrayList;

public class LyricView2 extends View {

    /** 高亮歌词的颜色 */
    private int highlightColor = Color.GREEN;
    /** 默认歌词的颜色 */
    private int defaultColor = Color.WHITE;
    /** 高亮歌词的大小 */
    private float highlightSize;
    /** 默认歌词的大小 */
    private float defaultSize;
    /** 歌词数据 */
    private ArrayList<Lyric> lyrics;
    private Paint paint;
    /** 高亮索引 */
    private int highlightIndex;
    /** 高亮行歌词的y坐标 */
    private float highlightY;
    /** 行高 */
    private float rowHeight;
    private int currentPosition;

    public LyricView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        highlightSize = getResources().getDimension(R.dimen.highlight_size);
        defaultSize = getResources().getDimension(R.dimen.default_size);

        paint = new Paint();
        paint.setColor(defaultColor);
        paint.setTextSize(defaultSize);
        paint.setAntiAlias(true);	// 抗据齿（让字体边缘平滑一点）

        rowHeight = getTextHeight("哈哈哈") + 10;

        // 模拟歌词数据
        highlightIndex = 4;
        lyrics = new ArrayList<Lyric>();
        for (int i = 0; i < 10; i++) {
            lyrics.add(new Lyric(i * 2000, "我明天将会拥有" + i + "千万"));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (lyrics == null) {
            drawCenterText(canvas, "找不到对应的歌词");
            return;
        }

        // 取出高亮行歌词
        Lyric lyric = lyrics.get(highlightIndex);


        if (highlightIndex != lyrics.size() - 1) {
            // 高亮行歌词已经显示的时间 = 当前播放时间 - 高亮行的开始显示时间
            int showedTime = currentPosition - lyric.startShowPosition;

            // 总显示时间 = 下一行歌词的开始显示时间 - 高亮行的开始显示时间
            int totalTime = lyrics.get(highlightIndex + 1).startShowPosition - lyric.startShowPosition;

            // 计算一个比例：高亮行歌词已经显示的时间 / 总显示时间
            float scale = ((float)showedTime) / totalTime;

            // 计算移动距离：比例 * 行高
            float translateY = scale * rowHeight;
            Logger.i(this, "translateY = " + translateY);
            canvas.translate(0, -translateY);
        }

        // 画高亮行歌词：
        drawCenterText(canvas, lyric.text);

        // 画高亮行上面的歌词：
        // x跟高行用一样的公式
        for (int i = 0; i < highlightIndex; i++) {
            // y = 高亮行y - （行差距（高亮行索引 - 上面歌词的索引） * 行高）
            float y = highlightY - (highlightIndex - i) * rowHeight;
            drawHorizotalText(canvas, lyrics.get(i).text, y, false);
        }

        // 画高亮行下面的歌词：
        // x跟高行用一样的公式
        for (int i = highlightIndex + 1; i < lyrics.size(); i++) {
            // y = 高亮行y + （行差距（下面歌词的索引 - 高亮行索引） * 行高）
            float y = highlightY + (i - highlightIndex) * rowHeight;
            drawHorizotalText(canvas, lyrics.get(i).text, y, false);
        }
    }

    /**
     * 画水平和垂直都居中的文本
     * @param canvas
     * @param text
     */
    private void drawCenterText(Canvas canvas, String text) {
        // y = LyricView.height / 2 + 歌词文本.height / 2
        int textHeight = getTextHeight(text);
        highlightY = getHeight() / 2 + textHeight / 2;

        drawHorizotalText(canvas, text, highlightY, true);
    }

    /**
     * 画水平居中的文本
     * @param canvas
     * @param text
     * @param y
     * @param isHighlight 是否是高亮行
     */
    private void drawHorizotalText(Canvas canvas, String text, float y, boolean isHighlight) {
        paint.setTextSize(isHighlight ? highlightSize : defaultSize);
        paint.setColor(isHighlight ? highlightColor : defaultColor);

        int textWidth = getTextWidth(text);
        // x = LyricView.width / 2 - 歌词文本.width / 2
        float x = getWidth() / 2 - textWidth / 2;	// 指定把文本画在x轴的什么位置

        canvas.drawText(text, x, y, paint);
    }

    /** 获取文本高 */
    private int getTextHeight(String text) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int textHeight = bounds.height();
        return textHeight;
    }

    /** 获取文本宽 */
    private int getTextWidth(String text) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int textWidth = bounds.width();
        return textWidth;
    }

    /**
     * 更新当前的播放位置
     * @param currentPosition 音频当前的播放位置
     */
    public void updatePosition(int currentPosition) {
        if (lyrics == null) {
            return;
        }

        this.currentPosition = currentPosition;
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
     * @param musicPath
     */
    public void setMusicPath(String musicPath) {
        highlightIndex = 0;
        lyrics = LyricLoader.loadLyric(musicPath);
    }
}
