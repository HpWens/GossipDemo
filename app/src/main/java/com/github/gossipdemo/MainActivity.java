package com.github.gossipdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.github.gossipdemo.radius.RadiusTextView;
import com.github.gossipdemo.widget.FloatButtonView;
import com.github.gossipdemo.widget.GossipDialog;
import com.github.gossipdemo.widget.GossipView;

import static com.github.gossipdemo.R.id.gossip;

public class MainActivity extends AppCompatActivity {

    GossipDialog mDialog;

    FloatButtonView mFloatBtn;

    GossipView mGossipView;

    FrameLayout mGossipLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mDialog = new GossipDialog(this);

        mGossipView = (GossipView) findViewById(gossip);
        mFloatBtn = (FloatButtonView) findViewById(R.id.fbv);
        mGossipLayout = (FrameLayout) findViewById(R.id.layout_gossip);

        mFloatBtn.setOnPublishGossipListener(new FloatButtonView.OnPublishGossipListener() {
            @Override
            public void publish(View v) {
                mDialog.show();
            }
        });

        mDialog.setOnPublishListener(new GossipDialog.OnPublishListener() {
            @Override
            public void publish(View v) {
                mGossipView.setCompText(mDialog.getText());
                mGossipView.setCompVisible(false);
                mGossipView.setVisibility(View.VISIBLE);

                mDialog.dismiss();
            }
        });

        mGossipView.setOnGossipListener(new GossipView.OnGossipListener() {
            @Override
            public void onCancel(View v) {
                mGossipView.setVisibility(View.GONE);
                mDialog.show();
            }

            @Override
            public void onConfirm(float x, float y, float width, String text) {
                mGossipView.setVisibility(View.GONE);

                View itemLayout = LayoutInflater.from(mGossipLayout.getContext()).inflate(R.layout
                        .view_gossip_item, null);
                final RadiusTextView rtv = (RadiusTextView) itemLayout.findViewById(R.id.rvt_name);

                rtv.setLayoutParams(new LinearLayout.LayoutParams((int) width, ViewGroup.LayoutParams
                        .WRAP_CONTENT));

                rtv.setText(text);

                rtv.setX(x);
                rtv.setY(y);

                mGossipLayout.addView(itemLayout);

            }
        });
    }
}
