package com.airon.photo.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.airon.photo.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Calendar;

public class CropUtils {

    /**
     * @param activity
     * @param CropPhotoUrl 图片裁剪
     */
    public static void StartCrop(Activity activity, String CropPhotoUrl, boolean IsRound) {
        Uri sourceUri = Uri.parse("file://" + CropPhotoUrl);
        //裁剪后保存到文件中
        Uri destinationUri = Uri.fromFile(new File(activity.getCacheDir(), "SampleCropImage.png"));
        UCrop.Options options = new UCrop.Options();
        //设置圆形裁剪框阴影
        options.setCircleDimmedLayer(IsRound);
        //设置矩形裁剪框阴影
        options.setShowCropFrame(!IsRound);
        // 修改标题栏颜色
        options.setToolbarColor(activity.getResources().getColor(R.color.color4D4D4D));
        // 修改状态栏颜色
        options.setStatusBarColor(activity.getResources().getColor(R.color.colorE6E6E6));
        // 隐藏底部工具
        options.setHideBottomControls(true);
        // 如果不开启，用户不能拖动选框，只能缩放图片
        options.setFreeStyleCropEnabled(false);
        //设置竖线的数量
        options.setCropGridColumnCount(0);
        //设置横线的数量
        options.setCropGridRowCount(0);
        //设置裁剪图片的最大尺寸
//        options.setMaxBitmapSize(800);
        //设置裁剪图片的质量
        options.setCompressionQuality(100);
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        UCrop uCrop = UCrop.of(sourceUri, destinationUri).withAspectRatio(1, 1).withMaxResultSize(2000, 2000);
        //结束设置
        uCrop.withOptions(options);
        uCrop.start(activity);
    }

    /**
     * @param data 获取裁剪结果并保存在指定文件里
     */
    public static String SaveCropPic(Intent data) {
        Uri croppedFileUri = UCrop.getOutput(data);
        //获取默认的下载目录
        String downloadsDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        @SuppressLint("DefaultLocale") String filename = String.format("%d_%s", Calendar.getInstance().getTimeInMillis(), croppedFileUri.getLastPathSegment());
        File saveFile = new File(downloadsDirectoryPath, filename);
        //保存下载的图片
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inStream = new FileInputStream(new File(croppedFileUri.getPath()));
            outStream = new FileOutputStream(saveFile);
            inChannel = inStream.getChannel();
            outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            Log.i("TAG", "裁切后的图片保存在：" + saveFile.getAbsolutePath());
            return saveFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outChannel != null) {
                    outChannel.close();
                }
                if (outStream != null) {
                    outStream.close();
                }
                if (inChannel != null) {
                    inChannel.close();
                }
                if (inStream != null) {
                    inStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return downloadsDirectoryPath;
    }

}
