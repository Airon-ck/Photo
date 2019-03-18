package com.airon.photo.show;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airon.photo.R;
import com.airon.photo.select.SelectPhotoActivity;
import com.airon.photo.utils.CropUtils;
import com.airon.photo.widget.ActionSheetDialog;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ShowPicAdapter.OnItemClickListener, ShowPicAdapter.OnDeleteClickListener {

    protected int PERMISSIONS_REQUEST_CAMERA = 10011;
    protected String fileName;//拍摄得到的照片uri
    protected int REQ_Camera = 1101; //调用相机后返回的参数
    protected int REQ_Photos = 1103; //调用相册
    private String[] photos = new String[3];//图片路径
    private LinearLayout mBack;
    private TextView tvTitle;
    private RecyclerView mRecyclerView;
    private View mTop;
    private List<String> ImageList = new ArrayList<>();
    private ShowPicAdapter Adapter;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);
        initView();
    }

    private void initView() {
        mTop = findViewById(R.id.top);
        mBack = findViewById(R.id.back);
        mBack.setVisibility(View.INVISIBLE);
        tvTitle = findViewById(R.id.tvToolbarSubTitle);
        tvTitle.setText(R.string.app_name);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        Adapter = new ShowPicAdapter(this, ImageList, 3);
        mRecyclerView.setAdapter(Adapter);
        Adapter.setOnItemClick(this);
        Adapter.setOnDeleteClick(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == UCrop.REQUEST_CROP) {//裁切成功
                AddListToView(CropUtils.SaveCropPic(data));
            } else if (requestCode == REQ_Camera) {//拍照成功
                CropUtils.StartCrop(this, fileName, false);
            }
        } else if (resultCode == REQ_Photos) {//选取相册成功
            Bundle bundle = data.getExtras();
            String Pic = bundle.getString("pic");
            if (Pic != null)
                AddListToView(Pic);
        } else if (resultCode == UCrop.RESULT_ERROR) {//裁切失败
            Toast.makeText(this, "裁切图片失败!", Toast.LENGTH_SHORT).show();
        }
    }

    private void AddListToView(String ImageUrl) {
        for (int i = 0; i < photos.length; i++) {
            if (TextUtils.isEmpty(photos[i])) {
                photos[i] = ImageUrl;
                ImageList.clear();
                for (String photo : photos) {
                    if (photo != null)
                        ImageList.add(photo);
                }
                Adapter.AddData(ImageList);
                return;
            }
        }
    }

    // 实例化UI 弹出界面
    protected void ShowDialog(final String[] url) {
        ActionSheetDialog dialog = new ActionSheetDialog(this);
        dialog.builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .addSheetItem("拍照", ActionSheetDialog.SheetItemColor.Blue,
                        which -> requestCameraAccess())
                .addSheetItem("从手机相册选择", ActionSheetDialog.SheetItemColor.Blue,
                        which -> requestPictureAccess(url));
        dialog.show();
    }

    /**
     * 6.0申请相机权限
     */
    protected void requestCameraAccess() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
            //判断是否需要 向用户解释，为什么要申请该权限
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA);
        } else {
            hasCameraAccess();
        }
    }

    /**
     * 6.0申请相册权限
     */
    protected void requestPictureAccess(String[] pic) {
        this.photos = pic;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CAMERA);
            //判断是否需要 向用户解释，为什么要申请该权限
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            hasPictureAccess();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //权限申请结果
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            for (int index = 0; index < permissions.length; index++) {
                switch (permissions[index]) {
                    case Manifest.permission.CAMERA:
                        if (grantResults[index] == PackageManager.PERMISSION_GRANTED)
                        /**用户已经受权*/
                            hasCameraAccess();
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        if (grantResults[index] == PackageManager.PERMISSION_GRANTED)
                        /**用户已经受权*/
                            hasPictureAccess();
                        break;
                }
            }
        }
    }

    /**
     * 获得了相机权限
     */
    protected void hasCameraAccess() {
        onTakeCamera(REQ_Camera);
    }

    /**
     * 获得了相册权限
     */
    protected void hasPictureAccess() {
        toSelectPhotos(photos);
    }

    /**
     * 调用相机拍照
     */
    protected void onTakeCamera(int i) {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            Toast.makeText(this, "存储卡不可用!", Toast.LENGTH_SHORT).show();
        } else {
            File file = new File(Environment.getExternalStorageDirectory() + "/myImage/");
            if (!file.exists())
                file.mkdirs();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");//获取当前时间，进一步转化为字符串
            Date date = new Date();
            String str = format.format(date);
            fileName = Environment.getExternalStorageDirectory() + "/myImage/" + str + ".jpg";
            Uri photoUri = Uri.fromFile(new File(fileName));
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            int CurrentApiVersion = Build.VERSION.SDK_INT;
            //获取当前系统的Android版本号
            if (CurrentApiVersion < 24) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, i);
            } else {
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, photoUri.getPath());
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, i);
            }
        }
    }

    /**
     * 从相册中选择图片
     */
    protected void toSelectPhotos(String[] photos) {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            Toast.makeText(this, "存储卡不可用!", Toast.LENGTH_SHORT).show();
        } else {
            File file = new File(Environment.getExternalStorageDirectory() + "/myImage/");
            if (!file.exists())
                file.mkdirs();
            Bundle bundle = new Bundle();
            int count = 0;
            for (int i = 0; i < photos.length; i++) {
                if (TextUtils.isEmpty(photos[i]))
                    count += 1;
            }
            bundle.putInt("count", count);
            bundle.putStringArray("pic_list", photos);
            Intent intent = new Intent(this, SelectPhotoActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQ_Photos);
        }
    }

    @Override
    public void onClick(View view, int position) {
        if (photos[position] == null)
            ShowDialog(photos);
    }

    @Override
    public void onDelete(View view, int position) {
        DeleteDialog(position);
    }

    private void DeleteDialog(int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.pop_delete, null);
        RelativeLayout mSure = view.findViewById(R.id.btn_sure);
        RelativeLayout mCancel = view.findViewById(R.id.btn_cancel);
        popupWindow = new PopupWindow();
        popupWindow.setContentView(view);
        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        popupWindow.setBackgroundDrawable(dw);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.setOutsideTouchable(true);
        if (Build.VERSION.SDK_INT < 24) {
            popupWindow.showAsDropDown(mTop, 0, 0);
        } else {
            int[] a = new int[2];
            view.getLocationInWindow(a);
            popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.NO_GRAVITY, 0, view.getHeight() + a[1] + 60);
            popupWindow.update();
        }
        mCancel.setOnClickListener(v -> popupWindow.dismiss());
        mSure.setOnClickListener(v -> {
            popupWindow.dismiss();
            RemoveImageUrl(position);
        });
    }

    private void RemoveImageUrl(int position) {
        photos[position] = null;
        ImageList.clear();
        for (String photo : photos) {
            if (photo != null)
                ImageList.add(photo);
        }
        Adapter.AddData(ImageList);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (popupWindow != null)
            popupWindow.dismiss();
    }
}
