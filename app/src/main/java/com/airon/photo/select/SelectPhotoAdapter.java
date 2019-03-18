package com.airon.photo.select;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.airon.photo.R;
import com.airon.photo.utils.CropUtils;
import com.bumptech.glide.Glide;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SelectPhotoAdapter extends RecyclerView.Adapter<SelectPhotoAdapter.ViewHolder> {

    public static Set<String> mSelectedImg = new HashSet<>();
    public static String[] pic_list;//原有的数据
    private Activity activity;
    private List<String> mData;
    private int count;//剩余的图片添加个数
    private String mDirPath;
    private int mScreenWidth;

    public SelectPhotoAdapter(Activity activity, List<String> mData, String[] old_list, int count, String dirPath) {
        this.activity = activity;
        this.mData = mData;
        this.pic_list = old_list;
        this.count = count;
        this.mDirPath = dirPath;
        mScreenWidth = GetScreenWidth();
        AddData();
    }

    private int GetScreenWidth() {
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    private void AddData() {
        mSelectedImg.clear();
        for (String photo : pic_list) {
            if (photo != null)
                mSelectedImg.add(photo);
        }
    }

    /**
     * 删除已选中的
     *
     * @param path
     */
    private void deletePicPath(String path) {
        count++;
        for (int i = 0; i < pic_list.length; i++) {
            if (path.equals(pic_list[i])) {
                pic_list[i] = null;
                break;
            }
        }
    }

    /**
     * 添加未选中的
     *
     * @param path
     */
    private void addPicPath(String path) {
        if (count > 0)
            count--;
        for (int i = 0; i < pic_list.length; i++) {
            if (null == pic_list[i]) {
                pic_list[i] = path;
                break;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_select_photo, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String filePath = mDirPath + "/" + mData.get(position);
        Glide.with(activity).load(filePath).error(R.drawable.zanwu).into(holder.mImageView);
        holder.mSelect.setOnClickListener(v -> {
            if (count > 0) {//尚有剩余图片位可选择
                if (mSelectedImg.contains(filePath)) {//已被选择
                    holder.mImageView.setColorFilter(null);
                    holder.mSelect.setImageResource(R.drawable.uncheck);
                    mSelectedImg.remove(filePath);
                    deletePicPath(filePath);
                    if (mListener != null)
                        mListener.onSelect(false);
                } else {//未被选择
                    holder.mImageView.setColorFilter(Color.parseColor("#77000000"));
                    holder.mSelect.setImageResource(R.drawable.selected);
                    mSelectedImg.add(filePath);
                    addPicPath(filePath);
                    if (mListener != null)
                        mListener.onSelect(true);
                    CropUtils.StartCrop(activity, filePath, false);
                }
            } else {//选择图片已达到上限
                if (mSelectedImg.contains(filePath)) {//已被选择
                    holder.mImageView.setColorFilter(null);
                    holder.mSelect.setImageResource(R.drawable.uncheck);
                    mSelectedImg.remove(filePath);
                    deletePicPath(filePath);
                    if (mListener != null)
                        mListener.onSelect(false);
                } else {
                    Toast.makeText(activity, "选择图片已达到上限!", Toast.LENGTH_LONG).show();
                }
            }
        });
        holder.mImageView.setOnClickListener(v -> {
            if (mListener != null)
                mListener.onClick(v, filePath);
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private ImageButton mSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.Image_photo);
            mImageView.setMaxWidth(mScreenWidth / 3);
            mImageView.setColorFilter(null);
            mSelect = itemView.findViewById(R.id.select_item);
        }
    }

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onClick(View view, String url);

        void onSelect(boolean IsAdd);
    }

    public void setOnItemClick(OnItemClickListener listener) {
        mListener = listener;
    }
}
