package com.airon.photo.show;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airon.photo.R;
import com.bumptech.glide.Glide;

/**
 * 查看大图
 */
public class ShowBigPicActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout mBack;
    private TextView tvTitle;
    private ImageView mBigImage;
    private String ImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_show_big_pic);
        initView();
    }

    private void initView() {
        mBack = findViewById(R.id.back);
        mBack.setOnClickListener(this);
        tvTitle = findViewById(R.id.tvToolbarSubTitle);
        tvTitle.setText("查看大图");
        mBigImage = findViewById(R.id.Image_big_photo);
        ImageUrl = getIntent().getStringExtra("Url");
        Glide.with(this).load(ImageUrl).error(R.drawable.zanwu).into(mBigImage);
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
