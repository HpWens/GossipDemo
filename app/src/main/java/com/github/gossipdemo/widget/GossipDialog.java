package com.github.gossipdemo.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.github.gossipdemo.R;

/**
 * desc:
 * author: ws
 * date: 2017/5/22.
 */

public class GossipDialog extends Dialog {

    private Context context;

    private View contentView;

    private EditText compEditText;

    private OnPublishListener listener;

    private String gossipText = "";

    public GossipDialog(@NonNull Context context,String text) {
        super(context);
        this.context = context;
        this.gossipText = text;
        addContentView();

        setCanceledOnTouchOutside(true);
    }

    public GossipDialog(@NonNull Context context) {
        this(context, "");
    }

    private void addContentView() {
        //布局的高度参数不要设置为 match_parent  不然 setCanceledOnTouchOutside 方法无效
        View view = LayoutInflater.from(context).inflate(R.layout.view_app_dialog, null);
        setContentView(view);
        //设置dialog大小
        this.contentView = view;
        this.compEditText = (EditText) view.findViewById(R.id.player_edit_view);

        compEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (listener != null) {
                    listener.publish(v);
                }
                return false;
            }
        });

        if (gossipText != null) {
            compEditText.setText(gossipText);
            compEditText.setSelection(compEditText.getText().length());
        }

        Window dialogWindow = getWindow();
        WindowManager manager = ((Activity) context).getWindowManager();
        WindowManager.LayoutParams params = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        dialogWindow.setGravity(Gravity.BOTTOM);
        Display d = manager.getDefaultDisplay(); // 获取屏幕宽、高度
        params.width = (int) (d.getWidth() * 1.0f); // 宽度设置为屏幕的0.65，根据实际情况调整

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));// android:windowBackground
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);// android:backgroundDimEnabled默认是true的

        dialogWindow.setAttributes(params);
    }

    public void setDialogLayoutParams() {
        WindowManager.LayoutParams params = getWindow().getAttributes(); // 获取对话框当前的参数值
        WindowManager manager = ((Activity) context).getWindowManager();
        Display d = manager.getDefaultDisplay(); // 获取屏幕宽、高度
        params.width = (int) (d.getWidth() * 1.0f); // 宽度设置为屏幕的0.65，根据实际情况调整
        getWindow().setAttributes(params);
    }

    /**
     * @param text 设置吐槽文本
     */
    public void setGossipText(String text) {
        compEditText.setText("");
    }

    public View getContentView() {
        return contentView;
    }

    public EditText getEditTextView() {
        return compEditText;
    }

    public String getText() {
        return compEditText.getText().toString();
    }

    public interface OnPublishListener {

        void publish(View v);
    }

    public void setOnPublishListener(OnPublishListener listener) {
        this.listener = listener;
    }

}
