package com.airon.photo.select;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airon.photo.ImageBean;
import com.airon.photo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectPhotoActivity extends AppCompatActivity implements View.OnClickListener, SelectPhotoAdapter.OnItemClickListener, ImageDirPopWindow.OnDirSelectedListener, PopupWindow.OnDismissListener {

    private TextView tvtitle, tvright, tvDirName, tvPhotoCount;
    private LinearLayout linback;
    private RelativeLayout bottom;
    private RecyclerView mRecyclerView;
    private List<ImageBean> FolderList = new ArrayList<>();
    private List<String> ImageList = new ArrayList<>();
    private SelectPhotoAdapter Adapter;
    private String[] pic_list;
    private boolean hasScanPhoto = true;//扫描到了图片
    private File mCurrentDir;
    private int MaxPicCount;
    private int CountOfEmpty;//剩余的图片添加数量
    private int MaxSelect;//最大能选择图片的数量
    private int CountOfSelect;//选择的图片数量
    private static final int DATA_LOADED = 0x110;
    protected int REQ_Photos = 1103; //调用相册
    private ImageDirPopWindow mDirPopWindow;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_select_photo);
        initView();
        initData();
    }

    private void initData() {
        pic_list = getIntent().getExtras().getStringArray("pic_list");
        CountOfEmpty = getIntent().getExtras().getInt("count");
        MaxSelect = pic_list.length;
        CountOfSelect = MaxSelect - CountOfEmpty;//已选择图片的数量
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "当前存储卡不可用!", Toast.LENGTH_SHORT).show();
            hasScanPhoto = false;
            return;
        }
        mProgressDialog = ProgressDialog.show(this, null, "正在加载...");
        new Thread() {
            public void run() {
                Uri mImgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver cr = SelectPhotoActivity.this.getContentResolver();
                Cursor cursor = cr.query(mImgUri, null, MediaStore.Images.Media.MIME_TYPE
                                + " = ? or " + MediaStore.Images.Media.MIME_TYPE
                                + " = ? ", new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED);
                Set<String> mDirPaths = new HashSet<>();
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null) {
                        continue;
                    }
                    String dirPath = parentFile.getAbsolutePath();
                    ImageBean folderBean;
                    if (mDirPaths.contains((dirPath))) {
                        continue;
                    } else {
                        mDirPaths.add(dirPath);
                        folderBean = new ImageBean();
                        folderBean.setDir(dirPath);
                        folderBean.setFirstImgPath(path);
                    }
                    if (parentFile.list() == null) {
                        continue;
                    }
                    //文件夹中图片的数量
                    int picSize = parentFile.list((file, filename) -> {
                        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")
                                || filename.endsWith(".png")) {
                            return true;
                        }
                        return false;
                    }).length;
                    folderBean.setCount(picSize);
                    FolderList.add(folderBean);
                    if (picSize > MaxPicCount) {
                        MaxPicCount = picSize;
                        mCurrentDir = parentFile;
                    }
                }
                cursor.close();
                /**
                 * 通知Handler扫描完成
                 */
                mHandler.sendEmptyMessage(DATA_LOADED);
            }
        }.start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == DATA_LOADED) {
                mProgressDialog.dismiss();
                AddDataToView();
                InitImageDirPopWindow();
            }
        }
    };

    private void InitImageDirPopWindow() {
        mDirPopWindow = new ImageDirPopWindow(this, FolderList);
        mDirPopWindow.setOnDirSelectedListener(this);
        mDirPopWindow.setOnDismissListener(this);
    }

    private void AddDataToView() {
        if (mCurrentDir == null) {
            Toast.makeText(this, "未扫描到任何图片!", Toast.LENGTH_SHORT).show();
            hasScanPhoto = false;
            return;
        }
        ImageList = Arrays.asList(mCurrentDir.list((file, filename) -> {
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")
                    || filename.endsWith(".png")) {
                return true;
            }
            return false;
        }));
        Adapter = new SelectPhotoAdapter(this, ImageList, pic_list, CountOfEmpty, mCurrentDir.getAbsolutePath());
        mRecyclerView.setAdapter(Adapter);
        Adapter.setOnItemClick(this);
        tvDirName.setText(mCurrentDir.getName());
        tvPhotoCount.setText(String.valueOf(MaxPicCount));
    }

    private void initView() {
        tvtitle = findViewById(R.id.tvToolbarSubTitle);
        tvtitle.setText("选择图片");
        tvright = findViewById(R.id.tvToolbarRight);
        tvright.setOnClickListener(this);
        tvDirName = findViewById(R.id.txt_dir_name);
        tvPhotoCount = findViewById(R.id.txt_photo_count);
        linback = findViewById(R.id.back);
        linback.setOnClickListener(this);
        bottom = findViewById(R.id.bottom);
        bottom.setOnClickListener(this);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.bottom:
                if (hasScanPhoto) {
                    mDirPopWindow.setAnimationStyle(R.style.PopupAnimation);
                    mDirPopWindow.showAsDropDown(bottom, 0, 0);
                    lightOnOrOff(false);
                } else {
                    Toast.makeText(this, "未扫描到任何图片!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tvToolbarRight:
                Intent mIntent = new Intent();
                mIntent.putExtra("pic_list", SelectPhotoAdapter.pic_list);
                setResult(REQ_Photos, mIntent);
                finish();
                break;
        }
    }

    @Override
    public void onClick(View view, String url) {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSelect(boolean IsAdd) {
        if (IsAdd) {
            CountOfSelect++;
        } else {
            CountOfSelect--;
        }
        tvright.setText("（" + CountOfSelect + "/" + MaxSelect + "）确定");
    }

    @Override
    public void onSelected(View view, ImageBean folderBean) {
        mCurrentDir = new File(folderBean.getDir()); //更新文件夹
        AddDataToView();
        mDirPopWindow.dismiss();
    }

    @Override
    public void onDismiss() {
        lightOnOrOff(true);
    }

    /**
     * 背景变亮或变暗
     */
    private void lightOnOrOff(boolean Islight) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = Islight ? 1.0f : 0.3f;
        getWindow().setAttributes(lp);
    }
}
