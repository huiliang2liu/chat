package com.chat.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.chat.R;


/**
 * com.chat.widget
 * 2019/3/20 15:49
 * instructions：
 * author:liuhuiliang  email:825378291@qq.com
 **/
public class ItemView extends FrameLayout {
    public ItemView(@NonNull Context context) {
        super(context);
        init(null);
    }

    public ItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public ItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ItemView, -1, -1);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_view, null);
        int leftId = array.getResourceId(R.styleable.ItemView_ItemViewLeftIV, -1);
        if (leftId == -1)
            throw new RuntimeException("leftId is not found");
        ImageView left = view.findViewById(R.id.item_view_left);
        left.setImageResource(leftId);
        ImageView right = view.findViewById(R.id.item_view_right);
        int rightId = array.getResourceId(R.styleable.ItemView_ItemViewRightIV, -1);
        if (rightId == -1)
            right.setVisibility(GONE);
        else
            right.setImageResource(rightId);
        TextView tv = view.findViewById(R.id.item_view_tv);
        String text = array.getString(R.styleable.ItemView_ItemViewTV);
        tv.setText(text);
        addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        array.recycle();
    }
}
