package com.lf.phoneav.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.lf.phoneav.R;
import com.lf.phoneav.base.BaseActivity;

public class OpenNetVideoActivity extends BaseActivity {
    @Override
    public int getLayoutResID() {
        return R.layout.activity_open_net_video;
    }

    @Override
    public void initView() {

    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {

    }

    @Override
    public void onClick(View v, int id) {
        switch (id) {
            case R.id.btn_open:
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setDataAndType(Uri.parse("http://192.168.31.24:8080/oppo.mp4"), "video/*");
                startActivity(intent);
                break;
        }
    }
}
