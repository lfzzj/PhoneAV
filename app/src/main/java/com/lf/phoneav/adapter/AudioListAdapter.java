package com.lf.phoneav.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.lf.phoneav.R;
import com.lf.phoneav.bean.AudioItem;

public class AudioListAdapter extends CursorAdapter {

    public AudioListAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // 填充一个View
        View view = View.inflate(context, R.layout.adapter_audio_list, null);

        // 把子View引用保存到ViewHolder
        ViewHolder holder = new ViewHolder();
        holder.tv_title = (TextView) view.findViewById(R.id.tv_title);
        holder.tv_artist = (TextView) view.findViewById(R.id.tv_artist);

        // 把ViewHolder保存View里面
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        AudioItem item = AudioItem.fromCursor(cursor);
        holder.tv_title.setText(item.getTitle());
        holder.tv_artist.setText(item.getArtist());
    }

    class ViewHolder {
        TextView tv_title;
        TextView tv_artist;
    }

}