package com.lf.phoneav.util;

import android.widget.ImageView;

import com.lf.phoneav.R;

public class PlayerUtll {
    /**
     * 更新电量背景图片
     *
     * @param level 当前的电量级别
     */
    public static void updateBatteryBg(int level , ImageView ivBattery) {
        int resid = R.drawable.ic_battery_0;
        if (level == 0) {
            resid = R.drawable.ic_battery_0;
        } else if (level >= 100) {
            resid = R.drawable.ic_battery_100;
        } else if (level >= 80) {
            resid = R.drawable.ic_battery_80;
        } else if (level >= 60) {
            resid = R.drawable.ic_battery_60;
        } else if (level >= 40) {
            resid = R.drawable.ic_battery_40;
        } else if (level >= 20) {
            resid = R.drawable.ic_battery_20;
        } else if (level >= 10) {
            resid = R.drawable.ic_battery_10;
        }
        ivBattery.setBackgroundResource(resid);
    }
}
