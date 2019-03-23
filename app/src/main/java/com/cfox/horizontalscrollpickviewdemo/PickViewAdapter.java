package com.cfox.horizontalscrollpickviewdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PickViewAdapter extends HorizontalScrollPickView.PickAdapter {

    private String[] mItems;
    private Context mContext;

    public PickViewAdapter(Context context, String[] items) {
        this.mContext = context;
        this.mItems = items;
    }

    @Override
    public int getCount() {
        if (this.mItems == null) return 0;
        return mItems.length;
    }

    @Override
    public View getPositionView(int position, ViewGroup parent, LayoutInflater inflater) {
        TextView textView = (TextView) inflater.inflate(R.layout.item, parent, false);
        textView.setText(mItems[position]);
        return textView;
    }

    @Override
    public void initView(View view) {
        ((TextView)view).setTextColor(mContext.getResources().getColor(R.color.item_unselect));
    }

    @Override
    public void selectView(View view) {
        ((TextView)view).setTextColor(mContext.getResources().getColor(R.color.item_select));
    }

    @Override
    public void preView(View view) {
        ((TextView)view).setTextColor(mContext.getResources().getColor(R.color.item_unselect));
}
}
