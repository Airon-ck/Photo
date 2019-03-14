package com.airon.photo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    protected int PERMISSIONS_REQUEST_CAMERA = 10011;
    protected String fileName;//拍摄得到的照片uri
    protected int REQ_Camear = 1101; //调用相机后返回的参数
    protected int REQ_Photos = 1103; //调用相册
    protected boolean hasPhoto = false;//是否添加照片
    private String[] photos;//图片路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);
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
                        if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                            /**用户已经受权*/
                            hasCameraAccess();
                        }
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                            /**用户已经受权*/
                            hasPictureAccess();
                        }
                        break;
                }
            }
        }
    }

    /**
     * 获得了相机权限
     */
    protected void hasCameraAccess() {
        onTakeCamera(REQ_Camear);
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
            Toast.makeText(this, "存储卡不可用", Toast.LENGTH_SHORT).show();
        } else {
            /*
            创建myImage文件夹
			 */
            File file = new File(Environment.getExternalStorageDirectory() + "/myImage/");
            if (!file.exists()) {
                file.mkdirs();
            }
            /*
             * 设置图片保存的路径
             */
            Date date;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");//获取当前时间，进一步转化为字符串
            date = new Date();
            String str = format.format(date);
            fileName = Environment.getExternalStorageDirectory() + "/myImage/" + str + ".jpg";
            Uri photoUri = Uri.fromFile(new File(fileName));
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            int currentapiVersion = Build.VERSION.SDK_INT;
            //获取当前系统的Android版本号
            if (currentapiVersion < 24) {
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
            Toast.makeText(this, "存储卡不可用", Toast.LENGTH_SHORT).show();
            return;
        } else {
            /*
            创建myImage文件夹
			 */
            File file = new File(Environment.getExternalStorageDirectory() + "/myImage/");
            if (!file.exists()) {
                file.mkdirs();
            }
            Bundle bundle = new Bundle();
            int count = 0;
            for (int i = 0; i < photos.length; i++) {
                if (TextUtils.isEmpty(photos[i])) {
                    count += 1;
                }
            }
            bundle.putInt("count", count);
            bundle.putStringArray("pic_list", photos);
            Intent intent = new Intent(this, SelectPhotoActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQ_Photos);
        }
    }

}
