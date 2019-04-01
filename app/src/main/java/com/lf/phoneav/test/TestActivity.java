package com.lf.phoneav.test;

import android.view.View;

import com.lf.phoneav.R;
import com.lf.phoneav.base.BaseActivity;
import com.lf.phoneav.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestActivity extends BaseActivity {
    @Override
    public int getLayoutResID() {
        return R.layout.activity_test;
    }

    @Override
    public void initView() {

    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        /**
         * 原来的日期
         */
        List<Person> list = new ArrayList<Person>();
        list.add(new Person(1,"001我是", "小芳"));
        list.add(new Person(1,"11斯诺克设计师", "小花"));
        list.add(new Person(1,"002", "小狗"));
        list.add(new Person(1,"000是上次好", "小猫"));
        list.add(new Person(1,"巴巴爸爸", "小米"));
        list.add(new Person(1,"哎哎啊啊 ", "小明"));

        Collections.sort(list, new MapComparator());
        for (Person person : list) {
            Logger.i(this,"   ===="+person.name+";"+person.beginTime);
        }

    }

    @Override
    public void onClick(View v, int id) {

    }
}
