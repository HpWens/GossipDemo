package com.github.gossipdemo.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.gossipdemo.R;


/**
 * desc: 吐槽控件
 * author: ws
 * date: 2017/5/3.
 */

public class GossipView extends FrameLayout {

    LinearLayout headerLayout;
    PullExpandView mPullExpandView;
    LinearLayout footerLayout;
    LinearLayout contentLayout;

    ImageView headerCancelImg;
    ImageView headerConfirmImg;

    ImageView footerCancelImg;
    ImageView footerConfirmImg;

    TextView hideTv;//提示文本控件

    OnGossipListener listener;

    boolean compEnable; // 是否滑动到边缘
    boolean bottomEnable; // 底部是否可见
    boolean topOrBottomMoveEnable; //控件是否移动到左边缘   文本控件只能左右运动

    int mode = DRAG;
    float simTouchX = 0; //单个手指的X坐标
    float simTouchY = 0;

    float fingersLength = 0;//多根手指之间的距离

    private static final int DRAG = 1;//单手指
    private static final int ZOOM = 2;//多手指
    private static final int NONE = 3;//未触碰

    private int bottomEdge = 0;
    private int topEdge = 0;

    public GossipView(Context context) {
        this(context, null);
    }

    public GossipView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GossipView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addCompLayoutView();
        setListener();
    }


    private void addCompLayoutView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_gossip, null, false);
        headerLayout = (LinearLayout) view.findViewById(R.id.header_layout);
        mPullExpandView = (PullExpandView) view.findViewById(R.id.body_layout);
        footerLayout = (LinearLayout) view.findViewById(R.id.footer_layout);
        contentLayout = (LinearLayout) view.findViewById(R.id.content_layout);

        headerCancelImg = (ImageView) view.findViewById(R.id.header_img_cancel);
        headerConfirmImg = (ImageView) view.findViewById(R.id.header_img_confirm);

        footerCancelImg = (ImageView) view.findViewById(R.id.footer_img_cancel);
        footerConfirmImg = (ImageView) view.findViewById(R.id.footer_img_confirm);

        hideTv = (TextView) view.findViewById(R.id.hind_tv);

        addView(view);
    }

    private void setListener() {
        OnClickListener cancelListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCancel(v);
                }
            }
        };

        OnClickListener confirmListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int[] loc = new int[2];
                    mPullExpandView.getTvComp().getLocationOnScreen(loc);
                    listener.onConfirm(loc[0], loc[1], mPullExpandView.getTvComp().getWidth(),
                            mPullExpandView.getCompText());
                }
            }
        };

        headerCancelImg.setOnClickListener(cancelListener);
        footerCancelImg.setOnClickListener(cancelListener);

        headerConfirmImg.setOnClickListener(confirmListener);
        footerConfirmImg.setOnClickListener(confirmListener);
    }


    /**
     * @param text
     */
    public void setCompText(String text) {
        mPullExpandView.setFixedText(text);
    }

    /**
     * @param text
     * @param lineNum 每行多少个字符
     */
    public void setCompText(String text, int lineNum) {
        StringBuilder sb = new StringBuilder();
        sb.append(text);
        //固定每行 20 个字符
        if (lineNum > 0 && text.length() > lineNum) {
            for (int i = 0; i < text.length() / lineNum; i++) {
                sb.insert((i + 1) * lineNum, '\n');
            }
        }
        mPullExpandView.setCompText(sb.toString());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (bottomEdge == 0) {
            bottomEdge = getHeight();
        }
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                simTouchX = event.getX();
                simTouchY = event.getY();

                setContentLocation(simTouchX, simTouchY);

                mode = DRAG;  //第一根手指按下
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //隐藏箭头
                mPullExpandView.setVisibleArrow(true);
                mPullExpandView.setVisibleActive(false);

                //只支持两根手指的缩小,放大
                float multiTouchX = event.getX(1);
                float multiTouchY = event.getY(1);

                float dx = multiTouchX - simTouchX;
                float dy = multiTouchY - simTouchY;

                fingersLength = (float) Math.sqrt(dx * dx + dy * dy);

                mode = ZOOM;//第二根手指按下
                break;
            case MotionEvent.ACTION_POINTER_UP:

                mode = NONE;
                break;
            case MotionEvent.ACTION_UP:
                //显示左下角 右上角 显示箭头
                mPullExpandView.setVisibleActive(false);
                mPullExpandView.setVisibleArrow(false);

                handlerEdge();

                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                //隐藏左下角 右上角
                if (mode == DRAG) {
                    mPullExpandView.setVisibleActive(true);
                    simpleFingerSlide(event);
                } else if (mode == ZOOM) {//多跟手指

                    float twoPointDx = event.getX() - event.getX(1);
                    float twoPointDy = event.getY() - event.getY(1);

                    float twoPointLength = (float) Math.sqrt(twoPointDx * twoPointDx + twoPointDy * twoPointDy);

                    float twoPoint = twoPointLength - fingersLength;

                    mPullExpandView.setCompTextWidth((int) twoPoint, getWidth());

                    fingersLength = twoPointLength;
                }

                simTouchX = event.getX();
                simTouchY = event.getY();

                break;
            case MotionEvent.ACTION_CANCEL:

                break;
        }
        //super.onTouchEvent(event)
        return true;
    }

    /**
     * 处理 快速拖拽到边缘 控件延伸到屏幕外面
     */
    private void handlerEdge() {
        post(new Runnable() {
            @Override
            public void run() {

                float width = contentLayout.getWidth();
                float x = contentLayout.getX();

                float height = contentLayout.getHeight();
                float y = contentLayout.getY();

                if (x + width >= getWidth()) {
                    contentLayout.setX(getWidth() - width);
                }

                if ((y + height) >= bottomEdge) {
                    contentLayout.setY(bottomEdge - height);
                }
            }
        });
    }

    /**
     * 单根手指
     *
     * @param event
     */
    private void simpleFingerSlide(MotionEvent event) {
        float dx = event.getX() - simTouchX;
        float dy = event.getY() - simTouchY;

        //判定防止滑动到顶部   上下左右边缘
        float locationX = contentLayout.getX() + dx;
        float locationY = contentLayout.getY() + dy;
        //处理边缘拉伸压缩
        if (locationX <= 0) {  //左边缘挤压   来回滑动单独处理

            if (locationY >= 0) {

                mPullExpandView.setCompTextLocation(footerLayout.getWidth(), (int) dx, getWidth());

            } else {
                comTextFixedMove((int) dx);
                mPullExpandView.setCompTextWidth((int) dy, getWidth());
            }

        } else if (locationY <= topEdge) { //上边缘

            comTextFixedMove((int) dx);

            mPullExpandView.setCompTextWidth(-(int) dy, getWidth());


        } else if (locationX >= (getWidth() - contentLayout.getWidth())) {//右边缘

            if (locationY >= 0) {

                mPullExpandView.setCompTextWidth(-(int) dx, getWidth());
            }

        } else if (locationY >= (bottomEdge - contentLayout.getHeight())) {

            comTextFixedMove((int) dx);

            mPullExpandView.setCompTextWidth((int) dy, getWidth());

        } else {

            comTextFixedMove((int) dx);
        }

        if (locationX <= 0 && locationY <= topEdge) { //右上角
            locationX = 0;
            locationY = topEdge;
        } else if (locationX <= 0 && locationY >= topEdge) {//左移动
            locationX = 0;
            locationY = bottomMargin(locationY, dy);
        } else if (locationY <= topEdge && locationX >= 0) {//上移动
            locationY = topEdge;
            locationX = rightMargin(locationX);
            if (topOrBottomMoveEnable) {
                locationX = 0;
            }
        } else {
            locationY = bottomMargin(locationY, dy);
            locationX = rightMargin(locationX);
            if (topOrBottomMoveEnable) {
                locationX = 0;
            }
        }

        contentLayout.setX(locationX);
        contentLayout.setY(locationY);
    }

    /**
     * @param dx
     */
    private void comTextFixedMove(int dx) {
        if (mPullExpandView.getX() + mPullExpandView.getWidth() < footerLayout.getWidth()) { //使用小于
            mPullExpandView.setCompTextLocation(footerLayout.getWidth(), dx, getWidth());
            //上下移动控件
            topOrBottomMoveEnable = true;
            mPullExpandView.setParentTopMoveEnable(true);
        } else {
            //正常移动控件
            topOrBottomMoveEnable = false;
            mPullExpandView.setParentTopMoveEnable(false);
        }
    }

    /**
     * @param locationX
     * @return
     */
    private float rightMargin(float locationX) {
        if (locationX >= (getWidth() - contentLayout.getWidth())) {
            locationX = getWidth() - contentLayout.getWidth();
        }
        return locationX;
    }


    /**
     * @param locationY
     * @param dy        头部是否隐藏
     * @return
     */
    private float bottomMargin(float locationY, float dy) {
        //超过屏幕 Y 坐标    处理向下滑动挤压
        if (locationY >= (bottomEdge - contentLayout.getHeight())) {
            locationY = bottomEdge - contentLayout.getHeight();
            //头部显示
            //if (locationY >= (getHeight() - contentLayout.getHeight())) {
            bottomEnable = false;
            setHeaderVisible(false);
            //}
        }

        if (dy < 0) {
            bottomEnable = true;
        }

        //头部隐藏  底部显示
        if (locationY + footerLayout.getHeight() + contentLayout.getHeight() < bottomEdge && bottomEnable) {
            setHeaderVisible(true);
        }
        return locationY;
    }

    /**
     * 确定文本位置
     *
     * @param simTouchX
     * @param simTouchY
     */
    private void setContentLocation(float simTouchX, float simTouchY) {
        if (!compEnable) {
            setCompVisible(true);
            //判定触摸点是否在边缘
            float x = 0;
            float y = 0;
            int w = contentLayout.getWidth();
            int h = contentLayout.getHeight();

            if ((w + simTouchX) > getWidth() && (h + simTouchY) > bottomEdge) { //右下角

                x = getWidth() - w;

                y = bottomEdge - h;

            } else if ((w + simTouchX) > getWidth() && (h + simTouchY) <= bottomEdge) {//右边

                x = getWidth() - w;

                y = simTouchY;

            } else if ((w + simTouchX) <= getWidth() && (h + simTouchY) > bottomEdge) {//下边

                x = simTouchX;

                y = bottomEdge - h;

            } else {
                x = simTouchX;
                y = simTouchY;
            }

            //处理顶部边缘
            if (y <= topEdge) {
                y = topEdge;
            }

            contentLayout.setX(x);
            contentLayout.setY(y);
            compEnable = true;
        }
    }

    public int getBottomEdge() {
        return bottomEdge;
    }

    public void setBottomEdge(int bottomEdge) {
        int height = ((ViewGroup) getParent()).getHeight();
        this.bottomEdge = bottomEdge >= height ? height : bottomEdge;
    }

    /**
     * 获取缩放比例
     *
     * @return
     */
    public float getScalePercent() {
        int height = ((ViewGroup) getParent()).getHeight();
        int width = ((ViewGroup) getParent()).getWidth();
        return height <= width ? (float) width / height : (float) height / width;
    }

    public int getTopEdge() {
        return topEdge;
    }

    public void setTopEdge(int topEdge) {
        this.topEdge = topEdge <= 0 ? 0 : topEdge;
    }

    /**
     * 重置 拉伸控件的布局参数
     */
    public void resetLayoutParams() {
        mPullExpandView.resetTvCompParams();
    }

    /**
     * @param visible 默认 true
     */
    public void setHeaderVisible(boolean visible) {
        headerLayout.setVisibility(visible ? View.GONE : VISIBLE);
        footerLayout.setVisibility(visible ? View.VISIBLE : GONE);
    }

    /**
     * @param visible
     */
    public void setCompVisible(boolean visible) {
        compEnable = visible;
        contentLayout.setVisibility(visible ? VISIBLE : INVISIBLE);
        hideTv.setVisibility(visible ? GONE : VISIBLE);
    }


    public interface OnGossipListener {

        void onCancel(View v);

        /**
         * @param x
         * @param y
         * @param width
         * @param text
         */
        void onConfirm(float x, float y, float width, String text);

    }

    /**
     * @param clickListener
     */
    public void setOnGossipListener(OnGossipListener clickListener) {
        this.listener = clickListener;
    }

    /**
     * @param dpValue
     * @return
     */
    public int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
