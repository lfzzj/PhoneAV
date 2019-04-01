package com.lf.phoneav.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lf.phoneav.R;
import com.lf.phoneav.interfaces.UiOperation;
import com.lf.phoneav.util.Utils;

/***
 * Fragment的基类，其它Fragment应该继承这个类
 * @author dzl
 *
 */
public abstract class BaseFragment extends Fragment implements UiOperation {

    protected View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(getLayoutResID(), null);
        Utils.setButtonOnClickListener(rootView, this);
        initView();
        initListener();
        initData();
        return rootView;
    }

    /**
     * 查找View，这个方法可以省去我们的强转操作
     * @param id
     * @return
     */
    public <T> T findView(int id) {
        @SuppressWarnings("unchecked")
        T view = (T) rootView.findViewById(id);
        return view;
    }

    /**
     * 在屏幕中间显示一个Toast
     * @param text
     */
    public void showToast(String text) {
        Utils.showToast(getActivity(), text);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:	// 处理共同操作
                getActivity().finish();
                break;
            default:
                // 如果单击的不是返回按钮，则还是由子类去作处理
                onClick(v, v.getId());
                break;
        }
    }
}
