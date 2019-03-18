package com.airon.photo.show;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.airon.photo.R;
import com.bumptech.glide.Glide;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ShowPicAdapter extends RecyclerView.Adapter<ShowPicAdapter.ViewHolder> {

    private Context context;
    private List<String> ImageList;
    private int MaxCount;
    private int mScreenWidth;

    public ShowPicAdapter(Context context, List<String> ImageList, int MaxCount) {
        this.context = context;
        this.ImageList = ImageList;
        this.MaxCount = MaxCount;
        mScreenWidth = GetScreenWidth();
    }

    private int GetScreenWidth() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (ImageList.size() == 0) {//尚未添加照片
            holder.mImage.setImageResource(R.drawable.add_photos);
        } else if (ImageList.size() == MaxCount) {////达到上限
            Glide.with(context).load(ImageList.get(position)).error(R.drawable.zanwu).into(holder.mImage);
            holder.delete.setVisibility(View.VISIBLE);
            holder.tvOrder.setVisibility(View.INVISIBLE);
        } else {//添加照片尚未达到上限
            if (position < ImageList.size()) {
                Glide.with(context).load(ImageList.get(position)).error(R.drawable.zanwu).into(holder.mImage);
            } else {
                holder.mImage.setImageResource(R.drawable.add_photos);
                holder.tvOrder.setText(ImageList.size() + "/" + MaxCount);
            }
            holder.delete.setVisibility(position < ImageList.size() ? View.VISIBLE : View.INVISIBLE);
            holder.tvOrder.setVisibility(position < ImageList.size() && position == ImageList.size() - 1 ? View.INVISIBLE : View.VISIBLE);
        }
        holder.delete.setOnClickListener(v -> {
            if (mDeleteListener != null)
                mDeleteListener.onDelete(v, position);
        });
        holder.mImage.setOnClickListener(v -> {
            if (mItemListener != null)
                mItemListener.onClick(v, position);
        });
    }

    @Override
    public int getItemCount() {
        return ImageList.size() < MaxCount ? ImageList.size() + 1 : ImageList.size();
    }

    public void AddData(List<String> imageList) {
        this.ImageList = imageList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImage;
        private ImageButton delete;
        private TextView tvOrder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.Image_photo);
//            mImage.setMaxHeight(mScreenWidth / 3);
            delete = itemView.findViewById(R.id.delete);
            tvOrder = itemView.findViewById(R.id.txt_order);
        }
    }

    private OnItemClickListener mItemListener;
    private OnDeleteClickListener mDeleteListener;

    public interface OnItemClickListener {
        void onClick(View view, int position);
    }

    public interface OnDeleteClickListener {
        void onDelete(View view, int position);
    }

    public void setOnItemClick(OnItemClickListener listener) {
        mItemListener = listener;
    }

    public void setOnDeleteClick(OnDeleteClickListener listener) {
        mDeleteListener = listener;
    }
}
