package com.demo.photogallary.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.demo.photogallary.R;
import com.demo.photogallary.utils.NativeImageLoader;
import com.demo.photogallary.utils.ScreenUtils;

import java.util.List;

public class GridViewAdapter extends BaseAdapter {

    private List<String> mList;
    private Context mContext;
    private NativeImageLoader nativeImageLoader;
    private int imageWidth;

    private onClickListener mClickListener;
    private int mNumColumns = 3;

    public GridViewAdapter(Context context) {
        nativeImageLoader = NativeImageLoader.getInstance();
        mContext = context;
        imageWidth = ScreenUtils.getScreenWidth(mContext) / mNumColumns - ScreenUtils.dip2px(mContext, 10);

    }

    public void setData(List<String> imageList) {
        mList = imageList;
        notifyDataSetChanged();
    }

    public List<String> getData(){
        return mList;
    }

    public void setOnClickListener(onClickListener listener) {
        mClickListener = listener;
    }


    public interface onClickListener {
        void onClick(String imageUrl);
        void onLongClick(String imageUrl);
    }


    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View itemView;
        ImageView imageView;
        if (convertView == null) {
            itemView = View.inflate(parent.getContext(), R.layout.image_item, null);
        } else {
            itemView = convertView;
        }
        imageView = itemView.findViewById(R.id.image);

        imageView.setTag(mList.get(position));//Solve the picture loading flashing
        imageView.setImageResource(R.mipmap.ic_launcher);

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
        }
        layoutParams.width = layoutParams.height = imageWidth;
        imageView.setLayoutParams(layoutParams);

        nativeImageLoader.loadNativeImage(mList.get(position), imageWidth, imageWidth, false, new NativeImageLoader.NativeImageCallBack() {
            @Override
            public void onImageLoader(Bitmap bitmap, final String path) {


                ImageView imageView = parent.findViewWithTag(path);
                if (imageView == null) {
                    Log.d("drr", "onImageLoader: ");
                } else {

                    imageView.setImageBitmap(bitmap);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mClickListener != null) {
                                mClickListener.onClick(path);
                            }
                        }
                    });

                    imageView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (mClickListener != null) {
                                mClickListener.onLongClick(path);
                            }
                            return false;
                        }
                    });
                }
            }
        });



        return itemView;
    }


    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
        imageWidth = ScreenUtils.getScreenWidth(mContext) / mNumColumns - ScreenUtils.dip2px(mContext, 10);
    }
}
