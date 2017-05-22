package com.github.gossipdemo.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.gossipdemo.R;
import com.github.gossipdemo.radius.RadiusLinearLayout;

/**
 * desc:
 * author: ws
 * date: 2017/5/22.
 */

public class FloatButtonView extends LinearLayout {

    private ImageView mCloseImg;
    private TextView mCloseTv;
    private TextView mGossipNumTv;

    private RadiusLinearLayout mCloseLayout;
    private RadiusLinearLayout mPublishLayout;
    private RadiusLinearLayout mGossipLayout;

    private int mCloseMove;
    private int mPublishMove;
    private int mWidgetHeight;

    private Animator mOpenAnimator;
    private Animator mCloseAnimator;

    private boolean mExpand;

    public FloatButtonView(Context context) {
        this(context, null);
    }

    public FloatButtonView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        addLayoutView();

        initData();

        setListener();
    }

    private void initData() {
        mOpenAnimator = openAnimator();
        mCloseAnimator = closeAnimator();
    }

    private void setListener() {
        mGossipLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpand = !mExpand;
                if (mExpand) {
                    if (mOpenAnimator.isRunning()) {
                        mOpenAnimator.cancel();
                    }
                    mCloseAnimator.start();
                } else {
                    if (mCloseAnimator.isRunning()) {
                        mCloseAnimator.cancel();
                    }
                    mOpenAnimator.start();
                }
            }
        });

        mCloseLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "请根据具体业务处理", Toast.LENGTH_LONG).show();
            }
        });

        mPublishLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.publish(v);
                }
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCloseMove = mCloseLayout.getMeasuredHeight() + mPublishLayout.getMeasuredHeight() + dip2px(12) * 2;
        mPublishMove = mPublishLayout.getMeasuredHeight() + dip2px(12);

        mWidgetHeight = h;
    }

    private void addLayoutView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_float_btn, null, false);
        //设置 MATCH_PARENT 不然 layout_marginBottom 无效
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
                .MATCH_PARENT));

        mCloseImg = (ImageView) view.findViewById(R.id.img_close);
        mCloseTv = (TextView) view.findViewById(R.id.tv_close);
        mGossipNumTv = (TextView) view.findViewById(R.id.tv_num);

        mCloseLayout = (RadiusLinearLayout) view.findViewById(R.id.layout_close);
        mPublishLayout = (RadiusLinearLayout) view.findViewById(R.id.layout_publish);
        mGossipLayout = (RadiusLinearLayout) view.findViewById(R.id.layout_gossip);

        addView(view);
    }

    public void setGossipNum(int num) {
        mGossipNumTv.setText(num > 999 ? "999+" : num + "");
    }

    /**
     * 展开动画
     *
     * @return
     */
    private ValueAnimator openAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1.0f);
        animator.setDuration(500);
        animator.setInterpolator(new OvershootInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setAnimatorParams(animation);
            }
        });
        return animator;
    }

    public ValueAnimator closeAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0f);
        animator.setDuration(200);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setAnimatorParams(animation);
            }
        });
        return animator;
    }

    /**
     * 设置动画参数
     *
     * @param animation
     */
    private void setAnimatorParams(ValueAnimator animation) {
        float value = (float) animation.getAnimatedValue();

        mCloseLayout.setY(mGossipLayout.getY() - mCloseMove * value);
        mPublishLayout.setY(mGossipLayout.getY() - mPublishMove * value);

        mCloseLayout.setAlpha(value);
        mPublishLayout.setAlpha(value);
    }

    /**
     * @param dpValue
     * @return
     */
    public int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    OnPublishGossipListener listener;

    public interface OnPublishGossipListener {

        void publish(View v);
    }

    public void setOnPublishGossipListener(OnPublishGossipListener listener) {
        this.listener = listener;
    }

}
