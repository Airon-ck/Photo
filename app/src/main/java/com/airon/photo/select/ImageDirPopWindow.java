package com.airon.photo.select;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.airon.photo.ImageBean;
import com.airon.photo.R;
import com.bumptech.glide.Glide;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ImageDirPopWindow extends PopupWindow {

    private int mWidth;
    private int mHeight;
    private View mConvertView;
    private RecyclerView mRecyclerView;
    private List<ImageBean> mData;

    @SuppressLint("ClickableViewAccessibility")
    public ImageDirPopWindow(Context context, List<ImageBean> data) {
        calWidthAndHeight(context);
        mConvertView = LayoutInflater.from(context).inflate(R.layout.pop_select_file, null);
        mData = data;

        setContentView(mConvertView);
        setWidth(mWidth);
        setHeight(mHeight);

        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setTouchInterceptor((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE) {
                dismiss();
                return true;
            }
            return false;
        });
        initView(context);
    }

    private void initView(Context context) {
        mRecyclerView = mConvertView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(new ListDirAdapter(context, mData));
    }

    /**
     * 计算popupwindow的宽度和高度
     *
     * @param context
     */
    private void calWidthAndHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mWidth = outMetrics.widthPixels;
        mHeight = (int) (outMetrics.heightPixels * 0.7);
    }

    private class ListDirAdapter extends RecyclerView.Adapter<ListDirAdapter.ViewHolder> {

        private Context context;
        private List<ImageBean> mData;

        public ListDirAdapter(Context context, List<ImageBean> mData) {
            this.context = context;
            this.mData = mData;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_select_file, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Glide.with(context).load(mData.get(position).getFirstImgPath()).into(holder.ImgSelect);
            holder.tvSelectFile.setText(mData.get(position).getName());
            holder.tvSelectCount.setText(String.valueOf(mData.get(position).getCount()));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private ImageView ImgSelect;
            private TextView tvSelectFile, tvSelectCount;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                ImgSelect = itemView.findViewById(R.id.select_file_img);
                tvSelectFile = itemView.findViewById(R.id.select_file_name);
                tvSelectCount = itemView.findViewById(R.id.select_file_count);
            }

            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onSelected(v, mData.get(getAdapterPosition()));
            }
        }
    }

    public OnDirSelectedListener mListener;

    public interface OnDirSelectedListener {
        void onSelected(View view, ImageBean folderBean);
    }

    public void setOnDirSelectedListener(OnDirSelectedListener mListener) {
        this.mListener = mListener;
    }
}
