package com.lf.phoneav.ui.fragment;

import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.*;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lf.phoneav.R;
import com.lf.phoneav.adapter.VideoListAdapter;
import com.lf.phoneav.base.BaseFragment;
import com.lf.phoneav.bean.VideoItem;
import com.lf.phoneav.interfaces.Keys;
import com.lf.phoneav.ui.activity.VideoPlayerActivity;
import com.lf.phoneav.util.Utils;

import java.util.ArrayList;

public class VideoFragment extends BaseFragment {

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

            /**
             * parent参数就是ListView
             * view 点击的item的View
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                ArrayList<VideoItem> videos = getVideoItems(cursor);
                enterVideoPlayerActivity(videos, position);
            }
        });
    }

    /***
     * 把Cursor里面的所有数据封装到一个ArrayList中
     * @param cursor
     * @return
     */
    protected ArrayList<VideoItem> getVideoItems(Cursor cursor) {
        ArrayList<VideoItem> videos = new ArrayList<VideoItem>();
        cursor.moveToFirst();
        do {
            videos.add(VideoItem.fromCursor(cursor));
        } while (cursor.moveToNext());

        return videos;
    }

    /**
     * 进入视频播放界面
     *
     * @param videos
     * @param position
     */
    protected void enterVideoPlayerActivity(ArrayList<VideoItem> videos, int position) {
        Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putExtra(Keys.ITEMS, videos);
        intent.putExtra(Keys.CURRENT_POSITION, position);
        startActivity(intent);
    }

    @Override
    public void initData() {
        // 这个查询方法会运行在主线程
        // getActivity().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder)
        AsyncQueryHandler queryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {

            // 查询到数据的回调方法，这个方法会运行在主线程
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
//                 Utils.printCursor(cursor);
                VideoListAdapter adapter = new VideoListAdapter(getActivity(), cursor);
                listView.setAdapter(adapter);
            }
        };

        int token = 0;            // 相当于Message.what
        Object cookie = null;    // 相当于Message.obj
        Uri uri = Media.EXTERNAL_CONTENT_URI;
        String[] projection = {    // 指定要查询哪些列
                Media._ID, Media.TITLE, Media.DURATION, Media.SIZE, Media.DATA,
        };
        String selection = null;                  // 指定查询条件
        String[] selectionArgs = null;            // 指定查询条件中的参数
        String orderBy = Media.TITLE + " ASC";    //  指定为升序 ASC-->升序 DSC-->降序
        // 这个查询方法会运行在子线程
        queryHandler.startQuery(token, cookie, uri, projection, selection, selectionArgs, orderBy);
    }

    @Override
    public void onClick(View v, int id) {

    }

}
