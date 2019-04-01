package com.lf.phoneav.base;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;

import com.lf.phoneav.R;
import com.lf.phoneav.interfaces.UiOperation;
import com.lf.phoneav.util.Utils;

/**
 * activity的基类，其它的Activity应该继承这个类
 * @author dzl
 *
 */
public abstract class BaseActivity extends FragmentActivity implements UiOperation {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(getLayoutResID());	// 多态
		View rootView = findViewById(android.R.id.content);	// android.R.id.content 这个id可以获取到Activity的根View
		Utils.setButtonOnClickListener(rootView, this);
		initView();
		initListener();
		initData();
	}

	/**
	 * 查找View，这个方法可以省去我们的强转操作
	 * @param id
	 * @return
	 */
	public <T> T findView(int id) {
		@SuppressWarnings("unchecked")
		T view = (T) findViewById(id);
		return view;
	}

	/**
	 * 在屏幕中间显示一个Toast
	 * @param text
	 */
	public void showToast(String text) {
		Utils.showToast(this, text);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_back:	// 处理共同操作
				finish();
				break;
			default:
				// 如果单击的不是返回按钮，则还是由子类去作处理
				onClick(v, v.getId());
				break;
		}
	}
}
