package com.lf.phoneav.ui.fragment;

import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lf.phoneav.R;
import com.lf.phoneav.adapter.AudioListAdapter;
import com.lf.phoneav.base.BaseFragment;
import com.lf.phoneav.bean.AudioItem;
import com.lf.phoneav.interfaces.Keys;
import com.lf.phoneav.ui.activity.AudioPlayerActivity;

import java.util.ArrayList;

public class AudioFragment extends BaseFragment{
    private ListView listView;

    @Override
    public int getLayoutResID() {
        return R.layout.fragment_media_list;
    }

    @Override
    public void initView() {
        listView = (ListView) rootView;
    }

    @Override
    public void initListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                ArrayList<AudioItem> audios = getAudios(cursor);
                startAudioPlayerActivity(audios, position);
            }
        });
    }

    /**
     * 开启音频播放器界面
     * @param audios 音频数据列表
     * @param position 当前点击的音频位置
     */
    protected void startAudioPlayerActivity(ArrayList<AudioItem> audios, int position) {
        Intent intent = new Intent(getActivity(), AudioPlayerActivity.class);
        intent.putExtra(Keys.ITEMS, audios);
        intent.putExtra(Keys.CURRENT_POSITION, position);
        startActivity(intent);
    }
    /**
     * 把Cursor里面的数据取出来封装到集合当中
     * @param cursor
     * @return
     */
    protected ArrayList<AudioItem> getAudios(Cursor cursor) {
        ArrayList<AudioItem> audios = new ArrayList<AudioItem>();
        cursor.moveToFirst();
        do {
            audios.add(AudioItem.fromCursor(cursor));
        } while (cursor.moveToNext());
        return audios;
    }

    @Override
    public void initData() {
        AsyncQueryHandler queryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                AudioListAdapter adapter = new AudioListAdapter(getActivity(), cursor);
                listView.setAdapter(adapter);
            }
        };
        int token = 0;            // 相当于Message.what
        Object cookie = null;    // 相当于Message.obj
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {    // 指定要查询哪些列
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
        };
        String selection = null;                  // 指定查询条件
        String[] selectionArgs = null;            // 指定查询条件中的参数
        String orderBy = MediaStore.Video.Media.TITLE + " ASC";    //  指定为升序 ASC-->升序 DSC-->降序
        // 这个查询方法会运行在子线程
        queryHandler.startQuery(token, cookie, uri, projection, selection, selectionArgs, orderBy);
    }

    @Override
    public void onClick(View v, int id) {

    }
}
