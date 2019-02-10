package com.zchr.rd.gridview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/** @Description:瑙ｅ喅鍦╯crollview涓彧鏄剧ず绗竴琛屾暟鎹殑闂
 * @author http://blog.csdn.net/finddreams */
public class MyGridView extends GridView {
	public MyGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyGridView(Context context) {
		super(context);
	}

	public MyGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

}
