package com.cfox.horizontalscrollpickviewdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.cfox.horizontalscrollpickviewdemo.HorizontalScrollPickView.SelectListener;

public class MainActivity extends AppCompatActivity implements SelectListener{
    private HorizontalScrollPickView mScrollPickView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScrollPickView = findViewById(R.id.ll_scroll_view);
        mScrollPickView.setAdapter(new PickViewAdapter(this, this.getResources().getStringArray(R.array.items)));
        mScrollPickView.setSelectListener(this);
        mScrollPickView.setDefaultSelectedIndex(3);

    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        return mScrollPickView.dispatchTouchEvent(ev);
//    }

    @Override
    public void onSelect(int lastPosition, int position) {
        Toast.makeText(this, "last Point:" + lastPosition + "    position:" + position, Toast.LENGTH_SHORT).show();
    }
}
